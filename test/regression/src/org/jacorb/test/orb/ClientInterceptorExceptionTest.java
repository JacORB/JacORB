package org.jacorb.test.orb;

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
import org.jacorb.test.bugs.bugrtj634.RTJ634Test;


/*
 * This test validates that throwing typed runtime exceptions from a send_request work.
 */
public class ClientInterceptorExceptionTest extends RTJ634Test
{
   public ClientInterceptorExceptionTest(String name, ClientServerSetup setup)
   {
      super(name, setup);
   }

   /**
    * <code>suite</code> initialise the tests with the correct environment.
    *
    * @return a <code>Test</code> value
    */
   public static Test suite( )
   {
      TestSuite suite = new TestSuite( "ClientInterceptorExceptionTest" );

      Properties client_props = new Properties();
      Properties server_props = new Properties();

      client_props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.jacorb.test.orb.CInitializer", "");

      ClientServerSetup setup = new ClientServerSetup
                                (suite,
                                 "org.jacorb.test.bugs.bugrtj634.TimingServerImpl",
                                 client_props,
                                 server_props);

      suite.addTest( new ClientInterceptorExceptionTest( "testcinterceptor", setup ));

      return setup;
   }

   public void testcinterceptor() throws Exception
   {
        try
        {
            server1.operation(0, 0);
            fail();
        }
        catch(RuntimeException e)
        {
            // expected exception
        }
        try
        {
            server1.operation(0, 0);
            fail();
        }
        catch(org.omg.CORBA.TRANSIENT e)
        {
            // expected exception
        }
        catch(RuntimeException e)
        {
            fail();
        }
   }
}
