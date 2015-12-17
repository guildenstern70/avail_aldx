package net.littlelite.aledana;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.aledana_ep.aledanaapi.Aledanaapi;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsAliveResponse;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsGetPhoneNumberRequest;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsGetPhoneNumberResponse;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsSetPhoneNumberRequest;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsSetPhoneNumberResponse;

import java.io.IOException;


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
        this.theLogic = Logic.getInstance();

        // Connect to server and show server version
        new GetServerVersionTask().execute();

        this.initUI();
    }

    private void refresh()
    {
        final EditText txtCell = (EditText)this.findViewById(R.id.txtNumCell);
        new GetPhoneNumberTask().execute();
        txtCell.setEnabled(false);
        txtCell.setText("...checking...");
    }

    private void initUI()
    {

        // Set statusbar color
        if (Build.VERSION.SDK_INT >= 21)
        {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(R.color.status_color));
        }

        // User: Ale o Dana
        this.initSpinners();

        // Telephone number
        final EditText txtCell = (EditText)this.findViewById(R.id.txtNumCell);

        txtCell.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                txtCell.setText("");
            }
        });

        txtCell.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    txtCell.setEnabled(false);
                    Log.d(TAG, "WebService SetPhoneNumber Launch ");
                    new SetPhoneNumberTask().execute(txtCell.getText().toString());
                    return true;
                }
                return false;
            }
        });

        this.refresh();
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

        String user = this.theLogic.getUsername();
        if (user.equals("Dana"))
        {
            spinner1.setSelection(0);
        }
        else
        {
            spinner1.setSelection(1);
        }
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

        this.refresh();
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
        // Another interface callback
    }

    private class SetPhoneNumberTask extends AsyncTask<String, Void, Boolean>
    {

        @Override
        protected Boolean doInBackground(String... objects)
        {
            Boolean result = false;
            String phoneNr = objects[0];
            Log.d(Logic.TAG, "Trying to connect to server for setting phone nr to " + phoneNr);

            Aledanaapi apis = theLogic.buildRemoteServiceObject();

            try
            {
                String currentUser = theLogic.getUsername();
                AledanaEndpointsSetPhoneNumberRequest request =
                        new AledanaEndpointsSetPhoneNumberRequest();
                request.setPhone(phoneNr);
                request.setUsername(currentUser);
                request.setKey(Security.getSecurityToken());
                AledanaEndpointsSetPhoneNumberResponse resultResp =
                        apis.setphonenumber(request).execute();
                result = resultResp.getResult();
            }
            catch (IOException e)
            {
                Log.e(Logic.TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            EditText phoneNumber = (EditText) findViewById(R.id.txtNumCell);
            phoneNumber.setEnabled(true);
            Spinner spinner = (Spinner) findViewById(R.id.spinnerWho);
            spinner.requestFocus();
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            CharSequence text;
            if (result)
            {
                text = "Nr. di telefono impostato";
            }
            else
            {
                text = "Errore di sicurezza (504)";
            }
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

    }

    private class GetPhoneNumberTask extends AsyncTask<Void, Void, String>
    {

        @Override
        protected String doInBackground(Void... objects)
        {
            String phoneNr = "?";
            Log.d(Logic.TAG, "Trying to connect to server for phone nr...");
            Aledanaapi apis = theLogic.buildRemoteServiceObject();

            try
            {
                AledanaEndpointsGetPhoneNumberRequest request = new AledanaEndpointsGetPhoneNumberRequest();
                request.setUsername(theLogic.getUsername());
                AledanaEndpointsGetPhoneNumberResponse phone = apis.getphonenumber(request).execute();
                phoneNr = phone.getResult();
                Log.d(Logic.TAG, "Phone nr = " + phoneNr);
            }
            catch (IOException e)
            {
                Log.e(Logic.TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return phoneNr;
        }

        @Override
        protected void onPostExecute(String result)
        {
            EditText phoneNumber = (EditText) findViewById(R.id.txtNumCell);
            phoneNumber.setEnabled(true);
            phoneNumber.setText(result);
        }
    }

    private class GetServerVersionTask extends AsyncTask<Void, Void, String>
    {

        @Override
        protected String doInBackground(Void... objects)
        {
            String version = "UNKNOWN";
            Log.d(Logic.TAG, "Trying to connect to server for version...");
            Aledanaapi apis = theLogic.buildRemoteServiceObject();

            try
            {
                AledanaEndpointsAliveResponse alive = apis.alive().execute();
                version = alive.getServerVersion();
                Log.d(Logic.TAG, "Connected to server v." + version);
            }
            catch (IOException e)
            {
                Log.e(Logic.TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return version;
        }

        @Override
        protected void onPostExecute(String result)
        {
            Log.d(Logic.TAG, "OK, connected to server.");
            String version = "AeD Services API v. ";
            TextView textVersion = (TextView)findViewById(R.id.textVersion);
            textVersion.setText(version + result);
        }
    }

}
