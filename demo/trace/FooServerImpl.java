package demo.trace;

import java.util.Random;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;

import jacorb.orb.domain.*;
/**
 * FooServerImpl.java
 *
 *
 * Created: Mon Jul 24 14:47:07 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */

public class FooServerImpl 
    extends FooServerPOA
{
    
    private static final int MAX_SLEEP = 100; //ms
    
    private Random rnd = null;

    public FooServerImpl()
    {        
        rnd = new Random();
    }

    public void anOperation(String param)
    {
        int sleep = Math.abs(rnd.nextInt()) % MAX_SLEEP;
     
        System.out.println("FooServer received: " + param );        
        System.out.println("FooServer will sleep " + sleep + "ms");       

        try
        {
            Thread.sleep(sleep);
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Usage: jaco demo.trace.FooServerImpl [ 1 | 2 ]");
            
            System.exit(-1);
        }

        try
        {
            ORB orb = ORB.init(args, null);
            
            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	    poa.the_POAManager().activate();
	    
	    org.omg.CORBA.Object o = poa.servant_to_reference(new FooServerImpl());

            NamingContextExt nc = NamingContextExtHelper.narrow
                (orb.resolve_initial_references("NameService"));

            nc.bind( nc.to_name("server" + args[0] + ".trace_demo"), o );

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
} // FooServerImpl
