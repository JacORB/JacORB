package demo.concurrency;

import java.io.*;

public class InConsole
{
    public static String read ()
    {
        StringBuffer buff = new StringBuffer ();
        char ch = ' ';
        BufferedReader reader = new BufferedReader (new InputStreamReader (System.in));
        try
        {
            while (ch != '\r' && ch != '\n')
            {
                ch = (char) reader.read ();
                if (ch != (char) -1 && ch != '\r' && ch != '\n')
                {
                    buff.append (ch);
                }
            }
        }
        catch (Exception e)
        {
            ch = '\r';
        }
        return buff.toString ();
    }
}
