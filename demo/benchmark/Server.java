package demo.benchmark;

import org.omg.CosNaming.*;
import org.omg.PortableServer.*;

public class Server
{
    public static void main( String[] args )
    {
	try
	{
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	    org.omg.PortableServer.POA rootPOA = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));		

            org.omg.CORBA.Policy [] policies = new org.omg.CORBA.Policy[2];

            policies[0] =
                rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
            policies[1] =
                rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);

            POA benchPOA = 
                rootPOA.create_POA("BenchPOA",rootPOA.the_POAManager(),policies);

	    rootPOA.the_POAManager().activate();

            byte [] oid = "benchServer".getBytes();

            benchPOA.activate_object_with_id(oid, new benchImpl());

	    org.omg.CORBA.Object o = benchPOA.id_to_reference(oid);
		 
	    if( args.length == 0 )
	    {
		NamingContextExt nc = 
		    NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
		nc.bind(nc.to_name("benchmark"), o);
	    }
	    else
	    {
                try
                {
                    String ref = orb.object_to_string( o );
                    String refFile = args[0];
                    java.io.PrintWriter out =
                        new java.io.PrintWriter(new java.io.FileOutputStream(refFile));
                    out.println(ref);
                    out.flush();
                }
                catch(java.io.IOException ex)
                {
                    System.err.println("Server: can't write to `" +
                                       ex.getMessage() + "'");
                    System.exit( 1 );
                }
	    }
	    orb.run();			
	} 
	catch (Exception e )
	{
	    e.printStackTrace();
	}
    }
}




