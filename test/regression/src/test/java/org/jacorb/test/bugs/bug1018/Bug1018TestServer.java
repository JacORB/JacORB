package org.jacorb.test.bugs.bug1018;

import org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageImpl;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Bug1018TestServer
{
    public static void main(String[] args)
    {
        try
        {
            //init ORB
            ORB orb = ORB.init(args, null);

            //init POA
            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            //register Factory with the naming service
            NamingContextExt nc =
                    NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

            try
            {
                nc.bind_new_context (nc.to_name("bug1018"));
            }
            catch (Exception e) {}

            EchoMessageImpl echoServant = new EchoMessageImpl("bug1018 test server");

            poa.activate_object(echoServant);
            org.omg.CORBA.Object echoRef = poa.servant_to_reference(echoServant);
            nc.rebind(nc.to_name("bug1018/echo"), echoRef);
            System.out.println("SERVER IOR: " + orb.object_to_string(echoRef));

            //poa.the_POAManager().activate();
            //orb.run();
            Thread.sleep(30000);

        }
        catch (Exception e)
        {
        }
    }
}
