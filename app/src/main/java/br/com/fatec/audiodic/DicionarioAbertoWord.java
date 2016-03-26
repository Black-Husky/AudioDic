package br.com.fatec.audiodic;

import android.text.Html;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * Created by Minoru on 09/03/2016.
 */
public class DicionarioAbertoWord {
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
    public String getDefinitions(JSONArray sense){
        String finalText = "";
        String def;
        String gramGrp;
        JSONObject usg;

        for(int i = 0; i < sense.length(); i++){
            JSONObject definition = sense.optJSONObject(i);
            def = definition.optString("def");
            gramGrp = definition.optString("gramGrp");
            usg = definition.optJSONObject("usg");
            def = replaceUnderscore(def);
            finalText = finalText+"<p><strong>Definição "+(i+1)+" </strong>"+(gramGrp != "" ? ": <i>"+gramGrp +"</i>" : "")+(usg != null && usg.optString("#text") != "" ? " : <i>"+usg.optString("#text")+"</i>":"")+"<br/>"+def.replace("<br/>", "<br/><br/>")+"</p>";
        }
        return finalText;
    }
    //End of playing

    public String getApresentationText(JSONObject response){
        String definitions = "";
        if(response.has("superEntry")){
            JSONArray superEntry = response.optJSONArray("superEntry");
            for(int i = 0; i < superEntry.length(); i++){
                JSONObject entry = superEntry.optJSONObject(i).optJSONObject("entry");
                JSONObject etym = entry.optJSONObject("etym");
                definitions = definitions + "<p><big><strong>Origem "+(i+1)+"</strong></big> "+(etym != null ? replaceUnderscore(etym.optString("#text")) : "")+"<br/><br/>" + getDefinitions(entry.optJSONArray("sense"))+"</p>";
            }
        }
        else if(response.has("entry")){
            JSONObject entry = response.optJSONObject("entry");
            JSONObject etym = entry.optJSONObject("etym");
            definitions ="<p><big><strong>Origem</strong></big> "+(etym != null ? replaceUnderscore(etym.optString("#text")) : "")+"<br/><br/>"+getDefinitions(entry.optJSONArray("sense"));
        }
        return definitions;
    }
}
