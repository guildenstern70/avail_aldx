package net.littlelite.aledana;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SettingsActivity extends Activity implements AdapterView.OnItemSelectedListener
{
    public final static String TAG = "SettingsActivity";
    private Logic theLogic;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "Created Settings");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.initSpinners();
        this.theLogic = Logic.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    private void initSpinners()
    {
        Spinner spinner1 = (Spinner) findViewById(R.id.spinnerWho);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.tu_sei_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner1.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id)
    {
        String newUser = parent.getSelectedItem().toString();
        this.theLogic.setUsername(newUser);
        Log.d(TAG, "Setting new user to " + this.theLogic.getUsername());

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(Logic.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("User", newUser);

        // Commit the edits!
        editor.apply();
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
        // Another interface callback
    }

}
