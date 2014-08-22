package org.jacorb.test.harness;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.jacorb.naming.NameServer;
import org.junit.rules.TemporaryFolder;


public class NameServiceSetup extends ServerSetup
{
    public NameServiceSetup (TemporaryFolder folder) throws Exception
    {
        super(NameServiceSetup.class.getName(), NameServer.class.getName(), newProps(folder, null));

        errName = "NS-ERR";
        outName = "NS-OUT";
    }

    public NameServiceSetup (TemporaryFolder folder, Properties props, int id) throws Exception
    {
        super (NameServiceSetup.class.getName(), NameServer.class.getName(), newProps(folder, props));

        errName = "NS-" + Integer.toString(id) + "-ERR";
        outName = "NS-" + Integer.toString(id) + "-OUT";
    }

    private static Properties newProps(TemporaryFolder folder, Properties override) throws IOException
    {
        Properties props = new Properties();

        if (override != null)
        {
            props.putAll(override);
        }

        if ( ! props.containsKey("jacorb.naming.ior_filename"))
        {
            File nsIOR = folder.newFile("ns.ior");

            props.setProperty ("jacorb.naming.ior_filename", nsIOR.toString());
        }
        props.setProperty ("jacorb.naming.db_dir", folder.getRoot().toString());
        props.setProperty ("jacorb.test.timeout.server", Long.toString(15000));

        return props;
    }


    public static void main (final String [] args) throws Exception
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                TestUtils.getLogger().debug ("Starting the JacORB NameService");
                NameServer.main (args);
            }
        };

        thread.start ();

        try
        {
            Thread.sleep (3000);
        }
        catch (InterruptedException ie)
        {
        }

        File file = new File (System.getProperty ("jacorb.naming.ior_filename"));
        TestUtils.printServerIOR (file);
    }
}
