package org.jacorb.test.orb.factory;

import java.net.Socket;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.NullLogger;
import org.easymock.MockControl;
import org.jacorb.config.Configuration;
import org.jacorb.orb.factory.PortRangeSocketFactory;
import org.jacorb.orb.factory.SocketFactory;

public class PortRangeSocketFactoryTest extends AbstractSocketFactoryTestCase
{
    private static final int MIN = 40000;
    private static final int MAX = 40010;

    protected SocketFactory newObjectUnderTest() throws ConfigurationException
    {
        final PortRangeSocketFactory factory = new PortRangeSocketFactory();

        MockControl configControl = MockControl.createControl(Configuration.class);
        Configuration configMock = (Configuration) configControl.getMock();

        configControl.expectAndReturn(configMock.getNamedLogger("jacorb.orb.socketfactory"), new NullLogger());
        configControl.expectAndReturn(configMock.getAttributeAsInteger(PortRangeSocketFactory.MIN_PROP), MIN);
        configControl.expectAndReturn(configMock.getAttributeAsInteger(PortRangeSocketFactory.MAX_PROP), MAX);
        configControl.replay();

        factory.configure(configMock);

        return factory;
    }

    public void testPortsAreCreatedInCorrectRange() throws Exception
    {
        Socket socket = objectUnderTest.createSocket(hostname, serverPort);
        assertTrue(socket.getLocalPort() >= MIN);
        assertTrue(socket.getLocalPort() <= MAX);
    }
}
