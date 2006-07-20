package org.jacorb.test.bugs.bugjac181;

import org.jacorb.orb.iiop.IIOPConnection;
import org.jacorb.orb.listener.TCPConnectionEvent;
import org.jacorb.orb.listener.TCPConnectionListener;


/**
 * <code>TCPListener</code> is a simple implementation of a listener.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class TCPListener implements TCPConnectionListener
{
    private static boolean listenerOpen = false;
    private static boolean listenerClose = false;
    private static boolean isCorrectType = true;

    /**
     * <code>connectionOpened</code> will be called whenever a socket
     * is opened.
     *
     * @param e a <code>TCPConenctionEvent</code> value
     */
    public void connectionOpened(TCPConnectionEvent e)
    {
        validateEvent(e);

        setListenerOpen(true);
    }

    /**
     * <code>connectionClosed</code> will be called whenever a socket
     * is closed.
     *
     * @param e a <code>TCPConenctionEvent</code> value
     */
    public void connectionClosed(TCPConnectionEvent e)
    {
        validateEvent(e);

        setListenerClose(true);
    }

    public static synchronized boolean isListenerClose()
    {
        return listenerClose;
    }

    public static synchronized void setListenerClose(boolean listenerClose)
    {
        TCPListener.listenerClose = listenerClose;
    }

    public static synchronized boolean isListenerOpen()
    {
        return listenerOpen;
    }

    public static synchronized void setListenerOpen(boolean listenerOpen)
    {
        TCPListener.listenerOpen = listenerOpen;
    }

    public static synchronized void reset()
    {
        listenerClose = false;
        listenerOpen = false;
        isCorrectType = true;
    }

    public boolean isListenerEnabled()
    {
        return true;
    }

    public static boolean isEventOfCorrectType()
    {
        return isCorrectType;
    }

    private void validateEvent(TCPConnectionEvent e)
    {
        if (! (e.getSource() instanceof IIOPConnection) )
        {
            isCorrectType = false;
        }
    }
}
