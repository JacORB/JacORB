package org.jacorb.orb;

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

import org.jacorb.orb.dns.DNSLookup;
import org.apache.avalon.framework.configuration.*;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class IIOPAddress 
    implements Configurable
{
    private String hostname = null; // dns name
    private String ip = null;       // dotted decimal
    private int port;               // 0 .. 65536
 
    private org.jacorb.config.Configuration configuration;
    private boolean configured = true;
    private DNSLookup lookup;
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
    public IIOPAddress(String host, int port)
    {
        if (host.length() == 0 )
            throw new IllegalArgumentException();

        lookup = new DNSLookup();

        if (isIP(host))
            this.ip = host;
        else
            this.hostname = host;

        if (port < 0)
            this.port = port + 65536;
        else
            this.port = port;

        
    }
    
    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)configuration;
        logger = this.configuration.getNamedLogger("jacorb.iiop.address");
        lookup.configure(configuration);
        configured = true;
    }


    public static IIOPAddress read(org.omg.CORBA.portable.InputStream in)
    {
        String host = in.read_string();
        short  port = in.read_ushort();
        IIOPAddress addr = new IIOPAddress(host, port);
        return addr;
    }
    
    /**
     * Returns true if host is a numeric IP address.
     */
    private static boolean isIP(String host)
    {
        int index       = 0;
        int numberStart = 0;
        int length      = host.length();
        char ch = ' ';
        
        for (int i = 0; i < 4; i++)
        {
            while (true)
            {
                if (index >= length) 
                    break;
                ch = host.charAt(index);
                if (ch == '.') 
                    break;
                if (ch < '0' || ch > '9') 
                    return false;
                index++;
            }
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

    /**
     * Returns the host part of this IIOPAddress, as a numeric IP address in 
     * dotted decimal form.  If the numeric IP address was specified when 
     * this object was created, then that address is returned.  Otherwise,
     * this method performs a DNS lookup on the hostname.
     */    
    public String getIP()
    {
        if (ip == null)
        {
            try
            {
                ip = InetAddress.getByName(hostname).getHostAddress();
            }
            catch (UnknownHostException ex)
            {
                throw new RuntimeException("could not resolve hostname: " 
                                            + hostname);
            }  
        }
        return ip;
    }

    /**
     * Returns the host part of this IIOPAddress, as a DNS hostname.
     * If the DNS name was specified when this IIOPAddress was created,
     * then that name is returned.  Otherwise, this method performs a
     * reverse DNS lookup on the IP address.
     */
    public String getHostname()
    {    
        if (hostname == null)
        {
            if (!configured)
            {
                throw new Error("Unconfigured IIOPAddress!");
            }
            
            hostname = lookup.inverseLookup(ip);
            if (hostname == null) 
                hostname = ip;
        }
        return hostname;      
    }

    /**
     * Returns the port number of this address, represented as an integer
     * in the range 0..65535.
     */
    public int getPort()
    {
        return port;
    }
    
    public boolean equals(Object other)
    {
        if (other instanceof IIOPAddress)
        {
            IIOPAddress x = (IIOPAddress)other;
            if (this.port == x.port)
                return this.getIP().equals(x.getIP());
            else
                return false;
        }
        else
            return false;
    }
    
    public int hashCode()
    {
        return this.getIP().hashCode() + port;
    }
    
    public String toString()
    {
        if (hostname != null)
            return hostname + ":" + port;
        else
            return ip + ":" + port;
    }
    
    public byte[] toCDR()
    {
    	CDROutputStream out = new CDROutputStream();
    	out.beginEncapsulatedArray();
    	out.write_string(ip);
    	out.write_ushort((short)port);
    	return out.getBufferCopy();
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
        if (hostname == null)
        {
            return ip;
        }
        else if (ip == null)
        {
            return hostname;
        }
        else
        {
            return hostname + " / " + ip;
        }
    }
}
