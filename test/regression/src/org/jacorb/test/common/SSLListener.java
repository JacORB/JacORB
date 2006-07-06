package org.jacorb.test.common;

import org.jacorb.orb.listener.SSLSessionEvent;
import org.jacorb.orb.listener.SSLSessionListener;

/**
 * <code>SSLListener</code> is a simple implementation of a listener.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class SSLListener implements SSLSessionListener
{
    public void sessionCreated(SSLSessionEvent e)
    {
        debug("### Received sessionCreated message " + e);
    }

    public void handshakeException(SSLSessionEvent e)
    {
        debug("### Received handshakeException message " + e);
    }
    public void keyException(SSLSessionEvent e)
    {
        debug("### Received keyException message " + e);
    }
    public void peerUnverifiedException(SSLSessionEvent e)
    {
        debug("### Received peerUnverifiedException message " + e);
    }
    public void protocolException(SSLSessionEvent e)
    {
        debug("### Received protocolException message " + e);
    }
    public void sslException(SSLSessionEvent e)
    {
        debug("### Received sslException message " + e);
    }

    private static void debug(String mesg)
    {
        TestUtils.log(mesg);
    }
}
