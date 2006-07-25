package org.jacorb.test.orb.factory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Properties;

import junit.framework.TestCase;

import org.jacorb.orb.factory.SocketFactory;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public abstract class AbstractSocketFactoryTest extends TestCase
{
    protected final byte[] sent = new byte[] {1,2,3,4};
    protected final byte[] received = new byte[sent.length];

    protected ServerSocket serverSocket;
    protected SocketFactory objectUnderTest;
    private Thread thread;
    protected String hostname;
    protected int serverPort;

    protected final void setUp() throws Exception
    {
        Properties props = getPropertiesForTest(getName());
        if (props != null)
        {
        }

        objectUnderTest = newObjectUnderTest();

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
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
        thread.start();

        InetAddress address = serverSocket.getInetAddress();

        hostname = address.getHostName();
        serverPort = serverSocket.getLocalPort();

        doSetup();
    }

    protected Properties getPropertiesForTest(String name)
    {
        return null;
    }

    protected void doSetup()
    {
    }

    protected abstract SocketFactory newObjectUnderTest() throws Exception;

    protected final void tearDown() throws Exception
    {
        thread.interrupt();
        serverSocket.close();
    }

    protected void checkSocketIsConnected(Socket socket) throws IOException, InterruptedException
    {
        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(sent);
        outputStream.flush();
        outputStream.close();

        Thread.sleep(1000);

        assertTrue(Arrays.equals(sent, received));
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
}
