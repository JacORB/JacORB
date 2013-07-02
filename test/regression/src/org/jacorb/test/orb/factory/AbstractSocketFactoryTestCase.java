package org.jacorb.test.orb.factory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import javax.net.ssl.SSLSocket;
import junit.framework.TestCase;
import org.jacorb.config.Configurable;
import org.jacorb.orb.factory.SocketFactory;
import org.omg.CORBA.TIMEOUT;

/**
 * @author Alphonse Bendt
 */
public abstract class AbstractSocketFactoryTestCase extends TestCase
{
    protected final byte[] sent = new byte[] {'a', 'b', 'c', 'd'};
    protected final byte[] received = new byte[sent.length];

    protected ServerSocket serverSocket;
    protected SocketFactory objectUnderTest;
    private Thread thread;
    protected String hostname;
    protected int serverPort;
    private boolean socketClosed;
    private Exception socketException;

    protected final void setUp() throws Exception
    {
        objectUnderTest = newObjectUnderTest();

        if (objectUnderTest instanceof Configurable)
        {
            configureObjectUnderTest(getName(), (Configurable)objectUnderTest);
        }

        serverSocket = new ServerSocket();
        serverSocket.bind(null);
        assertFalse(serverSocket.isClosed());
        assertTrue(serverSocket.isBound());

        thread = new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            Socket socket = serverSocket.accept();
                            InputStream in = socket.getInputStream();

                            int x;
                            int pos = 0;
                            while( (x = in.read()) != -1)
                            {
                                received[pos++] = (byte) x;
                            }

                            if ( ! (socket instanceof SSLSocket) && ! socket.isClosed ())
                            {
                               socket.shutdownOutput ();
                            }
                            socket.close ();
                        }
                        catch (Exception e)
                        {
                            if (!socketClosed)
                            {
                                socketException = e;
                            }
                        }
                    }
                };
        thread.start();

        InetAddress address = serverSocket.getInetAddress();

        hostname = address.getHostName();
        serverPort = serverSocket.getLocalPort();

        doSetup();
    }

    protected void configureObjectUnderTest(String name, Configurable configurable) throws Exception
    {
    }

    protected void doSetup()
    {
    }

    protected abstract SocketFactory newObjectUnderTest() throws Exception;

    protected final void tearDown() throws Exception
    {
        Thread.sleep(2000);

        try
        {
            assertNull(socketException);
        }
        finally
        {
            socketClosed = true;
            serverSocket.close();
            thread.interrupt();
        }
    }

    protected void checkSocketIsConnected(Socket socket) throws IOException, InterruptedException
    {
        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(sent);
        outputStream.flush();
        outputStream.close();

        Thread.sleep(1000);

        assertTrue(new String(sent) + " != " + new String(received), Arrays.equals(sent, received));
    }

    public final void testConnect() throws Exception
    {
        Socket socket = objectUnderTest.createSocket(hostname, serverPort);
        checkSocketIsConnected(socket);
    }

    public final void testConnectWithTimeout() throws Exception
    {
        Socket socket = objectUnderTest.createSocket(hostname, serverPort, 100);
        checkSocketIsConnected(socket);
    }

    public final void testConnectToNonExistentPortWithTimeout() throws Exception
    {
        try
        {
            // NOTE we expect the connection to the specified host
            // to timeout! if this is not the case the test will
            // fail and another host/port combination must be used.
            objectUnderTest.createSocket("10.1.0.222", 45000, 1);
        }
        catch (TIMEOUT e)
        {
            // expected
        }
    }
}
