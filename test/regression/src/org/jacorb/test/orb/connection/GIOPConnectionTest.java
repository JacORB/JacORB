package org.jacorb.test.orb.connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.config.Configuration;
import org.jacorb.config.JacORBConfiguration;
import org.jacorb.orb.ORB;
import org.jacorb.orb.giop.ClientConnection;
import org.jacorb.orb.giop.GIOPConnection;
import org.jacorb.orb.giop.GIOPConnectionManager;
import org.jacorb.orb.giop.LocateRequestOutputStream;
import org.jacorb.orb.giop.MessageOutputStream;
import org.jacorb.orb.giop.Messages;
import org.jacorb.orb.giop.ReplyInputStream;
import org.jacorb.orb.giop.ReplyListener;
import org.jacorb.orb.giop.RequestInputStream;
import org.jacorb.orb.giop.RequestListener;
import org.jacorb.orb.giop.RequestOutputStream;
import org.jacorb.orb.giop.ServerGIOPConnection;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.test.common.JacORBTestCase;
import org.jacorb.test.common.JacORBTestSuite;
import org.omg.ETF.BufferHolder;
import org.omg.ETF.Profile;
import org.omg.GIOP.MsgType_1_1;

/**
 * GIOPConnectionTest.java
 *
 *
 * Created: Sat Jun 22 14:26:15 2002
 *
 * @jacorb-client-since 2.2
 * @author Nicolas Noffke
 */

