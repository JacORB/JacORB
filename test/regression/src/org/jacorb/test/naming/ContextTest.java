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
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;

public class ContextTest extends ClientServerTestCase
{
    private NamingContextExt rootContext;
    private NameComponent[] firstName, secondName, thirdName , failureName;

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

    protected void tearDown() throws Exception
    {
        rootContext = null;
        firstName = secondName = thirdName = failureName = null;
    }

    public ContextTest (String name, ClientServerSetup setup)
    {
        super (name,setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Naming Context Tests");

        final String name = "contextTest";
        File tmpDir = TestUtils.createTempDir(name);

        Properties clientProps = new Properties();
        Properties serverProps = new Properties();

        serverProps.put("jacorb.naming.ior_filename", "");
        serverProps.put("jacorb.naming.print_ior", "true");
        serverProps.put("jacorb.naming.db_dir", tmpDir.toString());
        serverProps.put(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

        ClientServerSetup setup = new ClientServerSetup( suite , NameServer.class.getName(),  "ignored", clientProps, serverProps);

        suite.addTest (new  ContextTest("testNameService", setup));

        return setup;
    }

    /**
     * this is a bad example of an JUnit test as the testmethods need to be run in a particular order
     * to succeed. to make this explicit i've renamed the testmethods to step1-step3 and
     * have introduced a test method that invokes them in the proper order.
     */
    public void testNameService() throws Exception
    {
        step1_CreateContextSuccess();
        step2_CreateContextFailure();
        step3_UnbindContext();
    }

    private void step3_UnbindContext() throws Exception
    {
        rootContext.unbind(failureName);
        rootContext.unbind(secondName);
        rootContext.unbind(firstName);
    }


    private void step2_CreateContextFailure() throws Exception
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
    private void step1_CreateContextSuccess() throws Exception
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
