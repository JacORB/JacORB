package org.jacorb.orb;

import java.net.*;

import org.jacorb.orb.dns.DNSLookup;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class IIOPAddress 
{
    private String hostname = null; // dns name
    private String ip = null;       // dotted decimal
    private int port;               // 0 .. 65536
    
    /**
     * Creates a new IIOPAddress for <code>host</code> and <code>port</code>.
     * @param host either a DNS name, or a textual representation of a
     *     numeric IP address (dotted decimal)
     * @param port the port number represented as an integer, in the range
     *     0..65535.  As a special convenience, a negative number is
     *     converted by adding 65536 to it; this helps using values that were
     *     previously stored in a Java <code>short</code>. 
     */
    public IIOPAddress (String host, int port)
    {
        if (isIP (host))
            this.ip = host;
        else
            this.hostname = host;

        if (port < 0)
            this.port = port + 65536;
        else
            this.port = port;
    }
    
    public static IIOPAddress read (org.omg.CORBA.portable.InputStream in)
    {
        String host = in.read_string();
        short  port = in.read_ushort();
        return new IIOPAddress (host, port);
    }
    
    /**
     * Returns true if host is a numeric IP address.
     */
    private static boolean isIP (String host)
    {
        int index       = 0;
        int numberStart = 0;
        int length      = host.length();
        char ch = ' ';
        
        for (int i=0; i<4; i++)
        {
            while (true)
            {
                if (index >= length) break;
                ch = host.charAt(index);
                if (ch == '.') break;
                if (ch < '0' || ch > '9') return false;
                index++;
            }
            if (index >= length && i == 3 
                && (index - numberStart) <= 3 && (index-numberStart) > 0)
                return true;
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
                String result = InetAddress.getByName(hostname).toString();
                ip = result.substring (result.indexOf('/')+1);
            }
            catch (UnknownHostException ex)
            {
                throw new RuntimeException ("could not resolve hostname: " 
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
            hostname = DNSLookup.inverseLookup (ip);
            if (hostname == null) hostname = ip;
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
    
    public boolean equals (Object other)
    {
        if (other instanceof IIOPAddress)
        {
            IIOPAddress x = (IIOPAddress)other;
            if (this.port == x.port)
            {
                if (this.ip != null)
                    return this.ip.equals (x.ip);
                else
                    return this.hostname.equals (x.hostname);
            }
            else
                return false;
        }
        else
            return false;
    }
    
    public int hashCode()
    {
        if (ip != null)
            return ip.hashCode() + port;
        else
            return hostname.hashCode() + port;
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
    	out.write_string (ip);
    	out.write_ushort ((short)port);
    	return out.getBufferCopy();
    }
}
