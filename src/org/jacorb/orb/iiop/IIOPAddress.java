package org.jacorb.orb.iiop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.net.*;

import org.apache.avalon.framework.configuration.*;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.orb.etf.ProtocolAddressBase;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.CDRInputStream;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class IIOPAddress
    extends ProtocolAddressBase
{
    private String source_name = null; // initializing string
    private InetAddress host = null;
    private int port;               // 0 .. 65536

    private boolean dnsEnabled = false;
    private Logger logger;

    /**
     * Creates a new IIOPAddress for <code>host</code> and <code>port</code>.
     * @param host either a DNS name, or a textual representation of a
     *     numeric IP address (dotted decimal)
     * @param port the port number represented as an integer, in the range
     *     0..65535.  As a special convenience, a negative number is
     *     converted by adding 65536 to it; this helps using values that were
     *     previously stored in a Java <code>short</code>.
     */
    public IIOPAddress(String hoststr, int port)
    {
        source_name = hoststr;
        init_host ();

        if (port < 0)
            this.port = port + 65536;
        else
            this.port = port;
    }

    /**
     * Creates a new IIOPAddress that will be initialized later by a string
     */
    public IIOPAddress()
    {
    }

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        super.configure(configuration);
        logger = this.configuration.getNamedLogger("jacorb.iiop.address");
        dnsEnabled =
            configuration.getAttribute("jacorb.dns.enable","off").equals("on");
    }

    private void init_host()
    {
        try {
            if (source_name == null || source_name.length() == 0 ) {
                host = InetAddress.getLocalHost();
            }
            else {
                String hostname = null;
                String ip = null;

                int slash = source_name.indexOf('/');
                if (slash == -1)
                    ip = source_name;
                else {
                    ip = source_name.substring(slash+1);
                    if (slash > 0)
                        hostname = source_name.substring(0,slash);
                }
                host = InetAddress.getByName(ip);
            }
        }
        catch (UnknownHostException ex) {
            throw new RuntimeException("could not resolve hostname: "
                                       + source_name);
        }
    }


    public static IIOPAddress read(org.omg.CORBA.portable.InputStream in)
    {
        String host = in.read_string();
        short  port = in.read_ushort();
        IIOPAddress addr = new IIOPAddress(host, port);
        return addr;
    }


    /**
     * Returns the host part of this IIOPAddress, as a numeric IP address in
     * dotted decimal form.  If the numeric IP address was specified when
     * this object was created, then that address is returned.  Otherwise,
     * this method performs a DNS lookup on the hostname.
     */
    public String getIP()
    {
        return host.getHostAddress();
    }

    /**
     * Returns the host part of this IIOPAddress, as a DNS hostname.
     * If the DNS name was specified when this IIOPAddress was created,
     * then that name is returned.  Otherwise, this method performs a
     * reverse DNS lookup on the IP address.
     */
    public String getHostname()
    {
        return dnsEnabled ? host.getHostName() : host.getHostAddress();
    }

    /**
     * Returns the host as supplied to the constructor. This replaces
     * IIOPListener.getConfiguredHost().
     */
    public InetAddress getConfiguredHost()
    {
        return (source_name == null || source_name.length() == 0) ? null : host;
    }

    /**
     * Returns the port number of this address, represented as an integer
     * in the range 0..65535.
     */
    public int getPort()
    {
        return port;
    }

    public void setPort(int p)
    {
        port = p;
    }

    public boolean equals(Object other)
    {
        if (other instanceof IIOPAddress)
        {
            IIOPAddress x = (IIOPAddress)other;
            if (this.port == x.port)
                return this.host.equals(x.host);
            else
                return false;
        }
        else
            return false;
    }

    public int hashCode()
    {
        return this.host.hashCode() + port;
    }

    public String toString()
    {
        return this.getHostname() + ":" + port;
    }

    public boolean fromString(String s)
    {
        int colon = s.indexOf (':');
        if (colon == -1)
            return false;

        source_name = null;
        int p = 0;
        if (colon > 0) {
            source_name = s.substring(0,colon);
        }
        if (colon < s.length()-1)
            p = Integer.parseInt(s.substring(colon+1));

        init_host ();

        if (p < 0)
            port = p + 65536;
        else
            port = p;

        return true;
    }

    public void write (CDROutputStream cdr)
    {
        cdr.write_string(getHostname());
        cdr.write_ushort( (short) port);
    }

    static public IIOPAddress read (CDRInputStream cdr)
    {
        String hostname = cdr.read_string();
        short port = cdr.read_ushort();
        return new IIOPAddress (hostname,port);
    }

    /**
    * Method for use by the PrintIOR utility. Previously it called
    * getHostname() which may or may not have returned what was
    * actually encoded in the IOR. This is of limited use for
    * debugging purposes. This method attempts to return the string
    * that this address was actually constructed with (i.e. what the
    * IOR actually contains as its host string).
    * @return Host name or IP address or both if the original host string
    * cannot be determined.
    */
    public String getOriginalHost()
    {
        if (source_name == null)
            if (!dnsEnabled)
                return getIP();
            else
                return getHostname() + " / " + getIP();
        else
            return source_name;
    }

}
