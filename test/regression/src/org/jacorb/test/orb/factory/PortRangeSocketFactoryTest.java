package org.jacorb.test.orb.factory;

import java.net.Socket;
import java.util.Properties;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.NullLogger;
import org.easymock.MockControl;
import org.jacorb.config.Configuration;
import org.jacorb.orb.factory.PortRangeSocketFactory;
import org.jacorb.orb.factory.SocketFactory;

public class PortRangeSocketFactoryTest extends AbstractSocketFactoryTest
{
    protected SocketFactory newObjectUnderTest() throws ConfigurationException
    {
        final PortRangeSocketFactory factory = new PortRangeSocketFactory();

        MockControl configControl = MockControl
                .createControl(Configuration.class);
        Configuration configMock = (Configuration) configControl.getMock();

        configControl.expectAndReturn(configMock.getNamedLogger("jacorb.orb.socketfactory"), new NullLogger());
        configControl.expectAndReturn(configMock.getAttributeAsInteger(PortRangeSocketFactory.MIN_PROP), 40000);
        configControl.expectAndReturn(configMock.getAttributeAsInteger(PortRangeSocketFactory.MAX_PROP), 40005);
        configControl.replay();

        factory.configure(configMock);

        return factory;
    }

    public void testPortsAreCreatedInCorrectRange() throws Exception
    {
        Socket socket = objectUnderTest.createSocket(hostname, serverPort);
        assertTrue(socket.getLocalPort() >= 40000);
        assertTrue(socket.getLocalPort() <= 40005);
    }
}
