package net.littlelite.aledana;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.aledana_ep.aledanaapi.Aledanaapi;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsAliveResponse;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsGetAvailabilityRequest;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsGetAvailabilityResponse;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsSetAvailabilityRequest;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsSetAvailabilityResponse;

import java.io.IOException;

public class MainActivity extends Activity
{

    private Logic theLogic;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(Logic.TAG, "Created MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.theLogic = Logic.getInstance();

        // Restore settings
        SharedPreferences settings = getSharedPreferences(Logic.PREFS_NAME, 0);
        String username = settings.getString("User", "Dana");
        this.theLogic.setUsername(username);

        Log.d(Logic.TAG, "Current user is " + this.theLogic.getUsername());

        // Connect to server and show server version
        new GetServerVersionTask().execute();

    }

    private void refresh()
    {
        TextView textViewToBeChanged1 = (TextView)findViewById(R.id.my_availability);
        textViewToBeChanged1.setText("...");
        textViewToBeChanged1.setBackgroundColor(Color.LTGRAY);
        TextView textViewToBeChanged2 = (TextView)findViewById(R.id.theother_availability);
        textViewToBeChanged2.setText("...");
        textViewToBeChanged2.setBackgroundColor(Color.LTGRAY);

        // Get availability
        new GetAvailabilityTask().execute(theLogic.getUsername());
        new GetAvailabilityTask().execute(theLogic.getTheOther());
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Log.d(Logic.TAG, "Resumed MainActivity");

        this.initUI();
        this.refresh();
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
        else if (id == R.id.action_refresh)
        {
            this.refresh();
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

    public void callTheOther(View view)
    {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + this.theLogic.getTheOtherPhone()));
        this.startActivity(intent);
    }

