package org.jacorb.test.poa;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import junit.framework.*;


public class Deactivate extends TestCase
{
   public Deactivate (String name)
   {
      super (name);
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite ("POA Tests");

      suite.addTest (new Deactivate ("test_deactivate"));

      return suite;
   }

   public static void test_deactivate ()
   {
      ORB orb = null;
      POA root, system = null;
      byte[] id1, id2;

      try
      {
         orb = ORB.init(new String[0], null);
         root = POAHelper.narrow(orb.resolve_initial_references( "RootPOA" ));

         // create POA
         Policy policies[] = new Policy[3];
         policies[0] = root.create_id_assignment_policy(
            org.omg.PortableServer.IdAssignmentPolicyValue.SYSTEM_ID);
         policies[1] = root.create_id_uniqueness_policy(
            org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID);
         policies[2] = root.create_servant_retention_policy(
            org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN);

         system = root.create_POA("system_id", root.the_POAManager(), policies);
      }
      catch(AdapterAlreadyExists ex)
      {
         fail( "unexpected exception: " + ex );
      }
      catch(InvalidPolicy ex)
      {
         fail( "unexpected exception: " + ex );
      }
      catch(Exception  ex)
      {
         fail( "unexpected exception: " + ex );
      }

      // create Servants
      Test_impl servant1 = new Test_impl();
      Test_impl servant2 = new Test_impl();
      // first activate servants
      try
      {
         id1 = system.activate_object(servant1);
         id2 = system.activate_object(servant2);

         // deactivate the servants now
         // no request is pending
         try
         {
            system.deactivate_object(id2);
            system.deactivate_object(id1);

            // now again try to deactivate
            // I would expect ObjectNotActive Exception but didn't get one
            try
            {
               system.deactivate_object(id1);
               fail( "deactivate_object called twice, expecting ObjectNotActive exception, but didn't");
            }
            catch(ObjectNotActive ex)
            {
               System.out.println("Success - caught ObjectNotActive");
            }
            catch(WrongPolicy ex)
            {
               fail( "unexpected exception: " + ex );
            }
         }
         catch(ObjectNotActive ex)
         {
            fail( "unexpected exception: " + ex );
         }
         catch(WrongPolicy ex)
         {
            fail( "unexpected exception: " + ex );
         }
      }
      catch(ServantAlreadyActive ex)
      {
         fail( "unexpected exception: " + ex );
      }
      catch(WrongPolicy ex)
      {
         fail( "unexpected exception: " + ex );
      }
   }
}
