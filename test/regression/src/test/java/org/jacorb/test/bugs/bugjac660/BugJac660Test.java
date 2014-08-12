package org.jacorb.test.bugs.bugjac660;

import java.util.Properties;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.slf4j.Logger;

public class BugJac660Test extends ORBTestCase
{
    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.a",
                          "org.jacorb.test.bugs.bugjac660.Initializer" );
    }

    @Test
    public void testSlotsAndCurrent() throws Exception
    {
        ServerThread th = new ServerThread (orb);
        th.start();

        Thread.sleep(1000);

        // create the object reference
        org.omg.CORBA.Object obj =
                rootPOA.servant_to_reference (new TestObjectImpl(orb));

        TestObject to = TestObjectHelper.narrow (obj);

        org.omg.PortableInterceptor.Current current =
                (org.omg.PortableInterceptor.Current)
                orb.resolve_initial_references ("PICurrent");

        Logger logger = ((org.jacorb.orb.ORB)orb).getConfiguration ().getLogger("org.jacorb.test");

        Any any = orb.create_any();
        any.insert_string ("This is a test AAA" );

        current.set_slot (Initializer.slot_id, any);

        logger.debug ("[" + Thread.currentThread() + "] Client added any to PICurrent : " + any);
        to.foo();

        logger.debug ("[" + Thread.currentThread() + "] Client end");
    }
}


class ServerThread extends Thread
{
    private ORB orb = null;

    public ServerThread(ORB orb)
    {
        this.orb = orb;
    }

    @Override
    public void run ()
    {
        try
        {
            // wait for requests
            orb.run();
        }
        catch (Exception e)
        {
            throw new INTERNAL (e.getMessage());
        }
    }
}

class TestObjectImpl
    extends TestObjectPOA
{
    public ORB orb;
    public Logger logger;

    public TestObjectImpl(ORB orb)
    {
        this.orb = orb;
        logger = ((org.jacorb.orb.ORB)orb).getConfiguration ().getLogger("org.jacorb.test");
    }

    @Override
    public void foo()
    {
        try
        {
            org.omg.PortableInterceptor.Current current =
                (org.omg.PortableInterceptor.Current)
            orb.resolve_initial_references( "PICurrent" );

            Any any = current.get_slot (Initializer.slot_id);

            String result = any.extract_string();

            if (! result.equals ("This is a test AAA"))
            {
                throw new Exception ("Did not receive correct message : got <"
                                     + result + "> and expected <This is a test AAA>");
            }

            logger.debug ("[" + Thread.currentThread() + "] Server extracted from PICurrent: >>" + result + "<<");
        }
        catch( Exception e )
        {
            throw new INTERNAL (e.getMessage());
        }
    }
}
