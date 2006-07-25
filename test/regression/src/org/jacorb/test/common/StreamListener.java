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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

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
    private boolean active = true;
    private final StringBuffer buffer = new StringBuffer();

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
                final long waitTime = waitUntil - System.currentTimeMillis();

                if (waitTime > 0)
                {
                    try
                    {
                        this.wait(waitTime);
                    }
                    catch (InterruptedException ex)
                    {
                        // ignore
                    }
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
                final long waitTime = waitUntil - System.currentTimeMillis();

                if (waitTime > 0)
                {
                    try
                    {
                        this.wait(waitTime);
                    }
                    catch (InterruptedException ex)
                    {
                        // ignore
                    }
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
        active = false;
    }

    public String getBuffer()
    {
        return buffer.toString();
    }


    public void run()
    {
        buffer.append("Starttime: " + new Date());
        buffer.append('\n');

        while (active)
        {
            try
            {
                String line = in.readLine();
                buffer.append(line);
                buffer.append('\n');

                if (line == null)
                {
                    break;
                }
                else if (line.startsWith("SERVER IOR: "))
                {
                    buffer.append("Detected IOR: " + new Date());
                    buffer.append('\n');
                    setIOR(line.substring(12));
                }
                else if (line.matches("^(\\w+\\.)+\\w+: .*"))
                {
                    buffer.append("Detected Exception: " + new Date());
                    buffer.append('\n');
                    setException(line);
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
                if (active)
                {
                    ex.printStackTrace();
                }
                else
                {
                    System.out.println ("Server has been destroyed so likely this is JDK bugs 4956099, 4505257 or 4728096");
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

    private void setException(String line)
    {
        synchronized(this)
        {
            exception = line;
            notifyAll();
        }
    }

    private void setIOR(String line)
    {
        synchronized (this)
        {
            ior = line;
            notifyAll();
        }
    }

    public String toString()
    {
        StringBuffer details = new StringBuffer();
        details.append("Details from " + id + ":\n");
        details.append("Active: " + active + "\n");
        details.append("IOR: " + ior + "\n");
        details.append("Exception: " + exception + "\n");
        details.append("Raw Buffer:\n");
        details.append(getBuffer());
        details.append('.');
        details.append('\n');

        return details.toString();
    }
}
