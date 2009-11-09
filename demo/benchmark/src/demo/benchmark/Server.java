package demo.benchmark;

import java.io.File;
import java.util.Properties;

import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;

public class Server
{
    public static void main (String[] args) throws Exception
    {
        Properties props = new Properties ();
        props.setProperty ("jacorb.implname", "demo.benchmark.server");

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
        org.omg.PortableServer.POA rootPOA = 
            org.omg.PortableServer.POAHelper.narrow (orb.resolve_initial_references ("RootPOA"));

        org.omg.CORBA.Policy[] policies = new org.omg.CORBA.Policy[2];

        policies[0] = rootPOA.create_id_assignment_policy (IdAssignmentPolicyValue.USER_ID);
        policies[1] = rootPOA.create_lifespan_policy (LifespanPolicyValue.PERSISTENT);

        POA benchPOA = rootPOA.create_POA ("BenchPOA",
                                           rootPOA.the_POAManager (), policies);

        rootPOA.the_POAManager ().activate ();

        byte[] oid = "benchServer".getBytes ();

        benchPOA.activate_object_with_id (oid, new benchImpl ());

        org.omg.CORBA.Object o = benchPOA.id_to_reference (oid);

        String ref = orb.object_to_string (o);
        String refFile = args[0];
        java.io.PrintWriter out = new java.io.PrintWriter (new java.io.FileOutputStream (refFile));
        out.println (ref);
        out.flush ();

        if (args.length == 2)
        {
            File killFile = new File (args[1]);
            while (!killFile.exists ())
            {
                Thread.sleep (1000);
            }
            orb.shutdown (true);
        }
        else
        {
            orb.run ();
        }
    }
}
