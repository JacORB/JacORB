package org.jacorb.orb.connection;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.Constructor;

import org.jacorb.orb.*;
import org.jacorb.orb.factory.*;
import org.jacorb.util.*;

/**
 * This class manages connections.<br>
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 *
 */

public class ConnectionManager
{    
    private org.jacorb.orb.ORB orb;

    /** connection mgmt. */
    private Hashtable connections = new Hashtable();

    //    private org.jacorb.util.net.SocketFactory defaultSSLSocketFactory;

    private SocketFactory socket_factory = null;
    private SocketFactory ssl_socket_factory = null;

    /** for proxy */

    private  org.jacorb.proxy.Forwarder     proxyObj = null;
    private  boolean                    proxyConnectDirectly = false;
    private  Hashtable                  unproxyTable = null;
    private  boolean                    applet_properties_read = false;
    private  Vector                     proxyEntries=new Vector();

    
    public ConnectionManager(ORB orb)
    {
        this.orb = orb;

        socket_factory = new SocketFactory(){
            public Socket createSocket( String host,
                                        int port )
            throws IOException, UnknownHostException
            {
                return new Socket( host, port );
            }

            public boolean isSSL( Socket socket )
            {
                //this factory doesn't know about ssl
                return false;
            }
        };

        if( Environment.supportSSL() )
        {
            String s = Environment.getProperty( "jacorb.ssl.socket_factory" );
            if( s == null || s.length() == 0 )
            {
                throw new RuntimeException( "SSL support is on, but the property \"jacorb.ssl.socket_factory\" is not set!" );
            }

            try
            {
                Class ssl = Class.forName( s );

                Constructor constr = ssl.getConstructor( new Class[]{
                    ORB.class });
   
                ssl_socket_factory = (SocketFactory)constr.newInstance( new Object[]{ orb });
            }
            catch (Exception e)
            {
                Debug.output( Debug.IMPORTANT | Debug.ORB_CONNECT,
                              e );
                
                throw new RuntimeException( "SSL support is on, but the ssl socket factory can't be instanciated (see trace)!" );
            }
        }
    }

    /**
     * Lookup operation for existing connections to destinations, <br>
     * opens a new one if no connection exists
     *
     * @param <code>String host_and_port</code> - in "host:xxx" notation
     * @return <code>Connection</code>
     */

    public final ClientConnection _getConnection( Delegate delegate )
    {
        return _getConnection( delegate.get_adport(), delegate.port_is_ssl());
    }


    /**
     * Low-level lookup operation for existing connections to destinations, <br>
     * opens a new one if no connection exists.
     *
     * @param <code>String host_and_port</code> - in "host:xxx" notation
     * @return <code>Connection</code>
     */