public class GIOPConnectionTest
    extends JacORBTestCase
{
    private Configuration config;
    private ORB orb;

    public void setUp()
        throws Exception
    {
        orb = (ORB) ORB.init(new String[0], null);
        config = JacORBConfiguration.getConfiguration(null, orb, false);
    }

    protected void tearDown() throws Exception
    {
        config = null;
        orb.shutdown(true);
        orb = null;
    }

    public static Test suite()
    {
        TestSuite suite = new JacORBTestSuite ("GIOPConnection Test",
                                               GIOPConnectionTest.class);

        suite.addTest (new GIOPConnectionTest ("testGIOP_1_0_CorrectRefusing"));
        suite.addTest (new GIOPConnectionTest ("testGIOP_1_1_IllegalMessageType"));
        suite.addTest (new GIOPConnectionTest ("testGIOP_1_2_CorrectFragmentedRequest"));
        suite.addTest (new GIOPConnectionTest ("testGIOP_1_2_CorrectCloseOnGarbage"));
        suite.addTest (new GIOPConnectionTest ("testGIOP_1_1_CorrectRequest"));
        suite.addTest (new GIOPConnectionTest ("testGIOP_1_1_CorrectFragmentedRequest"));

        return suite;
    }


    class DummyTransport extends org.omg.ETF._ConnectionLocalBase
    {
        private boolean closed = false;
        private byte[] data = null;
        private int index = 0;
        private ByteArrayOutputStream b_out = new ByteArrayOutputStream();
        private org.omg.ETF.Profile profile = new IIOPProfile
        (
            new IIOPAddress ("127.0.0.1", 4711),
            null,
            orb.getGIOPMinorVersion()
        );

        public DummyTransport( List<byte[]> messages )
        {
            // convert the message list into a plain byte array

            int size = 0;
            for (Iterator<byte[]> i = messages.iterator(); i.hasNext();)
            {
                size += i.next().length;
            }
            data = new byte[size];
            int index = 0;
            for (Iterator<byte[]> i = messages.iterator(); i.hasNext();)
            {
                byte[] msg = i.next();
                System.arraycopy(msg, 0, data, index, msg.length);
                index += msg.length;
            }
        }

        public byte[] getWrittenMessage()
        {
            return b_out.toByteArray();
        }

        public void connect (org.omg.ETF.Profile profile, long time_out)
        {
            // nothing
        }

        public boolean hasBeenClosed()
        {
            return closed;
        }

        public boolean is_connected()
        {
            return !closed;
        }

        public void write( boolean is_first, boolean is_last,
                           byte[] message, int start, int size,
                           long timeout )
        {
            b_out.write( message, start, size );
        }


        public void flush()
        {
        }

        public void close()
        {
            closed = true;
        }

        public boolean isSSL()
        {
            return false;
        }

        public void turnOnFinalTimeout()
        {
        }

        public Profile get_server_profile()
        {
            return profile;
        }

        public int read (BufferHolder data, int offset,
                         int min_length, int max_length, long time_out)
        {
            if (this.index + min_length > this.data.length)
            {
                throw new org.omg.CORBA.COMM_FAILURE ("end of stream");
            }
            System.arraycopy(this.data, this.index, data.value, offset, min_length);
            this.index += min_length;
            return min_length;
        }

        public boolean is_data_available()
        {
            return true;
        }

        public boolean supports_callback()
        {
            return false;
        }

        public boolean use_handle_time_out()
        {
            return false;
        }

        public boolean wait_next_data(long time_out)
        {
            return false;
        }

    }


    private class DummyRequestListener
        implements RequestListener
    {
        private byte[] request = null;

        public DummyRequestListener()
        {
        }

        public byte[] getRequest()
        {
            return request;
        }

        public void requestReceived( byte[] request,
                                     GIOPConnection connection )
        {
            this.request = request;
        }

        public void locateRequestReceived( byte[] request,
                                           GIOPConnection connection )
        {
            this.request = request;
        }
        public void cancelRequestReceived( byte[] request,
                                           GIOPConnection connection )
        {
            this.request = request;
        }
    }

    private class DummyReplyListener
        implements ReplyListener
    {
        private byte[] reply = null;

        public DummyReplyListener()
        {
        }

        public byte[] getReply()
        {
            return reply;
        }

        public void replyReceived( byte[] reply,
                                   GIOPConnection connection )
        {
            this.reply = reply;
        }

        public void locateReplyReceived( byte[] reply,
                                         GIOPConnection connection )
        {
            this.reply = reply;
        }

        public void closeConnectionReceived( byte[] close_conn,
                                             GIOPConnection connection )
        {
            this.reply = close_conn;
        }

    }

    public GIOPConnectionTest( String name )
    {
        super( name );
    }

    public void testGIOP_1_1_CorrectFragmentedRequest()
    {
        List<byte[]> messages = new Vector<byte[]>();

        RequestOutputStream r_out =
            new RequestOutputStream( orb, //ClientConnection
                                     (ClientConnection) null,           //request id
                                     0,       //operation
                                     "foo",        // response expected
                                     true,   // SYNC_SCOPE (irrelevant)
                                     (short)-1,        //request start time
                                     null,        //request end time
                                     null,        //reply start time
                                     null, //object key
                                     new byte[1], 1            // giop minor
                                   );

        //manually write the first half of the string "barbaz"
        r_out.write_ulong( 7 ); //string length
        r_out.write_octet( (byte) 'b' );
        r_out.write_octet( (byte) 'a' );
        r_out.write_octet( (byte) 'r' );
        r_out.insertMsgSize();

        byte[] b = r_out.getBufferCopy();

        b[6] |= 0x02; //set "more fragments follow"

        messages.add( b );

        MessageOutputStream m_out =
            new MessageOutputStream(orb);
        m_out.writeGIOPMsgHeader( MsgType_1_1._Fragment,
                                  1 // giop minor
                                );
        m_out.write_octet( (byte) 'b' );
        m_out.write_octet( (byte) 'a' );
        m_out.write_octet( (byte) 'z' );
        m_out.write_octet( (byte) 0);
        m_out.insertMsgSize();

        messages.add( m_out.getBufferCopy() );

        DummyTransport transport =
            new DummyTransport( messages );

        DummyRequestListener request_listener =
            new DummyRequestListener();

        DummyReplyListener reply_listener =
            new DummyReplyListener();

        GIOPConnectionManager giopconn_mg =
            new GIOPConnectionManager();
        try
        {
            giopconn_mg.configure (config);
        }
        catch (Exception e)
        {
        }

        ServerGIOPConnection conn =
            giopconn_mg.createServerGIOPConnection( null,
                    transport,
                    request_listener,
                    reply_listener );

        try
        {
            //will not return until an IOException is thrown (by the
            //DummyTransport)
            conn.receiveMessages();
        }
        catch( IOException e )
        {
            //o.k., thrown by DummyTransport
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Caught exception: " + e );
        }

        //did the GIOPConnection hand the complete request over to the
        //listener?
        assertTrue( request_listener.getRequest() != null );

        RequestInputStream r_in = new RequestInputStream
        ( orb, null, request_listener.getRequest() );

        //is the body correct?
        assertEquals( "barbaz", r_in.read_string() );
    }

    public void testGIOP_1_2_CorrectFragmentedRequest()
    {
        List<byte[]> messages = new Vector<byte[]>();

        RequestOutputStream r_out =
            new RequestOutputStream( orb, //ClientConnection
                                     (ClientConnection) null,           //request id
                                     0,       //operation
                                     "foo",        // response expected
                                     true,   // SYNC_SCOPE (irrelevant)
                                     (short)-1,        //request start time
                                     null,        //request end time
                                     null,        //reply start time
                                     null, //object key
                                     new byte[1], 2            // giop minor
                                     );

        //manually write the first half of the string "barbaz"
        r_out.write_ulong( 7 ); //string length
        r_out.write_octet( (byte) 'b' );
        r_out.write_octet( (byte) 'a' );
        r_out.write_octet( (byte) 'r' );
        r_out.insertMsgSize();

        byte[] b = r_out.getBufferCopy();

        b[6] |= 0x02; //set "more fragments follow"

        messages.add( b );

        MessageOutputStream m_out =
            new MessageOutputStream(orb);
        m_out.writeGIOPMsgHeader( MsgType_1_1._Fragment,
                                     2 // giop minor
                                     );
        m_out.write_ulong( 0 ); // Fragment Header (request id)
        m_out.write_octet( (byte) 'b' );
        m_out.write_octet( (byte) 'a' );
        m_out.write_octet( (byte) 'z' );
        m_out.write_octet( (byte) 0);
        m_out.insertMsgSize();

        messages.add( m_out.getBufferCopy() );

        DummyTransport transport =
            new DummyTransport( messages );

        DummyRequestListener request_listener =
            new DummyRequestListener();

        DummyReplyListener reply_listener =
            new DummyReplyListener();

        GIOPConnectionManager giopconn_mg =
            new GIOPConnectionManager();
        try
        {
            giopconn_mg.configure (config);
        }
        catch (Exception e)
        {
        }

        ServerGIOPConnection conn =
            giopconn_mg.createServerGIOPConnection( null,
                                                    transport,
                                                    request_listener,
                                                    reply_listener );

        try
        {
            //will not return until an IOException is thrown (by the
            //DummyTransport)
            conn.receiveMessages();
        }
        catch( IOException e )
        {
            //o.k., thrown by DummyTransport
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Caught exception: " + e );
        }

        //did the GIOPConnection hand the complete request over to the
        //listener?
        assertTrue( request_listener.getRequest() != null );

        RequestInputStream r_in = new RequestInputStream
            ( orb, null, request_listener.getRequest() );

        //is the body correct?
        assertEquals( "barbaz", r_in.read_string() );
    }

    public void testGIOP_1_0_CorrectRefusing()
    {
        List<byte[]> messages = new Vector<byte[]>();

        RequestOutputStream r_out =
            new RequestOutputStream( orb, //ClientConnection
                                     null,           //request id
                                     0,       //operation
                                     "foo",        //response expected
                                     true,   //SYNC_SCOPE (irrelevant)
                                     (short)-1,        //request start time
                                     null,        //request end time
                                     null,        //reply end time
                                     null, //object key
                                     new byte[1], 0            // giop minor
                                     );

        r_out.write_string( "bar" );
        r_out.insertMsgSize();

        byte[] b = r_out.getBufferCopy();

        b[6] |= 0x02; //set "more fragments follow"

        messages.add( b );

        DummyTransport transport =
            new DummyTransport( messages );

        DummyRequestListener request_listener =
            new DummyRequestListener();

        DummyReplyListener reply_listener =
            new DummyReplyListener();

        GIOPConnectionManager giopconn_mg =
            new GIOPConnectionManager();
        try
        {
            giopconn_mg.configure (config);
        }
        catch (Exception e)
        {
        }

        GIOPConnection conn =
            giopconn_mg.createServerGIOPConnection( null,
                                                    transport,
                                                    request_listener,
                                                    reply_listener );

        try
        {
            //will not return until an IOException is thrown (by the
            //DummyTransport)
            conn.receiveMessages();
        }
        catch( IOException e )
        {
            //o.k., thrown by DummyTransport
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Caught exception: " + e );
        }

        //no request or reply must have been handed over
        assertTrue( request_listener.getRequest() == null );
        assertTrue( reply_listener.getReply() == null );

        //instead, an error message have must been sent via the
        //transport
        assertTrue( transport.getWrittenMessage() != null );

        byte[] result = transport.getWrittenMessage();

        assertTrue( Messages.getMsgType( result ) == MsgType_1_1._MessageError );
        MessageOutputStream m_out =
            new MessageOutputStream(orb);
        m_out.writeGIOPMsgHeader( MsgType_1_1._Fragment,
                                     0 // giop minor
                                     );
        m_out.write_ulong( 0 ); // Fragment Header (request id)
        m_out.write_octet( (byte) 'b' );
        m_out.write_octet( (byte) 'a' );
        m_out.write_octet( (byte) 'z' );
        m_out.insertMsgSize();

        messages.add( m_out.getBufferCopy() );

        try
        {
            //will not return until an IOException is thrown (by the
            //DummyTransport)
            conn.receiveMessages();
        }
        catch( IOException e )
        {
            //o.k., thrown by DummyTransport
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Caught exception: " + e );
        }

        //no request or reply must have been handed over
        assertTrue( request_listener.getRequest() == null );
        assertTrue( reply_listener.getReply() == null );

        //instead, an error message have must been sent via the
        //transport
        assertTrue( transport.getWrittenMessage() != null );

        //must be a new one
        assertTrue( transport.getWrittenMessage() != result );
        result = transport.getWrittenMessage();

        assertTrue( Messages.getMsgType( result ) == MsgType_1_1._MessageError );

    }

    public void testGIOP_1_1_IllegalMessageType()
    {
        List<byte[]> messages = new Vector<byte[]>();

        LocateRequestOutputStream r_out =
            new LocateRequestOutputStream(
                orb,
                new byte[1], //object key
                0,           //request id
                1            // giop minor
                );

        r_out.insertMsgSize();

        byte[] b = r_out.getBufferCopy();

        b[6] |= 0x02; //set "more fragments follow"

        messages.add( b );

//        MessageOutputStream m_out =
//            new MessageOutputStream();

        DummyTransport transport =
            new DummyTransport( messages );

        DummyRequestListener request_listener =
            new DummyRequestListener();

        DummyReplyListener reply_listener =
            new DummyReplyListener();

        GIOPConnectionManager giopconn_mg =
            new GIOPConnectionManager();
        try
        {
            giopconn_mg.configure (config);
        }
        catch (Exception e)
        {
        }

        GIOPConnection conn =
            giopconn_mg.createServerGIOPConnection( null,
                                                    transport,
                                                    request_listener,
                                                    reply_listener );

        try
        {
            //will not return until an IOException is thrown (by the
            //DummyTransport)
            conn.receiveMessages();
        }
        catch( IOException e )
        {
            //o.k., thrown by DummyTransport
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Caught exception: " + e );
        }

        //no request or reply must have been handed over
        assertTrue( request_listener.getRequest() == null );
        assertTrue( reply_listener.getReply() == null );

        //instead, an error message have must been sent via the
        //transport
        assertTrue( transport.getWrittenMessage() != null );

        byte[] result = transport.getWrittenMessage();

        assertTrue( Messages.getMsgType( result ) == MsgType_1_1._MessageError );
    }

    public void testGIOP_1_2_CorrectCloseOnGarbage()
    {
        List<byte[]> messages = new Vector<byte[]>();

        String garbage = "This is a garbage message";
        byte[] b = garbage.getBytes();

        messages.add( b );

        DummyTransport transport =
            new DummyTransport( messages );

        DummyRequestListener request_listener =
            new DummyRequestListener();

        DummyReplyListener reply_listener =
            new DummyReplyListener();

        GIOPConnectionManager giopconn_mg =
            new GIOPConnectionManager();
        try
        {
            giopconn_mg.configure (config);
        }
        catch (Exception e)
        {
        }

        GIOPConnection conn =
            giopconn_mg.createServerGIOPConnection( null,
                                                    transport,
                                                    request_listener,
                                                    reply_listener );

        try
        {
            //will not return until an IOException is thrown (by the
            //DummyTransport)
            conn.receiveMessages();
        }
        catch( IOException e )
        {
            //o.k., thrown by DummyTransport
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Caught exception: " + e );
        }

        //no request or reply must have been handed over
        assertTrue( request_listener.getRequest() == null );
        assertTrue( reply_listener.getReply() == null );

        //instead, connection should be closed
        assertTrue( transport.hasBeenClosed() );

        //no message is written (makes no real sense)
        assertTrue( transport.getWrittenMessage() != null );
        assertTrue( transport.getWrittenMessage().length == 0 );
    }

    public void testGIOP_1_1_CorrectRequest()
    {
        List<byte[]> messages = new Vector<byte[]>();

        RequestOutputStream r_out =
            new RequestOutputStream( orb, //ClientConnection
                                     (ClientConnection) null,           //request id
                                     0,       //operation
                                     "foo",        // response expected
                                     true,   // SYNC_SCOPE (irrelevant)
                                     (short)-1,        //request start time
                                     null,        //request end time
                                     null,        //reply start time
                                     null, //object key
                                     new byte[1], 1            // giop minor
                                     );

        String message = "Request";
        r_out.write_string(message);
        r_out.insertMsgSize();

        messages.add( r_out.getBufferCopy() );

        DummyTransport transport =
            new DummyTransport( messages );

        DummyRequestListener request_listener =
            new DummyRequestListener();

        DummyReplyListener reply_listener =
            new DummyReplyListener();

        GIOPConnectionManager giopconn_mg =
            new GIOPConnectionManager();
        try
        {
            giopconn_mg.configure (config);
        }
        catch (Exception e)
        {
        }

        ServerGIOPConnection conn =
            giopconn_mg.createServerGIOPConnection( null,
                                                    transport,
                                                    request_listener,
                                                    reply_listener );

        try
        {
            //will not return until an IOException is thrown (by the
            //DummyTransport)
            conn.receiveMessages();
        }
        catch( IOException e )
        {
            //o.k., thrown by DummyTransport
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Caught exception: " + e );
        }

        //did the GIOPConnection hand the complete request over to the
        //listener?
        assertTrue( request_listener.getRequest() != null );

        RequestInputStream r_in =
        new RequestInputStream( orb, null, request_listener.getRequest() );

        //is the body correct?
        assertEquals( message, r_in.read_string() );
    }

}// GIOPConnectionTest
