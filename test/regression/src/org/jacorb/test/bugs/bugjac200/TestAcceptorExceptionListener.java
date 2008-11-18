package org.jacorb.test.bugs.bugjac200;

import javax.net.ssl.SSLException;
import org.jacorb.orb.listener.AcceptorExceptionEvent;
import org.jacorb.util.ObjectUtil;

/**
 * @author Nicolas Noffke
 */
public class TestAcceptorExceptionListener
    implements org.jacorb.orb.listener.AcceptorExceptionListener
{
    public static volatile boolean hasBeenCreated;
    public static volatile boolean doShutdown;
    private static boolean hasBeenCalled;
    private static final Object lock = new Object();

    public TestAcceptorExceptionListener()
    {
        hasBeenCreated = true;
    }

    /**
     * Throwable <code>th</code> has been caught by the acceptor thread.
     */
    public void exceptionCaught(AcceptorExceptionEvent exception)
    {
        exception.getException().printStackTrace();

        if ((exception.getException() instanceof Error) ||
            (exception.getException() instanceof SSLException))
        {
            if (doShutdown)
            {
                exception.getORB().shutdown(true);
            }
        }

        synchronized (lock)
        {
            hasBeenCalled = true;
            lock.notifyAll();
        }
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
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
    }
}
