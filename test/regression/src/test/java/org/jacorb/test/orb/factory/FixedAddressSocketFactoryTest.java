package org.jacorb.test.orb.factory;

import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import org.easymock.MockControl;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.orb.factory.FixedAddressSocketFactory;
import org.jacorb.orb.factory.SocketFactory;
import org.jacorb.test.harness.TestUtils;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */
public class FixedAddressSocketFactoryTest extends AbstractSocketFactoryTestCase
{
    @Override
    protected SocketFactory newObjectUnderTest()
    {
        return new FixedAddressSocketFactory();
    }

    @Test
    public void testSetLocalhost() throws Exception
    {
        Socket socket = objectUnderTest.createSocket(hostname, serverPort);
        checkSocketIsConnected(socket);
        if ( ! (socket instanceof SSLSocket) && ! socket.isClosed ())
        {
           socket.shutdownOutput ();
        }
        socket.close ();
    }

    @Test
    public void testSetLocalhost2() throws Exception
    {
        Socket socket = objectUnderTest.createSocket(hostname, serverPort, 1000);
        checkSocketIsConnected(socket);
        if ( ! (socket instanceof SSLSocket) && ! socket.isClosed ())
        {
           socket.shutdownOutput ();
        }
        socket.close ();
    }

    @Test
    public void testSetHostname() throws Exception
    {
        Socket socket = objectUnderTest.createSocket(hostname, serverPort);
        checkSocketIsConnected(socket);
        if ( ! (socket instanceof SSLSocket) && ! socket.isClosed ())
        {
           socket.shutdownOutput ();
        }
        socket.close ();
    }

    @Test
    public void testSetHostname2() throws Exception
    {
        Socket socket = objectUnderTest.createSocket(hostname, serverPort, 1000);
        checkSocketIsConnected(socket);
        if ( ! (socket instanceof SSLSocket) && ! socket.isClosed ())
        {
           socket.shutdownOutput ();
        }
        socket.close ();
    }

    @Override
    protected void configureObjectUnderTest(String name, Configurable configurable) throws Exception
    {
        MockControl configControl = MockControl
            .createControl(Configuration.class);
        Configuration configMock = (Configuration) configControl.getMock();
        configControl.expectAndReturn(configMock.getLogger("org.jacorb.orb.socketfactory"), TestUtils.getLogger());
        if (name.startsWith("testSetLocalhost"))
        {
            configControl.expectAndReturn(configMock.getAttribute("OAIAddr", ""), "localhost");
        }
        else if (name.startsWith("testSetHostname"))
        {
            configControl.expectAndReturn(configMock.getAttribute("OAIAddr", ""), InetAddress.getLocalHost().getCanonicalHostName());
        }
        else
        {
            configControl.expectAndReturn(configMock.getAttribute("OAIAddr", ""), "");
        }

        configControl.replay();

        configurable.configure(configMock);

        configControl.verify();
    }
}
