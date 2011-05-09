
package org.jacorb.test.orb.localinterceptors;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;

import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.ORBPackage.InvalidName;

import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.PortableInterceptor.ForwardRequest;


import org.jacorb.test.common.StreamListener;
import org.jacorb.test.common.TestUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * This class tests the use of PortableInterceptors during a local
 * call.  There are 3 client interceptors and 3 server interceptors
 * involved.  Complete calls, calls with Exceptions and calls with
 * ForwardRequests are thrown.  The ForwardRequests involve a remote
 * server object in the first instance and then replaced with the
 * original local object.
 */
public class LocalPITest extends TestCase
{
    private static boolean localDebugOn = false;

    private static PIServer serverRef = null;
    private static PIServer clientRef = null;

    private static PIServer remoteServer = null;
    private static org.omg.CORBA.Object remoteServerObj = null;

    private static final int TEST_SCID = 0x444f7F01;

    private static final int SERVER_MINOR = 0x999;
    private static final int REMOTE_SERVER_MINOR = 0x666;

    private static final int CLIENTA_SEND_REQ = 0x1;
    private static final int CLIENTB_SEND_REQ = 0x2;
    private static final int CLIENTC_SEND_REQ = 0x4;

    private static final int CLIENTA_SEND_POLL = 0x8;
    private static final int CLIENTB_SEND_POLL = 0x16;
    private static final int CLIENTC_SEND_POLL = 0x24;

    private static final int CLIENTA_RECEIVE_REPLY = 0x32;
    private static final int CLIENTB_RECEIVE_REPLY = 0x40;
    private static final int CLIENTC_RECEIVE_REPLY = 0x48;

    private static final int CLIENTA_RECEIVE_EXCEPTION = 0x56;
    private static final int CLIENTB_RECEIVE_EXCEPTION = 0x64;
    private static final int CLIENTC_RECEIVE_EXCEPTION = 0x72;

    private static final int CLIENTA_RECEIVE_OTHER = 0x80;
    private static final int CLIENTB_RECEIVE_OTHER = 0x88;
    private static final int CLIENTC_RECEIVE_OTHER = 0x96;

    private static final int SERVERA_RECEIVE_REQUEST_SC = 0x104;
    private static final int SERVERB_RECEIVE_REQUEST_SC = 0x112;
    private static final int SERVERC_RECEIVE_REQUEST_SC = 0x120;

    private static final int SERVERA_RECEIVE_REQUEST = 0x128;
    private static final int SERVERB_RECEIVE_REQUEST = 0x136;
    private static final int SERVERC_RECEIVE_REQUEST = 0x144;

    private static final int SERVERA_SEND_REPLY = 0x152;
    private static final int SERVERB_SEND_REPLY = 0x160;
    private static final int SERVERC_SEND_REPLY = 0x168;

    private static final int SERVERA_SEND_EXCEPTION = 0x176;
    private static final int SERVERB_SEND_EXCEPTION = 0x184;
    private static final int SERVERC_SEND_EXCEPTION = 0x192;

    private static final int SERVERA_SEND_OTHER = 0x200;
    private static final int SERVERB_SEND_OTHER = 0x208;
    private static final int SERVERC_SEND_OTHER = 0x216;

    private static final int COMPLETE_PATH =
    CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ | SERVERA_RECEIVE_REQUEST_SC |
    SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC | SERVERA_RECEIVE_REQUEST |
    SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST | SERVERA_SEND_REPLY |
    SERVERB_SEND_REPLY | SERVERC_SEND_REPLY | CLIENTA_RECEIVE_REPLY | CLIENTB_RECEIVE_REPLY |
    CLIENTC_RECEIVE_REPLY;

    private static int throwException = 0;

    private static int callsMade = 0;

    private static int throwForwardRequest = 0;

    private static int slotID;

    private static String operation = null;

    private static boolean forwardRequestThrown = false;

    private static boolean hasServiceContexts = false;

    private static org.omg.CORBA.Any any;

    private ORB orb;
    private POA rootPOA;

    private static String [] args = new String [0];

    public static Test suite ()
    {
        TestSuite suite = new TestSuite (LocalPITest.class);

        return suite;
    }