    public final ClientConnection _getConnection( String host_and_port, 
                                            boolean target_ssl )
    {
        int retries = Environment.noOfRetries();

        if( host_and_port.indexOf('/') > 0)
        {
            host_and_port = host_and_port.substring( host_and_port.indexOf('/') + 1 );
        }

        String host = host_and_port.substring(0,host_and_port.indexOf(":"));
        String port = host_and_port.substring(host_and_port.indexOf(":")+1);
        try
        {
            /** make sure we have a raw IP address here */
            java.net.InetAddress inet_addr = java.net.InetAddress.getByName( host );
            host_and_port = inet_addr.getHostAddress() + ":" + port;
        }
        catch( java.net.UnknownHostException uhe )
        {
            throw new org.omg.CORBA.COMM_FAILURE("Unknown host " + host);
        }

        /* look for an existing connection */

        ClientConnection e = 
            (ClientConnection)connections.get( host_and_port );

        if( e != null )
        {
            if( !e.isSSL() )
            {
                if( target_ssl )
                {
                    throw new org.omg.CORBA.NO_PERMISSION ( 0,  // NO_CLEAR
                                                            org.omg.CORBA.CompletionStatus.COMPLETED_NO
                                                            );
                }

                if( Environment.enforceSSL() )
                {
                    throw new org.omg.CORBA.NO_PERMISSION ("Illegal connection setup, SSL required", 
                                                           0,  // NO_CLEAR
                                                           org.omg.CORBA.CompletionStatus.COMPLETED_NO
                                                           );
                }                    
            }

            e.incUsers();
            return e;
        } 

        int _port = -1;
        try
        {
            _port = Integer.parseInt( port );
        }
        catch( NumberFormatException nfe )
        {
            Debug.output( 1, "Unable to create port int from string >" +
                          port + '<' );

            throw new org.omg.CORBA.BAD_PARAM();
        }

        if( _port < 0)
            _port += 65536;


        /* create a new connection */

        while( retries + 1 > 0 )  // +1 for initial connection
        {
            try 
            {                        
                ClientConnection c = null;

		if ( Environment.useHTTPTunneling( host ))
                {
                    c = (ClientConnection)
                        new org.jacorb.orb.connection.http.ClientConnection( this, 
                                                                         host, 
                                                                         _port, 
                                                                         socket_factory);
                    connections.put( c.getInfo(), c );
                    return c;
		}

                java.net.Socket s = null;
                try
                {               
                    if( target_ssl )
                    {
                        s = ssl_socket_factory.createSocket( host, _port );
                    }
                    else
                    {
                        if( Environment.enforceSSL())
                        {
                            // error, we don't allow unprotected outgoing connections
                            throw new org.omg.CORBA.NO_PERMISSION ("Illegal connection request to non-SSL target, SSL required", 
                                                                   0,  // NO_CLEAR
                                                                   org.omg.CORBA.CompletionStatus.COMPLETED_NO
                                                                   );
                        }
                        s = socket_factory.createSocket( host, _port );
                    }
                }
                catch( SecurityException ace )
                {
                    // could only happen, if called by applet
                    // ->connect must goto applethost
                    s = socket_factory.createSocket( orb.getApplet().getCodeBase().getHost(),
                                                     _port );     
                }

                s.setTcpNoDelay(true);
                String prop = 
                    Environment.getProperty("jacorb.connection.client_timeout");

                if( prop != null )
                {
                    try
                    {
                        s.setSoTimeout( Integer.parseInt(prop) );
                    } 
                    catch ( java.lang.NumberFormatException nfe )
                    {
                        // just ignore
                    }
                }

                c = 
                    (ClientConnection)new ClientConnection( this, 
                                                            s, 
                                                            target_ssl ? ssl_socket_factory : socket_factory
                                                            );
                
                connections.put( c.getInfo(), c );
                return c;
            } 
            catch ( java.io.IOException c ) 
            { 
                Debug.output(Debug.INFORMATION | Debug.ORB_CONNECT, c );
                Debug.output(1,"Retrying connection to " + host_and_port);
                try 
                {
                    Thread.sleep( Environment.retryInterval() );
                } 
                catch ( InterruptedException i ){}
                retries--;
            }
        }
        if( retries < 0 )
            throw new org.omg.CORBA.COMM_FAILURE("Retries exceeded, couldn't connect to " + 
                                                 host_and_port);
        return e;
    }


    /**
     * bnv: For SSL connections
     * Lookup operation for existing connections to destinations, <br>
     * it is an error if the connection is not an SSL one
     * opens a new SSL one if no connection exists
     *
     * @param <code>String host_and_port</code> - in "host:xxx" notation
     * @return <code>Connection</code>
     * @except <code>org.omg.CORBA.NO_PERMISSION</code>
     */

