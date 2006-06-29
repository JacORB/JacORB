package org.jacorb.test.bugs.bugjac195;

import org.jacorb.orb.listener.TCPConnectionListener;
import org.jacorb.orb.listener.TCPConnectionEvent;

/**
 * <code>TCPListener</code> is a simple implementation of a listener.
 */
public class TCPListener implements TCPConnectionListener
{
    public void connectionOpened(TCPConnectionEvent e)
    {
        JAC195ServerImpl.connectionsOpened++;
    }

    public void connectionClosed (TCPConnectionEvent e)
    {
        JAC195ServerImpl.connectionsClosed++;
    }

    public boolean isListenerEnabled()
    {
        return true;
    }
}
