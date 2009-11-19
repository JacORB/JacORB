package org.jacorb.test.common;

import java.util.Properties;

import junit.framework.TestCase;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class ORBTestCase extends TestCase
{
    protected ORB orb;
    protected POA rootPOA;

    protected final void setUp() throws Exception
    {
    	Properties properties = new Properties();

    	properties.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
    	properties.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

    	patchORBProperties(properties);

        orb = ORB.init(new String[0], properties);
        rootPOA = POAHelper.narrow(orb.resolve_initial_references( "RootPOA" ));

        doSetUp();

        rootPOA.the_POAManager().activate();
    }

    protected void patchORBProperties(Properties properties)
    {
	}

	protected void doSetUp() throws Exception
    {
    }

    protected final void tearDown() throws Exception
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
