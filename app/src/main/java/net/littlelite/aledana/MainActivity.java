package net.littlelite.aledana;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.appspot.aledana_ep.aledanaapi.Aledanaapi;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsGetAllAvailabilitiesResponse;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsSetAvailabilityRequest;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsSetAvailabilityResponse;

import java.io.IOException;

public class MainActivity extends Activity
{

    private Logic theLogic;
    private int howManyHours;
    private String secretMessage;
    private boolean secretDisplayed;

    private static final int COLOR_BLACK = Color.parseColor("#FFFFFF");
    private static final int COLOR_GRAY = Color.parseColor("#111111");
    private static final int COLOR_LTGRAY = Color.parseColor("#EEEEEE");

    private TextView welcomeText;
    private TextView theOtherIsText;
    private ImageButton callmeButton;
    private Switch swtSms;
    private TextView textViewMyAvailability;
    private TextView textViewTheOtherAvailability;
    private ImageButton btnSend;
    private SeekBar seekbar;
    private EditText editMessaggioOpzionale;
    private ToggleButton showMessageToggle;
    private LinearLayout panelTheOtherLoading;
    private LinearLayout panelMyLoading;

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
        textViewMyAvailability = (TextView) findViewById(R.id.my_availability);
        textViewTheOtherAvailability = (TextView) findViewById(R.id.theother_availability);
        btnSend = (ImageButton) findViewById(R.id.set_avail_btn);
        seekbar = (SeekBar) findViewById(R.id.seekHowManyHours1);
        editMessaggioOpzionale = (EditText) findViewById(R.id.editMessaggioOpzionale);
        showMessageToggle = (ToggleButton) findViewById(R.id.show_message);
        panelMyLoading = (LinearLayout) findViewById(R.id.my_loading);
        panelTheOtherLoading = (LinearLayout) findViewById(R.id.theother_loading);

        // Restore settings
        SharedPreferences settings = getSharedPreferences(Logic.PREFS_NAME, 0);
        String username = settings.getString("User", "Dana");
        this.theLogic.setUsername(username);

        Log.d(Logic.TAG, "Current user is " + this.theLogic.getUsername());