    public String effective_host_and_port( Delegate delegate )
    {
        String host_and_port = null;

	if( proxyConnectDirectly ||
            !Environment.useAppligator(orb.getApplet() != null) )
        {
            host_and_port = delegate.get_adport();
        }
        else
        {
            //the forward call must not diverted
            proxyConnectDirectly = true;
            if( proxyObj == null )
                initProxy();    

            //divert connection to the proxy
            Debug.output(2,"ORB:Applet-Proxy diverting");

            org.omg.CORBA.StringHolder proxyEntryId = new org.omg.CORBA.StringHolder();

            String newIORString = 
                proxyObj.forward(delegate.getParsedIOR().getIORString(),proxyEntryId);

            proxyEntries.addElement(proxyEntryId.value);
            ParsedIOR divpior = new ParsedIOR(newIORString);

            //put in unproxyTable
            unproxyTable.put( divpior.getIORString(), 
                              delegate.getParsedIOR().getIORString());

            delegate.setIOR( divpior.getIOR() );
            delegate.set_adport_and_key( divpior.getProfileBody().host + ":" +
                                         divpior.getProfileBody().port,
                                         divpior.getProfileBody().object_key );
            proxyConnectDirectly = false;
                     
            Debug.output(2,"ORB:Applet-Proxy new address set");

            host_and_port = delegate.get_adport();
        }

        if( host_and_port.indexOf('/') > 0)
            host_and_port = host_and_port.substring( host_and_port.indexOf('/') + 1 );

        String host = host_and_port.substring(0,host_and_port.indexOf(":"));
        String port = host_and_port.substring(host_and_port.indexOf(":")+1);
        try
        {
            /* make sure we have a raw IP address here */
            java.net.InetAddress inet_addr = java.net.InetAddress.getByName( host );
            host_and_port = inet_addr.getHostAddress() + ":" + port;
        }
        catch( java.net.UnknownHostException uhe )
        {
            throw new org.omg.CORBA.COMM_FAILURE("Unknown host " + host);
        }

        return host_and_port;
    }

    public ClientConnection getConnection( Delegate delegate )
    {
        if(  proxyConnectDirectly ||
             !Environment.useAppligator(orb.getApplet() != null) )
        {         
            return _getConnection( delegate );
        }
        else
        {
            /* applet stuff follows */

            // the forward call must not be diverted
            proxyConnectDirectly = true;

            if( proxyObj == null )
                initProxy();    

            //divert connection to the proxy
            Debug.output(2, "ORB:Applet-Proxy diverting" );  
      
            org.omg.CORBA.StringHolder proxyEntryId = new org.omg.CORBA.StringHolder();
            String newIORString = 
                proxyObj.forward( delegate.getParsedIOR().getIORString(), 
                                  proxyEntryId );

            proxyEntries.addElement(proxyEntryId.value);

            ParsedIOR divpior = new ParsedIOR(newIORString);

            // put in unproxyTable
            unproxyTable.put( divpior.getIORString(), 
                              delegate.getParsedIOR().getIORString());

            delegate.setIOR( divpior.getIOR() );
            delegate.set_adport_and_key( divpior.getProfileBody().host + ":" +
                                         divpior.getProfileBody().port,
                                         divpior.getProfileBody().object_key );
            proxyConnectDirectly = false;
                     
            Debug.output(2,"ORB:Applet-Proxy new address set");

            return _getConnection( delegate );
        }        
    }


    public void removeConnection( ClientConnection e )
    {
        connections.remove( e.getInfo() );
    }

    public void addConnection( ClientConnection e )
    {
        connections.put( e.getInfo(), e );
    }


    public void shutdown()
    {
        // release proxy objects
        for( int i = 0; i < proxyEntries.size(); i++ )
        {
            proxyObj.release( (String)proxyEntries.elementAt( i ) );
        }

        /* release all open connections */

        for( Enumeration e = connections.elements(); e.hasMoreElements(); )
        {
            ( (ClientConnection)e.nextElement()).closeConnection();
        }

        Debug.output(3,"ConnectionManager shut down (all connections released)");

        connections.clear();
    }

    /**
     * initialize IIOP proxy (appligator)
     */

