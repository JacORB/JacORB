package org.jacorb.orb;

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
    
    public String toString()
    {
        return host + ":" + port;
    }
}
