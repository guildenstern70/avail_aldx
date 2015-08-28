package net.littlelite.aledana;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
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
    private int howManyHours;

    private static final int COLOR_GREEN = Color.parseColor("#00C853");
    private static final int COLOR_LTGREEN = Color.parseColor("#48E18A");
    private static final int COLOR_YELLOW = Color.parseColor("#FFDE5C");
    private static final int COLOR_LTYELLOW = Color.parseColor("#F9E594");
    private static final int COLOR_BLACK = Color.parseColor("#FFFFFF");
    private static final int COLOR_GRAY = Color.parseColor("#111111");
    private static final int COLOR_RED = Color.parseColor("#C62828");
    private static final int COLOR_LTGRAY = Color.parseColor("#333333");

    private TextView welcomeText;
    private TextView theOtherIsText;
    private ImageButton callmeButton;
    private Switch swtSms;
    private TextView textViewToBeChanged;
    private TextView textViewToBeChanged2;
    private ImageButton btnSend;
    private SeekBar seekbar;
    private EditText editMessaggioOpzionale;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(Logic.TAG, "Created MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.theLogic = Logic.getInstance();

        // Controls handlers
        welcomeText = (TextView) findViewById(R.id.benvenuto);
        theOtherIsText = (TextView) findViewById(R.id.theotheris);
        callmeButton = (ImageButton) findViewById(R.id.callme_button);
        swtSms = (Switch) findViewById(R.id.telSmsSwitch);
        textViewToBeChanged = (TextView) findViewById(R.id.my_availability);
        textViewToBeChanged2 = (TextView) findViewById(R.id.theother_availability);
        btnSend = (ImageButton) findViewById(R.id.set_avail_btn);
        seekbar = (SeekBar) findViewById(R.id.seekHowManyHours1);
        editMessaggioOpzionale = (EditText) findViewById(R.id.editMessaggioOpzionale);

        // Restore settings
        SharedPreferences settings = getSharedPreferences(Logic.PREFS_NAME, 0);
        String username = settings.getString("User", "Dana");
        this.theLogic.setUsername(username);

        Log.d(Logic.TAG, "Current user is " + this.theLogic.getUsername());

        // Connect to server and show server version
        new GetServerVersionTask().execute();

        try
        {
            getActionBar().setDisplayShowTitleEnabled(false);
        }
        catch (NullPointerException nx) {}

    }

    private void refresh()
    {
        textViewToBeChanged.setText("...");
        textViewToBeChanged.setBackgroundColor(Color.LTGRAY);

        textViewToBeChanged2.setText("...");
        textViewToBeChanged2.setBackgroundColor(Color.LTGRAY);

        seekbar.setProgress(0);
        swtSms.setChecked(true);
        this.setSendButtonVisible(false);

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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        boolean result;

        switch (id)
        {
            case R.id.action_settings:
                this.goToSettings();
                result = true;
                break;

            case R.id.action_refresh:
                this.refresh();
                result = true;
                break;

            default:
                result = super.onOptionsItemSelected(item);
        }

        return result;
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        // Preferences
        SharedPreferences settings = getSharedPreferences(Logic.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("User", this.theLogic.getUsername());
        editor.apply();
    }

    public void callTheOther(View view)
    {
        String tag = (String)callmeButton.getTag();
        Intent intent = new Intent();

        if (tag.equals("Phone"))
        {
            // Start a call
            intent.setAction(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + this.theLogic.getTheOtherPhone()));
        }
        else
        {
            // Open WhatsApp
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, "Ciao!");
            intent.setType("text/plain");
            intent.setPackage("com.whatsapp");
        }

        try
        {
            this.startActivity(intent);
        }
        catch (ActivityNotFoundException anex)
        {
            CharSequence text = "WhatsApp non trovato!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        }
    }

    public void setMyAvailability(View view)
    {
        Intent intent = new Intent(this, SetAvailabilityActivity.class);
        this.startActivityForResult(intent, 10);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        String availType;
        String availTime;
        String availMessage;

        if (requestCode == 10 && resultCode == RESULT_OK && data != null)
        {
            availType = data.getStringExtra("AVAIL_TYPE");
            availTime = data.getStringExtra("AVAIL_HOURS");
            availMessage = data.getStringExtra("AVAIL_MESSAGE");
            new SetAvailabilityTask().execute(availType, availTime, availMessage);
            this.refresh();
        }

    }

    private void goToSettings()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }

    private void drawMyAvailability()
    {
        StringBuilder sb = new StringBuilder();
        String availType = "green";
        if (!swtSms.isChecked())
        {
            sb.append("Solo SMS e Whatsapp\n");
            availType = "yellow";
        }
        else
        {
            sb.append("Disponibile\n");
        }

        if (howManyHours > 1)
        {
            sb.append("per le prossime ");
            sb.append(howManyHours);
            sb.append(" ore.");
        }
        else if (howManyHours == 1)
        {
            sb.append("per la prossima ");
            sb.append("ora.");
        }
        else
        {
            availType = "red";
            sb = new StringBuilder("Non sono disponibile.");
        }

        this.setMyAvailUI(availType, true);
        textViewToBeChanged.setText(sb.toString());
        this.setSendButtonVisible(true);

    }

    private void setSendButtonVisible(boolean isVisible)
    {
        int width;

        if (isVisible)
        {
            width = 110;
            this.btnSend.setVisibility(View.VISIBLE);
            this.editMessaggioOpzionale.setVisibility(View.VISIBLE);
        }
        else
        {
            width = 0;
            this.btnSend.setVisibility(View.INVISIBLE);
            this.editMessaggioOpzionale.setVisibility(View.INVISIBLE);
        }

        ViewGroup.LayoutParams params = this.btnSend.getLayoutParams();
        params.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width,
                            getResources().getDisplayMetrics());

        this.btnSend.setLayoutParams(params);
    }

    private void initUI()
    {
        Log.d(Logic.TAG, "Refreshing UI");

        if (this.theLogic.getUsername().equals("Dana"))
        {
            welcomeText.setText("Benvenuta, Dana!");
            theOtherIsText.setText("A. è:");
        }
        else
        {
            welcomeText.setText("Benvenuto, Alessio!");
            theOtherIsText.setText("D. è:");
        }

        // Switch: SMS or full
        swtSms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                drawMyAvailability();
            }
        });

        // Seekbar: how many hours
        seekbar.setMax((Logic.MAX_HOURS - Logic.MIN_HOURS) / 1);
        seekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar)
                    {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar)
                    {
                        setSendButtonVisible(true);
                        if (swtSms.isChecked())
                        {
                            textViewToBeChanged.setBackgroundColor(COLOR_LTGREEN);
                        }
                        else
                        {
                            textViewToBeChanged.setBackgroundColor(COLOR_LTYELLOW);
                        }
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {
                        // if progress = 13 -> value = 3 + (13 * 0.1) = 4.3
                        double value = Logic.MIN_HOURS + (progress * 1);
                        howManyHours = ((int) value);
                        drawMyAvailability();
                    }
                }
        );

        callmeButton.setEnabled(false);
        btnSend.setOnClickListener(
                new Button.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String availType = getAvailType();
                        String availTime = Integer.valueOf(howManyHours).toString();
                        String availMessage = "";

                        Log.d(Logic.TAG, "Setting new availability... ");
                        new SetAvailabilityTask().execute(availType.toLowerCase(), availTime, availMessage);
                        refresh();
                    }
                }
        );
    }

    private String getAvailType()
    {
        String availType = "Al telefono";

        if (this.howManyHours == 0)
        {
            availType = "Non sono disponibile";
        }
        else if (!this.swtSms.isChecked())
        {
            availType = "SMS e WhatsApp";
        }

        return availType;
    }

    private class AvailabilityResult
    {
        private String resultColor;
        private String resultDescription;
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

        public String getResultDescription()
        {
            return resultDescription;
        }

        public void setResultDescription(String resultDescription)
        {
            this.resultDescription = resultDescription;
        }
    }

    /**
     *
     * @param uiType one of "green", "yellow", "red"
     */
    private void setMyAvailUI(String uiType, boolean isChanging)
    {
        TextView textView= textViewToBeChanged;

        int colorGreen = COLOR_GREEN;
        int colorYellow = COLOR_YELLOW;
        if (isChanging)
        {
            colorGreen = COLOR_LTGREEN;
            colorYellow = COLOR_LTYELLOW;
        }

        switch (uiType)
        {
            case "green":
                textView.setTextColor(COLOR_BLACK);
                textView.setBackgroundColor(colorGreen);
                break;
            case "yellow":
                textView.setTextColor(COLOR_GRAY);
                textView.setBackgroundColor(colorYellow);
                break;
            case "red":
                textView.setTextColor(COLOR_LTGRAY);
                textView.setBackgroundColor(COLOR_RED);
                break;
            default:
                textView.setTextColor(COLOR_LTGRAY);
                textView.setBackgroundColor(COLOR_GRAY);
                break;
        }

    }

    /**
     *
     * @param uiType one of "green", "yellow", "red"
     */
    private void setTheOtherAvailUI(String uiType)
    {

        TextView textView = this.textViewToBeChanged2;

        int imageId;
        boolean callEnabled;
        String callMeTag = "?";

        switch (uiType)
        {
            case "green":
                imageId = R.drawable.callme;
                callEnabled = true;
                callMeTag = "Phone";
                textView.setTextColor(COLOR_BLACK);
                textView.setBackgroundColor(COLOR_GREEN);
                break;
            case "yellow":
                imageId = R.drawable.whatsapp;
                callEnabled = true;
                callMeTag = "WhatsApp";
                textView.setTextColor(COLOR_GRAY);
                textView.setBackgroundColor(COLOR_YELLOW);
                break;
            case "red":
                imageId = R.drawable.callmebn;
                callEnabled = false;
                textView.setTextColor(COLOR_LTGRAY);
                textView.setBackgroundColor(COLOR_RED);
                break;
            default:
                imageId = R.drawable.callmebn;
                callEnabled = false;
                textView.setTextColor(COLOR_LTGRAY);
                textView.setBackgroundColor(COLOR_GRAY);
                break;
        }

        this.callmeButton.setImageResource(imageId);
        this.callmeButton.setEnabled(callEnabled);
        this.callmeButton.setTag(callMeTag);
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
                ar.setResultDescription(getAvailResponse.getAvailDescription());
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
            TextView textView;

            if (result == null)
                return;

            boolean isChangingMyAvailability = theLogic.getUsername().equals(this.userRequested);

            if (isChangingMyAvailability)
            {
                textView = (TextView) findViewById(R.id.my_availability);
                setMyAvailUI(result.getResultColor(), false);
            }
            else
            {
                textView = (TextView) findViewById(R.id.theother_availability);
                setTheOtherAvailUI(result.getResultColor());
            }

            String text = result.getResultDescription();
            String remTime = result.getTimeLeft();
            String message = result.getResultMessage();
            if (remTime != null)
            {
                if (!remTime.startsWith("-"))
                {
                    text += "\n";
                    text += "Ancora per ";
                    text += result.getTimeLeft().substring(0, 6);
                }
            }
            if (message != null)
            {
                if (!isChangingMyAvailability)
                {
                    if (!message.equals(""))
                    {
                        text += "\n\n\"";
                        text += message;
                        text += "\"";
                    }
                }
            }
            textView.setText(text);

        }

    }

    private class SetAvailabilityTask extends AsyncTask<String, Void, Boolean>
    {

        private String availType;
        private String availUser;
        private String availTime;
        private String availMessage;

        @Override
        protected Boolean doInBackground(String... params)
        {
            Boolean availSet = Boolean.FALSE;

            Log.d(Logic.TAG, "Trying to connect to server to set availability...");

            availType = params[0];
            availUser = theLogic.getUsername();
            availTime = params[1];
            availMessage = params[2];

            Log.d(Logic.TAG, "User = " + availUser);
            Log.d(Logic.TAG, "Type = " + availType);
            Log.d(Logic.TAG, "Time = " + availTime);
            Log.d(Logic.TAG, "Msg = " + availMessage);

            Aledanaapi apis = theLogic.buildRemoteServiceObject();
            AledanaEndpointsSetAvailabilityRequest request =
                    new AledanaEndpointsSetAvailabilityRequest();
            request.setAvailtype(availType);
            request.setUsername(availUser);
            request.setHours(availTime);
            request.setMessage(availMessage);

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
                Context context = getApplicationContext();
                if (availTime.equals("1"))
                {
                    availTime = "1 ora.";
                }
                else
                {
                    availTime = availTime + " ore.";
                }
                CharSequence text = "Impostato: " + availType;
                if (!availType.startsWith("non"))
                {
                    text = text + " per " + availTime;
                }
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
            TextView versionText = (TextView) findViewById(R.id.version);
            String version = "AeD Services API v. ";
            versionText.setText(version + result);
        }
    }
}
