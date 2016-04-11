package br.com.fatec.audiodic;

import android.content.Context;
import android.content.res.XmlResourceParser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Minoru on 09/03/2016.
 */
public class DicionarioAbertoWord implements Serializable {
    //Setting abbreviations
    private Map<String, String> abbreviations = new HashMap<String, String>();
    //End setting abbreviations

    private transient final JSONObject response;
    private String finalText;
    private String finalVoiceText;
    private LinkedList<String> origins = new LinkedList<String>();
    private LinkedList<Definition> definitions = new LinkedList<Definition>();

    public DicionarioAbertoWord(Context context){
        openAbbreviationsXml(context);
        this.response = null;
    }

    public DicionarioAbertoWord(JSONObject response, Context context){
        openAbbreviationsXml(context);
        this.response = response;
        this.getApresentationText();
    }

    public class Definition implements Serializable{
        private String wordId = "";
        private String definition = "";
        private String origin = "";
        private String grammaticalGroup = "";
        private String usage = "";

        public String getUsage(){
            return usage;
        }
        public void setUsage(String usage) {
            this.usage = usage;
        }
        public String getWordId() {
            return wordId;
        }
        public void setWordId(String id) {
            this.wordId = id;
        }
        public String getDefinition() {
            return definition;
        }
        public void setDefinition(String definition) {
            this.definition = definition;
        }
        public String getOrigin() {
            return origin;
        }
        public void setOrigin(String origin) {
            this.origin = origin;
        }
        public String getGrammaticalGroup() {
            return grammaticalGroup;
        }
        public void setGrammaticalGroup(String grammaticalGroup) {
            this.grammaticalGroup = grammaticalGroup;
        }
    }

    //Replace "_" for <i> tag
    public String replaceUnderscore(String text){
        boolean replaceFlag = true;
        do {
            if (replaceFlag) {
                text = text.replaceFirst("_", "<i>");
                replaceFlag = false;
            } else {
                text = text.replaceFirst("_", "</i>");
                replaceFlag = true;
            }
        }
        while(text.contains("_"));
        return text;
    }
    //End of Replace

    //Playing with Dicionário-Abertos's json
    private String getDefinitions(JSONArray sense, String origin, String wordId, boolean voice){
        Definition definition = new Definition();
        String finalText = "";
        String def;
        String gramGrp;
        String usg;
        String tempText;

        for(int i = 0; i < sense.length(); i++){
            JSONObject jsonObjectDefinition = sense.optJSONObject(i);
            def = replaceUnderscore(jsonObjectDefinition.optString("def"));
            gramGrp = jsonObjectDefinition.optString("gramGrp");
            usg = jsonObjectDefinition.optJSONObject("usg") != null && jsonObjectDefinition.optJSONObject("usg").optString("#text") != "" ? " uso - <i>"+(!voice ? jsonObjectDefinition.optJSONObject("usg").optString("#text") : getAbbreviationMeaning(jsonObjectDefinition.optJSONObject("usg").optString("#text")))+"</i>":"";
            if(!voice)
                tempText = "<p><strong>Definição "+(i+1)+" : </strong> <i>"+gramGrp +"</i>"+usg+"<br/>"+def.replace("<br/>", "<br/><br/>")+"</p>";
            else
                tempText = "<p><strong>Definição "+(i+1)+" : </strong> - <i>"+getAbbreviationMeaning(gramGrp) +"</i>"+usg+" -<br/>"+def.replace("<br/>", "<br/><br/>")+"</p>";

            finalText = finalText+tempText;

            definition.setWordId(wordId);
            definition.setOrigin(origin);
            definition.setGrammaticalGroup(gramGrp);
            definition.setUsage(usg);
            definition.setDefinition(tempText);
            if(!voice)
                definitions.add(definition);
        }
        return finalText;
    }
    //End of playing

