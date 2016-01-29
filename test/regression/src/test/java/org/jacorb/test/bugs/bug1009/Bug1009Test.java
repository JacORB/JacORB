package org.jacorb.test.bugs.bug1009;

import java.util.Properties;

import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import static org.junit.Assert.assertTrue;

public class Bug1009Test extends ORBTestCase
{
    public static ORB localOrb;
    public static i object;
    public static boolean ready = false;

    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.put("org.omg.PortableInterceptor.ORBInitializerClass."
                  + "org.jacorb.test.bugs.bug1009.ClientInitializer", "");
        props.put("jacorb.connection.client.connect_timeout","1000");
    }

    public static void main(String args[]) throws Exception
    {
        Bug1009Test bug = new Bug1009Test();
        Properties props = new Properties();
        bug.patchORBProperties(props);

        props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        try
        {
            localOrb = ORB.init((String[]) null, props);
            final POA poa = POAHelper.narrow(localOrb.resolve_initial_references("RootPOA"));
            poa.the_POAManager().activate();

            bug.testFailingCallsWithClientInterceptor();
        }
        catch (Exception e)
        {
            TestUtils.getLogger().debug("Caught exception ", e);
        }
    }

    @Test
    public void testFailingCallsWithClientInterceptor()
    {
        final String ref1 = "IOR:000000000000000f49444c3a746573742f693a312e300000000000010000000000000082000102000000000a3132372e302e312e3100c55d00000031afabcb000000002038168fa300000001000000000000000100000008526f6f74504f410000000008000000010000000014000000000000020000000100000020000000000001000100000002050100010001002000010109000000010001010000000026000000020002";
        final String ref2 = "IOR:000000000000000f49444c3a746573742f693a312e300000000000010000000000000082000102000000000a3132372e302e312e3100882300000031afabcb0000000020381e7d5d00000001000000000000000100000008526f6f74504f410000000008000000010000000014000000000000020000000100000020000000000001000100000002050100010001002000010109000000010001010000000026000000020002";

        if (localOrb == null)
        {
            localOrb = orb;
        }

        i t1 = iHelper.unchecked_narrow(orb.string_to_object(ref1));
        i t2 = iHelper.unchecked_narrow(orb.string_to_object(ref2));

        object = t2;
        ready = true;

        TestUtils.getLogger().debug("Invoking test.i.f function " + ClientInterceptor.count);

        try
        {
            t1.f();
        }
        catch (Exception e)
        {
            TestUtils.getLogger().debug("Caught exception ", e);
        }

        TestUtils.getLogger().debug("Invoked test.i.f function " + ClientInterceptor.count);
        assertTrue(ClientInterceptor.count != 2);

    }
}
