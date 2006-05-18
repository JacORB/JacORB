package org.jacorb.test.naming;

/**
 * ContextTest.java
 *
 * Naming Service Tests for creating, resolving, and destroying contexts
 *
 */

import java.io.File;
import java.util.Properties;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;

import junit.framework.*;

import org.jacorb.naming.NameServer;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;

public class ContextTest extends ClientServerTestCase
{
    private static NamingContextExt rootContext =  null;
    private static  NameComponent[] firstName, secondName, thirdName , failureName;

    protected void setUp()
    {
        rootContext = NamingContextExtHelper.narrow(setup.getServerObject());
        firstName = new NameComponent[1];
        firstName[0] = new NameComponent("first", "context");

        secondName = new NameComponent[1];
        secondName[0] = new NameComponent("second", "context");

        thirdName = new NameComponent[1];
        thirdName[0] = new NameComponent("third", "context");

        failureName = new NameComponent[2];
        failureName[0] = secondName[0];
        failureName[1] = thirdName[0];
    }


   public ContextTest (String name, ClientServerSetup setup)
   {
      super (name,setup);
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("Naming Context Tests");

      String dir = System.getProperty("java.io.tmpdir");
      dir += File.separator + "contextTest-" + System.currentTimeMillis();
      File tmpDir = new File(dir);
      assertTrue(tmpDir.mkdir());
      tmpDir.deleteOnExit();

      Properties clientProps = new Properties();
      Properties serverProps = new Properties();

      serverProps.put("jacorb.naming.ior_filename", "");
      serverProps.put("jacorb.naming.print_ior", "true");
      serverProps.put("jacorb.naming.db_dir", dir);

      ClientServerSetup setup = new ClientServerSetup( suite , "ignored", clientProps, serverProps)
      {
          public String getTestServerMain()
        {
              return NameServer.class.getName();
        }
      };

      suite.addTest (new  ContextTest("testCreateContextSuccess", setup));
      suite.addTest (new  ContextTest("testCreateContextFailure", setup));
      suite.addTest (new  ContextTest("testUnbindContext", setup));

      return setup;
   }

    public void testUnbindContext() throws Exception
    {
        rootContext.unbind(failureName);
        rootContext.unbind(secondName);
        rootContext.unbind(firstName);
    }


   public void testCreateContextFailure() throws Exception
   {
        /* create a subcontext with an existing name, must fail with
           AlreadyBound! */
        try
        {
            NamingContextExtHelper.narrow(rootContext.bind_new_context( failureName ));

            fail("NamingContext was expected to be already bound!");
        }
        catch (AlreadyBound e)
        {
            // expected
        }
    }

   /**
     * Test creating and resolving contexts
     */
    public void testCreateContextSuccess() throws Exception
    {
        /* create new contexts */
        NamingContextExtHelper.narrow(rootContext.bind_new_context(firstName));

        rootContext.resolve(firstName);

        /* create subcontexts */

        NamingContextExt secondsubContext = NamingContextExtHelper.narrow(rootContext
                .bind_new_context(secondName));

        NamingContextExtHelper.narrow(secondsubContext.bind_new_context(thirdName));
    }
}



