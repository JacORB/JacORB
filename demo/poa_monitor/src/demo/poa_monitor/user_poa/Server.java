package demo.poa_monitor.user_poa;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import java.io.*;

public class Server 
{
    public final static String [] DESCRIPTIONS = 
    { "Servant Activator",
      "Servant Activator & location forward",
      "Servant Locator",
      "Default Servant & RETAIN",
      "One servant multiple oid's",
      "SINGLE_THREAD_MODEL"
    };

    public static int    kind;
    public static String factoryPOAName = "factoryPOA";
    public static String fooPOAName;
    public static String description;

    public static void main(String[] args)  
    {
        if( args.length != 1 || 
            (kind = Integer.parseInt(args[0])) < 1 || 
            kind > DESCRIPTIONS.length) 
        {
            String str = "";
            for (int i=1; i <= DESCRIPTIONS.length; i++ )
                str = i==DESCRIPTIONS.length ? str+i : str+i+"|";

            System.err.println("\n<usage: jaco [properties] demo.poa_monitor.user_poa.Server "+str+">\n");
            System.exit(0);
        }

        fooPOAName = "fooPOA"+kind;
        description = DESCRIPTIONS[kind-1];	

        try 
        {  
            ORB        orb     = org.omg.CORBA.ORB.init(args, null);
            POA        rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            POAManager poaMgr  = rootPOA.the_POAManager();
		    
            // create a user defined poa for the foo factory

            org.omg.CORBA.Policy [] policies = 
            {				
                rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT)
            };

            POA factoryPOA = 
                rootPOA.create_POA( factoryPOAName, poaMgr, policies);

            for (int i = 0; i < policies.length; i++)
                policies[i].destroy();
			
            // implicit activation of an adpater activator on root poa
            factoryPOA.the_activator( new FooAdapterActivatorImpl( orb ) );
			
            // explicit activation of the factory servant on factory poa
            FooFactoryImpl factoryServant = new FooFactoryImpl();
            factoryPOA.activate_object_with_id( new String("FooFactory").getBytes(), factoryServant );
			
            // register factory on name service 
            NamingContextExt nc = 
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

            nc.bind( nc.to_name("FooFactory.service") , factoryServant._this(orb) );

            // activate the poa manager
            poaMgr.activate();
            System.out.println("[ Server ready ]");			
            orb.run();
			
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}
