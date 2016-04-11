package br.com.fatec.audiodic;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;
import java.util.TreeMap;

public class AbbreviationsActivity extends ModelActivity {

    //Setting Fields
    private LinearLayout linearLayoutMain;
    private TextView textViewInfo;
    private Context mainContext = this;
    //End of Setting Fields

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abbreviations);

        //Getting fields
        textViewInfo = (TextView) findViewById(R.id.textViewInfo);
        //End of getting fields

        Map<String, String> tempMap = new DicionarioAbertoWord(mainContext).getAbbreviations();
        Map<String, String> map = new TreeMap<String, String>(tempMap);
        String finalText = "";

        for(Map.Entry<String, String> entry : map.entrySet()){
            finalText = finalText + "<strong><i>"+entry.getKey()+"</i></strong> : "+entry.getValue()+"<br/>";
        }

        textViewInfo.setText(Html.fromHtml(finalText));

        linearLayoutMain = (LinearLayout) findViewById(R.id.linearLayoutMain);
        startVoiceService(linearLayoutMain);
    }
}
