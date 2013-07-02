package org.jacorb.test.bugs.bugjac660;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class BugJac660Test extends TestCase
{

    public static Test suite ()
    {
        TestSuite suite = new TestSuite (BugJac660Test.class);

        return suite;
    }

    protected void setUp () throws Exception
    {
    }

    protected void tearDown () throws Exception
    {
    }

    public void testSlotsAndCurrent()
    {
        Thread th = new ServerThread ();
        th.start();

        try
        {
            Thread.sleep(1000);

            // initialize the ORB.
            ORB orb = ORB.init (new String [0], null );

            //init POA
            POA poa =
            POAHelper.narrow( orb.resolve_initial_references ("RootPOA"));

            // create the object reference
            org.omg.CORBA.Object obj =
               poa.servant_to_reference (new TestObjectImpl());

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
    }
}


class ServerThread extends Thread
{
    public static ORB orb = null;

    public void run ()
    {
        try
        {
            System.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass.a",
                                "org.jacorb.test.bugs.bugjac660.Initializer" );

            //init ORB
            String [] args = null;
            orb = ORB.init( args, null );

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

    public void foo()
    {
        try
        {
            org.omg.PortableInterceptor.Current current =
                (org.omg.PortableInterceptor.Current)
            ServerThread.orb.resolve_initial_references( "PICurrent" );

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
