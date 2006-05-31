package org.jacorb.test.common;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import java.io.*;

/**
 * A <code>StreamListener</code> listens to a given <code>InputStream</code>
 * in its own thread of control.  It copies anything that it reads from the
 * stream to its own standard output stream.  There is a special function
 * that allows you to capture an IOR from the <code>InputStream</code>.
 *
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 * @version $Id$
 */
public class StreamListener extends Thread
{
    private BufferedReader in = null;
    private String id = null;
    private String ior = null;
    private String exception = null;
    private boolean destroyed;

    public StreamListener(InputStream stream, String id)
    {
        this.in = new BufferedReader(new InputStreamReader(stream));
        this.id = id;
        setDaemon (true);
        setName(id + "-StreamListener");
    }

    /**
     * This method blocks until a line of the form "SERVER IOR: <IOR>"
     * is received from the InputStream.
     */
    public String getIOR(long timeout)
    {
        long waitUntil = System.currentTimeMillis() + timeout;

        synchronized (this)
        {
            while (ior == null && System.currentTimeMillis() < waitUntil)
            {
                try
                {
                    this.wait(timeout);
                } catch (InterruptedException ex)
                {
                    // ignore
                }
            }

            return ior;
        }
    }


    public String getException(long timeout)
    {
        long waitUntil = System.currentTimeMillis() + timeout;

        synchronized(this)
        {
            while(exception == null && System.currentTimeMillis() < waitUntil)
            {
                try
                {
                    wait(timeout);
                } catch (InterruptedException e)
                {
                    // ignore
                }
            }

            return exception;
        }
    }


    /**
     * <code>setDestroyed</code> is called by ClientServerSetup when it is destroying
     * a subprocess. This is useful to signify to the streams that the process they
     * are listening to is about to 'go'.
     */
    public void setDestroyed()
    {
        destroyed = true;
    }


    public void run()
    {
        while (true)
        {
            try
            {
                String line = in.readLine();
                if (line == null)
                {
                    break;
                }
                else if (line.startsWith("SERVER IOR: "))
                {
                    synchronized (this)
                    {
                        this.ior = line.substring(12);
                        this.notifyAll();
                    }
                }
                else if (line.matches("^(\\w+\\.)+\\w+: .*"))
                {
                    synchronized(this)
                    {
                        exception = line;
                        notifyAll();
                    }
                    System.out.println("[ SERVER " + id + " " + line + " ]");
                }
                else
                {
                    System.out.println("[ SERVER " + id + " " + line + " ]");
                }
            }
            catch (IOException ex)
            {
                System.out.println("IOException reading from server: " + ex);
                break;
            }
            catch (NullPointerException ex)
            {
                System.out.println("NullPointerException reading from server.");
                if (destroyed)
                {
                    System.out.println ("Server has been destroyed so likely this is JDK bugs 4956099, 4505257 or 4728096");
                }
                else
                {
                    ex.printStackTrace();
                }
                break;
            }
            catch (Exception ex)
            {
                System.out.println("Exception reading from server: " + ex);
                System.out.println("StreamListener exiting");
                break;
            }
        }
    }
}
