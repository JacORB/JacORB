package org.jacorb.test.common;

import java.util.Properties;
import junit.framework.TestCase;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public abstract class ORBTestCase extends TestCase
{
    protected ORB orb;
    protected POA rootPOA;

    protected final void setUp() throws Exception
    {
        Properties props = new Properties();

        props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.setProperty("org.omg.CORBA.ORBSingleton", "org.jacorb.orb.ORBSingleton");

        patchORBProperties(getName(), props);

        orb = ORB.init(getORBArgs(getName()), props);
        rootPOA = POAHelper.narrow(orb.resolve_initial_references( "RootPOA" ));

        doSetUp();

        rootPOA.the_POAManager().activate();
    }

    protected String[] getORBArgs(String testName)
    {
        return new String[0];
    }

    protected void patchORBProperties(String testName, Properties props) throws Exception
    {
    }

    protected void doSetUp() throws Exception
    {
    }

    protected void tearDown() throws Exception
    {
        doTearDown();

        rootPOA = null;

        orb.shutdown(true);
        orb = null;
    }

    protected void doTearDown() throws Exception
    {
    }
}
