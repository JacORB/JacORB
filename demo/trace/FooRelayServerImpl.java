package demo.trace;

import java.util.Random;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;

import org.jacorb.orb.domain.*;
/**
 * FooRelayServerImpl.java
 *
 *
 * Created: Mon Jul 24 14:47:07 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */

public class FooRelayServerImpl 
    extends FooRelayServerPOA
{
    
    private static final int MAX_SLEEP = 100; //ms

    private static FooServer server1 = null;
    private static FooServer server2 = null;

    private Random rnd = null;

    public FooRelayServerImpl()
    {        
        rnd = new Random();
    }

    public void anotherOperation(String param)
    {
        int sleep = Math.abs(rnd.nextInt()) % MAX_SLEEP;
     
        System.out.println("FooRelayServer received: " + param );        
        System.out.println("FooRelayServer will sleep " + sleep + "ms");

        try
        {
            Thread.sleep(sleep);
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        System.out.println("FooRelayServer will call server 1");       
        server1.anOperation("Hello server 1");

        System.out.println("FooRelayServer will call server 2");       
        server2.anOperation("Hello server 2");
    }
    
    public static void main(String[] args)
    {
        try
        {
            ORB orb = ORB.init(args, null);
            
            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	    poa.the_POAManager().activate();
	    
	    org.omg.CORBA.Object o = poa.servant_to_reference(new FooRelayServerImpl());

            NamingContextExt nc = NamingContextExtHelper.narrow
                (orb.resolve_initial_references("NameService"));

            nc.bind( nc.to_name("relay_server.trace_demo"), o );

	    o = nc.resolve( nc.to_name("server1.trace_demo") );
            server1 = FooServerHelper.narrow( o );
            
            o = nc.resolve( nc.to_name("server2.trace_demo") );
            server2 = FooServerHelper.narrow( o );

            o = orb.resolve_initial_references( "LocalDomainService" );
            ORBDomain d = ORBDomainHelper.narrow( o );

            PropertyPolicy pp = d.createPropertyPolicy();
            pp.name("Tracing Policy");
            pp.put("trace", "on");
            pp.setPolicyType(303);
            d.set_domain_policy(pp);
            
            orb.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }              
} // FooRelayServerImpl
