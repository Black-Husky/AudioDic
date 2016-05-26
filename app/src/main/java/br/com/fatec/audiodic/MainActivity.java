package br.com.fatec.audiodic;

//Importing

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
//End of imports

public class MainActivity extends MainModelActivity {

    //Setting fields
    private EditText editTextSearch;
    private Button buttonSearch;
    private LinearLayout linearLayoutSearch;
    private LinearLayout linearLayoutMain;
    private RelativeLayout relativeLayoutMain;
    //End of Settigns fields

    //Common Variables

    //End of Common Variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Getting fields
        editTextSearch = (EditText) findViewById(R.id.editTextSearch);
        buttonSearch = (Button) findViewById(R.id.buttonSearch);
        linearLayoutSearch = (LinearLayout) findViewById(R.id.linearLayoutSearch);
        linearLayoutMain = (LinearLayout) findViewById(R.id.linearLayoutMain);
        relativeLayoutMain = (RelativeLayout) findViewById(R.id.relativeLayoutMain);
        //End of Getting fields

        startVoiceService(linearLayoutMain);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                informActionFlush("Bem Vindo Ao Dicionário Áudio Visual. Toque duas vezes a tela rapidamente para ajuda.");
            }
        }, 500);
    }

    public void onButtonSearchClick(View view){
        String wantedWord = editTextSearch.getText().toString().trim();
        if(!wantedWord.equalsIgnoreCase(""))
            onlineSearchWord(wantedWord, true, null);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menuItemSearch).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onMenuItemSearchClick(MenuItem menuItem) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }
}
