package org.jacorb.orb;

import java.net.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class IIOPAddress 
{
    private String hostname = null;
    private String ip = null;
    private int port;
    
    /**
     * Creates a new IIOPAddress for host and port.  Host can be
     * either a DNS name, or a textual representation of a numeric
     * IP address (dotted decimal).
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
            try
            {
                 hostname = InetAddress.getByName(ip).getHostName();
            }
            catch (UnknownHostException ex)
            {
                throw new RuntimeException ("could not resolve ip address: "
                                            + ip);
            }
        }
        return hostname;      
    }

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
