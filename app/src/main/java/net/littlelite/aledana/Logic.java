package net.littlelite.aledana;

import com.appspot.aledana_ep.aledanaapi.Aledanaapi;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;


/**
 * Created by Alessio on 06/08/2015.
 */
public class Logic
{
    public static final String PREFS_NAME = "MyPrefsFile";

    private static Logic instance = null;
    private String username;

    protected Logic()
    {
    }

    public static Logic getInstance()
    {
        if(instance == null)
        {
            instance = new Logic();
        }
        return instance;
    }

    public String getUsername()
    {
        return this.username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    private Aledanaapi buildRemoteServiceObject()
    {
        Aledanaapi.Builder builder = new Aledanaapi.Builder(
                AndroidHttp.newCompatibleTransport(),  new GsonFactory(), null);
        return builder.build();

    }
}

