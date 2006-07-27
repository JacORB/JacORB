package org.jacorb.test.bugs.bugjac200;

import org.jacorb.orb.listener.AcceptorExceptionEvent;
import org.jacorb.util.ObjectUtil;

/**
 * @author Nicolas Noffke
 */
public class TestAcceptorExceptionListener
    implements org.jacorb.orb.listener.AcceptorExceptionListener
{
    static boolean hasBeenCreated;
    private static boolean hasBeenCalled;
    static boolean doShutdown;
    static boolean shuttingDown;
    private static final Object lock = new Object();

    Class sslException;

    public TestAcceptorExceptionListener()
    {
        hasBeenCreated = true;

        try
        {
            sslException =
                ObjectUtil.classForName(
                    "javax.net.ssl.SSLException");
        }
        catch(ClassNotFoundException cnf) {} // NOPMD
    }

    /**
     * Throwable <code>th</code> has been caught by the acceptor thread.
     */
    public void exceptionCaught(AcceptorExceptionEvent exception)
    {
        if ((exception.getException() instanceof Error) ||
            (sslException != null && sslException.isInstance(exception.getException())))
        {
            if (doShutdown)
            {
                shuttingDown = true;
                exception.getORB().shutdown(true);
            }
        }

        synchronized (lock)
        {
            hasBeenCalled = true;
            lock.notifyAll();
        }
    }

    public static boolean getHasBeenCalled(long timeout, boolean expected)
    {
        final long waitUntil = System.currentTimeMillis() + timeout;
        synchronized (lock)
        {
            while(expected != hasBeenCalled && System.currentTimeMillis() < waitUntil)
            {
                try
                {
                    lock.wait(1000);
                }
                catch (InterruptedException e)
                {
                }
            }
        }
        return hasBeenCalled;
    }

    public static void reset()
    {
        hasBeenCreated = false;
        hasBeenCalled  = false;
        doShutdown     = false;
        shuttingDown   = false;
    }
}