    public void setMyAvailability(View view)
    {
        Intent intent = new Intent(this, SetAvailabilityActivity.class);
        this.startActivityForResult(intent, 10);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        String availType = "?";
        String availTime = "?";

        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            availType = data.getStringExtra("AVAIL_TYPE");
            availTime = data.getStringExtra("AVAIL_HOURS");
            new SetAvailabilityTask().execute(availType, availTime);
            this.refresh();
        }

    }

    private void goToSettings()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }

    private void initUI()
    {
        Log.d(Logic.TAG, "Refreshing UI");
        TextView welcomeText = (TextView)findViewById(R.id.benvenuto);
        TextView theOtherIsText = (TextView)findViewById(R.id.theotheris);
        Button theButton = (Button)findViewById(R.id.set_my_availabitly);
        ImageButton callmeButton = (ImageButton)findViewById(R.id.callme_button);
        if (this.theLogic.getUsername().equals("Dana"))
        {
            welcomeText.setText("Benvenuta, Dana!");
            theOtherIsText.setText("Alessio è:");
            theButton.setText("Imposta il tuo stato, Dana");
        }
        else
        {
            welcomeText.setText("Benvenuto, Alessio!");
            theOtherIsText.setText("Dana è:");
            theButton.setText("Imposta il tuo stato, Alessio");
        }
        callmeButton.setEnabled(false);
    }

    private class AvailabilityResult
    {
        private String resultColor;
        private String resultMessage;
        private String timeLeft;

        public String getResultColor()
        {
            return resultColor;
        }

        public void setResultColor(String resultColor)
        {
            this.resultColor = resultColor;
        }

        public String getResultMessage()
        {
            return resultMessage;
        }

        public void setResultMessage(String resultMessage)
        {
            this.resultMessage = resultMessage;
        }

        public String getTimeLeft()
        {
            return timeLeft;
        }

        public void setTimeLeft(String timeLeft)
        {
            this.timeLeft = timeLeft;
        }
    }

    private class GetAvailabilityTask extends AsyncTask<String, Void, AvailabilityResult>
    {

        private String userRequested;

        @Override
        protected AvailabilityResult doInBackground(String... params)
        {
            String username = params[0];
            Log.d(Logic.TAG, "Trying to connect to server to get availability...");
            Log.d(Logic.TAG, "username = " + username);
            Aledanaapi apis = theLogic.buildRemoteServiceObject();
            AledanaEndpointsGetAvailabilityRequest request =
                    new AledanaEndpointsGetAvailabilityRequest();
            this.userRequested = username;
            request.setUsername(this.userRequested);
            AvailabilityResult ar = new AvailabilityResult();

            try
            {
                AledanaEndpointsGetAvailabilityResponse getAvailResponse =
                        apis.getavailability(request).execute();
                ar.setResultColor(getAvailResponse.getAvailColor());
                ar.setResultMessage(getAvailResponse.getAvailMessage());
                ar.setTimeLeft(getAvailResponse.getAvailTime());
            }
            catch (IOException e)
            {
                Log.e(Logic.TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return ar;
        }

        @Override
        protected void onPostExecute(AvailabilityResult result)
        {
            TextView textViewToBeChanged = null;

            if (result == null)
                return;

            ImageButton callme = (ImageButton)findViewById(R.id.callme_button);

            if (theLogic.getUsername().equals(this.userRequested))  // set My Availability
            {
                textViewToBeChanged = (TextView)findViewById(R.id.my_availability);
            }
            else
            {
                textViewToBeChanged = (TextView)findViewById(R.id.theother_availability);
            }

            if (result.getResultColor().equals("green"))
            {
                callme.setImageResource(R.drawable.callme);
                callme.setEnabled(true);
                textViewToBeChanged.setTextColor(Color.parseColor("#FFFFFF"));
                textViewToBeChanged.setBackgroundColor(Color.parseColor("#00C853"));
            }
            else if (result.getResultColor().equals("yellow"))
            {
                callme.setImageResource(R.drawable.callmebn);
                callme.setEnabled(false);
                textViewToBeChanged.setTextColor(Color.parseColor("#111111"));
                textViewToBeChanged.setBackgroundColor(Color.parseColor("#DBD365"));
            }
            else
            {
                callme.setImageResource(R.drawable.callmebn);
                callme.setEnabled(false);
                //RedColor = C62828
                textViewToBeChanged.setTextColor(Color.parseColor("#333333"));
                textViewToBeChanged.setBackgroundColor(Color.parseColor("#C62828"));
            }

            String text = result.getResultMessage();
            String remTime = result.getTimeLeft();
            if (remTime != null) {
                if (!remTime.startsWith("-")) {
                    text += "\n";
                    text += "Ancora per ";
                    text += result.getTimeLeft();
                }
            }
            textViewToBeChanged.setText(text);

        }

    }

    private class SetAvailabilityTask extends AsyncTask<String, Void, Boolean>
    {

        private String availType;
        private String availUser;
        private String availTime;

        @Override
        protected Boolean doInBackground(String... params)
        {
            Boolean availSet = Boolean.FALSE;

            Log.d(Logic.TAG, "Trying to connect to server to set availability...");

            availType = params[0];
            availUser = theLogic.getUsername();
            availTime = params[1];

            Log.d(Logic.TAG, "User = " + availUser);
            Log.d(Logic.TAG, "Type = " + availType);
            Log.d(Logic.TAG, "Time = " + availTime);

            Aledanaapi apis = theLogic.buildRemoteServiceObject();
            AledanaEndpointsSetAvailabilityRequest request =
                    new AledanaEndpointsSetAvailabilityRequest();
            request.setAvailtype(availType);
            request.setUsername(availUser);
            request.setHours(availTime);

            try
            {
                AledanaEndpointsSetAvailabilityResponse setAvailResponse =
                        apis.setavailability(request).execute();
                availSet = setAvailResponse.getResult();
            }
            catch (IOException e)
            {
                Log.e(Logic.TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return availSet;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (result == Boolean.TRUE)
            {
                Log.d(Logic.TAG, "OK, availability was set.");

                Context context = getApplicationContext();
                if (availTime.equals("1"))
                {
                    availTime = "1 ora.";
                }
                else
                {
                    availTime = availTime + "ore.";
                }
                CharSequence text = "Impostato: " + availType + " per " + availTime;
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            else
            {
                Log.e(Logic.TAG, "Somerthing weird has happened...");
            }

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
                Log.d(Logic.TAG, "Connected to server v."+version);
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
            TextView versionText = (TextView)findViewById(R.id.version);
            String version = "AleDana Services API version ";
            versionText.setText(version + result);
        }
    }
}