    public void initProxy()
    {
        // check if proxy is to be used at all
        if( ! Environment.useAppligator( orb.getApplet() != null) )
        {
            return;
        }

        Debug.output(2, "using appligator");

        if ( proxyObj == null  )
        { 
            // proxy not known yet
            Debug.output(2,"ORB:Applet-Proxy Init");
            java.net.URL proxyURL = null;
            unproxyTable = new Hashtable();

	    if(orb.getApplet()!=null)
            {          
                try
                {
                    if (!applet_properties_read)
                    {
                        Environment.readFromURL(
                                  new java.net.URL( orb.getApplet().getCodeBase().toString()+
                                                    "jacorb.properties"));
                        applet_properties_read = true;
                        // reinitialize
                        //jacorb.util.Debug.initialize( new Environment() );
                        Debug.initialize( );
                    }
                }
                catch (java.net.MalformedURLException mue)
                {
                    Debug.output(2,"Bad URL: " + 
                                 orb.getApplet().getCodeBase().toString()+
                                 "jacorb.properties");
                    throw new RuntimeException("Bad URL for default context.");
                }           
                    

                /* try to get location of URL with proxy's IOR from applet parameter */
                try
                {
                    proxyURL = 
                        new java.net.URL(orb.getApplet().getParameter("JacorbProxyServerURL"));
                    Debug.output(2,"Trying address (applet param):"+proxyURL.toString());
                    readProxyIOR(proxyURL);
                    return;
                }
                catch(java.net.MalformedURLException murle)
                {
                    Debug.output(2,"Malformed proxyaddress in parametertags");
                }
                catch(java.lang.Exception e)
                {
                    Debug.output(2,"No proxy ior found in"+proxyURL.toString());
                }
	    }
            else
            { 
                // Applet==null
                applet_properties_read = true;
	    }
		
		
            if (proxyObj == null)
            {

                /* try to get location of URL with proxy"s IOR from local properties */

                try
                {
                    proxyURL = new java.net.URL(Environment.proxyURL());
                    Debug.output(2,"ORB:Trying address (Environment):"+proxyURL.toString());
                    readProxyIOR(proxyURL);
                    return;
                }
                catch(java.net.MalformedURLException murle)
                {
                    Debug.output(2,"ORB:No proxyaddress in local properties set");
                }
                catch(java.lang.Exception e)
                {
                    Debug.output(2,"ORB:No proxy ior found in (Environment) "+proxyURL.toString());
                }
            }
                                
            if( proxyObj == null )
            {
                String codebase = orb.getApplet().getCodeBase().toString();
                try
                {
                    /* try to get location of URL with proxy's IOR from remote
                       properties file at the applet's code base*/
                    
                    proxyURL = 
                        new java.net.URL(codebase.substring(codebase.lastIndexOf("/"))+"proxy.ior");
                    Debug.output(2,"ORB:Trying address (Magic):"+proxyURL.toString());
                    readProxyIOR(proxyURL);
                    return;
                }
                catch( java.net.MalformedURLException murle)
                {
                    Debug.output(2,"ORB:Malformed Applet-Codebase URL");
                }
                catch( java.lang.Exception e)
                {
                    Debug.output(2,"ORB:No proxy ior found in"+proxyURL.toString());
                }                                      
            }

            if ( proxyObj == null )
            {
                String codebase = orb.getApplet().getCodeBase().getHost();
                try
                {
                    /* try to get location of URL with proxy's IOR from a file
                       called proxy.ior at the applet's code base*/

                    proxyURL = new java.net.URL("http://"+codebase+"/proxy.ior");
                    Debug.output(2,"ORB:Trying address (MagicHome):"+proxyURL.toString());
                    readProxyIOR(proxyURL);
                    return;
                }
                catch( java.net.MalformedURLException murle)
                {
                    Debug.output(2,"ORB:Malformed Host URL");
                }
                catch( java.lang.Exception e)
                {
                    Debug.output(2,"ORB:No proxy ior found in "+proxyURL.toString());
                }
            }

            if ( proxyObj == null )
            {
                String codebase = orb.getApplet().getCodeBase().getHost();
                try
                {
                    /* try to get location of URL with proxy's IOR from a file
                       at the applet's code base */

                    proxyURL = new java.net.URL("http://" +
                                                codebase + "/" + 
                                                Environment.proxyURL() );
                    Debug.output(2,"ORB:Trying address (WebHome):" + proxyURL.toString());
                    readProxyIOR(proxyURL);
                    return;
                }
                catch( java.net.MalformedURLException murle)
                {
                    Debug.output(2,"ORB:Malformed Host/Env URL");
                }
                catch( java.lang.Exception e)
                {
                    Debug.output(2,"ORB:No proxy ior found in "+ proxyURL.toString());
                }
            }

            if ( proxyObj == null )
            {
                Debug.output(1,"ORB:Proxy server NOT found");
                if( orb.getApplet() != null )
                {
                    throw new RuntimeException("Appligator not reachable! Check configuration");
                }
            }

        }
        else
            Debug.output(2,"ORB:Tried to initialize proxy a second time -- ignored");
    }