    private void init (String op)
    {
        callsMade = 0;
        throwException = 0;
        throwForwardRequest = 0;
        operation = op;

        hasServiceContexts = false;
        forwardRequestThrown = false;

        try
        {
            Properties props = new java.util.Properties();
            props.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass."
                               + LocalPIInitializer.class.getName(), "" );

            props.setProperty ("jacorb.codeSet", "on");
            props.setProperty ("org.omg.PortableInterceptor.ORBInitializerClass.standard_init",
                               "org.jacorb.orb.standardInterceptors.IORInterceptorInitializer");

            // find the root poa
            orb = ORB.init (args, props);

            rootPOA = (POA) orb.resolve_initial_references ("RootPOA");

            Policy [] policies = new Policy [1];

            policies[0] = rootPOA.create_implicit_activation_policy
                (ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);

            POA childPOA = rootPOA.create_POA ("childPOA",
                                               rootPOA.the_POAManager (),
                                               policies);

            serverRef = ( new PIServerImpl (childPOA))._this (orb);

            rootPOA.the_POAManager().activate();
            clientRef = PIServerHelper.narrow (serverRef);

            any = orb.create_any();
            any.insert_boolean (true);

            if (remoteServerObj == null)
            {
                startRemoteServer (orb);
            }
        }
        catch ( org.omg.CORBA.UserException ex )
        {
            fail( "exception during setup:" + ex.toString() );
        }
    }

    protected void setUp () throws Exception
    {
    }

    protected void tearDown () throws Exception
    {
    }

    private void startRemoteServer (ORB orb)
    {
        Process serverProcess = null;
        String serverIOR;
        StreamListener outListener;
        StreamListener errListener;

        StringBuffer sb = new StringBuffer ();

        if (TestUtils.isWindows())
        {
            sb.append ("javaw ");
        }
        else
        {
            sb.append ("java ");
        }

        sb.append (" -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB");
        sb.append (" -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton");
        sb.append (" -Xbootclasspath/p:");
        sb.append (System.getProperty ("java.class.path"));

        sb.append (" org.jacorb.test.orb.localinterceptors.RemoteServer");

        try
        {
           serverProcess = Runtime.getRuntime().exec (sb.toString());
        }
        catch (IOException ioe)
        {
            fail ("Failed to start remote server " + ioe);
        }

        outListener = new StreamListener (serverProcess.getInputStream(), "");
        errListener = new StreamListener (serverProcess.getErrorStream(), "");
        outListener.start();
        errListener.start();
        serverIOR = outListener.getIOR (30000);

        remoteServerObj = orb.string_to_object (serverIOR);

        remoteServer = PIServerHelper.narrow (remoteServerObj);
    }


    /**
     * Test complete request call. This should call
     * SEND_REQUEST and RECEIVE_REPLY on all 3 client interceptors and
     * RECEIVE_REQUEST_SERVICE_CONTEXTS, RECEIVE_REQUEST SEND_REPLY and
     * RECEIVE_REPLY on all 3 Server interceptors in the order
     *
     * Client A SEND_REQUEST
     * Client B SEND_REQUEST
     * Client C SEND_REQUEST
     * Server A RECEIVE_REQUEST_SERVICE_CONTEXTS
     * Server B RECEIVE_REQUEST_SERVICE_CONTEXTS
     * Server C RECEIVE_REQUEST_SERVICE_CONTEXTS
     * Server A RECEIVE_REQUEST
     * Server B RECEIVE_REQUEST
     * Server C RECEIVE_REQUEST
     * Server C SEND_REPLY
     * Server B SEND_REPLY
     * Server A SEND_REPLY
     * Client C RECEIVE_REPLY
     * Client B RECEIVE_REPLY
     * Client A RECEIVE_REPLY
     */
    public void testCompleteCall()
    {
        System.out.println ("\ntestCompleteCall");

        init ("sendMessage");

        clientRef.sendMessage ("A Message from testCompleteCall()...");

        assertEquals ("Calls to interceptors not as expected",
                      COMPLETE_PATH,
                      callsMade);
    }

    /**
     * Test complete request call with service contexts.
     *
     * @exception org.omg.CORBA.UserException if any of the test cases fails
     */
    public void testCompleteCallWithSCs()
    {
        System.out.println ("\ntestCompleteCallWithScs");

        init ("sendMessage");
        try
        {
            Current curr = (Current) orb.resolve_initial_references ("PICurrent");
            curr.set_slot (slotID, any );
        }
        catch (InvalidName in)
        {
            fail ("Got InvalidName exception");
        }
        catch (InvalidSlot is)
        {
            fail ("Got InvalidSlot exception");
        }

        hasServiceContexts = true;

        clientRef.sendMessage ("A message from testCompleteCallWithSCs()...");

        assertEquals ("Calls to interceptors not as expected",
                      COMPLETE_PATH,
                      callsMade);

        callsMade = 0;

        operation = "returnMessage";

        /**
         * This is a new request so we would need to set the current slot again
         * if we wanted service contexts
         */
        hasServiceContexts = false;

        String msg = clientRef.returnMessage ("testCompleteCallWithSCs()..");

        assertEquals ("Calls to interceptors not as expected",
                      COMPLETE_PATH,
                      callsMade);

        assertTrue ("Returned message not as expected ",
                    msg.indexOf ("testCompleteCallWithSCs()..") != -1);
    }


    /**
     * Tests an exception being thrown by the Server.  The interception
     * points should be
     *
     * Client A SEND_REQUEST
     * Client B SEND_REQUEST
     * Client C SEND_REQUEST
     * Server A RECEIVE_REQUEST_SERVICE_CONTEXTS
     * Server B RECEIVE_REQUEST_SERVICE_CONTEXTS
     * Server C RECEIVE_REQUEST_SERVICE_CONTEXTS
     * Server A RECEIVE_REQUEST
     * Server B RECEIVE_REQUEST
     * Server C RECEIVE_REQUEST
     * Server C SEND_EXCEPTION
     * Server B SEND_EXCEPTION
     * Server A SEND_EXCETPION
     * Client C RECEIVE_EXCEPTION
     * Client B RECEIVE_EXCEPTION
     * Client A RECEIVE_EXCEPTION
     */
    public void testExceptionFromServer()
    {
        System.out.println ("\ntestExceptionFromServer");

        init ("throwException");

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERA_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION | SERVERC_SEND_EXCEPTION |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;

        String outMsg = "A message from testExceptionFromServer()...";
        try
        {
            clientRef.throwException (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception message does not match sent message",
                          outMsg,
                          un.getMessage());

            assertEquals ("Minor not as expected ", SERVER_MINOR, un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }



    /**
     * Tests an exception being raised by each client interceptor at
     * the SEND_REQUEST point.  Note that each call is separate
     */
    public void testClientInterceptorsRaiseExceptionAtSendRequest()
    {

        System.out.println ("\ntestClientInterceptorsRaiseExceptionAtSendRequest");

        init ("sendMessage");

        /* Test exception raised by ClientInterceptorA send_request */
        String outMsg = "testClientIntercptorARaisesExceptionAtSendRequest";
        throwException = CLIENTA_SEND_REQ;

        int expectedCalls = CLIENTA_SEND_REQ;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Client A interceptor send_request",
                          CLIENTA_SEND_REQ,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);

       /* Test exception raised by ClientInterceptorA send_request */
        outMsg = "testClientIntercptorBRaisesExceptionAtSendRequest";
        throwException = CLIENTB_SEND_REQ;

        /* reset the callsMade as this is a new request */
        callsMade = 0;
        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTA_RECEIVE_EXCEPTION;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Client B interceptor send_request",
                          CLIENTB_SEND_REQ,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);

       /* Test exception raised by ClientInterceptorA send_request */
        outMsg = "testClientIntercptorCRaisesExceptionAtSendRequest";
        throwException = CLIENTC_SEND_REQ;

        /* reset the callsMade as this is a new request */
        callsMade = 0;
        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Client C interceptor send_request",
                          CLIENTC_SEND_REQ,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    /**
     * Tests an exception being raised by each client interceptor at
     * the RECEIVE_REPLY point.  Note that each call is separate
     */
    public void testClientInterceptorsRaiseExceptionAtReceiveReply()
    {
        System.out.println ("\ntestClientInterceptorsRaiseExceptionAtReceiveReply");

        init ("sendMessage");

        String outMsg = "testClientIntercptorCRaisesExceptionAtReceiveReply";
        throwException = CLIENTC_RECEIVE_REPLY;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERA_SEND_REPLY | SERVERB_SEND_REPLY | SERVERC_SEND_REPLY |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_REPLY;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Client C interceptor receive_reply",
                          CLIENTC_RECEIVE_REPLY,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);

        /* ClientInterceptorB */
        outMsg = "testClientIntercptorBRaisesExceptionAtReceiveReply";
        throwException = CLIENTB_RECEIVE_REPLY;
        callsMade = 0;

        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_REPLY | CLIENTC_RECEIVE_REPLY;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Client B interceptor receive_reply",
                          CLIENTB_RECEIVE_REPLY,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);


        /* ClientInterceptorA */
        outMsg = "testClientIntercptorARaisesExceptionAtReceiveReply";
        throwException = CLIENTA_RECEIVE_REPLY;
        callsMade = 0;

        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        CLIENTA_RECEIVE_REPLY | CLIENTB_RECEIVE_REPLY | CLIENTC_RECEIVE_REPLY;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Client A interceptor receive_reply",
                          CLIENTA_RECEIVE_REPLY,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    /**
     * Tests an exception being raised by each client interceptor at
     * the RECEIVE_EXCEPTION point.  Note that each call is separate
     */
    public void testClientInterceptorsRaiseExceptionAtReceiveException()
    {
        System.out.println ("\ntestClientInterceptorsRaiseExceptionAtReceiveException");

        init ("throwException");

        String outMsg = "testClientIntercptorCRaisesExceptionAtReceiveException";
        throwException = CLIENTC_RECEIVE_EXCEPTION;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERA_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION | SERVERC_SEND_EXCEPTION |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.throwException (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Client C interceptor receive_exception",
                          CLIENTC_RECEIVE_EXCEPTION | SERVER_MINOR,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);

        /* ClientInterceptorB */
        outMsg = "testClientIntercptorBRaisesExceptionAtReceiveException";
        throwException = CLIENTB_RECEIVE_EXCEPTION;
        callsMade = 0;

        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERA_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION | SERVERC_SEND_EXCEPTION |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.throwException (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Client B interceptor receive_exception",
                          CLIENTB_RECEIVE_EXCEPTION | SERVER_MINOR,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);


        /* ClientInterceptorA */
        outMsg = "testClientIntercptorARaisesExceptionAtReceiveException";
        throwException = CLIENTA_RECEIVE_EXCEPTION;
        callsMade = 0;

        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERA_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION | SERVERC_SEND_EXCEPTION |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.throwException (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Client A interceptor receive_exception",
                          CLIENTA_RECEIVE_EXCEPTION | SERVER_MINOR,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    /**
     * Tests an exception being raised by each server interceptor at
     * the RECEIVE_REQUEST_SERVICE_CONTEXTS  point.  Note that each call is separate
     */
    public void testServerInterceptorsRaiseExceptionAtReceiveRequestSC()
    {

        System.out.println ("\ntestServerInterceptorsRaiseExceptionAtReceiveRequestSC");

        init ("sendMessage");

        /* Test exception raised by ServerInterceptorA receive_request_service_contexts */
        String outMsg = "testServerIntercptorARaisesExceptionAtReceiveRequestSC";
        throwException = SERVERA_RECEIVE_REQUEST_SC;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Server A interceptor receive_request_service_contexts",
                          SERVERA_RECEIVE_REQUEST_SC,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);

       /* Test exception raised by ServerInterceptorB receive_request_service_contexts */
        outMsg = "testServerIntercptorBRaisesExceptionAtReceiveRequestSC";
        throwException = SERVERB_RECEIVE_REQUEST_SC;

        /* reset the callsMade as this is a new request */
        callsMade = 0;
        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERA_SEND_EXCEPTION |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Server B interceptor receive_request_service_contexts",
                          SERVERB_RECEIVE_REQUEST_SC,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);

       /* Test exception raised by ServerInterceptorC receive_request_service_contexts */
        outMsg = "testClientIntercptorCRaisesExceptionAtReceiveRequestSC";
        throwException = SERVERC_RECEIVE_REQUEST_SC;

        /* reset the callsMade as this is a new request */
        callsMade = 0;
        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Server C interceptor receive_request_service_contexts",
                          SERVERC_RECEIVE_REQUEST_SC,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }


    /**
     * Tests an exception being raised by each server interceptor at
     * the RECEIVE_REQUEST  point.  Note that each call is separate
     */
    public void testServerInterceptorsRaiseExceptionAtReceiveRequest()
    {

        System.out.println ("\ntestServerInterceptorsRaiseExceptionAtReceiveRequest");

        init ("sendMessage");

        /* Test exception raised by ServerInterceptorA receive_request */
        String outMsg = "testServerIntercptorARaisesExceptionAtReceiveRequest";
        throwException = SERVERA_RECEIVE_REQUEST;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERC_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION |
        SERVERA_SEND_EXCEPTION | CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION |
        CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Server A interceptor receive_request",
                          SERVERA_RECEIVE_REQUEST,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);

       /* Test exception raised by ServerInterceptorB receive_request */
        outMsg = "testServerIntercptorBRaisesExceptionAtReceiveRequest";
        throwException = SERVERB_RECEIVE_REQUEST;

        /* reset the callsMade as this is a new request */
        callsMade = 0;
        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST |
        SERVERC_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION | SERVERA_SEND_EXCEPTION |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Server B interceptor receive_request",
                          SERVERB_RECEIVE_REQUEST,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);

       /* Test exception raised by ServerInterceptorC receive_request */
        outMsg = "testClientIntercptorCRaisesExceptionAtReceiveRequest";
        throwException = SERVERC_RECEIVE_REQUEST;

        /* reset the callsMade as this is a new request */
        callsMade = 0;
        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERC_SEND_EXCEPTION | SERVERA_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Server C interceptor receive_request",
                          SERVERC_RECEIVE_REQUEST,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    /**
     * Tests an exception being raised by each server interceptor at
     * the SEND_REPLY  point.  Note that each call is separate
     */
    public void testServerInterceptorsRaiseExceptionAtSendReply()
    {
        System.out.println ("\ntestServerInterceptorsRaiseExceptionAtSendReply");

        init ("sendMessage");

        String outMsg = "testServerIntercptorARaisesExceptionAtSendReply";
        throwException = SERVERA_SEND_REPLY;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERC_SEND_REPLY | SERVERB_SEND_REPLY | SERVERA_SEND_REPLY | CLIENTA_RECEIVE_EXCEPTION |
        CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Server A interceptor send_reply",
                          SERVERA_SEND_REPLY,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);

        /* ServerInterceptorB */
        outMsg = "testServerIntercptorBRaisesExceptionAtSendReply";
        throwException = SERVERB_SEND_REPLY;
        callsMade = 0;

        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERC_SEND_REPLY | SERVERB_SEND_REPLY | SERVERA_SEND_EXCEPTION |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Server B interceptor send_reply",
                          SERVERB_SEND_REPLY,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);


        /* ServerInterceptorC */
        outMsg = "testServerIntercptorCRaisesExceptionAtSendReply";
        throwException = SERVERC_SEND_REPLY;
        callsMade = 0;

        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERA_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION | SERVERC_SEND_REPLY |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;


        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Server C interceptor send_reply",
                          SERVERC_SEND_REPLY,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    /**
     * Tests an exception being raised by each server interceptor at
     * the SEND_EXCEPTION  point.  Note that each call is separate
     */
    public void testServerInterceptorsRaiseExceptionAtSendException()
    {
        System.out.println ("\ntestServerInterceptorsRaiseExceptionAtSendException");

        init ("sendMessage");

        String outMsg = "testServerIntercptorARaisesExceptionAtSendException";
        throwException = SERVERA_SEND_EXCEPTION;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST |  SERVERC_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION |
        SERVERA_SEND_EXCEPTION | CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION |
        CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Server A interceptor send_exception",
                          SERVERA_SEND_EXCEPTION | SERVERA_RECEIVE_REQUEST,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);

        /* ServerInterceptorB */
        outMsg = "testServerIntercptorBRaisesExceptionAtSendException";
        throwException = SERVERB_SEND_EXCEPTION;
        callsMade = 0;

        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST |
        SERVERC_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION | SERVERA_SEND_EXCEPTION |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;

        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Server B interceptor send_exception",
                          SERVERB_SEND_EXCEPTION | SERVERB_RECEIVE_REQUEST,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);


        /* ServerInterceptorC */
        outMsg = "testServerIntercptorCRaisesExceptionAtSendException";
        throwException = SERVERC_SEND_EXCEPTION;
        callsMade = 0;

        expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERA_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION | SERVERC_SEND_EXCEPTION |
        CLIENTA_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION | CLIENTC_RECEIVE_EXCEPTION;


        try
        {
            clientRef.sendMessage (outMsg);
            fail ("Expected an exception");
        }
        catch (UNKNOWN un)
        {
            assertEquals ("Exception not thrown in Server C interceptor send_exception",
                          SERVERC_SEND_EXCEPTION | SERVERC_RECEIVE_REQUEST,
                          un.minor);
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    /**
     * Tests an ForwardRequest being raised by Client A interceptor at
     * the SEND_REQUEST  point.  As the ForwardRequest is thrown before
     * the invocation is made a new call is made by the Stub to the
     * new server object without being returned to the client.  The
     * new server object is actually remote and the call is successful.
     * There are no interceptors registered with the remote object so
     * only the local client interceptors are called.
     */
    public void testClientInterceptorARaisesForwardRequestAtSendRequest()
    {

        System.out.println ("\ntestClientInterceptorARaisesForwardRequestAtSendRequest");

        init ("returnMessage");

        /* Test ForwardRequest raised by ClientInterceptorA send_request */
        String outMsg = "testClientIntercptorARaisesFRAtSendRequest";
        throwForwardRequest = CLIENTA_SEND_REQ;

        /* The call to ClientInterceptorA send_request is made twice as it is made for
         * the initial call and then called again as part of the call to the ForwardRequest
         */
        int expectedCalls = CLIENTA_SEND_REQ |  CLIENTA_SEND_REQ |  CLIENTB_SEND_REQ |
        CLIENTC_SEND_REQ | CLIENTA_RECEIVE_REPLY | CLIENTB_RECEIVE_REPLY | CLIENTC_RECEIVE_REPLY;

        String response = clientRef.returnMessage (outMsg);

        assertTrue ("Call not forwarded to RemoteServer", response.indexOf ("Remote") > -1);

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testClientInterceptorBRaisesForwardRequestAtSendRequest()
    {
        System.out.println ("\ntestClientInterceptorBRaisesForwardRequestAtSendRequest");

        init ("returnMessage");

        /* Test ForwardRequest raised by ClientInterceptorB send_request */
        String outMsg = "testClientIntercptorBRaisesFRAtSendRequest";
        throwForwardRequest = CLIENTB_SEND_REQ;

        /* The call to ClientInterceptorA send_request is made twice as it is made for
         * the initial call and then called again as part of the call to the ForwardRequest
         */
        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTA_RECEIVE_OTHER |
        CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ | CLIENTA_RECEIVE_REPLY |
        CLIENTB_RECEIVE_REPLY | CLIENTC_RECEIVE_REPLY;

        String response = clientRef.returnMessage (outMsg);

        assertTrue ("Call not forwarded to RemoteServer", response.indexOf ("Remote") > -1);

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testClientInterceptorCRaisesForwardRequestAtSendRequest()
    {
        System.out.println ("\ntestClientInterceptorCRaisesForwardRequestAtSendRequest");

        init ("returnMessage");

        /* Test ForwardRequest raised by ClientInterceptorC send_request */
        String outMsg = "testClientIntercptorCRaisesFRAtSendRequest";
        throwForwardRequest = CLIENTC_SEND_REQ;

        /* The call to ClientInterceptors send_request are made twice as it is made for
         * the initial call and then called again as part of the call to the ForwardRequest
         */
        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        CLIENTA_RECEIVE_OTHER | CLIENTB_RECEIVE_OTHER | CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |
        CLIENTC_SEND_REQ | CLIENTA_RECEIVE_REPLY | CLIENTB_RECEIVE_REPLY | CLIENTC_RECEIVE_REPLY;

        String response = clientRef.returnMessage (outMsg);

        assertTrue ("Call not forwarded to RemoteServer", response.indexOf ("Remote") > -1);

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testServerInterceptorARaisesForwardRequestAtRRSC()
    {
        System.out.println ("\ntestServerInterceptorARaisesForwardRequestAtRRSC");

        init ("returnMessage");

        String outMsg = "testServerInterceptorARaisesFRAtRRSC";
        throwForwardRequest = SERVERA_RECEIVE_REQUEST_SC;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | CLIENTA_RECEIVE_OTHER | CLIENTB_RECEIVE_OTHER |
        CLIENTC_RECEIVE_OTHER | CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ |
        CLIENTA_RECEIVE_REPLY | CLIENTB_RECEIVE_REPLY | CLIENTC_RECEIVE_REPLY;

        String response = clientRef.returnMessage (outMsg);

        assertTrue ("Call not forwarded to RemoteServer", response.indexOf ("Remote") > -1);

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testServerInterceptorBRaisesForwardRequestAtRRSC()
    {
        System.out.println ("\ntestServerInterceptorBRaisesForwardRequestAtRRSC");

        init ("returnMessage");

        String outMsg = "testServerInterceptorBRaisesFRAtRRSC";
        throwForwardRequest = SERVERB_RECEIVE_REQUEST_SC;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERA_SEND_OTHER |
        CLIENTA_RECEIVE_OTHER | CLIENTB_RECEIVE_OTHER | CLIENTC_RECEIVE_OTHER |
        CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ | CLIENTA_RECEIVE_REPLY |
        CLIENTB_RECEIVE_REPLY | CLIENTC_RECEIVE_REPLY;

        String response = clientRef.returnMessage (outMsg);

        assertTrue ("Call not forwarded to RemoteServer", response.indexOf ("Remote") > -1);

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testServerInterceptorCRaisesForwardRequestAtRRSC()
    {
        System.out.println ("\ntestServerInterceptorCRaisesForwardRequestAtRRSC");

        init ("returnMessage");

        String outMsg = "testServerInterceptorCRaisesFRAtRRSC";
        throwForwardRequest = SERVERC_RECEIVE_REQUEST_SC;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_SEND_OTHER |  SERVERB_SEND_OTHER | CLIENTA_RECEIVE_OTHER | CLIENTB_RECEIVE_OTHER |
        CLIENTC_RECEIVE_OTHER | CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |
        CLIENTC_SEND_REQ | CLIENTA_RECEIVE_REPLY | CLIENTB_RECEIVE_REPLY | CLIENTC_RECEIVE_REPLY;

        String response = clientRef.returnMessage (outMsg);

        assertTrue ("Call not forwarded to RemoteServer", response.indexOf ("Remote") > -1);

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testServerInterceptorARaisesForwardRequestAtRR()
    {
        System.out.println ("\ntestServerInterceptorARaisesForwardRequestAtRR");

        init ("returnMessage");

        String outMsg = "testServerInterceptorARaisesFRAtRR";
        throwForwardRequest = SERVERA_RECEIVE_REQUEST;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERA_SEND_OTHER |  SERVERB_SEND_OTHER |  SERVERC_SEND_OTHER |
        CLIENTA_RECEIVE_OTHER | CLIENTB_RECEIVE_OTHER | CLIENTC_RECEIVE_OTHER |
        CLIENTA_SEND_REQ | CLIENTB_SEND_REQ | CLIENTC_SEND_REQ | CLIENTA_RECEIVE_REPLY |
        CLIENTB_RECEIVE_REPLY | CLIENTC_RECEIVE_REPLY;

        String response = clientRef.returnMessage (outMsg);

        assertTrue ("Call not forwarded to RemoteServer", response.indexOf ("Remote") > -1);

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testServerInterceptorBRaisesForwardRequestAtRR()
    {
        System.out.println ("\ntestServerInterceptorBRaisesForwardRequestAtRR");
        init ("returnMessage");

        String outMsg = "testServerInterceptorBRaisesFRAtRR";
        throwForwardRequest = SERVERB_RECEIVE_REQUEST;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_SEND_OTHER |
        SERVERB_SEND_OTHER |  SERVERA_SEND_OTHER | CLIENTA_RECEIVE_OTHER | CLIENTB_RECEIVE_OTHER |
        CLIENTC_RECEIVE_OTHER | CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |
        CLIENTC_SEND_REQ | CLIENTA_RECEIVE_REPLY | CLIENTB_RECEIVE_REPLY | CLIENTC_RECEIVE_REPLY;

        String response = clientRef.returnMessage (outMsg);

        assertTrue ("Call not forwarded to RemoteServer", response.indexOf ("Remote") > -1);

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testServerInterceptorCRaisesForwardRequestAtRR()
    {
        System.out.println ("\ntestServerInterceptorCRaisesForwardRequestAtRR");
        init ("returnMessage");

        String outMsg = "testServerInterceptorCRaisesFRAtRR";
        throwForwardRequest = SERVERC_RECEIVE_REQUEST;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERA_SEND_OTHER | SERVERB_SEND_OTHER | SERVERC_SEND_OTHER | CLIENTA_RECEIVE_OTHER |
        CLIENTB_RECEIVE_OTHER | CLIENTC_RECEIVE_OTHER | CLIENTA_SEND_REQ |
        CLIENTB_SEND_REQ | CLIENTC_SEND_REQ | CLIENTA_RECEIVE_REPLY | CLIENTB_RECEIVE_REPLY |
        CLIENTC_RECEIVE_REPLY;

        String response = clientRef.returnMessage (outMsg);

        assertTrue ("Call not forwarded to RemoteServer", response.indexOf ("Remote") > -1);

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testServerInterceptorARaisesForwardRequestAtSE()
    {
        System.out.println ("\ntestServerInterceptorARaisesForwardRequestAtSE");
        init ("returnMessage");

        String outMsg = "testServerInterceptorARaisesFRAtSE";
        throwForwardRequest = SERVERA_SEND_EXCEPTION;
        throwException = SERVERC_SEND_REPLY;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERC_SEND_REPLY | SERVERB_SEND_EXCEPTION | SERVERA_SEND_EXCEPTION |
        CLIENTA_RECEIVE_OTHER | CLIENTB_RECEIVE_OTHER | CLIENTC_RECEIVE_OTHER;


        try
        {
            String response = clientRef.returnMessage (outMsg);
            fail ("Expected ForwardRequest");
        }
        catch (Exception ex)
        {
            Throwable t = ex.getCause();

            if (t instanceof org.omg.PortableInterceptor.ForwardRequest)
            {
                org.omg.CORBA.Object fwdObj = (org.omg.CORBA.Object)
                    ( (org.omg.PortableInterceptor.ForwardRequest ) t).forward;

                assertEquals ("ForwardRequest and remoteServer do not match ", remoteServerObj, fwdObj);
            }
            else
            {
                fail ("Expected a ForwardRequest and got " + ex);
            }
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testServerInterceptorBRaisesForwardRequestAtSE()
    {
        System.out.println ("\ntestServerInterceptorBRaisesForwardRequestAtSE");
        init ("returnMessage");

        String outMsg = "testServerInterceptorBRaisesFRAtSE";
        throwForwardRequest = SERVERB_SEND_EXCEPTION;
        throwException = SERVERC_SEND_REPLY;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERC_SEND_REPLY | SERVERC_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION |
        SERVERA_SEND_OTHER | CLIENTA_RECEIVE_OTHER | CLIENTB_RECEIVE_OTHER |
        CLIENTC_RECEIVE_OTHER;


        try
        {
            String response = clientRef.returnMessage (outMsg);
            fail ("Expected ForwardRequest");
        }
        catch (Exception ex)
        {
            Throwable t = ex.getCause();

            if (t instanceof org.omg.PortableInterceptor.ForwardRequest)
            {
                org.omg.CORBA.Object fwdObj = (org.omg.CORBA.Object)
                    ( (org.omg.PortableInterceptor.ForwardRequest ) t).forward;

                assertEquals ("ForwardRequest and remoteServer do not match ", remoteServerObj, fwdObj);
            }
            else
            {
                fail ("Expected a ForwardRequest and got " + ex);
            }
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }


    public void testClientInterceptorARaisesForwardRequestAtRE()
    {
        System.out.println ("\ntestClientInterceptorARaisesForwardRequestAtRE");

        init ("returnMessage");

        String outMsg = "testClientInterceptorARaisesFRAtRE";
        throwForwardRequest = CLIENTA_RECEIVE_EXCEPTION;
        throwException = SERVERC_SEND_REPLY;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERC_SEND_REPLY | SERVERC_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION |
        SERVERA_SEND_EXCEPTION |  CLIENTC_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION |
        CLIENTA_RECEIVE_EXCEPTION;

        try
        {
            String response = clientRef.returnMessage (outMsg);
            fail ("Expected ForwardRequest");
        }
        catch (Exception ex)
        {
            Throwable t = ex.getCause();

            if (t instanceof org.omg.PortableInterceptor.ForwardRequest)
            {
                org.omg.CORBA.Object fwdObj = (org.omg.CORBA.Object)
                    ( (org.omg.PortableInterceptor.ForwardRequest ) t).forward;

                assertEquals ("ForwardRequest and remoteServer do not match ", remoteServerObj, fwdObj);
            }
            else
            {
                fail ("Expected a ForwardRequest and got " + ex);
            }
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testClientInterceptorBRaisesForwardRequestAtRE()
    {
        System.out.println ("\ntestClientInterceptorBRaisesForwardRequestAtRE");

        init ("returnMessage");

        String outMsg = "testClientInterceptorBRaisesFRAtRE";
        throwForwardRequest = CLIENTB_RECEIVE_EXCEPTION;
        throwException = SERVERC_SEND_REPLY;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERC_SEND_REPLY | SERVERC_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION |
        SERVERA_SEND_EXCEPTION |  CLIENTC_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_EXCEPTION |
        CLIENTA_RECEIVE_OTHER;

        try
        {
            String response = clientRef.returnMessage (outMsg);
            fail ("Expected ForwardRequest");
        }
        catch (Exception ex)
        {
            Throwable t = ex.getCause();
            if (t instanceof org.omg.PortableInterceptor.ForwardRequest)
            {
                org.omg.CORBA.Object fwdObj = (org.omg.CORBA.Object)
                    ( (org.omg.PortableInterceptor.ForwardRequest ) t).forward;

                assertEquals ("ForwardRequest and remoteServer do not match ", remoteServerObj, fwdObj);
            }
            else
            {
                fail ("Expected a ForwardRequest and got " + ex);
            }
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testClientInterceptorCRaisesForwardRequestAtRE()
    {
        System.out.println ("\ntestClientInterceptorCRaisesForwardRequestAtRE");

        init ("returnMessage");

        String outMsg = "testClientInterceptorCRaisesFRAtRE";
        throwForwardRequest = CLIENTC_RECEIVE_EXCEPTION;
        throwException = SERVERC_SEND_REPLY;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERC_SEND_REPLY | SERVERC_SEND_EXCEPTION | SERVERB_SEND_EXCEPTION |
        SERVERA_SEND_EXCEPTION |  CLIENTC_RECEIVE_EXCEPTION | CLIENTB_RECEIVE_OTHER |
        CLIENTA_RECEIVE_OTHER;

        try
        {
            String response = clientRef.returnMessage (outMsg);
            fail ("Expected ForwardRequest");
        }
        catch (Exception ex)
        {
            Throwable t = ex.getCause();

            if (t instanceof org.omg.PortableInterceptor.ForwardRequest)
            {
                org.omg.CORBA.Object fwdObj = (org.omg.CORBA.Object)
                    ( (org.omg.PortableInterceptor.ForwardRequest ) t).forward;

                assertEquals ("ForwardRequest and remoteServer do not match ", remoteServerObj, fwdObj);
            }
            else
            {
                fail ("Expected a ForwardRequest and got " + ex);
            }
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testServerInterceptorARaisesForwardRequestAtSO()
    {
        System.out.println ("\ntestServerInterceptorARaisesForwardRequestAtSO");

        init ("returnMessage");

        String outMsg = "testServerInterceptorARaisesFRAtSO";
        throwForwardRequest = SERVERA_SEND_OTHER;
        throwException = SERVERC_SEND_REPLY;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERC_SEND_REPLY | SERVERB_SEND_EXCEPTION | SERVERA_SEND_OTHER |
        CLIENTA_RECEIVE_OTHER | CLIENTB_RECEIVE_OTHER | CLIENTC_RECEIVE_OTHER;


        try
        {
            String response = clientRef.returnMessage (outMsg);
            fail ("Expected ForwardRequest");
        }
        catch (Exception ex)
        {
            Throwable t = ex.getCause();

            if (t instanceof org.omg.PortableInterceptor.ForwardRequest)
            {
                org.omg.CORBA.Object fwdObj = (org.omg.CORBA.Object)
                    ( (org.omg.PortableInterceptor.ForwardRequest ) t).forward;

                assertEquals ("ForwardRequest and remoteServer do not match ",
                              (org.omg.CORBA.Object) serverRef,
                              fwdObj);
            }
            else
            {
                fail ("Expected a ForwardRequest and got " + ex);
            }
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testClientInterceptorBRaisesForwardRequestAtRO()
    {
        System.out.println ("\ntestClientInterceptorBRaisesForwardRequestAtRO");

        init ("returnMessage");

        String outMsg = "testClientInterceptorBRaisesFRAtRO";
        throwForwardRequest = CLIENTB_RECEIVE_OTHER;
        throwException = SERVERC_SEND_REPLY;

        int expectedCalls = CLIENTA_SEND_REQ | CLIENTB_SEND_REQ |CLIENTC_SEND_REQ |
        SERVERA_RECEIVE_REQUEST_SC | SERVERB_RECEIVE_REQUEST_SC | SERVERC_RECEIVE_REQUEST_SC |
        SERVERA_RECEIVE_REQUEST | SERVERB_RECEIVE_REQUEST | SERVERC_RECEIVE_REQUEST |
        SERVERC_SEND_REPLY | SERVERB_SEND_EXCEPTION | SERVERA_SEND_EXCEPTION |
        CLIENTA_RECEIVE_OTHER | CLIENTB_RECEIVE_OTHER | CLIENTC_RECEIVE_OTHER;


        try
        {
            String response = clientRef.returnMessage (outMsg);
            fail ("Expected ForwardRequest");
        }
        catch (Exception ex)
        {
            Throwable t = ex.getCause();

            if (t instanceof org.omg.PortableInterceptor.ForwardRequest)
            {
                org.omg.CORBA.Object fwdObj = (org.omg.CORBA.Object)
                    ( (org.omg.PortableInterceptor.ForwardRequest ) t).forward;

                assertEquals ("ForwardRequest and remoteServer do not match ",
                              (org.omg.CORBA.Object) serverRef,
                              fwdObj);
            }
            else
            {
                fail ("Expected a ForwardRequest and got " + ex);
            }
        }

        assertEquals ("Calls to interceptors not as expected",
                      expectedCalls,
                      callsMade);
    }

    public void testRemoteServerShutdown()
    {
        System.out.println ("\ntestRemoteServerShutdown");

        init ("shutdown");

        remoteServer.shutdown();
    }


    static class PIServerImpl
        extends PIServerPOA
    {
        private POA poa;

        PIServerImpl( POA poa )
        {
            this.poa = poa;
        }

        public void sendMessage (String msg)
        {
            System.out.println ("Server got " + msg);
        }

        public String returnMessage (String msg)
        {
            return "Local server got message " + msg;
        }

        public void throwException (String msg)
        {
            throw new UNKNOWN (msg,
                               SERVER_MINOR,
                               CompletionStatus.COMPLETED_YES);
        }

        public POA _default_POA()
        {
            return poa;
        }

        public void shutdown()
        {
        }
    }


    /**
     * An ORB initializer class.
     */
    public static class LocalPIInitializer
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableInterceptor.ORBInitializer
    {
        /**
         * Called before init of the actual ORB.
         *
         * @param info The ORB init info.
         */
        public void pre_init (org.omg.PortableInterceptor.ORBInitInfo info)
        {
            try
            {
                slotID = info.allocate_slot_id();
                info.add_server_request_interceptor (new LocalServerInterceptorA());
                info.add_server_request_interceptor (new LocalServerInterceptorB());
                info.add_server_request_interceptor (new LocalServerInterceptorC());
                info.add_client_request_interceptor (new LocalClientInterceptorA());
                info.add_client_request_interceptor (new LocalClientInterceptorB());
                info.add_client_request_interceptor (new LocalClientInterceptorC());
                info.add_ior_interceptor( new LocalIORInterceptor() );
            }
            catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ex )
            {
                fail ("unexpected exception received: " + ex);
            }
        }

        /**
         * Called after init of the actual ORB.
         *
         * @param info The ORB init info.
         */
        public void post_init (org.omg.PortableInterceptor.ORBInitInfo info)
        {
        }
    }

    static class LocalIORInterceptor
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableInterceptor.IORInterceptor
    {
        public String name()
        {
            return "";
        }

        public void destroy()
        {
        }

        public void establish_components (org.omg.PortableInterceptor.IORInfo info)
        {
            info.add_ior_component (new org.omg.IOP.TaggedComponent (TEST_SCID, new byte[ 0 ]));
        }
    }

    static class LocalClientInterceptorA
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableInterceptor.ClientRequestInterceptor
    {
        public String name()
        {
            return "";
        }

        public void destroy()
        {
        }

        public void send_request( org.omg.PortableInterceptor.ClientRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorA :: send_request");
            }


            callsMade = callsMade | CLIENTA_SEND_REQ;

            if (throwException == CLIENTA_SEND_REQ)
            {
                throw new UNKNOWN (CLIENTA_SEND_REQ,
                                   CompletionStatus.COMPLETED_NO );
            }

            if (throwForwardRequest == CLIENTA_SEND_REQ && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            // add service context.
            try
            {
                ri.get_slot (slotID);
                ri.get_slot (slotID).type ();
                ri.get_slot (slotID).type ().kind ();

                if (ri.get_slot (slotID).type().kind() != org.omg.CORBA.TCKind.tk_null)
                {
                    ri.add_request_service_context( new org.omg.IOP.ServiceContext( TEST_SCID,
                                                                                    new byte[ 0 ] ),
                                                    true );
                }
            }
            catch (org.omg.PortableInterceptor.InvalidSlot ex)
            {
                fail (ex.toString());
            }

            // request information.
            ri.request_id();

            assertEquals ("Operation name not correct",
                          operation,
                          ri.operation());

            assertTrue ("No response expected for request with response",
                        ri.response_expected());

            if (!forwardRequestThrown && ! ri.operation().equals ("shutdown"))
            {
               assertEquals ("Incorrect sync scope",
                             3,
                             ri.sync_scope());
            }

            try
            {
                ri.get_request_service_context (TEST_SCID);
            }
            catch (BAD_PARAM bp)
            {
                /**
                 * If a reply_service_context is not found for the request then
                 * a BAD_PARAM is thrown.  This is OK for tests that don't have a
                 * service context but not for tests where a service context should
                 * have been found.
                 */
                if (hasServiceContexts)
                {
                    fail ("Got a BAD_PARAM getting reply service contexts");
                }
            }


            // target information
            ri.target();
            ri.effective_target();
            ri.effective_profile();
            ri.get_effective_component (org.omg.IOP.TAG_CODE_SETS.value);
            ri.get_effective_components (org.omg.IOP.TAG_CODE_SETS.value);

            try
            {
                ri.arguments();
                fail ("Expected NO_RESOURCES exception");
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.exceptions();
                fail ("Expected NO_RESOURCES exception");
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.contexts();
                fail ("Expected NO_RESOURCES exception");
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.operation_context();
                fail ("Expected NO_RESOURCES exception");
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }

            try
            {
                ri.result();
                fail ("Expected BAD_INV_ORDER");
            }
            catch (BAD_INV_ORDER bio)
            {
                // OK expected this exception
            }

            try
            {
                ri.reply_status();
                fail ("Expected BAD_INV_ORDER");
            }
            catch (BAD_INV_ORDER bio)
            {
                // OK expected this exception
            }

            try
            {
                ri.forward_reference();
            }
            catch (BAD_INV_ORDER bio)
            {
                // OK expected this exception
            }

            try
            {
                ri.get_reply_service_context (TEST_SCID);
                fail ("Expected BAD_INV_ORDER");
            }
            catch (BAD_INV_ORDER bio)
            {
                // OK expected this exception
            }

            try
            {
                ri.received_exception();
                fail ("Expected BAD_INV_ORDER");
            }
            catch (BAD_INV_ORDER bio)
            {
                // OK expected this exception
            }

            try
            {
                ri.received_exception_id();
                fail ("Expected BAD_INV_ORDER");
            }
            catch (BAD_INV_ORDER bio)
            {
                // OK expected this exception
            }
        }

        public void send_poll( org.omg.PortableInterceptor.ClientRequestInfo ri )
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorA :: send_poll");
            }


            callsMade = callsMade | CLIENTA_SEND_POLL;
        }

        public void receive_reply (org.omg.PortableInterceptor.ClientRequestInfo ri )
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorA :: receive_reply");
            }

            callsMade = callsMade | CLIENTA_RECEIVE_REPLY;

            if (throwException == CLIENTA_RECEIVE_REPLY)
            {
                throw new UNKNOWN (CLIENTA_RECEIVE_REPLY,
                                   CompletionStatus.COMPLETED_YES);
            }

            try
            {
                ri.get_reply_service_context (TEST_SCID);
            }
            catch (BAD_PARAM ex )
            {
                /**
                 * If a reply_service_context is not found for the request then
                 * a BAD_PARAM is thrown.  This is OK for tests that don't have a
                 * service context but not for tests where a service context should
                 * have been found.
                 */
                if (hasServiceContexts)
                {
                    fail ("Got a BAD_PARAM getting reply service contexts");
                }
            }

            try
            {
                ri.result();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request results. These will always fail
            }


        }

        public void receive_other( org.omg.PortableInterceptor.ClientRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorA :: receive_other");
            }

            callsMade = callsMade | CLIENTA_RECEIVE_OTHER;

            if (throwException == CLIENTA_RECEIVE_OTHER)
            {
                throw new UNKNOWN (CLIENTA_RECEIVE_OTHER,
                                   CompletionStatus.COMPLETED_NO);
            }
        }

        public void receive_exception( org.omg.PortableInterceptor.ClientRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorA :: receive_exception");
            }

            callsMade = callsMade | CLIENTA_RECEIVE_EXCEPTION;

            if (throwForwardRequest == CLIENTA_RECEIVE_EXCEPTION && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }


            if ( ri.received_exception_id().equals (org.omg.CORBA.UNKNOWNHelper.id())
                 && throwException == CLIENTA_RECEIVE_EXCEPTION)
            {
                UNKNOWN uex = org.omg.CORBA.UNKNOWNHelper.extract(
                    ri.received_exception() );

                uex.minor = uex.minor | CLIENTA_RECEIVE_EXCEPTION;

                throw uex;
            }
        }
    }

    static class LocalClientInterceptorB
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableInterceptor.ClientRequestInterceptor
    {
        static boolean receiveOtherFRThrown = false;

        public String name()
        {
            return "";
        }

        public void destroy()
        {
        }

        public void send_request( org.omg.PortableInterceptor.ClientRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorB :: send_request");
            }

            callsMade = callsMade | CLIENTB_SEND_REQ;

            if (throwException == CLIENTB_SEND_REQ)
            {
                throw new UNKNOWN (CLIENTB_SEND_REQ,
                                   CompletionStatus.COMPLETED_NO );
            }

            if (throwForwardRequest == CLIENTB_SEND_REQ && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            // request information.
            ri.request_id();

            assertEquals ("Operation name not correct",
                          operation,
                          ri.operation());

            assertTrue ("No response expected for request with response",
                        ri.response_expected());

            if (!forwardRequestThrown && ! ri.operation().equals ("shutdown") )
            {
               assertEquals ("Incorrect sync scope",
                             3,
                             ri.sync_scope());
            }

            // target information
            ri.target();
            ri.effective_target();
            ri.effective_profile();
            ri.get_effective_component (org.omg.IOP.TAG_CODE_SETS.value);
            ri.get_effective_components (org.omg.IOP.TAG_CODE_SETS.value);

            try
            {
                ri.arguments();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.exceptions();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.contexts();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.operation_context();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
        }

        public void send_poll( org.omg.PortableInterceptor.ClientRequestInfo ri )
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorB :: send_poll");
            }

            callsMade = callsMade | CLIENTB_SEND_POLL;
        }

        public void receive_reply (org.omg.PortableInterceptor.ClientRequestInfo ri )
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorB :: receive_reply");
            }

            callsMade = callsMade | CLIENTB_RECEIVE_REPLY;

            if (throwException == CLIENTB_RECEIVE_REPLY)
            {
                throw new UNKNOWN (CLIENTB_RECEIVE_REPLY,
                                   CompletionStatus.COMPLETED_YES);
            }

            try
            {
                ri.get_reply_service_context( TEST_SCID );
            }
            catch (BAD_PARAM ex )
            {
                /**
                 * If a reply_service_context is not found for the request then
                 * a BAD_PARAM is thrown.  This is OK for tests that don't have a
                 * service context but not for tests where a service context should
                 * have been found.
                 */

                if (hasServiceContexts)
                {
                    fail ("Got a BAD_PARAM getting reply service contexts");
                }
            }

            try
            {
                ri.result();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request results. These will always fail
            }

        }

        public void receive_other( org.omg.PortableInterceptor.ClientRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorB :: receive_other");
            }

            callsMade = callsMade | CLIENTB_RECEIVE_OTHER;

            if (throwForwardRequest == CLIENTB_RECEIVE_OTHER && ! receiveOtherFRThrown)
            {
                receiveOtherFRThrown = true;
                throw new ForwardRequest (serverRef);
            }

            if (throwException == CLIENTB_RECEIVE_OTHER)
            {
                throw new UNKNOWN (CLIENTB_RECEIVE_OTHER,
                                   CompletionStatus.COMPLETED_NO);
            }
        }

        public void receive_exception( org.omg.PortableInterceptor.ClientRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorB :: receive_exception");
            }

            callsMade = callsMade | CLIENTB_RECEIVE_EXCEPTION;

            if (throwForwardRequest == CLIENTB_RECEIVE_EXCEPTION && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            if ( ri.received_exception_id().equals (org.omg.CORBA.UNKNOWNHelper.id())
                 && throwException == CLIENTB_RECEIVE_EXCEPTION)
            {
                UNKNOWN uex = org.omg.CORBA.UNKNOWNHelper.extract(
                    ri.received_exception() );

                uex.minor = uex.minor | CLIENTB_RECEIVE_EXCEPTION;

                throw uex;
            }
        }
    }

    static class LocalClientInterceptorC
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableInterceptor.ClientRequestInterceptor
    {
        public String name()
        {
            return "";
        }

        public void destroy()
        {
        }

        public void send_request( org.omg.PortableInterceptor.ClientRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorC :: send_request");
            }

            callsMade = callsMade | CLIENTC_SEND_REQ;

            if (throwException == CLIENTC_SEND_REQ)
            {
                throw new UNKNOWN (CLIENTC_SEND_REQ,
                                   CompletionStatus.COMPLETED_NO );
            }

            if (throwForwardRequest == CLIENTC_SEND_REQ && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            // request information.
            ri.request_id();

            assertEquals ("Operation name not correct",
                          operation,
                          ri.operation());

            assertTrue ("No response expected for request with response",
                        ri.response_expected());


            if (!forwardRequestThrown && ! ri.operation().equals ("shutdown"))
            {
                assertEquals ("Incorrect sync scope",
                              3,
                              ri.sync_scope());
            }

            // target information
            ri.target();
            ri.effective_target();
            ri.effective_profile();
            ri.get_effective_component (org.omg.IOP.TAG_CODE_SETS.value);
            ri.get_effective_components (org.omg.IOP.TAG_CODE_SETS.value);

            try
            {
                ri.arguments();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.exceptions();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.contexts();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.operation_context();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
        }

        public void send_poll( org.omg.PortableInterceptor.ClientRequestInfo ri )
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorC :: send_poll");
            }

            callsMade = callsMade | CLIENTC_SEND_POLL;
        }

        public void receive_reply (org.omg.PortableInterceptor.ClientRequestInfo ri )
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorC :: receive_reply");
            }

            callsMade = callsMade | CLIENTC_RECEIVE_REPLY;

            if (throwException == CLIENTC_RECEIVE_REPLY)
            {
                throw new UNKNOWN (CLIENTC_RECEIVE_REPLY,
                                   CompletionStatus.COMPLETED_YES);
            }

            try
            {
                ri.get_reply_service_context( TEST_SCID );
            }
            catch (BAD_PARAM ex )
            {
                /**
                 * If a reply_service_context is not found for the request then
                 * a BAD_PARAM is thrown.  This is OK for tests that don't have a
                 * service context but not for tests where a service context should
                 * have been found.
                 */
                if (hasServiceContexts)
                {
                    fail ("Got a BAD_PARAM getting reply service contexts");
                }
            }


            try
            {
                ri.result();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request results. These will always fail
            }
        }

        public void receive_other( org.omg.PortableInterceptor.ClientRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalClientInterceptorC :: receive_other");
            }

            callsMade = callsMade | CLIENTC_RECEIVE_OTHER;

            if (throwException == CLIENTC_RECEIVE_OTHER)
            {
                throw new UNKNOWN (CLIENTC_RECEIVE_OTHER,
                                   CompletionStatus.COMPLETED_NO);
            }
        }

        public void receive_exception( org.omg.PortableInterceptor.ClientRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                 System.out.println ("LocalClientInterceptorC :: receive_exception");
            }

            callsMade = callsMade | CLIENTC_RECEIVE_EXCEPTION;

            if (throwForwardRequest == CLIENTC_RECEIVE_EXCEPTION && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            if ( ri.received_exception_id().equals (org.omg.CORBA.UNKNOWNHelper.id())
                 && throwException == CLIENTC_RECEIVE_EXCEPTION)
            {
                UNKNOWN uex = org.omg.CORBA.UNKNOWNHelper.extract(
                    ri.received_exception() );

                uex.minor = uex.minor | CLIENTC_RECEIVE_EXCEPTION;

                throw uex;
            }
        }
    }


    static class LocalServerInterceptorA
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableInterceptor.ServerRequestInterceptor
    {
        static boolean sendOtherFRThrown = false;

        public java.lang.String name()
        {
            return "";
        }

        public void destroy()
        {
        }

        public void receive_request_service_contexts(
            org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorA - receive_request_service_contexts");
            }

            callsMade = callsMade | SERVERA_RECEIVE_REQUEST_SC;

            if (throwException == SERVERA_RECEIVE_REQUEST_SC)
            {
                throw new UNKNOWN (SERVERA_RECEIVE_REQUEST_SC,
                                   CompletionStatus.COMPLETED_NO);
            }

            if (throwForwardRequest == SERVERA_RECEIVE_REQUEST_SC && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            // request information.
            ri.request_id();

            assertEquals ("Operation name not correct",
                          operation,
                          ri.operation());

            assertTrue ("No response expected for request with response",
                        ri.response_expected() );

            assertEquals ("Incorrect sync scope",
                          3,
                          ri.sync_scope());

            try
            {
                ri.get_request_service_context (TEST_SCID);
                ri.set_slot (slotID, any);
            }
            catch (BAD_PARAM ex)
            {
                if (hasServiceContexts)
                {
                    fail ("Got a BAD_PARAM getting request service contexts");
                }
            }
            catch (org.omg.PortableInterceptor.InvalidSlot ex)
            {
                fail ("unexpected exception received: " + ex);
            }
        }

        public void receive_request( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorA - receive_request");
            }

            callsMade = callsMade | SERVERA_RECEIVE_REQUEST;

            if (throwException == SERVERA_RECEIVE_REQUEST || throwException == SERVERA_SEND_EXCEPTION)
            {
                throw new UNKNOWN (SERVERA_RECEIVE_REQUEST,
                                   CompletionStatus.COMPLETED_NO );
            }

            if (throwForwardRequest == SERVERA_RECEIVE_REQUEST && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            // request information
            ri.object_id();
            byte []aid = ri.adapter_id();

            assertTrue (aid != null);
            assertTrue (aid.length != 0);

            String adapters [] = ri.adapter_name ();
            assertTrue (adapters != null);
            assertTrue (adapters[0].equals ("RootPOA"));
            assertTrue (adapters[1].equals ("childPOA"));

            ri.target_most_derived_interface();

            assertTrue ("target does not implement object",
                        ri.target_is_a( "IDL:omg.org/CORBA/Object:1.0"));

            try
            {
                ri.arguments();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.exceptions();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.contexts();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.operation_context();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
        }

        public void send_reply( org.omg.PortableInterceptor.ServerRequestInfo ri )
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorA - send_reply");
            }

            callsMade = callsMade | SERVERA_SEND_REPLY;

            if (throwException == SERVERA_SEND_REPLY)
            {
                throw new UNKNOWN (SERVERA_SEND_REPLY,
                                   CompletionStatus.COMPLETED_YES);
            }
        }

        public void send_exception( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorA - send_exception");
            }

            callsMade = callsMade | SERVERA_SEND_EXCEPTION;

            if ((throwForwardRequest == SERVERA_SEND_EXCEPTION ||
                 throwForwardRequest == CLIENTB_RECEIVE_OTHER )
                && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            org.omg.CORBA.Any any = ri.sending_exception();

            try
            {
                if ( any.type().id().equals( org.omg.CORBA.UNKNOWNHelper.type().id() )
                     && throwException == SERVERA_SEND_EXCEPTION)
                {
                    UNKNOWN uex = org.omg.CORBA.UNKNOWNHelper.extract(
                        ri.sending_exception() );

                    uex.minor = uex.minor | SERVERA_SEND_EXCEPTION;

                    uex.completed = CompletionStatus.COMPLETED_YES;
                    throw uex;
                }
            }
            catch (org.omg.CORBA.TypeCodePackage.BadKind bk)
            {
                fail ("Got an unexpected BadKind exception");
            }

        }

        public void send_other( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorA - send_other");
            }

            callsMade = callsMade | SERVERA_SEND_OTHER;

            if (throwForwardRequest == SERVERA_SEND_OTHER
                 && ! sendOtherFRThrown)
            {
                sendOtherFRThrown = true;
                throw new ForwardRequest (serverRef);
            }

            if (throwException == SERVERA_SEND_OTHER)
            {
                throw new UNKNOWN (SERVERA_SEND_OTHER,
                                   CompletionStatus.COMPLETED_NO);
            }
        }
    }


    static class LocalServerInterceptorB
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableInterceptor.ServerRequestInterceptor
    {
        public java.lang.String name()
        {
            return "";
        }

        public void destroy()
        {
        }

        public void receive_request_service_contexts(
            org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorB - receive_request_service_contexts");
            }

            callsMade = callsMade | SERVERB_RECEIVE_REQUEST_SC;

            if (throwException == SERVERB_RECEIVE_REQUEST_SC)
            {
                throw new UNKNOWN (SERVERB_RECEIVE_REQUEST_SC,
                                   CompletionStatus.COMPLETED_NO);
            }

            if (throwForwardRequest == SERVERB_RECEIVE_REQUEST_SC && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            // request information.
            ri.request_id();

            assertEquals ("Operation name not correct",
                          operation,
                          ri.operation());

            assertTrue ("No response expected for request with response",
                        ri.response_expected() );

            assertEquals ("Incorrect sync scope",
                          3,
                          ri.sync_scope());

            try
            {
                ri.get_request_service_context (TEST_SCID);
                ri.set_slot (slotID, any );
            }
            catch (BAD_PARAM ex)
            {
                if (hasServiceContexts)
                {
                    fail ("Got a BAD_PARAM getting request service contexts");
                }
            }
            catch (org.omg.PortableInterceptor.InvalidSlot ex)
            {
                fail ("unexpected exception received: " + ex);
            }
        }

        public void receive_request( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorB - receive_request");
            }

            callsMade = callsMade | SERVERB_RECEIVE_REQUEST;

            if (throwException == SERVERB_RECEIVE_REQUEST || throwException == SERVERB_SEND_EXCEPTION)
            {
                throw new UNKNOWN (SERVERB_RECEIVE_REQUEST,
                                   CompletionStatus.COMPLETED_NO );
            }

            if (throwForwardRequest == SERVERB_RECEIVE_REQUEST && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            // request information
            ri.object_id();
            byte []aid = ri.adapter_id();

            assertTrue (aid != null);
            assertTrue (aid.length != 0);

            String adapters [] = ri.adapter_name ();
            assertTrue (adapters != null);
            assertTrue (adapters[0].equals ("RootPOA"));
            assertTrue (adapters[1].equals ("childPOA"));

            ri.target_most_derived_interface();

            assertTrue ("target does not implement object",
                        ri.target_is_a( "IDL:omg.org/CORBA/Object:1.0"));

            try
            {
                ri.arguments();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.exceptions();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.contexts();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.operation_context();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
        }

        public void send_reply( org.omg.PortableInterceptor.ServerRequestInfo ri )
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorB - send_reply");
            }

            callsMade = callsMade | SERVERB_SEND_REPLY;

            if (throwException == SERVERB_SEND_REPLY)
            {
                throw new UNKNOWN (SERVERB_SEND_REPLY,
                                   CompletionStatus.COMPLETED_YES);
            }
        }

        public void send_exception( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorB - send_exception");
            }

            callsMade = callsMade | SERVERB_SEND_EXCEPTION;


            if ((throwForwardRequest == SERVERB_SEND_EXCEPTION ||
                 throwForwardRequest == SERVERA_SEND_OTHER)
                 && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            org.omg.CORBA.Any any = ri.sending_exception();

            try
            {
                if ( any.type().id().equals( org.omg.CORBA.UNKNOWNHelper.type().id() )
                     && throwException == SERVERB_SEND_EXCEPTION)
                {
                    UNKNOWN uex = org.omg.CORBA.UNKNOWNHelper.extract(
                        ri.sending_exception() );

                    uex.minor = uex.minor | SERVERB_SEND_EXCEPTION;

                    uex.completed = CompletionStatus.COMPLETED_YES;
                    throw uex;
                }
            }
            catch (org.omg.CORBA.TypeCodePackage.BadKind bk)
            {
                fail ("Got an unexpected BadKind exception");
            }

        }

        public void send_other( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorB - send_other");
            }

            callsMade = callsMade | SERVERB_SEND_OTHER;

            if (throwException == SERVERB_SEND_OTHER)
            {
                throw new UNKNOWN (SERVERB_SEND_OTHER,
                                   CompletionStatus.COMPLETED_NO);
            }
        }
    }

    static class LocalServerInterceptorC
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableInterceptor.ServerRequestInterceptor
    {
        public java.lang.String name()
        {
            return "";
        }

        public void destroy()
        {
        }

        public void receive_request_service_contexts(
            org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorC - receive_request_service_contexts");
            }

            callsMade = callsMade | SERVERC_RECEIVE_REQUEST_SC;

            if (throwException == SERVERC_RECEIVE_REQUEST_SC)
            {
                throw new UNKNOWN (SERVERC_RECEIVE_REQUEST_SC,
                                   CompletionStatus.COMPLETED_NO);
            }

            if (throwForwardRequest == SERVERC_RECEIVE_REQUEST_SC && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            // request information.
            ri.request_id();

            assertEquals ("Operation name not correct",
                          operation,
                          ri.operation());

            assertTrue ("No response expected for request with response",
                        ri.response_expected() );

            assertEquals ("Incorrect sync scope",
                          3,
                          ri.sync_scope());

            try
            {
                ri.get_request_service_context (TEST_SCID);
                ri.set_slot (slotID, any );
            }
            catch (BAD_PARAM ex)
            {
                if (hasServiceContexts)
                {
                    fail ("Got a BAD_PARAM getting request service contexts");
                }
            }
            catch (org.omg.PortableInterceptor.InvalidSlot ex)
            {
                fail ("unexpected exception received: " + ex);
            }
        }

        public void receive_request( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
               System.out.println ("LocalServerInterceptorC - receive_request");
            }

            callsMade = callsMade | SERVERC_RECEIVE_REQUEST;

            if (throwException == SERVERC_RECEIVE_REQUEST || throwException == SERVERC_SEND_EXCEPTION)
            {
                throw new UNKNOWN (SERVERC_RECEIVE_REQUEST,
                                   CompletionStatus.COMPLETED_NO );
            }

            if (throwForwardRequest == SERVERC_RECEIVE_REQUEST && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            // request information
            ri.object_id();
            byte []aid = ri.adapter_id();

            assertTrue (aid != null);
            assertTrue (aid.length != 0);

            String adapters [] = ri.adapter_name ();
            assertTrue (adapters != null);
            assertTrue (adapters[0].equals ("RootPOA"));
            assertTrue (adapters[1].equals ("childPOA"));

            ri.target_most_derived_interface();

            assertTrue ("target does not implement object",
                        ri.target_is_a( "IDL:omg.org/CORBA/Object:1.0"));

            try
            {
                ri.arguments();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.exceptions();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.contexts();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
            try
            {
                ri.operation_context();
            }
            catch ( NO_RESOURCES ex )
            {
                // test retrieving request parameters. These will always fail
            }
        }

        public void send_reply( org.omg.PortableInterceptor.ServerRequestInfo ri )
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorC - send_reply");
            }

            callsMade = callsMade | SERVERC_SEND_REPLY;

            if (throwException == SERVERC_SEND_REPLY)
            {
                throw new UNKNOWN (SERVERC_SEND_REPLY,
                                   CompletionStatus.COMPLETED_YES);
            }

            try
            {
                if (ri.get_slot( slotID ).type().kind() != org.omg.CORBA.TCKind.tk_null)
                {
                    ri.add_reply_service_context (new org.omg.IOP.ServiceContext (TEST_SCID,
                                                                                  new byte[ 0 ]),
                                                  true);
                }
            }
            catch ( org.omg.PortableInterceptor.InvalidSlot ex )
            {
                fail (ex.toString());
            }
        }

        public void send_exception( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorC - send_exception");
            }

            callsMade = callsMade | SERVERC_SEND_EXCEPTION;

            if (throwForwardRequest == SERVERC_SEND_EXCEPTION && ! forwardRequestThrown)
            {
                forwardRequestThrown = true;
                throw new ForwardRequest (remoteServerObj);
            }

            org.omg.CORBA.Any any = ri.sending_exception();

            try
            {
                if ( any.type().id().equals( org.omg.CORBA.UNKNOWNHelper.type().id() )
                     && throwException == SERVERC_SEND_EXCEPTION)
                {
                    UNKNOWN uex = org.omg.CORBA.UNKNOWNHelper.extract(
                        ri.sending_exception() );

                    uex.minor = uex.minor | SERVERC_SEND_EXCEPTION;

                    uex.completed = CompletionStatus.COMPLETED_YES;
                    throw uex;
                }
            }
            catch (org.omg.CORBA.TypeCodePackage.BadKind bk)
            {
                fail ("Got an unexpected BadKind exception");
            }
        }

        public void send_other( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {

            if (localDebugOn)
            {
                System.out.println ("LocalServerInterceptorC - send_other");
            }

            callsMade = callsMade | SERVERC_SEND_OTHER;

            if (throwException == SERVERC_SEND_OTHER)
            {
                throw new UNKNOWN (SERVERC_SEND_OTHER,
                                   CompletionStatus.COMPLETED_NO);
            }
        }
    }

    /**
     * The entry point of the test case.
     *
     * @param args The command line arguments.
     */
    public static void main( String args[] )
    {
        if (args.length > 0)
        {
            for (int i = 0; i < args.length; i++)
            {
                if (args[i].equals ("-local"))
                {
                    localDebugOn = true;
                }
            }
        }

        junit.textui.TestRunner.run( new TestSuite( LocalPITest.class ) );
    }
}
