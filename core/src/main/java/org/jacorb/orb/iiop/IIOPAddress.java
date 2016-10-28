package org.jacorb.orb.iiop;

/*
 * JacORB - a free Java ORB
 *
 * Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Library General Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139,
 * USA.
 */

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.etf.ListenEndpoint.Protocol;
import org.jacorb.orb.etf.ProtocolAddressBase;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORBSingleton;
import org.slf4j.Logger;

/**
 * @author Andre Spiegel, Phil Mesnier
 */
public class IIOPAddress extends ProtocolAddressBase
{
    private static final String[] networkVirtualInterfaces;

    static
    {
        networkVirtualInterfaces = System.getProperty("jacorb.network.virtual", "VirtualBox,VMWare,VMware,virbr0,vboxnet,docker").split(",");
    }

    private String source_name = null; // initializing string
    private InetAddress host = null;
    private InetAddress pseudo_host = null;
    private int port = -1; // 0 .. 65536

    // if this address is used as part of an alias, the hostname may be
    // unresolvable. Thus regardless of the dnsEnabled state, the source
    // name as given will be reported.
    private boolean unresolvable = false;

    private boolean dnsEnabled = false;
    private boolean hideZoneID = true;
    private Logger logger;
    private boolean doEagerResolve = true;
    private boolean forceDNSLookup = true;
    private Protocol protocol = null;
    private boolean isWildcard = false;
    private boolean isConfigured = false;

    /**
     * Creates a new IIOPAddress that will be initialized later by a string
     */
    public IIOPAddress()
    {
        super();
    }

    /**
     * Creates a new IIOPAddress for <code>host</code> and <code>port</code>.
     *
     * @param hoststr
     *            either a DNS name, or a textual representation of a numeric IP address (dotted
     *            decimal)
     * @param port
     *            the port number represented as an integer, in the range 0..65535. As a special
     *            convenience, a negative number is converted by adding 65536 to it; this helps
     *            using values that were previously stored in a Java <code>short</code>.
     */
    public IIOPAddress(String hoststr, int port)
    {
        this();
        source_name = hoststr;

        init_port(port);
    }

    /**
     * Method for use by the IIOPListener. Create a new IIOPAddress for <code>serverSocket</code>
     * which has already been instantiated.
     *
     * @param serverSocket
     */
    public IIOPAddress(ServerSocket serverSocket)
    {
        this();

        /**
         * Once a Server socket has been instantiated, getInetAddress().toString() would return a
         * string in the form "hostname/hostaddress". Note that hostname and hostaddress may be the
         * same. So, the following code segment would extract the hostname from the returned
         * inetAddress.
         */
        setPort(serverSocket.getLocalPort());
        setHostInetAddress(serverSocket.getInetAddress());

        // Set the isConfigured flag to prevent calling init_host() later
        isConfigured = true;
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

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);

        logger = this.configuration.getLogger("org.jacorb.iiop.address");
        dnsEnabled = configuration.getAttributeAsBoolean("jacorb.dns.enable", false);
        hideZoneID = configuration.getAttributeAsBoolean("jacorb.ipv6.hide_zoneid", true);
        doEagerResolve = configuration.getAttributeAsBoolean("jacorb.dns.eager_resolve", true);
        forceDNSLookup = configuration.getAttributeAsBoolean("jacorb.dns.force_lookup", true);

        /**
         * Check if this object has already been configured. See IIOPAddress (ServerSocket)
         */
        if (isConfigured == true)
        {
            return;
        }

        if (doEagerResolve)
        {
            init_host();
        }

