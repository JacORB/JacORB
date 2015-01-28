package org.jacorb.test.harness;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014  Gerald Brose.
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
import java.util.regex.Pattern;

/**
 * A <code>StreamListener</code> listens to a given <code>InputStream</code>
 * in its own thread of control.  It copies anything that it reads from the
 * stream to its own standard output stream.  There is a special function
 * that allows you to capture an IOR from the <code>InputStream</code>.
 *
 * @author Andre Spiegel &lt;spiegel@gnu.org&gt;
 */
public class StreamListener extends Thread
{
    private final BufferedReader in;
    private final String id;
    private String ior = null;
    private String exception = null;
    private volatile boolean active = true;
    private final StringBuffer buffer = new StringBuffer();

    public StreamListener(InputStream stream, String id)
    {
        this.in = new BufferedReader(new InputStreamReader(stream));
        this.id = id;
        setDaemon (true);
        setName(id + "-StreamListener");
    }

    /**
     * This method blocks until a line of the form "SERVER IOR: &lt;IOR&gt;"
     * is received from the InputStream.
     */
    public String getIOR(long timeout)
    {
        final long waitUntil;

        if (timeout == 0)
        {
            waitUntil = Long.MAX_VALUE;
        }
        else
        {
            waitUntil = System.currentTimeMillis() + timeout;
        }

        synchronized (this)
        {
            while (ior == null && System.currentTimeMillis() < waitUntil)
            {
                try
                {
                    this.wait(1000);
                }
                catch (InterruptedException ex)
                {
                    // ignore
                }
            }

            return ior;
        }
    }


    public String getException(long timeout)
    {
        final long waitUntil = System.currentTimeMillis() + timeout;

        synchronized(this)
        {
            while(exception == null && System.currentTimeMillis() < waitUntil)
            {
                try
                {
                    this.wait(1000);
                }
                catch (InterruptedException ex)
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
        active = false;

        interrupt();
    }

    public String getBuffer()
    {
        return buffer.toString();
    }


    public void run()
    {
        buffer.append("Starttime: " + new Date());
        buffer.append('\n');

        Pattern pattern = Pattern.compile("^(\\w+\\.)+\\w+: .*");

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
                else if (TestUtils.patternMatcher(pattern, line) > 0)
                {
                    buffer.append("Detected Exception: " + new Date());
                    buffer.append('\n');
                    System.out.println("[ SERVER " + id + " " + line + " ]");
                    setException(line);
                }
                else
                {
                    System.out.println("[ SERVER " + id + " " + line + " ]");
                }
            }
            catch (IOException ex)
            {
                if (active)
                {
                    System.out.println(id + ": IOException reading from server: " + ex);
                    ex.printStackTrace();
                }
                break;
            }
            catch (NullPointerException ex)
            {
                System.out.println(id + ": NullPointerException reading from server.");
                if (active)
                {
                    ex.printStackTrace();
                }
                else
                {
                    System.out.println (id + ": Server has been destroyed so likely this is JDK bugs 4956099, 4505257 or 4728096");
                }
                break;
            }
            catch (Exception ex)
            {
                System.out.println(id + ": Exception reading from server: " + ex);
                System.out.println(id + ": StreamListener exiting");
                break;
            }
        }

        try
        {
            in.close();
        }
        catch (IOException e)
        {
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
