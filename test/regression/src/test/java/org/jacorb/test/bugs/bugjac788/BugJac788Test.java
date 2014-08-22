package org.jacorb.test.bugs.bugjac788;

import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


public class BugJac788Test extends ClientServerTestCase
{
    private HelloInterface server;

   /**
    * <code>setUp</code> for junit.
    *
    * @exception Exception if an error occurs
    */
   @Before
   public void setUp() throws Exception
   {
      server = HelloInterfaceHelper.narrow( setup.getServerObject() );
   }


   @BeforeClass
   public static void beforeClassSetUp() throws Exception
   {
      Properties client_props = new Properties();
      Properties server_props = new Properties();

      client_props.put("org.omg.PortableInterceptor.ORBInitializerClass.cdmw.test.TestORBInitializer",
                       TestORBInitializer.class.getName());
      server_props.put("org.omg.PortableInterceptor.ORBInitializerClass.cdmw.test.TestORBInitializer",
                       TestORBInitializer.class.getName());


      setup = new ClientServerSetup (
              "org.jacorb.test.bugs.bugjac788.BugJac788Test",
              "org.jacorb.test.bugs.bugjac788.HelloInterfaceImpl",
              client_props,
              server_props);
   }

    @Test
    public void test788_1() throws Exception
    {
        TestUtils.getLogger().debug("Calling hello on HelloInterface on A...");
        server.hello();
        TestUtils.getLogger().debug("hello on HelloInterface on A OK...");
    }


   /**
    * @param args a <code>String[]</code> value
    */
    public static void main (String[] args) throws Exception
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
}
