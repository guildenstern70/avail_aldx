package net.littlelite.aledana;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.appspot.aledana_ep.aledanaapi.Aledanaapi;
import com.appspot.aledana_ep.aledanaapi.model.AledanaEndpointsAliveResponse;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;


/**
 * Created by Alessio on 06/08/2015.
 */
public class Logic
{
    private static final String ALE_CELL = "+393484979839";
    private static final String DANA_CELL = "+393355999621";

    public static final String PREFS_NAME = "MyPrefsFile";
    public final static String TAG = "ALEDANA_LOG";

    private static Logic instance = null;
    private String username;

    protected Logic()
    {
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
        String theOtherPhone = ALE_CELL;
        if (this.username.equals("Alessio"))
            theOtherPhone = DANA_CELL;
        return theOtherPhone;
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

