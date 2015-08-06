package net.littlelite.aledana;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity
{

    private Logic theLogic;

    public final static String TAG = "MainActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "Created MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.theLogic = Logic.getInstance();

        // Restore settings
        SharedPreferences settings = getSharedPreferences(Logic.PREFS_NAME, 0);
        String username = settings.getString("User", "Dana");
        this.theLogic.setUsername(username);

        Log.d(TAG, "Current user is " + this.theLogic.getUsername());

        this.initUI();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        this.initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            this.goToSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(Logic.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("User", this.theLogic.getUsername());

        // Commit the edits!
        editor.commit();
    }

    public void setMyAvailability(View view)
    {
        Intent intent = new Intent(this, SetAvailabilityActivity.class);
        this.startActivity(intent);
    }

    private void goToSettings()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }

    private void initUI()
    {
        Log.d(TAG, "Refreshing UI");
        TextView welcomeText = (TextView)findViewById(R.id.benvenuto);
        Button theButton = (Button)findViewById(R.id.set_my_availabitly);
        if (this.theLogic.getUsername().equals("Dana"))
        {
            welcomeText.setText("Benvenuta, Dana!");
            theButton.setText("Imposta il tuo stato, Dana");
        }
        else
        {
            welcomeText.setText("Benvenuto, Alessio!");
            theButton.setText("Imposta il tuo stato, Alessio");
        }

    }
}
