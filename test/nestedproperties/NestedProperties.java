package test.nestedproperties;

import java.util.*;
import org.apache.avalon.framework.configuration.ConfigurationException;

public class NestedProperties
{
    private org.omg.CORBA.ORB orb;

    class Config
    {
        private Properties propsBuiltIn; // as a representative of default properties. These
        // could be obtained from a multitude of sources including files, command line, even
        // interactively. For the purpose of this test, it is sufficient to demonstrate only
        // a single level of nesting.
        private Properties allProps; // Accessor

        void initialize ()
            throws Exception
        {
            propsBuiltIn = new Properties ();
            allProps = new Properties (propsBuiltIn);

            propsBuiltIn.setProperty ("jacorb.poa.thread_priority", Integer.toString (Thread.NORM_PRIORITY)); // so it can be
            // overridden by
            // others
            propsBuiltIn.setProperty ("OAIAddr", java.net.InetAddress.getLocalHost ().getHostAddress ());
            propsBuiltIn.setProperty ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            propsBuiltIn.setProperty ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
            propsBuiltIn.setProperty ("java.vm.version", System.getProperty("java.vm.version"));
            propsBuiltIn.setProperty ("org.omg.PortableInterceptor.ORBInitializerClass.FailoverInit",
                                      "com.quantel.QuentinManager.Failover.ClientFailoverInitializer");
            propsBuiltIn.setProperty ("test_property1","do wah diddy");

            allProps.setProperty ("test_property2","tp2");
        }

        Properties getProperties ()
        {
            return allProps;
        }

    }

    public void start (String args[])
    {
        String propsFileName = null;
        Config myConfig = new Config();

        // Props file is first non-switch argument
        for (int i = 0; i < args.length; i++)
            if (!args[i].startsWith ("-"))
                {
                    if (propsFileName == null)
                        propsFileName = args[i];
                }

        try {
            myConfig.initialize ();
        } catch (Exception e) {
            System.err.println ("Problem with configuration");
            e.printStackTrace ();
            System.exit (1);
        }

        // initialise the ORB from the properties list
        Properties props = myConfig.getProperties ();
        orb = org.omg.CORBA.ORB.init (args, props);

        org.jacorb.orb.ORB jorb = (org.jacorb.orb.ORB)orb;
        org.jacorb.config.Configuration jconf = jorb.getConfiguration();
        String test;
        try {
            test = jconf.getAttribute ("test_property1");
            System.out.println ("test_property1 value = " + test);
            test = jconf.getAttribute ("test_property2");
            System.out.println ("test_property2 value = " + test);
            System.out.println ("SUCCESS");
        } catch (ConfigurationException ex) {
            System.out.println ("ERROR! test failed");
            ex.printStackTrace();
        }
    }

    public static void main (String args[])
    {
        NestedProperties np = new NestedProperties();
        np.start(args);

    }
}
