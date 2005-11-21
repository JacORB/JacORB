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
 * @author Andre Spiegel, Phil Mesnier
 * @version $Id$
 */
public class IIOPAddress
    extends ProtocolAddressBase
{
    private String source_name = null; // initializing string
    private InetAddress host = null;
    private int port = -1;             // 0 .. 65536

    // if this address is used as part of an alias, the hostname may be
    // unresolvable. Thus regardless of the dnsEnabled state, the source
    // name as given will be reported.
    private boolean unresolvable = false;

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
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        }
        catch (UnknownHostException ex) {
            byte lhaddr[] = {127,0,0,1};
            try {
                localhost = InetAddress.getByAddress("127.0.0.1",lhaddr);
            } catch (UnknownHostException ex2) {
            }
        }

        if (source_name == null || source_name.length() == 0 ) {
            host = localhost;
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

            byte hostIP[] = new byte[4];
            try {
                if (dnsEnabled || !isIP (ip, hostIP))
                    host = InetAddress.getByName(ip);
                else
                    host = InetAddress.getByAddress(hostIP);
            }
            catch (UnknownHostException ex) {
                if (logger != null && logger.isWarnEnabled())
                    logger.warn ("init_host, " + source_name + " unresolvable" );
                unresolvable = true;
                try {
                    host = InetAddress.getByAddress(source_name,
                                                    localhost.getAddress());
                } catch (UnknownHostException ex2) {
                }
            }
        }
    }


    /**
     * Returns true if host is a numeric IP address, and puts the
     * converted string into the supplied buffer.
     */
   private static boolean isIP (String host, byte [] buffer)
    {
        int index       = 0;
        int numberStart = 0;
        int length      = host.length();
        char ch = ' ';

        for (int i = 0; i < 4; i++)
        {
            int octet = 0;
            while (true)
            {
                if (index >= length)
                    break;
                ch = host.charAt(index);
                if (ch == '.')
                    break;
                if (ch < '0' || ch > '9')
                    return false;
                else
                    octet = (octet * 10) + (ch - '0');
                index++;
            }
            if (octet < 256)
                buffer[i] = (byte)octet;
            else
                return false;

            if (index >= length && i == 3
                && (index - numberStart) <= 3 && (index-numberStart) > 0)
            {
                return true;
            }
            else if (ch == '.' && (index - numberStart) <= 3
                               && (index - numberStart) > 0)
            {
                index++;
                numberStart = index;
            }
            else
                return false;
        }
        return false;
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
        if (host == null)
            init_host();
        if (unresolvable)
            return source_name;
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
        if (host == null)
            init_host();
        if (unresolvable)
            return source_name;
        return dnsEnabled ? host.getCanonicalHostName() :
            host.getHostAddress();
    }

    /**
     * Used by the ORB to configure just the hostname portion of a
     * proxy IOR address
     */

    public void setHostname (String hn)
    {
        host = null;
        source_name = hn;
    }

    /**
     * Returns the host as supplied to the constructor. This replaces
     * IIOPListener.getConfiguredHost().
     */
    public InetAddress getConfiguredHost()
    {
        if (source_name == null || source_name.length() == 0)
            return null;
        else {
            if (host == null)
                init_host();
            return host;
        }
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
                return this.source_name.equals(x.source_name);
            else
                return false;
        }
        else
            return false;
    }

    public int hashCode()
    {
        return this.source_name.hashCode() + port;
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

    /**
     * Package level method used by IIOPProfile to cause selective
     * replacement of either the hostname or the port or both
     */
    void replaceFrom (IIOPAddress other)
    {
        if (other.source_name != null)
            setHostname (other.source_name);
        if (other.port != -1)
            setPort(other.port);
    }

}
