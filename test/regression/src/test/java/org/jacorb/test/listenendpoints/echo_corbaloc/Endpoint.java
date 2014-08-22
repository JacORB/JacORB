package org.jacorb.test.listenendpoints.echo_corbaloc;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Endpoint {
    private InetAddress hostInetAddr = null;
    private String hostName = null;
    private int port = -1;
    private int ssl_port = -1;
    private String protocol = null;

    public Endpoint ()
    {
    }

    public Endpoint (String hostName, int port)  throws Exception
    {
        this(hostName, port, -1, null);
    }

    public Endpoint (String hostName, int port, int ssl_port)  throws Exception
    {
        this(hostName, port, ssl_port, null);
    }

    public Endpoint (String hostName, int port, int ssl_port, String protocol)
    {
        this.hostName = hostName;
        setPort(port);
        setSSLPort(ssl_port);
        this.protocol = protocol;
        if (protocol == null) {
            this.protocol = "iiop";
        }

    }

    public void init()
    {

        if (hostName == null || hostName.length() <= 0) {
            hostInetAddr = null;
        }
        else
        {
            try
            {
                hostInetAddr = InetAddress.getByName(hostName);
            }
            catch (UnknownHostException e)
            {
                System.out.println("Got an exception in Endpoint.init()" + e.getMessage());
                try
                {
                    hostInetAddr = InetAddress.getLocalHost();
                }
                catch (Exception ex)
                {
                    System.out.println("Got an exception in Endpoint.init()" + e.getMessage());
                }
            }
        }
    }

    public InetAddress getHostInetAddress ()
    {
        return hostInetAddr;
    }

    public String getHostName()
    {
        if (hostInetAddr !=null)
        {
            return hostInetAddr.getHostName();
        }
        return null;
    }

    public void setPort(int port)
    {
        this.port = port;
        if(port == -1)
        {
            this.port = 0;
        }
    }

    public int getPort()
    {
        return port;
    }

    public int getSSLPort()
    {
        return ssl_port;
    }

    public void setSSLPort(int port)
    {
        this.ssl_port = port;
        if(port == -1)
        {
            this.ssl_port = 0;
        }
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }
    public String getProtocol()
    {
        return protocol;
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

        // In case the IIOPAddress object is created using the host inetAddress
        // as the source_name which normally contains the routing zone id delimted
        // by the percent (%). As such, it needs to be removed.
        int route_delim = s.lastIndexOf('%', end_bracket);

        if (route_delim < 0) {
            hostName = s.substring(1, end_bracket);
        }
        else
        {
            hostName = s.substring(1, route_delim);

        }

        int port_colon = s.indexOf(':', end_bracket);
        if (port_colon < 0)
        {
            return false;
        }
        int _port = Integer.parseInt(s.substring(port_colon + 1));

        init();
        setPort (_port);

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
            hostName = s.substring(0,colon);
        }
        else
        {
            hostName = "";
        }

        int _port = 0;
        if (colon < s.length()-1)
        {
            _port = Integer.parseInt(s.substring(colon+1));
        }

        init();
        setPort(_port);

        return true;
    }

}