    /** 
     *  called by Delegate to retrieve an unproxyified, local IOR 
     */

    public org.omg.IOP.IOR unproxyfy(org.omg.IOP.IOR proxy_ior)
    {
        Debug.output(3,"ORB.unproxyfy ior with oid: " + 
                     new String(new ParsedIOR(proxy_ior).get_object_key()));

        String iorString=(String)unproxyTable.get( (new ParsedIOR(proxy_ior)).getIORString());

        if( iorString == null )
        {
            Debug.output(3,"ORB.unproxyfy, no original ior found for " + 
                         (new ParsedIOR(proxy_ior)).getIORString());
            return proxy_ior;
        }

        //loop until IOR is local

        String lastString = null;
        while ( iorString != null)
        {
            Debug.output(4,"ORB.unproxyfy, looping (IOR now:"+iorString+")");
            lastString=iorString;
            iorString=(String)unproxyTable.get(iorString);
        }
        Debug.output(3,"ORB.unproxyfy, original ior: "+lastString);
        return  (new ParsedIOR(lastString)).getIOR();
    }



    /**
     * service routine, opens a connection to URL and reads a string
     */

    private void readProxyIOR( java.net.URL proxyURL ) 
    //        throws java.lang.Exception
    {
        try
        {
            java.io.BufferedReader in = 
                new java.io.BufferedReader(new java.io.InputStreamReader(proxyURL.openStream()) );
            
            String line = in.readLine();
            while ( line.indexOf("IOR:") != 0)
                line = in.readLine();
            in.close();     //line contains the IOR now
            
            ParsedIOR pior = new ParsedIOR(line);
            org.omg.CORBA.Object po = orb.string_to_object(line);
            proxyObj = org.jacorb.proxy.ForwarderHelper.narrow(po);
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public org.omg.IOP.IOR proxyfy(String ior_str)
    {
        // if applet, return proxified IOR       
        Debug.output(4,"ORB.proxyfy(), proxifying original ior " +
                                 ior_str );
        
        org.omg.CORBA.StringHolder proxyEntryId = 
            new org.omg.CORBA.StringHolder();
        
        org.omg.IOP.IOR proxy_ior = 
            new ParsedIOR( proxyObj.forward(ior_str,proxyEntryId)).getIOR();
        
        String proxy_ior_str = 
            new ParsedIOR( proxyObj.forward(ior_str,proxyEntryId)).getIORString();

        //***
        proxyEntries.addElement( proxyEntryId.value );
        unproxyTable.put( proxy_ior_str, ior_str );
        //        unproxyTable.put( proxy_ior_str, (new ParsedIOR(_ior)).getIORString() );
        
        Debug.output(4,"ORB.createIOR, returning proxifyed ior " + 
                                 proxy_ior.hashCode());
        
        return proxy_ior;
    }


    public ORB getORB()
    {
        return orb;
    }

}
