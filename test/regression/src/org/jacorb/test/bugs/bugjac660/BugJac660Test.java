package org.jacorb.test.bugs.bugjac660;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.common.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class BugJac660Test extends ORBTestCase
{
    @Test
    public void testSlotsAndCurrent()
    {
        ServerThread th = new ServerThread (orbProps);
        th.start();

        try
        {
            Thread.sleep(1000);

            // initialize the ORB.
            ORB orb = ORB.init (new String [0], orbProps );

            //init POA
            POA poa =
            POAHelper.narrow( orb.resolve_initial_references ("RootPOA"));

            // create the object reference
            org.omg.CORBA.Object obj =
               poa.servant_to_reference (new TestObjectImpl(th.orb));

            TestObject to = TestObjectHelper.narrow (obj);

            org.omg.PortableInterceptor.Current current =
                (org.omg.PortableInterceptor.Current)
                   orb.resolve_initial_references ("PICurrent");

            Any any = orb.create_any();
            any.insert_string ("This is a test AAA" );

            current.set_slot (Initializer.slot_id, any);

            System.out.println ("[" + Thread.currentThread() + "] Client added any to PICurrent : " + any);
            to.foo();

            System.out.println ("[" + Thread.currentThread() + "] Client end");

            orb.shutdown (false);
        }
        catch( Exception ex )
        {
            fail ("Unexpected exception " + ex);
        }
        finally
        {
            th.orb.destroy();
        }
    }
}


class ServerThread extends Thread
{
    public ORB orb = null;
    public Properties props;

    public ServerThread(Properties orbProps)
    {
        props = orbProps;
    }

    public void run ()
    {
        try
        {
            System.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass.a",
                                "org.jacorb.test.bugs.bugjac660.Initializer" );

            //init ORB
            String [] args = null;
            orb = ORB.init( args, props );

            //init POA
            POA poa =
            POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

            poa.the_POAManager().activate();

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

    public TestObjectImpl(ORB orb)
    {
        this.orb = orb;
    }

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

            System.out.println ("[" + Thread.currentThread()
                               + "] Server extracted from PICurrent: >>" +
                               result + "<<");
        }
        catch( Exception e )
        {
            throw new INTERNAL (e.getMessage());
        }
    }
}
