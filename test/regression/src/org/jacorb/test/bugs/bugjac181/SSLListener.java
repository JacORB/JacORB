package org.jacorb.test.bugs.bugjac181;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.orb.listener.SSLSessionListener;
import org.jacorb.orb.listener.SSLSessionEvent;

/**
 * <code>SSLListener</code> is a simple implementation of a listener.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class SSLListener implements SSLSessionListener
{
    Logger logger = null;

    public void sessionCreated(SSLSessionEvent e)
    {
        logger.debug("### Received sessionCreated message " + e);
    }

    public void handshakeException(SSLSessionEvent e)
    {
        logger.debug("### Received handshakeException message " + e);
    }
    public void keyException(SSLSessionEvent e)
    {
        logger.debug("### Received keyException message " + e);
    }
    public void peerUnverifiedException(SSLSessionEvent e)
    {
        logger.debug("### Received peerUnverifiedException message " + e);
    }
    public void protocolException(SSLSessionEvent e)
    {
        logger.debug("### Received protocolException message " + e);
    }
    public void sslException(SSLSessionEvent e)
    {
        logger.debug("### Received sslException message " + e);
    }
}
