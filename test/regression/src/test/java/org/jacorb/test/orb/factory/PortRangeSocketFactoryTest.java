package org.jacorb.test.orb.factory;

import java.net.Socket;
import javax.net.ssl.SSLSocket;
import org.easymock.MockControl;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.factory.PortRangeSocketFactory;
import org.jacorb.orb.factory.SocketFactory;
import org.jacorb.test.harness.TestUtils;
import org.junit.Test;

public class PortRangeSocketFactoryTest extends AbstractSocketFactoryTestCase
{
    private static final int MIN = 40000;
    private static final int MAX = 40010;

    @Override
    protected SocketFactory newObjectUnderTest() throws ConfigurationException
    {
        final PortRangeSocketFactory factory = new PortRangeSocketFactory();

        MockControl configControl = MockControl.createControl(Configuration.class);
        Configuration configMock = (Configuration) configControl.getMock();

        configControl.expectAndReturn(configMock.getLogger("org.jacorb.orb.socketfactory"), TestUtils.getLogger());
        configControl.expectAndReturn(configMock.getAttributeAsInteger(PortRangeSocketFactory.MIN_PROP), MIN);
        configControl.expectAndReturn(configMock.getAttributeAsInteger(PortRangeSocketFactory.MAX_PROP), MAX);
        configControl.expectAndReturn(configMock.getAttribute("OAIAddr", ""), "");
        configControl.replay();

        factory.configure(configMock);

        return factory;
    }

    @Test
    public void testPortsAreCreatedInCorrectRange() throws Exception
    {
        Socket socket = objectUnderTest.createSocket(hostname, serverPort);

        assertTrue(socket.getLocalPort() >= MIN);
        assertTrue(socket.getLocalPort() <= MAX);
        if ( ! (socket instanceof SSLSocket) && ! socket.isClosed ())
        {
           socket.shutdownOutput ();
        }
        socket.close ();
    }
}
