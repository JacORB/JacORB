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

import org.jacorb.orb.*;
import org.jacorb.orb.connection.*;

import java.io.*;
import java.util.*;

import org.omg.GIOP.*;

public class GIOPConnectionTest 
    extends junit.framework.TestCase 
{
    public static junit.framework.TestSuite suite()
    {
      return new junit.framework.TestSuite( GIOPConnectionTest.class );
    }

    private class DummyTransport
        implements Transport
    {
        private boolean closed = false;
        private List messages = null;
        private int messages_index = 0;
        private ByteArrayOutputStream b_out = new ByteArrayOutputStream();

        public DummyTransport( List messages )
        {
            this.messages = messages;
        }

        public byte[] getWrittenMessage()
        {
            return b_out.toByteArray();
        }

        public boolean hasBeenClosed()
        {
            return closed;
        }

        public byte[] getMessage()
            throws IOException
        {
            if( messages_index < messages.size() )
            {
                return (byte[]) messages.get( messages_index++ );
            }
            else
            {
                throw new CloseConnectionException();
            }
        }

        public void write( byte[] message, int start, int size )
            throws IOException
        {
            b_out.write( message, start, size );
        }
    

        public void flush()
            throws IOException
        {
        }
    
        public void close()
            throws IOException
        {
            closed = true;
        }

        public void setIdle()
        {
        }

        public void setBusy()
        {
        }

        public boolean isIdle()
        {
            return false;
        }
    
        public boolean isSSL()
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
    
    public void testGIOP_1_2_CorrectFragmentedRequest()
    {
        List messages = new Vector();

        RequestOutputStream r_out = 
            new RequestOutputStream( (ClientConnection) null, //ClientConnection
                                     0,           //request id
                                     "foo",       //operation
                                     true,        //response expected
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
        
        GIOPConnection conn = new GIOPConnection( transport,
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
        
        GIOPConnection conn = new GIOPConnection( transport,
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
        
        GIOPConnection conn = new GIOPConnection( transport,
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
        
        GIOPConnection conn = new GIOPConnection( transport,
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