        // Set the isConfigured flag
        isConfigured = true;
    }

    /**
     * The InetAddress class can handle both IPv4 and IPv6 addresses. If the address is not in a
     * valid format, an exception is thrown. For this reason, isIP() is no longer needed.
     */
    private void init_host()
    {
        if (source_name == null || source_name.length() == 0)
        {
            /**
             * Setting host to null to indicate wildcard host so that when the ServerSocket function
             * is called, the system will create a wildcard listener that would listen on all
             * listenable network interfaces.
             */
            host = null;
        }
        else
        {
            int slash = source_name.indexOf('/');
            if (slash > 0)
            {
                // fixes two problems:
                // 1) if the user specified the network bits,
                // 2) if the user used the name/ip format
                source_name = source_name.substring(0, slash);
            }
            try
            {
                host = InetAddress.getByName(source_name);
            }
            catch (UnknownHostException ex)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("init_host, " + source_name + " unresolvable");
                }
                unresolvable = true;
                try
                {
                    // Attempt to fallback to some valid IP address.
                    host = InetAddress.getLocalHost();
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("init_host, " + "default to " + host.toString());
                    }
                }
                catch (UnknownHostException ex2)
                {
                }
            }
        }

    }

    // This is called by TaggedComponentList.getComponents from IIOPProfile.
    public static IIOPAddress read(org.omg.CORBA.portable.InputStream in)
    {
        String host = in.read_string();
        short port = in.read_ushort();

        return new IIOPAddress(host, port);
    }

    public void setProtocol(Protocol proto)
    {
        this.protocol = proto;
    }

    public Protocol getProtocol()
    {
        return this.protocol;
    }

    /**
     * Returns the host part of this IIOPAddress, as a numeric IP address in dotted decimal form. If
     * the numeric IP address was specified when this object was created, then that address is
     * returned. Otherwise, this method performs a DNS lookup on the hostname.
     */
    public String getIP()
    {
        String result = null;

        if (host == null)
        {
            init_host();
        }

        if (unresolvable || host == null)
        {
            return source_name;
        }

        if (!dnsEnabled)
        {
            if (!isWildcard())
            {
                result = host.getHostAddress();
            }
            else if (pseudo_host != null)
            {
                result = pseudo_host.getHostAddress();
            }
        }
        else
        {
            if (!isWildcard())
            {
                result = forceDNSLookup ? host.getCanonicalHostName() : host.getHostName();
            }
            else if (pseudo_host != null)
            {
                result = forceDNSLookup ? pseudo_host.getCanonicalHostName() : pseudo_host.getHostName();
            }
        }
        return processZoneID(result);
    }


    /**
     * Returns the host part of this IIOPAddress, as a DNS hostname. If the DNS name was specified
     * when this IIOPAddress was created, then that name is returned. Otherwise, this method
     * performs a reverse DNS lookup on the IP address.
     */
    public String getHostName()
    {
        if (host == null)
        {
            init_host();
        }
        if (unresolvable || host == null)
        {
            return source_name;
        }

        if (!isWildcard())
        {
            return processZoneID(dnsEnabled ? host.getCanonicalHostName() : host.getHostAddress());
        }
        else if (pseudo_host != null)
        {
            return processZoneID(dnsEnabled ? pseudo_host.getCanonicalHostName() : pseudo_host.getHostAddress());
        }

        // should not get here
        return null;
    }

    /**
     * Hide the zoneID if hideZoneID is true
     * @param source
     * @return
     */
    private String processZoneID (String source)
    {
        if (hideZoneID)
        {
            int zoneIndex;
            if ((zoneIndex = source.indexOf('%')) != -1)
            {
                source = source.substring(0, zoneIndex);
            }
        }
        return source;
    }

    /**
     * Used by the ORB to configure just the hostname portion of a proxy IOR address
     */
    public void setHostname(String hn)
    {
        host = null;
        source_name = hn;

        if (doEagerResolve)
        {
            init_host();
        }
    }

    /**
     * Returns the host as supplied to the constructor. This replaces
     * IIOPListener.getConfiguredHost().
     * Used by the IIOPListener to retrieve the host address for a wildcard listener after
     * the server socket has been instantiated.
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
     * Returns the port number of this address, represented as an integer in the range 0..65535.
     */
    public int getPort()
    {
        return port;
    }

    public void setPort(int p)
    {
        port = p;
    }

    /**
     * Method for use by the IIOPListener to set host address for a wildcard listener after the
     * server socket has been instantiated. The flag isWildcard and the source_name will be updated
     * to reflect the current state of the wildcard listener.
     *
     * @param hostInetAddr
     */
    public void setHostInetAddress(InetAddress hostInetAddr)
    {
        if (host == null)
        {
            host = hostInetAddr;
            isWildcard = host.isAnyLocalAddress();
            if (isWildcard)
            {
                pseudo_host = getLocalHost();
                source_name = pseudo_host.getHostName();
            }
            else
            {
                source_name = host.toString();
                int slash_delim = source_name.indexOf('/');
                if (slash_delim > 0)
                {
                    source_name = source_name.substring(0, slash_delim);
                }
            }
            source_name = processZoneID(source_name);
        }
    }

    /**
     *
     * @return the boolean state of the wildcard listener. A true state indicates a wildcard
     *         listener.
     */
    public boolean isWildcard()
    {
        return isWildcard;
    }

    /**
     * Method for use by the IIOPListener to set the wildcard state of a wildcard listener after the
     * server socket has been instantiated.
     *
     * @param state
     */
    public void setWildcardHost(boolean state)
    {
        isWildcard = state;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof IIOPAddress)
        {
            return toString().equals(other.toString());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        return getHostName() + ":" + port;
    }

    @Override
    public boolean fromString(String s)
    {
        if (s.charAt(0) == '[')
        {
            return fromStringIPv6(s);
        }
        return fromStringIPv4(s);
    }

    // NOTE: IPv6 format is "[address]:port" since address will include colons.
    private boolean fromStringIPv6(String s)
    {
        int end_bracket = s.indexOf(']');
        if (end_bracket < 0)
        {
            return false;
        }

        // In case the IIOPAddress object is created using the host inetAddress
        // as the source_name which normally contains the routing zone id
        // delimted
        // by the percent (%). As such, it needs to be removed.
        int route_delim = s.lastIndexOf('%', end_bracket);

        if (route_delim < 0)
        {
            source_name = s.substring(1, end_bracket);
        }
        else
        {
            source_name = s.substring(1, route_delim);
        }

        int port_colon = s.indexOf(':', end_bracket);
        if (port_colon < 0)
        {
            return false;
        }
        int _port = Integer.parseInt(s.substring(port_colon + 1));

        init_host();
        init_port(_port);

        return true;
    }

    private boolean fromStringIPv4(String s)
    {
        int colon = s.indexOf(':');
        if (colon == -1)
        {
            return false;
        }

        if (colon > 0)
        {
            source_name = s.substring(0, colon);
        }
        else
        {
            source_name = "";
        }

        int _port = 0;
        if (colon < s.length() - 1)
        {
            _port = Integer.parseInt(s.substring(colon + 1));
        }

        init_host();
        init_port(_port);

        return true;
    }

    @Override
    public void write(CDROutputStream cdr)
    {
        // If host name contains a zone ID, we need to remove it.
        // This would be used to write the address on an IOR or other
        // things that could be used off-host. Writing a link-local zone
        // ID would break the client. Site-local zone IDs are still used,
        // but deprecated. For now, we will ignore site-local zone IDs.
        cdr.write_string(getHostName());
        cdr.write_ushort((short) port);
    }

    /**
     * Method for use by the PrintIOR utility. Previously it called getHostname() which may or may
     * not have returned what was actually encoded in the IOR. This is of limited use for debugging
     * purposes. This method attempts to return the string that this address was actually
     * constructed with (i.e. what the IOR actually contains as its host string).
     *
     * @return Host name or IP address or both if the original host string cannot be determined.
     */
    public String getOriginalHost()
    {
        if (source_name == null)
        {
            if (!dnsEnabled)
            {
                return getIP();
            }
            return getHostName() + " / " + getIP();
        }
        return source_name;
    }

    /**
     * Package level method used by IIOPProfile to cause selective replacement of either the
     * hostname or the port or both
     */
    void replaceFrom(IIOPAddress other)
    {
        if (other.source_name != null)
        {
            setHostname(other.source_name);
        }
        if (other.port != -1)
        {
            setPort(other.port);
        }
    }

    /**
     * Returns a string representation of the localhost address.
     */
    public static String getLocalHostAddress(Logger logger)
    {
        InetAddress addr = getLocalHost();
        if (addr != null)
        {
            return addr.getHostAddress();
        }
        else
        {
            logger.warn("Unable to resolve local IP address - using default");
            return "127.0.0.1";
        }
    }

    /**
     * Returns an address for the localhost that is reasonable to use in the IORs we produce.
     */
    public static InetAddress getLocalHost()
    {
        return getNetworkInetAddresses().getFirst();
    }

    /**
     * Returns an ordered list of InetAddresses. Order is:
     *
     * IPv4/IPv6 routable address Point-to-point address Fallback to link-local and finally loopback.
     *
     */
    public static LinkedList<InetAddress> getNetworkInetAddresses()
    {
        LinkedList<InetAddress> result = new LinkedList<InetAddress>();
        LinkedList<InetAddress> virtual = new LinkedList<InetAddress>();
        LinkedList<InetAddress> p2plinklocal = new LinkedList<InetAddress>();
        LinkedList<InetAddress> loopback = new LinkedList<InetAddress>();

        // Its somewhat tricky in Java to return the default route (i.e. what ip
        // route show would provide).
        // Its also possible that a VirtualBox/VMWare or Docker interface would
        // get returned
        // before an Ethernet/WLAN interface. As those may not be routeable the
        // JVM System Property
        // jacorb.network.virtual may be used to deprioritise those in the list.
        //
        // https://stackoverflow.com/questions/8219664/java-gethostaddress-returning-virtualbox-ipv4-address
        // https://stackoverflow.com/questions/7348711/recommended-way-to-get-hostname-in-java/7353473#7353473
        // https://stackoverflow.com/questions/11797641/java-finding-network-interface-for-default-gateway
        // http://ireasoning.com/articles/find_local_ip_address.htm

        try
        {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces()))
            {
                boolean isVirtual = false;
                for (String nvi : networkVirtualInterfaces)
                {
                    String displayName = ni.getDisplayName();
                    if (displayName != null && displayName.contains(nvi))
                    {
                        isVirtual = true;
                        break;
                    }
                }

                if (ni.isPointToPoint())
                {
                    Enumeration<InetAddress> addr = ni.getInetAddresses();
                    while (addr.hasMoreElements())
                    {
                        p2plinklocal.addFirst(addr.nextElement());
                    }
                }
                else if (isVirtual)
                {
                    Enumeration<InetAddress> addrList = ni.getInetAddresses();

                    while (addrList.hasMoreElements())
                    {
                        InetAddress addr = addrList.nextElement();

                        if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress())
                        {
                            virtual.addLast(addr);
                        }
                        else if (addr.isLinkLocalAddress())
                        {
                            p2plinklocal.addLast(addr);
                        }
                        else
                        {
                            loopback.add(addr);
                        }
                    }
                }
                else
                {
                    Enumeration<InetAddress> addrList = ni.getInetAddresses();

                    while (addrList.hasMoreElements())
                    {
                        InetAddress addr = addrList.nextElement();

                        if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress())
                        {
                            if (addr instanceof Inet6Address)
                            {
                                result.addLast(addr);
                            }
                            else
                            {
                                result.addFirst(addr);
                            }
                        }
                        else if (addr.isLinkLocalAddress())
                        {
                            p2plinklocal.addLast(addr);
                        }
                        else
                        {
                            loopback.add(addr);
                        }
                    }
                }
            }
        }
        catch (SocketException se)
        {
            ((org.jacorb.orb.ORBSingleton) ORBSingleton.init()).getLogger().error(
                    "Unable to determine network interfaces", se);
            throw new INTERNAL("Unable to determine network interfaces: " + se);
        }

        result.addAll(virtual);
        result.addAll(p2plinklocal);
        result.addAll(loopback);

        return result;
    }

    @Override
    public ProtocolAddressBase copy()
    {
        IIOPAddress result = new IIOPAddress(getHostName(), port);
        result.logger = logger;
        return result;
    }
}
