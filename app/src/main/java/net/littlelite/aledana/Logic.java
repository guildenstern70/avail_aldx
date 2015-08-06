package net.littlelite.aledana;

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
}

