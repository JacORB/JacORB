package org.jacorb.test.miop;

import java.util.Properties;

import junit.framework.TestCase;

import org.jacorb.orb.util.CorbaLoc;
import org.omg.CORBA.INV_OBJREF;
import org.omg.PortableGroup.GOA;
import org.omg.PortableGroup.GOAHelper;

public class MIOPTest extends TestCase
{
    private Thread serverThread;
    private Properties props = new Properties ();
    private String miopURL   = "corbaloc:miop:1.0@1.0-TestDomain-1/224.1.239.2:1234";
    private String groupURL;

    private class Server implements Runnable
    {
        public volatile boolean isReady = false;

        public void run ()
        {
            try
            {
                org.omg.CORBA.ORB  orb = org.omg.CORBA.ORB.init((String[])null, props);

                GreetingService helloGroup = GreetingServiceHelper.unchecked_narrow(orb.string_to_object(miopURL));

                org.omg.PortableServer.POA poa;

                poa = org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

                poa.the_POAManager().activate();
                GOA goa = GOAHelper.narrow(poa);

                GreetingImpl helloServant = new GreetingImpl();

                byte[] oid = poa.activate_object(helloServant);
                goa.associate_reference_with_id(helloGroup,oid);

                groupURL = miopURL + ";" + CorbaLoc.generateCorbaloc (orb, helloServant._this());

                System.err.println ("Corbaloc: " + groupURL);

                this.isReady = true;
                orb.run();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public MIOPTest()
    {
        props.setProperty
           ("jacorb.transport.factories", "org.jacorb.orb.iiop.IIOPFactories,org.jacorb.orb.miop.MIOPFactories");
        props.setProperty
           ("jacorb.transport.client.selector", "org.jacorb.orb.miop.MIOPProfileSelector");
    }

    protected void setUp()
    {
        Server server = new Server();
        serverThread =  new Thread(server);
        serverThread.start();
        while(!server.isReady)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void testMIOP()
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init((String[])null,props);

        // Use an unchecked narrow so it doesn't do an is_a call remotely.
        GreetingService helloGroup = GreetingServiceHelper.unchecked_narrow(orb.string_to_object(groupURL));

        helloGroup.greeting_oneway("Oneway call");

        helloGroup.shutdown ();

        // A normal narrow should do a remote call. This will need the group IIOP profile which
        // may not have been transmitted so we do this part last.
        try
        {
           helloGroup = GreetingServiceHelper.narrow(orb.string_to_object(groupURL));
        }
        catch (INV_OBJREF e)
        {
            fail("Unable to narrow due to no Group IIOP Profile");
        }
    }

    protected void tearDown() throws Exception
    {
        serverThread.interrupt();
        serverThread.join();
    }
}
