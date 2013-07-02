package org.jacorb.test.bugs.bugjac788;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
//import org.omg.FT.PullMonitorable;


public class BugJac788Test extends ClientServerTestCase
{
    private static ComputInterface comput;

    private HelloInterface server;


   public BugJac788Test(String name, ClientServerSetup setup)
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
      server = HelloInterfaceHelper.narrow( setup.getServerObject() );
   }

   /**
    * <code>tearDown</code> us used by Junit for cleaning up after the tests.
    *
    * @exception Exception if an error occurs
    */
   protected void tearDown() throws Exception
   {
      super.tearDown();

      server._release ();
      server = null;
   }

   /**
    * <code>suite</code> initialise the tests with the correct environment.
    *
    * @return a <code>Test</code> value
    */
   public static Test suite( )
   {
      TestSuite suite = new TestSuite( "BugJac788Test" );

      Properties client_props = new Properties();
      Properties server_props = new Properties();

      client_props.put("org.omg.PortableInterceptor.ORBInitializerClass.cdmw.test.TestORBInitializer",
                       TestORBInitializer.class.getName());
      server_props.put("org.omg.PortableInterceptor.ORBInitializerClass.cdmw.test.TestORBInitializer",
                       TestORBInitializer.class.getName());


      TestCaseClientServerSetup setup = new TestCaseClientServerSetup (suite,
                                                      "org.jacorb.test.bugs.bugjac788.HelloInterfaceImpl",
                                                      client_props,
                                                      server_props);

      suite.addTest( new BugJac788Test( "test788_1", setup ));

      return setup;
   }

   public void test788_1() throws Exception
   {
       System.out.println("Calling hello on HelloInterface on A...");
       server.hello();
       System.out.println("hello on HelloInterface on A OK...");
   }


   /**
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

           ComputInterfaceImpl computInterface = new ComputInterfaceImpl(orb, rootPoa);
           ComputInterface computInterfaceRef = computInterface._this(orb);

           HelloInterfaceImpl helloInterface = new HelloInterfaceImpl(orb,rootPoa, computInterfaceRef);
           HelloInterface helloInterfaceRef = helloInterface._this(orb);

           System.out.println ("SERVER IOR: " + orb.object_to_string(helloInterfaceRef));
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
           super(test, BugJac788Test.class.getName(), servantName, clientOrbProperties, serverOrbProperties);
       }
   }
}
