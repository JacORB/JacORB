package org.jacorb.test.bugs.bugjac788Compat;

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


/*
 * This is almost an exact replica of bugjac788 test except that its
 * stubs have been compiled with a 2.3.1 compiler to test backward
 * compatibility.
 */
public class BugJac788Test extends ClientServerTestCase
{
    private HelloInterface server;

   @Before
   public void setUp() throws Exception
   {
      server = HelloInterfaceHelper.narrow( setup.getServerObject() );
   }

   @BeforeClass
   public static void beforeClassSetup() throws Exception
   {
      Properties client_props = new Properties();
      Properties server_props = new Properties();

      client_props.put("org.omg.PortableInterceptor.ORBInitializerClass.cdmw.test.TestORBInitializer",
                       TestORBInitializer.class.getName());
      server_props.put("org.omg.PortableInterceptor.ORBInitializerClass.cdmw.test.TestORBInitializer",
                       TestORBInitializer.class.getName());


      setup = new ClientServerSetup (
              BugJac788Test.class.getName(),
              "org.jacorb.test.bugs.bugjac788Compat.HelloInterfaceImpl",
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

        ComputInterfaceImpl computInterface = new ComputInterfaceImpl(rootPoa);
        ComputInterface computInterfaceRef = computInterface._this(orb);

        HelloInterfaceImpl helloInterface = new HelloInterfaceImpl(rootPoa, computInterfaceRef);
        HelloInterface helloInterfaceRef = helloInterface._this(orb);

        System.out.println ("SERVER IOR: " + orb.object_to_string(helloInterfaceRef));
        System.out.flush();

        // wait for requests
        orb.run();
    }
}
