package org.jacorb.test.nestedproperties;

import java.util.Properties;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jacorb.config.ConfigurationException;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class NestedPropertiesTest extends TestCase
{
    private static final String prop1 = "do wah diddy";
    private static final String prop2 = "tp2";

    protected ORB orb;
    protected POA rootPOA;

    private class Config
    {
        private Properties propsBuiltIn; // as a representative of default properties. These
        // could be obtained from a multitude of sources including files, command line, even
        // interactively. For the purpose of this test, it is sufficient to demonstrate only
        // a single level of nesting.
        private Properties allProps; // Accessor

        public void initialize () throws Exception
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
            propsBuiltIn.setProperty ("test_property1", prop1);

            allProps.setProperty ("test_property2", prop2);
        }

        public Properties getProperties ()
        {
            return allProps;
        }
    }

    protected final void setUp() throws Exception
    {
        Config myConfig = new Config();
        try
        {
            myConfig.initialize ();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Properties properties = myConfig.getProperties();

        properties.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        properties.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        patchORBProperties(properties);

        orb = ORB.init(new String[0], properties);
        rootPOA = POAHelper.narrow(orb.resolve_initial_references( "RootPOA" ));

        rootPOA.the_POAManager().activate();
    }

    protected final void tearDown() throws Exception
    {
        rootPOA = null;

        orb.shutdown(true);
        orb = null;
    }

    protected void patchORBProperties(Properties properties)
    {
        Config myConfig = new Config();
        try
        {
            myConfig.initialize ();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // initialise the ORB from the properties list
        properties.putAll(myConfig.getProperties());
    }

    public void testNestedProperties ()
    {
        org.jacorb.orb.ORB jorb = (org.jacorb.orb.ORB)orb;
        org.jacorb.config.Configuration jconf = jorb.getConfiguration();
        try {
                Assert.assertEquals(prop1, jconf.getAttribute ("test_property1"));
                Assert.assertEquals(prop2, jconf.getAttribute ("test_property2"));
        }
        catch (ConfigurationException ex)
        {
            ex.printStackTrace();
            fail();
        }
    }
}
