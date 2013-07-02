package org.jacorb.test.bugs.bugjac685;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import junit.framework.Test;
import org.jacorb.naming.NameServer;
import org.jacorb.test.common.ServerSetup;
import org.jacorb.test.common.TestUtils;


public class NameServiceSetup extends ServerSetup
{

    public NameServiceSetup (Test test) throws Exception
    {
        super (test, NameServiceRunner.class.getName(), NameServer.class.getName(), newProps());

        errName = "NS-ERR";
        outName = "NS-OUT";
    }

    protected String [] getServerArgs()
    {
        return null;
    }

    private static Properties newProps() throws IOException
    {
        Properties props = new Properties();

        final File nsIOR = File.createTempFile ("ns.ior", ".ior");

        nsIOR.deleteOnExit();
        props.setProperty ("jacorb.naming.ior_filename", nsIOR.toString());
        props.setProperty ("jacorb.test.timeout.server", Long.toString(15000));
        props.setProperty("jacorb.test.ssl", "false");

        if (TestUtils.verbose)
        {
            props.setProperty ("jacorb.log.default.verbosity", "4");
        }

        return props;
    }



}
