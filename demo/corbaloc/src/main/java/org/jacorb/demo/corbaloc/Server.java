
package demo.corbaloc;

import java.util.Properties;
import java.io.*;
import org.jacorb.orb.util.*;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

import org.omg.PortableServer.ImplicitActivationPolicyValue;

public class Server
{
   public static void main(String[] args)
   {
      try
      {

         Properties props = new Properties();
         props.setProperty("jacorb.implname", "HelloServer");
         props.setProperty("OAPort", "6969");

         String helloID = "HelloServerID";

         //init ORB
         ORB orb = ORB.init(args, props);

         //init POA
         POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

         //init new POA
         Policy[] policies = new Policy[2];
         policies[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
         policies[1] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);

         POA helloPOAPersistent = rootPOA.create_POA
            ("HelloPOAP", rootPOA.the_POAManager(), policies);


         // Setup a second POA with a transient policy therebye producing a different corbaloc.
         policies = new Policy[3];
         policies[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);
         policies[1] = rootPOA.create_id_assignment_policy (IdAssignmentPolicyValue.SYSTEM_ID);
         policies[2] = rootPOA.create_implicit_activation_policy (ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);

         POA helloPOATransient = rootPOA.create_POA
            ("HelloPOAT", rootPOA.the_POAManager(), policies);

         helloPOAPersistent.the_POAManager().activate();
         helloPOATransient.the_POAManager().activate();

         // create a GoodDay object
         GoodDayImpl goodDayImpl = new GoodDayImpl("SomewhereP");
         helloPOAPersistent.activate_object_with_id(helloID.getBytes(), goodDayImpl);

         // Manually create a persistent based corbaloc.
         String corbalocStr = "corbaloc::localhost:"
         + props.getProperty("OAPort") + "/"
         + props.getProperty("jacorb.implname") + "/"
         + helloPOAPersistent.the_name() + "/" + helloID;

         System.out.println("Server 1 can be reached with:");
         System.out.println("   " + corbalocStr + "\n");

         org.omg.CORBA.Object objP = orb.string_to_object(corbalocStr);
         System.out.println("Server 1 ior: " + orb.object_to_string (objP));

         PrintWriter ps = new PrintWriter(new FileOutputStream(new File( args[0] ) + "persistent"));
         ps.println( orb.object_to_string( objP ) );
         ps.close();

         // Setup second server
         org.omg.CORBA.Object objT = helloPOATransient.servant_to_reference(new GoodDayImpl("SomewhereT"));

         // Use the PrintIOR utility function to extract a transient corbaloc string.
         corbalocStr = PrintIOR.printCorbalocIOR (orb, orb.object_to_string(objT));
         System.out.println("Server 2 can be reached with:");
         System.out.println("   " + corbalocStr + "\n");

         ps = new PrintWriter(new FileOutputStream(new File( args[0] ) + "transient"));
         ps.println( corbalocStr );
         ps.close();


         // Add an object key mapping to second server
         System.out.println("Adding object mapping for server 1 ior:" + orb.object_to_string (objP));
         ((org.jacorb.orb.ORB)orb).addObjectKey ("VeryShortKey", orb.object_to_string (objP));


         // wait for requests

         if (args.length == 2)
         {
            File killFile = new File(args[1]);
            while(!killFile.exists())
            {
               Thread.sleep(1000);
            }
            orb.shutdown(true);
         }
         else
         {
            orb.run();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
