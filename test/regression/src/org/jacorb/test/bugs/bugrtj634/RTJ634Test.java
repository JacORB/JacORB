package org.jacorb.test.bugs.bugrtj634;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.ComplexTimingServer;
import org.jacorb.test.ComplexTimingServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


public class RTJ634Test extends ClientServerTestCase
{
   /**
    * <code>server1</code> is the server1 reference.
    */
   private ComplexTimingServer server1;

   static org.omg.CORBA.Object server2;

   public RTJ634Test(String name, ClientServerSetup setup)
   {
      super(name, setup);
   }

   /**
    * <code>setUp</code> for junit.
    *
    * @exception Exception if an error occurs
    */
   public void setUp() throws Exception
   {
      server1 = ComplexTimingServerHelper.narrow( setup.getServerObject() );

      org.omg.CORBA.Object o =  setup.getClientRootPOA().servant_to_reference (new TimingServerImpl (20));
      server2 = ComplexTimingServerHelper.narrow (o);
   }

   /**
    * <code>tearDown</code> us used by Junit for cleaning up after the tests.
    *
    * @exception Exception if an error occurs
    */
   protected void tearDown() throws Exception
   {
      super.tearDown();

      server1._release ();
      server1 = null;
   }

   /**
    * <code>suite</code> initialise the tests with the correct environment.
    *
    * @return a <code>Test</code> value
    */
   public static Test suite( )
   {
      TestSuite suite = new TestSuite( "RTJ634Test" );

      Properties client_props = new Properties();
      Properties server_props = new Properties();

      client_props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.jacorb.test.bugs.bugrtj634.CInitializer",
                       "");
      server_props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.jacorb.test.bugs.bugrtj634.SInitializer",
                       "");

      TestCaseClientServerSetup setup = new TestCaseClientServerSetup (suite,
                                                      "org.jacorb.test.bugs.bugrtj634.TimingServerImpl",
                                                      client_props,
                                                      server_props);

      suite.addTest( new RTJ634Test( "test634_1", setup ));

      return setup;
   }

   public void test634_1() throws Exception
   {
      try
      {
         int res = server1.operation(0, 0);

         // first invocation should have been redirected to obj 2. SInterceptor
         // counter will now be at 2.
         assertEquals( 2 , res );

         res = server1.operation(0, 0);
         assertEquals( 1 , res ); // second invocation should have been redirected to obj 1;

         // Try 2 - test we don't get BAD_OPERATION
         res = server1.operation(0, 0);

         // first invocation should have been redirected to obj 2. SInterceptor
         // counter will now be at 4. CInterceptor Counter will now be 9
         assertEquals( 2 , res ); // this invocation should have been redirected to obj 2;

         res = server1.operation(0, 0);
         assertEquals( 1 , res ); // this invocation should have been redirected to obj

         // Try 3 - final call should redirect to local server2
         res = server1.operation(0, 0);
         assertEquals( 2 , res );

         res = server1.operation(0, 0);
         assertEquals (20, res);

         // No forward request for this one...
         res = server1.operation(0, 0);
         assertEquals (20, res);
      }
      finally
      {
         server2._release ();
      }
   }


   /**
    * <code>main</code> is reimplemented here so that we can start
    * the server with a child POA and use servant_to_id.
    *
    * @param args a <code>String[]</code> value
    */
   public static void main (String[] args)
   {
       try
       {
           //init ORB
           ORB orb = ORB.init( args, null );

           //init POA
           POA rootPoa =
               POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));
           rootPoa.the_POAManager().activate();

           // create POA
           Policy policies[] = new Policy[2];
           policies[0] = rootPoa.create_id_uniqueness_policy(
               org.omg.PortableServer.IdUniquenessPolicyValue.MULTIPLE_ID);
           policies[1] = rootPoa.create_implicit_activation_policy(
               ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION );

           POA poa = rootPoa.create_POA
               ("childPOA1", rootPoa.the_POAManager(), policies);
           poa.the_POAManager().activate();

           TimingServerImpl servant = new TimingServerImpl (1);

           // Get the id
           byte []oid = poa.servant_to_id (servant);

           byte[] obj_2_id = rootPoa.activate_object(new TimingServerImpl(2));
           SInterceptor.OBJ_2 = rootPoa.id_to_reference(obj_2_id);

           // create the object reference
           org.omg.CORBA.Object obj = poa.id_to_reference (oid);

           System.out.println ("SERVER IOR: " + orb.object_to_string(obj));
           System.out.flush();

           // wait for requests
           orb.run();
       }
       catch( Exception e )
       {
           e.printStackTrace();
           System.out.println ("Caught error " + e);
       }
   }


   /**
    * <code>TestCaseClientServerSetup</code> overrides ClientServerSetup
    * so that we can provide a different main.
    */
   public static class TestCaseClientServerSetup extends ClientServerSetup
   {
       /**
        * Creates a new <code>TestCaseClientServerSetup</code> instance.
        *
        * @param test a <code>Test</code> value
        * @param servantName a <code>String</code> value
        * @param clientOrbProperties a <code>Properties</code> value
        * @param serverOrbProperties a <code>Properties</code> value
        */
       public TestCaseClientServerSetup( Test test,
                                       String servantName,
                                       Properties clientOrbProperties,
                                       Properties serverOrbProperties )
       {
           super(test, RTJ634Test.class.getName(), servantName, clientOrbProperties, serverOrbProperties);
       }
   }
}
