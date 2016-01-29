package org.jacorb.test.orb.giop;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;

import org.jacorb.config.Configuration;
import org.jacorb.orb.giop.ClientGIOPConnection;
import org.jacorb.orb.giop.GIOPConnection;
import org.jacorb.orb.giop.Messages;
import org.jacorb.orb.giop.ReplyListener;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.omg.ETF.BufferHolder;
import org.omg.ETF.Connection;
import org.omg.ETF.Profile;
import org.slf4j.LoggerFactory;

public class GIOPConnectionTest extends ORBTestCase
{
    @Test
    public void testCorrectFragmentedResponseSize() throws Exception
    {
        // Prepare data on the wire

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write("GIOP".getBytes());       // magic
        baos.write(1);                       // major: 1
        baos.write(2);                       // minor: 2
        baos.write(2);                       // more fragments, big endian (0000 0010)
        baos.write(1);                       // type: Response
        baos.write(new byte[]{0, 0, 0, 11}); // size
        baos.write(new byte[]{0, 0, 0, 0});  // request ID
        baos.write(new byte[]{0, 0, 0, 0});  // no service context
        baos.write("foo".getBytes());        // content

        baos.write("GIOP".getBytes());       // magic
        baos.write(1);                       // major: 1
        baos.write(2);                       // minor: 2
        baos.write(1);                       // no more fragments, little endian (0000 0001)
        baos.write(7);                       // type: Fragment
        baos.write(new byte[]{10, 0, 0, 0}); // size
        baos.write(new byte[]{0, 0, 0, 0});  // request ID
        baos.write("barbaz".getBytes());     // content

        final byte[] buffer = baos.toByteArray();

        Connection transport = mock(Connection.class);
        when(transport.is_connected())
            .thenReturn(true);
        when(transport.read(any(BufferHolder.class), anyInt(), anyInt(), anyInt(), anyLong()))
            .thenAnswer(new TransportAnswer(buffer));

        Configuration config = mock(Configuration.class);
        when(config.getORB())
            .thenReturn(getORB());
        when(config.getLogger(anyString()))
            .thenReturn(LoggerFactory.getLogger(GIOPConnectionTest.class));
        when(config.getAttributeAsBoolean(matches(".*\\.disconnect_after_systemexception"), anyBoolean()))
            .thenReturn(true);

        ReplyListener listener = mock(ReplyListener.class);

        GIOPConnection conn = new ClientGIOPConnection(mock(Profile.class), transport, null, listener, null);
        conn.configure(config);

        // Read the wire
        conn.receiveMessages();

        // Single message received, correct size
        ArgumentCaptor<byte[]> arg = ArgumentCaptor.forClass(byte[].class);

        verify(listener)
            .replyReceived(arg.capture(), eq(conn));

        assertEquals(1,  Messages.getMsgType(arg.getValue()));
        assertEquals(17, Messages.getMsgSize(arg.getValue()));
    }

    static class TransportAnswer implements Answer<Integer>
    {
        TransportAnswer(byte[] buffer)
        {
            this.buffer = buffer;
        }

        @Override
        public Integer answer(InvocationOnMock inv) throws Throwable
        {
            if (buffer.length <= readIndex)
                throw new org.omg.CORBA.COMM_FAILURE();

            BufferHolder holder = inv.getArgumentAt(0, BufferHolder.class);
            int offset          = inv.getArgumentAt(1, Integer.class);
            int minLength       = inv.getArgumentAt(2, Integer.class);

            System.arraycopy(buffer, readIndex, holder.value, offset, minLength);
            readIndex += minLength;

            return minLength;
        }

        private byte[] buffer;
        int readIndex = 0;
    }
}
