package org.jacorb.test.dii;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.dii.DIIServerPackage.DIIException;
import org.jacorb.test.dii.DIIServerPackage.DIIExceptionHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.WrongTransaction;

/**
 * converted from demo.dii
 *
 * @author Alphonse Bendt
 * @version $Id$
 */
public class DiiTest extends ClientServerTestCase
{
    private org.omg.CORBA.Object server;
    private org.omg.CORBA.ORB orb;

    public DiiTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite("DII tests (" + DiiTest.class.getName() + ")");

        Properties props = new Properties();

        props.put("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init", "null");

        ClientServerSetup setup = new ClientServerSetup(suite, DynamicServer.class.getName(), props, props);

        TestUtils.addToSuite(suite, setup, DiiTest.class);

        return setup;
    }

    protected void setUp() throws Exception
    {
        server = setup.getServerObject();
        orb = setup.getClientOrb();
    }


    public void testSimpleRequest()
    {
        org.omg.CORBA.Request request = server._request("_get_long_number");

        request.set_return_type(
                orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));

        request.invoke();

        assertNull(request.env().exception());
        assertEquals(47, request.return_value().extract_long());
    }

    public void testRequestWithOutArgs()
    {
        org.omg.CORBA.Request request = server._request("add");

        request.add_in_arg().insert_long( 3 );
        request.add_in_arg().insert_long( 4 );

        org.omg.CORBA.Any out_arg = request.add_out_arg();
        out_arg.type( orb.get_primitive_tc(
                org.omg.CORBA.TCKind.tk_long) );

        request.set_return_type( orb.get_primitive_tc(
                org.omg.CORBA.TCKind.tk_void));

        request.invoke();

        assertNull(request.env().exception());
        assertEquals(7, out_arg.extract_long());
    }

    public void testSimpleRequestWithStringArgumentOneway()
    {
        org.omg.CORBA.Request request =  server._request("notify");
        request.add_in_arg().insert_string("hallo");
        request.send_oneway();
    }

    public void testRequestWithReturnValue()
    {
        org.omg.CORBA.Request request =  server._request("writeNumber");
        request.add_in_arg().insert_long( 5 );
        request.set_return_type( orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_string ));
        request.invoke();

        assertNull(request.env().exception());
        assertEquals("Number written", request.return_value().extract_string());
    }

    public void testDeferredAsyncOperationSyncWithResult() throws Exception
    {
        final boolean[] success = new boolean[1];
        final Exception[] exception = new Exception[1];

        final org.omg.CORBA.Request request =  server._request("writeNumber");
        request.add_in_arg().insert_long( 5 );
        request.set_return_type( orb.get_primitive_tc(
                org.omg.CORBA.TCKind.tk_string ));

        request.send_deferred();

        Thread syncWithResult = new Thread()
        {
            public void run()
            {
                try
                {
                    request.get_response();
                    success[0] = true;
                }
                catch (WrongTransaction e)
                {
                    exception[0] = e;
                }
            }
        };
        syncWithResult.start();
        syncWithResult.join(5000);

        assertNull(exception[0]);
        assertTrue("unable to sync with result within 5sec", success[0]);

        assertNull(request.env().exception());
        assertEquals("Number written", request.return_value().extract_string());
    }

    public void testPollingUntilResponse()
    {
        long timeout = System.currentTimeMillis() + 5000;

        org.omg.CORBA.Request request =  server._request("writeNumber");
        request.add_in_arg().insert_long( 5 );
        request.set_return_type( orb.get_primitive_tc(
                org.omg.CORBA.TCKind.tk_string ));

        request.send_deferred();

        while( ! request.poll_response() && System.currentTimeMillis() < timeout)
        {
            try
            {
                Thread.sleep(100);
            }
            catch ( InterruptedException i){
                // ignored
            }
        }

        assertNull(request.env().exception());
        assertEquals("Number written", request.return_value().extract_string());
    }

    public void testReMarshalException()
    {
        Any any = orb.create_any();

        final String string = "bla bla bla";
        DIIException ex = new DIIException(string);

        DIIExceptionHelper.insert(any, ex);
        DIIException ex2 = DIIExceptionHelper.extract(any);

        assertEquals(string, ex2.why);
    }

    public void testSendRequestWhichCausesAnException() throws Exception
    {
        org.omg.CORBA.Request request = server._request("raiseException");

        org.omg.CORBA.ExceptionList exceptions = request.exceptions();

        org.omg.CORBA.TypeCode typeCode =
            orb.create_exception_tc(
                    DIIExceptionHelper.id(),
                    "e",
                    new org.omg.CORBA.StructMember[]{
                            new org.omg.CORBA.StructMember(
                                    "why",
                                    orb.create_string_tc(0),
                                    null)
                    }
            );

        exceptions.add( typeCode );

        request.invoke();

        Exception exception = request.env().exception();

        assertNotNull(exception);

        org.omg.CORBA.Any any = ((org.omg.CORBA.UnknownUserException) exception).except;
        DIIException ex = DIIExceptionHelper.extract(any);

        assertEquals("TestException", ex.why);

        Thread.sleep(2000);
    }
}
