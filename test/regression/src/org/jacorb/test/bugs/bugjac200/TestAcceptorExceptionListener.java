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
    static boolean hasBeenCalled;
    static boolean doShutdown;
    static boolean shuttingDown;

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
        hasBeenCalled = true;

        if ((exception.getException() instanceof Error) ||
            (sslException != null && sslException.isInstance(exception.getException())))
        {
            if (doShutdown)
            {
                shuttingDown = true;
                exception.getORB().shutdown(true);
            }
        }
    }
}
