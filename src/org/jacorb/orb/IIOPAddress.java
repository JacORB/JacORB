package org.jacorb.orb;

import java.net.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class IIOPAddress 
{
    private String host;
    private int port;
    
    public IIOPAddress (String host, int port)
    {
        this.host = host;
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
    
    private static String rawIP (String host)
    {
        try
        {
            /** make sure we have a raw IP address here */
            InetAddress inet_addr = 
                InetAddress.getByName( host );
                
            return inet_addr.getHostAddress();
        }
        catch( UnknownHostException uhe )
        {
            throw new org.omg.CORBA.TRANSIENT("Unknown host " + host);
        }
    }

    public String getHost()
    {
        return host;
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
            return this.host.equals (x.host) && this.port == x.port;
        }
        else
            return false;
    }
    
    public int hashCode()
    {
        return host.hashCode() + port;
    }
    
    public String toString()
    {
        return host + ":" + port;
    }
    
    public byte[] toCDR()
    {
    	CDROutputStream out = new CDROutputStream();
    	out.beginEncapsulatedArray();
    	out.write_string (host);
    	out.write_ushort ((short)port);
    	return out.getBufferCopy();
    }
}
