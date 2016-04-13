package br.com.fatec.audiodic;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class MainModelActivity extends VoiceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    //Inflating menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    //End inflating menu

    public void openHelpActivity(MenuItem menuItem){
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void openAbbreviationsActivity(MenuItem menuItem){
        Intent intent = new Intent(this, AbbreviationsActivity.class);
        startActivity(intent);
    }

    public void openSettingsActivity(MenuItem menuItem){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openAboutActivity(MenuItem menuItem){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public abstract void onMenuItemSearchClick(MenuItem menuItem);
}
