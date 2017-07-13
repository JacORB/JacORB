package org.jacorb.test.orb.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.jacorb.config.Configuration;
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
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Before;
import org.junit.Test;
import org.omg.ETF.BufferHolder;
import org.omg.ETF.Profile;
import org.omg.GIOP.MsgType_1_1;

/**
 * GIOPConnectionTest.java
 *
 * @author Nicolas Noffke
 */
public class GIOPConnectionTest extends ORBTestCase
{
    private Configuration config;

    @Before
    public void setUp()
        throws Exception
    {
        config = this.getORB().getConfiguration();
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
            getORB().getGIOPMinorVersion()
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

        @Override
        public void connect (org.omg.ETF.Profile profile, long time_out)
        {
            // nothing
        }

        public boolean hasBeenClosed()
        {
            return closed;
        }

        @Override
        public boolean is_connected()
        {
            return !closed;
        }

        @Override
        public void write( boolean is_first, boolean is_last,
                           byte[] message, int start, int size,
                           long timeout )
        {
            b_out.write( message, start, size );
        }


        @Override
        public void flush()
        {
        }

        @Override
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

        @Override
        public Profile get_server_profile()
        {
            return profile;
        }

        @Override
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

        @Override
        public boolean is_data_available()
        {
            return true;
        }

        @Override
        public boolean supports_callback()
        {
            return false;
        }

        @Override
        public boolean use_handle_time_out()
        {
            return false;
        }

        @Override
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

        @Override
        public void requestReceived( byte[] request,
                                     GIOPConnection connection )
        {
            this.request = request;
        }

        @Override
        public void locateRequestReceived( byte[] request,
                                           GIOPConnection connection )
        {
            this.request = request;
        }
        @Override
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

        @Override
        public void replyReceived( byte[] reply,
                                   GIOPConnection connection )
        {
            this.reply = reply;
        }

        @Override
        public void locateReplyReceived( byte[] reply,
                                         GIOPConnection connection )
        {
            this.reply = reply;
        }

        @Override
        public void closeConnectionReceived( byte[] close_conn,
                                             GIOPConnection connection )
        {
            this.reply = close_conn;
        }

    }

    @Test
    public void testGIOP_1_2_CorrectFragmentedRequest()
    {
        List<byte[]> messages = new Vector<byte[]>();

        RequestOutputStream r_out =
            new RequestOutputStream( getORB(), //ClientConnection
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

        conn.receiveMessages();

        //did the GIOPConnection hand the complete request over to the
        //listener?
        assertTrue( request_listener.getRequest() != null );

        RequestInputStream r_in = new RequestInputStream
            ( getORB(), null, request_listener.getRequest() );

        //is the body correct?
        assertEquals( "barbaz", r_in.read_string() );

        r_out.close();
        r_in.close();
        m_out.close();
    }

    @Test
    public void testGIOP_1_0_CorrectRefusing()
    {
        List<byte[]> messages = new Vector<byte[]>();

        RequestOutputStream r_out =
            new RequestOutputStream( getORB(), //ClientConnection
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

        conn.receiveMessages();

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

        conn.receiveMessages();

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

        r_out.close();
        m_out.close();
    }

    @Test
    public void testGIOP_1_1_IllegalMessageType()
    {
        List<byte[]> messages = new Vector<byte[]>();

        LocateRequestOutputStream r_out =
            new LocateRequestOutputStream(
                getORB(),
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

        conn.receiveMessages();

        //no request or reply must have been handed over
        assertTrue( request_listener.getRequest() == null );
        assertTrue( reply_listener.getReply() == null );

        //instead, an error message have must been sent via the
        //transport
        assertTrue( transport.getWrittenMessage() != null );

        byte[] result = transport.getWrittenMessage();

        assertTrue( Messages.getMsgType( result ) == MsgType_1_1._MessageError );

        r_out.close();
    }

    @Test
    public void testGIOP_1_1_NoImplement()
    {
        List<byte[]> messages = new Vector<byte[]>();

        RequestOutputStream r_out =
            new RequestOutputStream( getORB(), //ClientConnection
                                     null,           //request id
                                     0,       //operation
                                     "foo",        //response expected
                                     true,   //SYNC_SCOPE (irrelevant)
                                     (short)-1,        //request start time
                                     null,        //request end time
                                     null,        //reply end time
                                     null, //object key
                                     new byte[1], 1            // giop minor
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

        conn.receiveMessages();

        //no request or reply must have been handed over
        assertTrue( request_listener.getRequest() == null );
        assertTrue( reply_listener.getReply() == null );

        //instead, an error message have must been sent via the
        //transport
        assertTrue( transport.getWrittenMessage() != null );

        byte[] result = transport.getWrittenMessage();

        ReplyInputStream r_in = new ReplyInputStream( getORB(), result );

        Exception ex = r_in.getException();
        if ( ex != null && ex.getClass() == org.omg.CORBA.NO_IMPLEMENT.class )
        {
            // o.k.
        }
        else
        {
            fail();
        }

        MessageOutputStream m_out =
            new MessageOutputStream(orb);
        m_out.writeGIOPMsgHeader( MsgType_1_1._Fragment,
                                  1 // giop minor
                                  );
        m_out.write_ulong( 0 ); // Fragment Header (request id)
        m_out.write_octet( (byte) 'b' );
        m_out.write_octet( (byte) 'a' );
        m_out.write_octet( (byte) 'z' );
        m_out.insertMsgSize();

        messages.add( m_out.getBufferCopy() );

        conn.receiveMessages();

        //no request or reply must have been handed over
        assertTrue( request_listener.getRequest() == null );
        assertTrue( reply_listener.getReply() == null );

        //can't check more, message is discarded
        m_out.close();
        r_out.close();
        r_in.close();
    }

    @Test
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

        conn.receiveMessages();

        //no request or reply must have been handed over
        assertTrue( request_listener.getRequest() == null );
        assertTrue( reply_listener.getReply() == null );

        //instead, connection should be closed
        assertTrue( transport.hasBeenClosed() );

        //no message is written (makes no real sense)
        assertTrue( transport.getWrittenMessage() != null );
        assertTrue( transport.getWrittenMessage().length == 0 );
    }

    @Test
    public void testGIOP_1_1_CorrectRequest()
    {
        List<byte[]> messages = new Vector<byte[]>();

        RequestOutputStream r_out =
            new RequestOutputStream( getORB(), //ClientConnection
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

        conn.receiveMessages();

        //did the GIOPConnection hand the complete request over to the
        //listener?
        assertTrue( request_listener.getRequest() != null );

        RequestInputStream r_in =
        new RequestInputStream( getORB(), null, request_listener.getRequest() );

        //is the body correct?
        assertEquals( message, r_in.read_string() );

        r_out.close();
        r_in.close();
    }

}// GIOPConnectionTest
