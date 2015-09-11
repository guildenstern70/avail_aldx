package net.littlelite.aledana;

import com.appspot.aledana_ep.aledanaapi.Aledanaapi;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;


/**
 * Created by Alessio on 06/08/2015.
 */
public class Logic
{
    private String theOtherPhone;

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final  String TAG = "ALEDANA_LOG";

    public static final int MIN_HOURS = 0;
    public static final int MAX_HOURS = 5;

    private static Logic instance = null;
    private String username;

    protected Logic()
    {
        this.theOtherPhone = "?";
    }

    public static Logic getInstance()
    {
        if (instance == null)
        {
            instance = new Logic();
        }
        return instance;
    }

    public String getUsername()
    {
        return this.username;
    }

    public String getTheOther()
    {
        if (this.username.equals("Alessio"))
            return "Dana";
        return "Alessio";
    }

    public String getTheOtherPhone()
    {
        return this.theOtherPhone;
    }

    public void setTheOtherPhone(String theOther)
    {
        this.theOtherPhone = theOther;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public Aledanaapi buildRemoteServiceObject()
    {
        Aledanaapi.Builder builder = new Aledanaapi.Builder(
                AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
        builder.setApplicationName("aledana-ep");
        return builder.build();
    }

}

