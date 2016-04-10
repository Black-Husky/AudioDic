package br.com.fatec.audiodic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ModelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);
    }

    //Configuring Menu Items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menuItemBack:
                back();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //End of configuring Menu Items

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //Inflating menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.secondary_menu, menu);
        return true;
    }
    //End inflating menu

    public void back(){
        onBackPressed();
    }
}