        // Set statusbar color
        if (Build.VERSION.SDK_INT >= 21)
        {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(R.color.status_color));
        }

        ActionBar actionBar = this.getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayShowTitleEnabled(false);
        }

    }

    private String getAeDVersion()
    {
        String versionText;

        try
        {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionText = pInfo.versionName;
        }
        catch (PackageManager.NameNotFoundException nex)
        {
            versionText = "AeD unknown version";
        }

        return versionText;

    }

    private void setUIStandBy()
    {
        // Set UI in waiting state...
        this.panelMyLoading.setVisibility(View.VISIBLE);
        this.panelTheOtherLoading.setVisibility(View.VISIBLE);
        this.textViewMyAvailability.setVisibility(View.INVISIBLE);
        this.textViewTheOtherAvailability.setVisibility(View.INVISIBLE);

        seekbar.setProgress(0);
        swtSms.setChecked(true);

    }

    private void refresh()
    {
        // Set UI in waiting state...
        this.setUIStandBy();
        this.setSendButtonVisible(false);

        // Get availability
        new GetAvailabilityTask().execute();
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
    public void onBackPressed()
    {
        this.finish();
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
        else if (howManyHours == 0)
        {
            availType = "red";
            sb = new StringBuilder("Non disponibile.");
        }
        else
        {
            availType = "gray";
            sb = new StringBuilder("...");
        }

        this.setMyAvailColor(availType, true);
        textViewMyAvailability.setText(sb.toString());
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
                Log.d(Logic.TAG, "SMS Check change...");
                drawMyAvailability();
            }
        });

        // Seekbar: how many hours
        seekbar.setMax(Logic.MAX_HOURS - Logic.MIN_HOURS);
        seekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar)
                    {
                        if (howManyHours == 0)
                        {
                            swtSms.setEnabled(false);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar)
                    {
                        Log.d(Logic.TAG, "Seekbar start change...");

                        setSendButtonVisible(true);
                        swtSms.setEnabled(true);
                        editMessaggioOpzionale.setText("");
                        howManyHours = Logic.MIN_HOURS + seekBar.getProgress();
                        drawMyAvailability();
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {
                        Log.d(Logic.TAG, "Seekbar progress change...");
                        if (fromUser)
                        {
                            howManyHours = Logic.MIN_HOURS + progress;
                            drawMyAvailability();
                        }
                    }
                }
        );

        callmeButton.setEnabled(false);
        swtSms.setEnabled(false);

        btnSend.setOnClickListener(
                new Button.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        sendMyAvailability();
                    }
                }
        );

        showMessageToggle.setOnClickListener(
                new ToggleButton.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        displaySecret(textViewTheOtherAvailability);
                    }
                }
        );
    }

    private void sendMyAvailability()
    {
        String availType = getAvailType();
        String availTime = Integer.valueOf(howManyHours).toString();
        String availMessage = "";
        if (this.editMessaggioOpzionale.getText().length() > 0)
        {
            availMessage = this.editMessaggioOpzionale.getText().toString();
        }

        Log.d(Logic.TAG, "Setting new availability... ");
        this.setUIStandBy();
        new SetAvailabilityTask().execute(availType.toLowerCase(), availTime, availMessage);
    }

    private String getAvailType()
    {
        String availType = "Al telefono";

        if (this.howManyHours == 0)
        {
            availType = "Non disponibile";
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
        private String theOtherPhone;

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

        public String getTheOtherPhone()
        {
            return theOtherPhone;
        }

        public void setTheOtherPhone(String theOtherPhone)
        {
            this.theOtherPhone = theOtherPhone;
        }
    }

    /**
     *
     * @param uiType one of "green", "yellow", "red"
     * @param isChanging if I am changing my availability
     */
    private void setMyAvailColor(String uiType, boolean isChanging)
    {

        Log.d(Logic.TAG, "Setting My Avail = " + uiType);

        TextView textView = textViewMyAvailability;
        Drawable background;

        int colorGreen = R.drawable.green_rounded;
        int colorYellow = R.drawable.yellow_rounded;
        int colorRed = R.drawable.red_rounded;

        if (isChanging)
        {
            colorGreen = R.drawable.light_green_rounded;
            colorYellow = R.drawable.light_yellow_rounded;
            colorRed = R.drawable.light_red_rounded;
        }

        switch (uiType)
        {
            case "green":
                background = ContextCompat.getDrawable(this, colorGreen);
                textView.setTextColor(COLOR_BLACK);
                break;
            case "yellow":
                textView.setTextColor(COLOR_GRAY);
                background = ContextCompat.getDrawable(this, colorYellow);
                break;
            case "red":
                textView.setTextColor(COLOR_LTGRAY);
                background = ContextCompat.getDrawable(this, colorRed);
                break;
            default:
                textView.setTextColor(COLOR_LTGRAY);
                background = ContextCompat.getDrawable(this, R.drawable.gray_rounded);
                break;
        }

        if (textView.getVisibility() == View.INVISIBLE)
        {
            this.panelMyLoading.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
        }

        textView.setBackground(background);

        if (!isChanging)
        {
            textView.startAnimation(AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_in));
        }

    }

    /**
     *
     * @param uiType one of "green", "yellow", "red"
     */
    private void setTheOtherAvailUI(String uiType)
    {

        TextView textView = this.textViewTheOtherAvailability;
        Drawable background;

        int imageId;
        boolean callEnabled;
        String callMeTag = "?";

        switch (uiType)
        {
            case "green":
                imageId = R.drawable.callme;
                callEnabled = true;
                callMeTag = "Phone";
                background = ContextCompat.getDrawable(this, R.drawable.green_rounded);
                textView.setTextColor(COLOR_BLACK);
                break;
            case "yellow":
                imageId = R.drawable.whatsapp;
                callEnabled = true;
                callMeTag = "WhatsApp";
                textView.setTextColor(COLOR_GRAY);
                background = ContextCompat.getDrawable(this, R.drawable.yellow_rounded);
                break;
            case "red":
                imageId = R.drawable.callmebn;
                callEnabled = false;
                textView.setTextColor(COLOR_LTGRAY);
                background = ContextCompat.getDrawable(this, R.drawable.red_rounded);
                break;
            default:
                imageId = R.drawable.callmebn;
                callEnabled = false;
                textView.setTextColor(COLOR_LTGRAY);
                background = ContextCompat.getDrawable(this, R.drawable.gray_rounded);
                break;
        }

        this.panelTheOtherLoading.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.VISIBLE);

        textView.setBackground(background);
        textView.startAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in));

        this.callmeButton.setImageResource(imageId);
        this.callmeButton.setEnabled(callEnabled);
        this.callmeButton.setTag(callMeTag);
    }

    private class GetAvailabilityTask extends AsyncTask<Void, Void, AvailabilityResult[]>
    {
        @Override
        protected AvailabilityResult[] doInBackground(Void... params)
        {
            Log.d(Logic.TAG, "Trying to connect to server to get availability...");
            Aledanaapi apis = theLogic.buildRemoteServiceObject();
            AvailabilityResult[] results = new AvailabilityResult[]
                    {
                            new AvailabilityResult(),
                            new AvailabilityResult()
                    };

            try
            {
                AledanaEndpointsGetAllAvailabilitiesResponse getAllAvailResponse =
                        apis.getall().execute();
                results[0].setResultColor(getAllAvailResponse.getAvailAleColor());
                results[0].setResultDescription(getAllAvailResponse.getAvailAleDescription());
                results[0].setResultMessage(getAllAvailResponse.getAvailAleMessage());
                results[0].setTimeLeft(getAllAvailResponse.getAvailAleTime());
                results[0].setTheOtherPhone(getAllAvailResponse.getAlePhone());
                results[1].setResultColor(getAllAvailResponse.getAvailDanaColor());
                results[1].setResultDescription(getAllAvailResponse.getAvailDanaDescription());
                results[1].setResultMessage(getAllAvailResponse.getAvailDanaMessage());
                results[1].setTimeLeft(getAllAvailResponse.getAvailDanaTime());
                results[1].setTheOtherPhone(getAllAvailResponse.getDanaPhone());
            }
            catch (IOException e)
            {
                Log.e(Logic.TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return results;
        }

        @Override
        protected void onPostExecute(AvailabilityResult[] results)
        {
            Log.d(Logic.TAG, "Received results from server...");

            if (results == null)
                return;

            if (results.length != 2)
                return;

            if (results[0].getResultColor() == null)
                return;

            int[] resultIndex = {1, 0};
            if (theLogic.getUsername().equals("Alessio"))
            {
                resultIndex[0] = 0;
                resultIndex[1] = 1;
            }

            secretMessage = results[resultIndex[1]].getResultMessage();
            setMyAvailColor(results[resultIndex[0]].getResultColor(), false);
            setTheOtherAvailUI(results[resultIndex[1]].getResultColor());
            theLogic.setTheOtherPhone(results[resultIndex[0]].getTheOtherPhone());
            displayMessage(textViewMyAvailability, results[resultIndex[0]]);
            displayMessage(textViewTheOtherAvailability, results[resultIndex[1]]);
            displaySecret(textViewTheOtherAvailability);

            // Set version
            TextView textVersion = (TextView)findViewById(R.id.version);
            textVersion.setText("AeD version " + getAeDVersion());

        }

    }

    private void displaySecret(TextView textView)
    {
        String secret = secretMessage;
        String text = textView.getText().toString();

        if (secret != null && !secret.isEmpty())
        {
            showMessageToggle.setEnabled(true);
            if (showMessageToggle.isChecked())
            {
                if (!secretDisplayed)
                {
                    text += "\n\n\"";
                    text += secret;
                    text += "\"";
                }
                secretDisplayed = true;
            }
            else
            {
                if (secretDisplayed)
                {
                    text = text.substring(0, text.length() - secret.length() - 4);
                }
                secretDisplayed = false;
            }
        }
        else
        {
            showMessageToggle.setEnabled(false);
        }

        textView.setText(text);

    }

    private void displayMessage(TextView textView, AvailabilityResult result)
    {
        String text = result.getResultDescription();
        String remTime = result.getTimeLeft();

        if (remTime != null)
        {
            if (!remTime.startsWith("-"))
            {
                text += "\n";
                text += "Ancora per ";
                text += result.getTimeLeft().substring(0, 6);
            }
        }

        textView.setText(text);

    }

    private class SetAvailabilityTask extends AsyncTask<String, Void, Boolean>
    {

        private String availType;
        private String availUser;
        private String availTime;
        private String availMessage;
        private String key;

        @Override
        protected Boolean doInBackground(String... params)
        {
            Boolean availSet = Boolean.FALSE;

            Log.d(Logic.TAG, "Trying to connect to server to set availability...");

            availType = params[0];
            availUser = theLogic.getUsername();
            availTime = params[1];
            availMessage = params[2];
            key = Security.getSecurityToken();

            Log.d(Logic.TAG, "User = " + availUser);
            Log.d(Logic.TAG, "Type = " + availType);
            Log.d(Logic.TAG, "Time = " + availTime);
            Log.d(Logic.TAG, "Msg = " + availMessage);
            Log.d(Logic.TAG, "Key = " + key);

            Aledanaapi apis = theLogic.buildRemoteServiceObject();
            AledanaEndpointsSetAvailabilityRequest request =
                    new AledanaEndpointsSetAvailabilityRequest();
            request.setAvailtype(availType);
            request.setUsername(availUser);
            request.setHours(availTime);
            request.setMessage(availMessage);
            request.setKey(key);

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
            CharSequence text;
            Log.d(Logic.TAG, "Server responded with Availability...");

            if (result == Boolean.TRUE)
            {
                if (availTime.equals("1"))
                {
                    availTime = "1 ora.";
                }
                else
                {
                    availTime = availTime + " ore.";
                }
                text = "Impostato: " + availType;
                if (!availType.startsWith("non"))
                {
                    text = text + " per " + availTime;
                }
            }
            else
            {
                text = "Errore di sicurezza (503)";
                Log.e(Logic.TAG, "Something weird has happened...");
            }

            Toast toast = Toast.makeText(getApplicationContext(),
                    text, Toast.LENGTH_SHORT);
            toast.show();

            refresh();

        }

    }

}
