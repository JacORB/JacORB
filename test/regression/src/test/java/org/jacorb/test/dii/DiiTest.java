package org.jacorb.test.dii;

import org.jacorb.orb.Delegate;
import org.jacorb.orb.giop.ClientConnection;
import org.jacorb.orb.giop.ClientConnectionManager;
import org.jacorb.test.BasicServer;
import org.jacorb.test.dii.DIIServerPackage.DIIException;
import org.jacorb.test.dii.DIIServerPackage.DIIExceptionHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;
import org.omg.ETF.Profile;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * converted from demo.dii
 *
 * @author Alphonse Bendt
 */
public class DiiTest extends ClientServerTestCase
{
    private org.omg.CORBA.Object server;
    private org.omg.CORBA.ORB orb;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties props = new Properties();

        setup = new ClientServerSetup(DynamicServer.class.getName(), props, props);

    }

    @Before
    public void setUp() throws Exception
    {
        server = setup.getServerObject();
        orb = setup.getClientOrb();
    }


    @After
    public void tearDown() throws Exception
    {
        server = null;
        orb = null;
    }


    @Test
    public void testSimpleRequest()
    {
        org.omg.CORBA.Request request = server._request("_get_long_number");

        request.set_return_type(
                orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));

        request.invoke();

        assertNull(request.env().exception());
        assertEquals(47, request.return_value().extract_long());
    }

    @Test
    public void testRequestConnectionClosed() throws Exception
    {
        org.omg.CORBA.Request request = server._request("_get_long_number");

        request.set_return_type(
                orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));

        request.invoke();

        server._release();

        Field fconnmgr = Delegate.class.getDeclaredField("conn_mg");
        fconnmgr.setAccessible(true);
        Delegate d = (Delegate) ((org.omg.CORBA.portable.ObjectImpl)server)._get_delegate();
        ClientConnectionManager ccm = (ClientConnectionManager) fconnmgr.get(d);
        Field connections = ClientConnectionManager.class.getDeclaredField("connections");
        connections.setAccessible(true);
        @SuppressWarnings("unchecked")
        HashMap<Profile, ClientConnection> c = (HashMap<Profile, ClientConnection>) connections.get(ccm);

        assertTrue (c.size() == 0);
    }

    @Test (expected = BAD_INV_ORDER.class)
    public void testDoubleInvoke()
    {
        org.omg.CORBA.Request request = server._request("_get_long_number");

        request.set_return_type(
                orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));

        request.invoke();
        request.invoke();
    }

    @Test
    public void testRepositoryId()
    {
        org.omg.CORBA.Request request = server._request("_repository_id");

        request.set_return_type( orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_string ));
        request.invoke();
        assertNull(request.env().exception());
        assertEquals("IDL:org/jacorb/test/dii/DIIServer:1.0", request.return_value().extract_string());

    }

    @Test
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

    @Test
    public void testSimpleRequestWithStringArgumentOneway()
    {
        org.omg.CORBA.Request request =  server._request("notify");
        request.add_in_arg().insert_string("hallo");
        request.send_oneway();
    }

    @Test
    public void testRequestWithReturnValue()
    {
        org.omg.CORBA.Request request =  server._request("writeNumber");
        request.add_in_arg().insert_long( 5 );
        request.set_return_type( orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_string ));
        request.invoke();

        assertNull(request.env().exception());
        assertEquals("Number written", request.return_value().extract_string());
    }

    @Test
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
            @Override
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

    @Test
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

    @Test
    public void testReMarshalException()
    {
        Any any = orb.create_any();

        final String string = "bla bla bla";
        DIIException ex = new DIIException(string);

        DIIExceptionHelper.insert(any, ex);
        DIIException ex2 = DIIExceptionHelper.extract(any);

        assertEquals(string, ex2.why);
    }

    @Test
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
    }

    @Test
    public void testSendRequestWhichCausesASystemException() throws Exception
    {
        org.omg.CORBA.Request request = server._request("raiseSystemException");
        request.add_in_arg().insert_boolean(false);

        request.invoke();

        Exception exception = request.env().exception();

        assertNotNull(exception);

        assertTrue (exception instanceof BAD_PARAM);
    }


    @Test
    public void testSendRequestWhichCausesAWrappedSystemException() throws Exception
    {
        org.omg.CORBA.Request request = server._request("raiseSystemException");
        request.add_in_arg().insert_boolean(true);

        org.omg.CORBA.ExceptionList exceptions = request.exceptions();

        org.omg.CORBA.TypeCode typeCode = BAD_PARAMHelper.type();

        exceptions.add( typeCode );

        request.invoke();

        Exception exception = request.env().exception();

        assertNotNull(exception);

        org.omg.CORBA.Any any = ((org.omg.CORBA.UnknownUserException) exception).except;
        BAD_PARAM ex = BAD_PARAMHelper.extract(any);
        assertTrue (ex != null);
    }

    @Test
    public void testDiiSimpleMultiThread() throws Exception
    {
        final int SIZE = 2;
        ExecutorService executor = Executors.newFixedThreadPool(SIZE);

        class SimpleRequestor implements Callable<Integer>
        {
            org.omg.CORBA.Object server;

            SimpleRequestor(org.omg.CORBA.Object server)
            {
                this.server = server;
            }

            @Override
            public Integer call() throws Exception
            {
                int result = 0;

                for (int i = 0; i < 100; ++i)
                {
                    TestUtils.getLogger().debug(this.toString() + ": iteration " + i);
                    org.omg.CORBA.Request request = null;

                    request = server._request("_get_long_number");

                    request.set_return_type(
                            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));

                    request.invoke();

                    assertNull(request.env().exception());
                    result = request.return_value().extract_long();
                }
                return result;
            }
        }

        try
        {
            List<Future<Integer>> list = new ArrayList<Future<Integer>>();
            for (int i = 0; i < SIZE; i++)
            {
                list.add(executor.submit(new SimpleRequestor(server)));
            }
            for (Future<Integer> b : list)
            {
                assertEquals(b.get().intValue(), 47);
            }
        }
        finally
        {
            executor.shutdown();
        }
    }
}
