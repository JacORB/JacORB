package org.jacorb.security.ssl.sun_jsse;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2006 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.security.SecureRandom;

/**
 * <code>JSRandomImplThread</code> is another basic example showing an
 * initialization of SecureRandom..
 *
 * @author Nick Cross
 * @version $Id$
 */
public class JSRandomImplThread implements JSRandom
{
    /**
     * Static store <code>secRandom</code>.
     */
    private static SecureRandom secRandom;
    private final static Object lock = new Object();
    private static boolean initializing = false;

    /**
     * <code>initSecureRandom</code> may be called from a developers main
     * program in order to init the SecureRandom ahead of time.
     */
    public static void initSecureRandom()
    {
        synchronized(lock)
        {
            if (secRandom != null)
            {
                return;
            }

            initializing = true;
        }

        (new Thread()
        {
            public void run()
            {
                SecureRandom random = new SecureRandom();
                random.nextInt();

                synchronized (lock)
                {
                    secRandom = random;
                    initializing = false;
                    lock.notifyAll();
                }
            }
        }).start();
    }


    /**
     * <code>getSecureRandom</code> returns the initialized secure random or null.
     * the initialization is done in initSecureRandom. this method will block
     * if a initialization currently is in progress.
     *
     * @see #initSecureRandom
     * @return a <code>SecureRandom</code> value
     */
    public SecureRandom getSecureRandom()
    {
        synchronized(lock)
        {
            while(initializing)
            {
                try
                {
                    lock.wait();
                }
                catch(InterruptedException e)
                {
                    // ignored
                }
            }
            return secRandom;
        }
    }


    /**
     * <code>toString</code> override for debug.
     *
     * @return a <code>String</code> value
     */
    public String toString()
    {
        return "JacORB thread example JSRandom (" + super.toString() + ')';
    }
}