    private void getApresentationText(){
        String finalText = "";
        String finalVoiceText = "";
        if(response.has("superEntry")){
            JSONArray superEntry = response.optJSONArray("superEntry");
            for(int i = 0; i < superEntry.length(); i++){
                JSONObject entry = superEntry.optJSONObject(i).optJSONObject("entry");
                String origin = entry.optJSONObject("etym") != null ? entry.optJSONObject("etym").optString("@orig") : "";
                String originText = entry.optJSONObject("etym") != null ? entry.optJSONObject("etym").optString("#text") : "";
                String abbreviationOriginText = origin != "" ? "(Do "+getAbbreviationMeaning(origin+".")+" "+replaceUnderscore(originText.substring(originText.lastIndexOf(" ")+1)) : "";
                finalText = finalText + "<p><big><strong>Origem "+(i+1)+"</strong></big> "+replaceUnderscore(originText)+"<br/><br/>" + getDefinitions(entry.optJSONArray("sense"), origin, entry.optString("@id"), false)+"</p>";
                finalVoiceText = finalVoiceText + "<p><big><strong>Origem "+(i+1)+"</strong></big> "+abbreviationOriginText+"<br/><br/>" + getDefinitions(entry.optJSONArray("sense"), origin, entry.optString("@id"), true)+"</p>";
                origins.add(origin);
            }
        }
        else if(response.has("entry")){
            JSONObject entry = response.optJSONObject("entry");
            String origin = entry.optJSONObject("etym") != null ? entry.optJSONObject("etym").optString("@orig") : "";
            String originText = entry.optJSONObject("etym") != null ? entry.optJSONObject("etym").optString("#text") : "";
            String abbreviationOriginText = origin != "" ? "(Do "+getAbbreviationMeaning(origin+".")+" "+replaceUnderscore(originText.substring(originText.lastIndexOf(" ")+1)) : "";
            finalText = "<p><big><strong>Origem</strong></big> "+replaceUnderscore(originText)+"<br/><br/>"+getDefinitions(entry.optJSONArray("sense"), origin,  entry.optString("@id"), false);
            finalVoiceText = "<p><big><strong>Origem</strong></big> "+abbreviationOriginText+" "+replaceUnderscore(originText.substring(originText.lastIndexOf(" ")+1))+"<br/><br/>"+getDefinitions(entry.optJSONArray("sense"), origin,  entry.optString("@id"), true);
            origins.add(origin);
        }
        this.finalVoiceText = finalVoiceText;
        this.finalText = finalText;
    }

    //Opening the fucking xml to populate the abbreviations map
    private void openAbbreviationsXml(Context context){
        XmlResourceParser xmlAbbreviations = context.getResources().getXml(R.xml.abbreviations);
        try {
            Boolean initalsFlag = false;
            Boolean meaningFlag = false;
            String initials = "";
            String meaning = "";
            int eventType = xmlAbbreviations.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    //System.out.println("Start document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    //System.out.println("Start tag " + xmlAbbreviations.getName());
                    if(xmlAbbreviations.getName().equalsIgnoreCase("initials"))
                        initalsFlag = true;
                    else if(xmlAbbreviations.getName().equalsIgnoreCase("meaning"))
                        meaningFlag = true;
                } else if (eventType == XmlPullParser.END_TAG) {
                    //System.out.println("End tag " + xmlAbbreviations.getName());
                    if(xmlAbbreviations.getName().equalsIgnoreCase("initials"))
                        initalsFlag = false;
                    else if(xmlAbbreviations.getName().equalsIgnoreCase("meaning"))
                        meaningFlag = false;
                    else if(xmlAbbreviations.getName().equalsIgnoreCase("abbreviation"))
                        abbreviations.put(initials, meaning);
                } else if (eventType == XmlPullParser.TEXT) {
                    //System.out.println("Text " + xmlAbbreviations.getText());
                    if(initalsFlag)
                        initials = xmlAbbreviations.getText();
                    else if(meaningFlag)
                        meaning = xmlAbbreviations.getText();
                }
                eventType = xmlAbbreviations.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //End of Opening the fucking xml

    private String getAbbreviationMeaning(String initials){
        initials = initials.toLowerCase();
        return abbreviations.get(initials) != null ? abbreviations.get(initials) : "";
    }

    public LinkedList<Definition> getDefinitions(){
        return definitions;
    }

    public LinkedList<String> getOrigins(){
        return origins;
    }

    public String getFinalText(){
        return finalText;
    }

    public Map<String, String> getAbbreviations() {
        return abbreviations;
    }

    public String getFinalVoiceText(){
        return finalVoiceText;
    }
}