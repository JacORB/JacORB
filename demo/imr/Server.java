package demo.grid;

import java.io.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;

public class Server
{
    public static void main( String[] args )
    {

        System.setProperty( "jacorb.implname", "grid" );
        System.setProperty( "jacorb.use_imr", "off" );
        System.setProperty( "OAPort", "8899" );
	try
	{
            System.out.println("ImR Grid Server");
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);

       	    org.omg.PortableServer.POA root_poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    root_poa.the_POAManager().activate();

	    org.omg.CORBA.Policy [] policies = new org.omg.CORBA.Policy[2];

	    policies[0] = root_poa.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
	    policies[0] = root_poa.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
	    POA grid_poa = root_poa.create_POA( "GridServerPOA", 
                                                root_poa.the_POAManager(), 
                                                policies );
	    for (int i=0; i<policies.length; i++) 
		policies[i].destroy();			

            gridImpl gi = new gridImpl();

            byte[] id = "grid".getBytes();

            grid_poa.activate_object_with_id( id, gi );
	    
            // use the naming service

            NamingContextExt nc = 
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            try
            {
                nc.resolve( nc.to_name("grid.example") );
            }
            catch( org.omg.CosNaming.NamingContextPackage.NotFound nf )
            {
                //Server isn't bound, so bind it
                org.omg.CORBA.Object o = 
                    grid_poa.servant_to_reference( gi );

                nc.bind( nc.to_name("grid.example"), o);
            }            
        
	    root_poa.the_POAManager().activate();

            orb.run();
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	}        
    }
}


