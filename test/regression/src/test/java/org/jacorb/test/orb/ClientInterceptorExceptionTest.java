package org.jacorb.test.orb;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.ComplexTimingServer;
import org.jacorb.test.ComplexTimingServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/*
 * This test validates that throwing typed runtime exceptions from a send_request work.
 */
public class ClientInterceptorExceptionTest extends ClientServerTestCase
{
   protected ComplexTimingServer server1;

   @BeforeClass
   public static void beforeClassSetUp() throws Exception
   {
      Properties client_props = new Properties();
      Properties server_props = new Properties();

      client_props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.jacorb.test.orb.CInitializer", "");

      setup = new ClientServerSetup(
                                 "org.jacorb.test.bugs.bugrtj634.TimingServerImpl",
                                 client_props,
                                 server_props);
   }

   @Before
   public void setUp() throws Exception
   {
      server1 = ComplexTimingServerHelper.narrow( setup.getServerObject() );
   }

   @Test
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
