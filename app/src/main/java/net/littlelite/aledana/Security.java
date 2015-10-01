package net.littlelite.aledana;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

public class Security
{
    /**
     * Python equivalent:
     *
     *

         import datetime
         import md5

         def security_check():
         d = datetime.date.today()
         month = d.month
         day = d.day
         year = d.year
         return month*day*year*17

         def token():
         m = md5.new()
         m.update(str(security_check()))
         return m.hexdigest()

     *
     */

    private static int getMagicNumber()
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get((Calendar.DAY_OF_MONTH);
        return (month*day*year*17);
    }

    private static String getToken(int magicNumber)
    {
        String hashtext = "?";
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(String.valueOf(magicNumber).getBytes());
            byte[] digest = md5.digest();
            BigInteger bigInt = new BigInteger(1,digest);
            hashtext = bigInt.toString(16);
            while(hashtext.length() < 32 )
            {
                hashtext = "0"+hashtext;
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return hashtext;
    }

    public static void test()
    {
        System.out.println("Magic Number: " + String.valueOf(getMagicNumber()));
        System.out.println("Token: " + getToken(getMagicNumber()));
    }
}
