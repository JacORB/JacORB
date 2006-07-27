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
    private boolean hideZoneID = true;
    private Logger logger;

    /**
     * Creates a new IIOPAddress for <code>host</code> and <code>port</code>.
     * @param hoststr either a DNS name, or a textual representation of a
     *     numeric IP address (dotted decimal)
     * @param port the port number represented as an integer, in the range
     *     0..65535.  As a special convenience, a negative number is
     *     converted by adding 65536 to it; this helps using values that were
     *     previously stored in a Java <code>short</code>.
     */
    public IIOPAddress(String hoststr, int port)
    {
        this();

        source_name = hoststr;

        init_port(port);
    }

    private void init_port(int port)
    {
        if (port < 0)
        {
            this.port = port + 65536;
        }
        else
        {
            this.port = port;
        }
    }

    /**
     * Creates a new IIOPAddress that will be initialized later by a string
     */
    public IIOPAddress()
    {
        super();
    }

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        super.configure(configuration);
        logger = this.configuration.getNamedLogger("jacorb.iiop.address");
        dnsEnabled =
            configuration.getAttribute("jacorb.dns.enable","off").equals("on");
        hideZoneID =
            configuration.getAttribute("jacorb.ipv6.hide_zoneid","on").equals("on");
    }

    /*
     * The InetAddress class can handle both IPv4 and IPv6 addresses.  If the
     * address is not in a valid format, an exception is thrown.  For this
     * reason, isIP() is no longer needed.
     *
     * The problem with Java versions prior to 1.5 is handling a zone ID.
     * In order to maintain compatiblity with Java 1.4.2, a zone ID
     * will prompt us to bind to all interfaces and use the host name
     * in the IOR.  In the future, this can be changed to properly handle
     * the zone ID using the new methods available in Java 1.5.
     */

    private void init_host()
    {
        InetAddress localhost = null;
        boolean hasZoneId = false;
        try
        {
            localhost = InetAddress.getLocalHost();
        }
        catch (UnknownHostException ex)
        {
            try
            {
                //get loopback
                localhost = InetAddress.getByName(null);
            }
            catch (UnknownHostException ex2)
            {
            }
        }

        if (source_name == null || source_name.length() == 0 )
        {
            host = localhost;
        }
        else {
           //String hostname = source_name;
           //String ip = null;

            int slash = source_name.indexOf('/');
            if (slash > 0)
            {
                //fixes two problems, 1) if the user specifed
                //the network bits, 2) if the user used the
                //name/ip format
                source_name = source_name.substring(0,slash);
            }

            int zone = source_name.indexOf('%');
            if (zone != -1)
            {
                hasZoneId = true;
            }

            if (!hasZoneId)
            {
                try
                {
                    host = InetAddress.getByName(source_name);
                }
                catch (UnknownHostException ex)
                {
                    if (logger != null && logger.isWarnEnabled())
                    {
                        logger.warn ("init_host, " + source_name + " unresolvable" );
                    }
                    unresolvable = true;
                    try
                    {
                        host = InetAddress.getByName(null); //localhost
                    }
                    catch (UnknownHostException ex2)
                    {
                    }
                }
            }
            else
            {
                if (logger != null && logger.isWarnEnabled())
                {
                    logger.warn ("init_host, " + source_name +
                            " is local-link address");
                }
                unresolvable = true;
                host = null; //will allow binds on all interfaces
            }
        }
    }


    public static IIOPAddress read(org.omg.CORBA.portable.InputStream in)
    {
        String host = in.read_string();
        short  port = in.read_ushort();

        return (new IIOPAddress(host, port));
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
        {
            init_host();
        }

        if (unresolvable)
        {
            return source_name;
        }

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
        {
            init_host();
        }
        if (unresolvable)
        {
            return source_name;
        }

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
        {
            return null;
        }
        if (host == null)
        {
            init_host();
        }
        return host;
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
            {
                return this.source_name.equals(x.source_name);
            }
            return false;
        }
        return false;
    }

    public int hashCode()
    {
        if (this.source_name != null)
        {
            return this.source_name.hashCode() + port;
        }
        else if (this.host != null)
        {
            return this.host.hashCode() + port;
        }
        else
        {
            return port;
        }
    }

    public String toString()
    {
        return this.getHostname() + ":" + port;
    }

    public boolean fromString(String s)
    {
        if (s.charAt(0) == '[')
        {
            return fromStringIPv6(s);
        }
        return fromStringIPv4(s);
    }

    //NOTE: IPv6 format is "[address]:port" since address will include colons.
    private boolean fromStringIPv6(String s)
    {
        int end_bracket = s.indexOf(']');
        if (end_bracket < 0)
        {
            return false;
        }
        source_name = s.substring(1, end_bracket);

        int port_colon = s.indexOf(':', end_bracket);
        if (port_colon < 0)
        {
            return false;
        }
        int _port = Integer.parseInt(s.substring(port_colon + 1));

        init_host();
        init_port (_port);

        return true;
    }

    private boolean fromStringIPv4(String s)
    {
        int colon = s.indexOf (':');
        if (colon == -1)
        {
            return false;
        }

        if (colon > 0)
        {
            source_name = s.substring(0,colon);
        }
        else
        {
            source_name = "";
        }

        int _port = 0;
        if (colon < s.length()-1)
        {
            _port = Integer.parseInt(s.substring(colon+1));
        }

        init_host();
        init_port(_port);

        return true;
    }

    public void write(CDROutputStream cdr)
    {
        //If host name contains a zone ID, we need to remove it.
        //This would be used to write the address on an IOR or other
        //things that could be used off-host.  Writing a link-local zone
        //ID would break the client.  Site-local zone IDs are still used,
        //but deprecated.  For now, we will ignore site-local zone IDs.
        String hostname = getHostname();
        if (hideZoneID)
        {
            int zoneIndex;
            if ((zoneIndex=hostname.indexOf('%')) != -1)
            {
                hostname = hostname.substring(0, zoneIndex);
            }
        }
        cdr.write_string(hostname);
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
        {
            if (!dnsEnabled)
            {
                return getIP();
            }
            return getHostname() + " / " + getIP();
        }
        return source_name;
    }

    /**
     * Package level method used by IIOPProfile to cause selective
     * replacement of either the hostname or the port or both
     */
    void replaceFrom (IIOPAddress other)
    {
        if (other.source_name != null)
        {
            setHostname (other.source_name);
        }
        if (other.port != -1)
        {
            setPort(other.port);
        }
    }
}
