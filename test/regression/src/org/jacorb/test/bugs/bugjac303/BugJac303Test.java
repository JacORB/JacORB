package org.jacorb.test.bugs.bugjac303;

import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ORBTestCase;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.ObjectImpl;

public class BugJac303Test extends ORBTestCase
{
    private BasicServer server;
    private ORB clientORB;

    public void doSetUp() throws Exception
    {
        BasicServerImpl servant = new BasicServerImpl();
        BasicServer tmpServer = BasicServerHelper.narrow(rootPOA.servant_to_reference(servant));

        clientORB = ORB.init(new String[0], null);

        server = BasicServerHelper.narrow(clientORB.string_to_object(orb.object_to_string(tmpServer)));
    }

    protected void doTearDown() throws Exception
    {
        server._release();
        server = null;
        clientORB.shutdown(true);
        clientORB = null;
    }

    public void testInvokeSafeOperationAfterShutdown() throws Exception
    {
        clientORB.shutdown(true);
        BasicServer server2 = BasicServerHelper.narrow(server._duplicate());
        server2._release();
    }

    public void testInvokeIsNilAfterShutdown() throws Exception
    {
        clientORB.shutdown(true);
        org.jacorb.orb.Delegate delegate = (org.jacorb.orb.Delegate) ((ObjectImpl)server)._get_delegate();
        assertFalse(delegate.is_nil());
    }

    public void testInvokeServerOperationAfterShutdown() throws Exception
    {
        clientORB.shutdown(true);

        try
        {
            server.ping();
            fail();
        }
        catch (BAD_INV_ORDER e)
        {
            assertEquals(4, e.minor);
        }
    }

    public void testInvokeObjectReferenceOperations1() throws Exception
    {
        clientORB.shutdown(true);

        try
        {
            server._is_equivalent(server);
            fail();
        }
        catch (BAD_INV_ORDER e)
        {
            // expected
        }
    }

    public void testInvokeObjectReferenceOperations2() throws Exception
    {
        clientORB.shutdown(true);

        try
        {
            server._get_interface_def();
            fail();
        }
        catch (BAD_INV_ORDER e)
        {
            // expected
        }
    }

    public void testInvokeObjectReferenceOperations3() throws Exception
    {
        clientORB.shutdown(true);

        try
        {
            server._is_a("bla");
            fail();
        }
        catch (BAD_INV_ORDER e)
        {
            // expected
        }
    }

    public void testInvokeObjectReferenceOperations4() throws Exception
    {
        clientORB.shutdown(true);

        try
        {
            server._hash(100);
            fail();
        }
        catch (BAD_INV_ORDER e)
        {
            // expected
        }
    }

    public void testInvokeObjectReferenceOperations5() throws Exception
    {
        clientORB.shutdown(true);

        try
        {
            server._is_equivalent(server);
            fail();
        }
        catch (BAD_INV_ORDER e)
        {
            // expected
        }
    }

    public void testInvokeObjectReferenceOperations6() throws Exception
    {
        clientORB.shutdown(true);

        try
        {
            server._get_policy(0);
            fail();
        }
        catch (BAD_INV_ORDER e)
        {
            // expected
        }
    }

    public void testInvokeCreateRequest() throws Exception
    {
        clientORB.shutdown(true);

        try
        {
            server._create_request(null, null, null, null);
            fail();
        }
        catch (BAD_INV_ORDER e)
        {
            // expected
        }
    }

    public void testInvokeRequest() throws Exception
    {
        clientORB.shutdown(true);

        try
        {
            server._request("ping");
            fail();
        }
        catch (BAD_INV_ORDER e)
        {
            // expected
        }
    }

    public void testORBOperations1() throws Exception
    {
        String ref = clientORB.object_to_string(server);

        clientORB.shutdown(true);

        try
        {
            clientORB.string_to_object(ref);
            fail();
        }
        catch (BAD_INV_ORDER e)
        {
            // expected
        }

        try
        {
            clientORB.object_to_string(server);
            fail();
        }
        catch (BAD_INV_ORDER e)
        {
            // expected
        }
    }
}
