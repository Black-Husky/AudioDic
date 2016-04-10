package br.com.fatec.audiodic;

import android.os.Bundle;
import android.widget.LinearLayout;

public class HelpActivity extends ModelActivity {

    //Setting Fields
    private LinearLayout linearLayoutMain;
    //End of Setting Fields

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        linearLayoutMain = (LinearLayout) findViewById(R.id.linearLayoutMain);
        startVoiceService(linearLayoutMain);
    }
}