package org.jacorb.test.orb.connection;

/**
 * GIOPConnectionTest.java
 *
 *
 * Created: Sat Jun 22 14:26:15 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

import org.jacorb.orb.connection.*;
import org.jacorb.orb.iiop.*;

import java.io.*;
import java.util.*;

import org.omg.ETF.BufferHolder;
import org.omg.ETF.Profile;
import org.omg.GIOP.*;
import org.jacorb.orb.*;

import junit.framework.*;

public class GIOPConnectionTest extends TestCase
{
    public static junit.framework.TestSuite suite()
    {
        TestSuite suite = new TestSuite ("GIOPConnection Test");

        suite.addTest (new GIOPConnectionTest ("testGIOP_1_2_CorrectFragmentedRequest"));
        suite.addTest (new GIOPConnectionTest ("testGIOP_1_0_CorrectRefusing"));
        suite.addTest (new GIOPConnectionTest ("testGIOP_1_1_IllegalMessageType"));
        suite.addTest (new GIOPConnectionTest ("testGIOP_1_1_NoImplement"));

        return suite;
    }

    private class DummyTransport
        extends IIOPConnection
    {
        private boolean closed = false;
        private byte[] data = null;
        private int index = 0;
        private ByteArrayOutputStream b_out = new ByteArrayOutputStream();
        private org.omg.ETF.Profile profile = new IIOPProfile
        (
            new IIOPAddress ("127.0.0.1", 4711),
            null
        );

        public DummyTransport( List messages )
        {
            // convert the message list into a plain byte array

            int size = 0;
            for (Iterator i = messages.iterator(); i.hasNext();)
            {
                size += ((byte[])i.next()).length;
            }
            data = new byte[size];
            int index = 0;
            for (Iterator i = messages.iterator(); i.hasNext();)
            {
                byte[] msg = (byte[])i.next();
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

        public void read (BufferHolder data, int offset,
                          int min_length, int max_length, long time_out)
        {
            if (this.index + min_length > this.data.length)
            {
                throw new org.omg.CORBA.COMM_FAILURE ("end of stream");
            }
            else
            {
                System.arraycopy (this.data, this.index, data.value, offset, min_length);
                this.index += min_length;
            }
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

    public void testGIOP_1_2_CorrectFragmentedRequest()
    {
        List messages = new Vector();

        RequestOutputStream r_out =
            new RequestOutputStream( (ClientConnection) null, //ClientConnection
                                     0,           //request id
                                     "foo",       //operation
                                     true,        // response expected
                                     (short)-1,   // SYNC_SCOPE (irrelevant)
                                     null,        //request start time
                                     null,        //request end time
                                     null,        //reply start time
                                     new byte[1], //object key
                                     2            // giop minor
                                     );

        //manually write the first half of the string "barbaz"
        r_out.write_ulong( 6 ); //string length
        r_out.write_octet( (byte) 'b' );
        r_out.write_octet( (byte) 'a' );
        r_out.write_octet( (byte) 'r' );
        r_out.insertMsgSize();

        byte[] b = r_out.getBufferCopy();

        b[6] |= 0x02; //set "more fragments follow"

        messages.add( b );

        MessageOutputStream m_out =
            new MessageOutputStream();
        m_out.writeGIOPMsgHeader( MsgType_1_1._Fragment,
                                     2 // giop minor
                                     );
        m_out.write_ulong( 0 ); // Fragment Header (request id)
        m_out.write_octet( (byte) 'b' );
        m_out.write_octet( (byte) 'a' );
        m_out.write_octet( (byte) 'z' );
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
            new RequestInputStream( null, request_listener.getRequest() );

        //is the body correct?
        assertEquals( "barbaz", r_in.read_string() );
    }

    public void testGIOP_1_0_CorrectRefusing()
    {
        List messages = new Vector();

        RequestOutputStream r_out =
            new RequestOutputStream( null, //ClientConnection
                                     0,           //request id
                                     "foo",       //operation
                                     true,        //response expected
                                     (short)-1,   //SYNC_SCOPE (irrelevant)
                                     null,        //request start time
                                     null,        //request end time
                                     null,        //reply end time
                                     new byte[1], //object key
                                     0            // giop minor
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
            new MessageOutputStream();
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
        List messages = new Vector();

        LocateRequestOutputStream r_out =
            new LocateRequestOutputStream(
                new byte[1], //object key
                0,           //request id
                1            // giop minor
                );

        r_out.insertMsgSize();

        byte[] b = r_out.getBufferCopy();

        b[6] |= 0x02; //set "more fragments follow"

        messages.add( b );

        MessageOutputStream m_out =
            new MessageOutputStream();

        DummyTransport transport =
            new DummyTransport( messages );

        DummyRequestListener request_listener =
            new DummyRequestListener();

        DummyReplyListener reply_listener =
            new DummyReplyListener();

        GIOPConnectionManager giopconn_mg =
            new GIOPConnectionManager();

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

    public void testGIOP_1_1_NoImplement()
    {
        List messages = new Vector();

        RequestOutputStream r_out =
            new RequestOutputStream( null, //ClientConnection
                                     0,           //request id
                                     "foo",       //operation
                                     true,        //response expected
                                     (short)-1,   //SYNC_SCOPE (irrelevant)
                                     null,        //request start time
                                     null,        //request end time
                                     null,        //reply end time
                                     new byte[1], //object key
                                     1            // giop minor
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

        ReplyInputStream r_in = new ReplyInputStream( null, result );

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
            new MessageOutputStream();
        m_out.writeGIOPMsgHeader( MsgType_1_1._Fragment,
                                  1 // giop minor
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

        //can't check more, message is discarded
    }
}// GIOPConnectionTest
