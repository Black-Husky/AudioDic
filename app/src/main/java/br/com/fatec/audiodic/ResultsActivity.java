package br.com.fatec.audiodic;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

public class ResultsActivity extends MainModelActivity {
    //Setting and Getting Fields
    private TabHost tabHostResults;
    private TextView textViewDefinitions;
    private LinearLayout linearLayoutSearch;
    private LinearLayout linearLayoutMain;
    private TextView textViewWantedWord;
    private EditText editTextSearch;
    private Button buttonSearch;
    //End of Setting and Getting Fields

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        //Getting Intent Data
        Intent intent = getIntent();
        //End of Getting Intent Data

        //Getting Fields
        tabHostResults = (TabHost) findViewById(R.id.tabHostResults);
        textViewDefinitions = (TextView) findViewById(R.id.textViewDefinitions);
        linearLayoutSearch = (LinearLayout) findViewById(R.id.linearLayoutSearch);
        textViewWantedWord = (TextView) findViewById(R.id.textViewWantedWord);
        editTextSearch = (EditText) findViewById(R.id.editTextSearch);
        linearLayoutMain = (LinearLayout) findViewById(R.id.linearLayoutMain);
        //End of getting Fields

        //Getting Intent Data
        String results = intent.getStringExtra(VoiceActivity.RESULTS);
        String wantedWord = intent.getStringExtra(VoiceActivity.WANTED_WORD);
        dicionarioAbertoWord = (DicionarioAbertoWord) intent.getSerializableExtra(VoiceActivity.DICIONARIO_ABERTO_WORD);
        textViewWantedWord.setText(wantedWord);
        editTextSearch.setText(wantedWord);
        //End of Getting Intent Data

        tabHostResults.setVisibility(View.VISIBLE);
        linearLayoutSearch.setVisibility(View.GONE);
        textViewWantedWord.setVisibility(View.VISIBLE);
        textViewDefinitions.setText(Html.fromHtml(results));

        //Configuring tabs
        tabHostResults.setup();
        TabHost.TabSpec tabSpecResults = tabHostResults.newTabSpec("Definições");
        tabSpecResults.setContent(R.id.linearLayoutDefinitions);
        tabSpecResults.setIndicator("Definições");
        tabHostResults.addTab(tabSpecResults);
        //End of Configuring

        startVoiceService(linearLayoutMain);
    }

    @Override
    public void onMenuItemSearchClick(MenuItem menuItem) {
        if (linearLayoutSearch.getVisibility() == View.GONE) {
            textViewWantedWord.setVisibility(View.GONE);
            linearLayoutSearch.setVisibility(View.VISIBLE);
        } else {
            linearLayoutSearch.setVisibility(View.GONE);
            textViewWantedWord.setVisibility(View.VISIBLE);
        }
    }

    public void onButtonSearchClick(View view){
        String word = editTextSearch.getText().toString().trim();
        if(!word.equalsIgnoreCase("")) {
            linearLayoutSearch.setVisibility(View.GONE);
            textViewWantedWord.setText(word);
            textViewWantedWord.setVisibility(View.VISIBLE);
            onlineSearchWord(word, false, textViewDefinitions);
        }
    }
}
