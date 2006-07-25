package org.jacorb.test.orb.factory;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.apache.avalon.framework.logger.NullLogger;
import org.jacorb.orb.factory.AbstractSocketFactory;
import org.omg.CORBA.TIMEOUT;

public class JDK13AbstractSocketFactoryTest extends TestCase
{
    private AbstractSocketFactory objectUnderTest;
    private boolean shouldBlock = false;
    private final Object lock = new Object();

    protected void setUp() throws Exception
    {
        objectUnderTest = new AbstractSocketFactory()
        {
            {
                logger = new NullLogger();
            }

            public Socket createSocket(String host, int port) throws IOException, UnknownHostException
            {
                if (shouldBlock)
                {
                    synchronized(lock)
                    {
                        try
                        {
                            lock.wait();
                        }
                        catch (InterruptedException e)
                        {
                        }
                    }
                }
                return null;
            }

            public boolean isSSL(Socket socket)
            {
                return false;
            }
        };
    }

    public void testCreateSocket() throws Exception
    {
        shouldBlock = false;

        assertNull(objectUnderTest.createSocket("bogus.org", 1234));
    }

    public void testConnectHangs() throws Exception
    {
        shouldBlock = true;
        final boolean[] done = new boolean[1];

        Thread thread = new Thread()
        {
            public void run()
            {
                try
                {
                    objectUnderTest.createSocket("bogus.org", 1234);
                }
                catch (Exception e)
                {
                }
                done[0] = true;
            }
        };

        thread.start();

        thread.join(2000);

        assertFalse(done[0]);
    }

    public void testConnectHangsButTimeoutWorks() throws Exception
    {
        shouldBlock = true;
        final boolean[] done = new boolean[1];

        Thread thread = new Thread()
        {
            public void run()
            {
                try
                {
                    objectUnderTest.createSocket("bogus.org", 1234, 1000);
                }
                catch (TIMEOUT e)
                {
                    done[0] = true;
                }
                catch (Exception e)
                {
                }
            }
        };

        thread.start();

        thread.join(2000);

        assertTrue(done[0]);
    }
}
