package org.jacorb.proxy;

import org.omg.PortableInterceptor.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.Any;
import org.jacorb.util.*;
import org.omg.IOP.*;
import org.omg.IOP.ServiceContext;
import org.jacorb.orb.*;
import java.util.*;

/**
 * This interceptor will silently redirect requests of a
 * client to another target by throwing a ForwardRequest
 * exception.
 *
 * @author Nicolas Noffke, Sebastian Müller, Steve Osselton
 */

public class ProxyClientForwardInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
{
    private class ProxyInfo
    {
        long network = 0;
        long netmask = 0;
        org.jacorb.proxy.Proxy proxy = null;
    }

    private long localNetwork = 0;
    private long localNetmask = 0;
    private org.jacorb.orb.ORB orb = null;
    private Vector proxies = new Vector ();
    private Codec codec = null;

    public ProxyClientForwardInterceptor (ORBInitInfo info, Codec codec)
    {
        orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB ();
        this.codec = codec;

        getProxies ();
        getSubnet ();
    }

/**
 * Throws a ForwardRequest, if target is not local to subnet
 * and not proxy
 */

    public void send_request (ClientRequestInfo ri)
        throws ForwardRequest
    {
        ServiceContext ctx;
        org.omg.CORBA.Object target = ri.target ();
        org.jacorb.proxy.Proxy proxy;
        org.omg.CORBA.Any any;

        if (isProxy (target))
        {
            Debug.output (1, "ProxyClientForwardInterceptor calling to proxy");

            // Get real target

            any = orb.create_any ();
            any.insert_string (orb.object_to_string (ri.effective_target ()));

            // Pass target in service context

            try
            {
                ctx = new ServiceContext
                    (ORBConstants.SERVICE_PROXY_CONTEXT, codec.encode (any));
                ri.add_request_service_context (ctx, false);
            }
            catch (org.omg.IOP.CodecPackage.InvalidTypeForEncoding ex)
            {
                ex.printStackTrace ();
            }
        }
        else
        {
            // If not calling to proxy see if should redirect to proxy

            proxy = redirectProxy (target);
            if (proxy != null)
            {
                throw new ForwardRequest ("ProxyForward", proxy);
            }
        }
    }

    public String name ()
    {
       return "JacORB.ProxyClientForwardInterceptor";
    }

    public void send_poll (ClientRequestInfo ri)
    {
    }

    public void receive_reply (ClientRequestInfo ri)
    {
    }

    public void receive_exception (ClientRequestInfo ri)
        throws ForwardRequest
    {
    }

    public void receive_other (ClientRequestInfo ri)
        throws ForwardRequest
    {
    }
    
    public void destroy ()
    {
        orb = null;
        proxies = null;
        codec = null;
    }

/**
 * Returns whether object is a proxy
 */

    private boolean isProxy (org.omg.CORBA.Object target)
    {
        boolean result = false;
        org.jacorb.proxy.Proxy proxy;
        Enumeration enum = proxies.elements ();
        ProxyInfo info;

        while (enum.hasMoreElements ())
        {
            info = (ProxyInfo) enum.nextElement ();
            if (info.proxy._is_equivalent (target))
            {
                result = true;
                break;
            }
        }

        return result;
    }

/**
 * Returns proxy for a particular target or null if one not found
 */

    private org.jacorb.proxy.Proxy redirectProxy (org.omg.CORBA.Object target)
    {
        org.jacorb.proxy.Proxy proxy = null;
        boolean redirect = false;
        ParsedIOR pior = new ParsedIOR (target.toString ());
        String host = pior.getHost ();
        long hostIP = ipToInt (host);

        // Determine whether to redirect to proxy

        if (orb.isApplet ())
        {
            // If applet, redirect if target is not local host

            redirect = ! orb.getApplet().getCodeBase().getHost().equals (host);
        }
        else
        {
            if (localNetwork != 0)
            {
                // Redirect if target not within local subnet

                redirect = (hostIP & localNetmask) != localNetwork;
            }
            else
            {
                // If subnet not configured always redirect

                redirect = true;
            }
        }

        // Find proxy to redirect to

        if (redirect)
        {
            Debug.output (1, "ProxyClientForwardInterceptor forwarding to " + host);

            Enumeration enum = proxies.elements ();
            ProxyInfo info;

            while (enum.hasMoreElements ())
            {
                info = (ProxyInfo) enum.nextElement ();

                // Check for default proxy 

                if (info.network == 0)
                {
                    proxy = info.proxy;
                    break;
                }

                // Check if proxy for subnet

                if ((hostIP & info.netmask) == info.network)
                {
                    proxy = info.proxy;
                    break;
                }
            }
        }

        return proxy;
    }

/**
 * Finds proxies for subnets from configuration.
 */

    private void getProxies ()
    {
        Hashtable props = Environment.getProperties ("jacorb.ProxyServer.URL-");
        Enumeration keys = props.keys ();
        ProxyInfo info;
        String key;
        String value;

        // Get proxies configured for subnets

        while (keys.hasMoreElements ())
        {
            key = (String) keys.nextElement ();
            value = (String) props.get (key);
            try
            {
                info = getProxyInfo (key, value);
                proxies.add (info);
            }
            catch (org.omg.CORBA.BAD_PARAM ex) {}
        }

        // Get default proxy

        value = Environment.getProperty ("jacorb.ProxyServer.URL", "");
        if (value.length () > 0)
        {
            info = new ProxyInfo ();
            try
            {
                info.proxy = getProxy (value);
                proxies.add (info);
            }
            catch (org.omg.CORBA.BAD_PARAM ex) {}
        }
    }

/**
 * Creates a ProxyInfo class by parsing a configured proxy property
 * of the form:
 *
 * jacorb.ProxyServer.URL-<network>-<netmask>=<url>
 */

    private ProxyInfo getProxyInfo (String key, String value)
        throws org.omg.CORBA.BAD_PARAM
    {
        ProxyInfo info = new ProxyInfo ();
        StringTokenizer tok = new StringTokenizer (key, "-");
        String token;

        if (tok.countTokens () == 3)
        {
            tok.nextToken ();
            info.network = ipToInt (tok.nextToken ());
            info.netmask = ipToInt (tok.nextToken ());
            info.network = info.network & info.netmask;
            info.proxy = getProxy (value);
        }
        else
        {
            Debug.output (1, "ProxyClientForwardInterceptor Invalid proxy: " + key);
            throw new org.omg.CORBA.BAD_PARAM ();
        }

        return info;
    }

/**
 * Gets a Proxy from a URL string.
 */

    private org.jacorb.proxy.Proxy getProxy (String url)
        throws org.omg.CORBA.BAD_PARAM
    {
        try
        {
            return (org.jacorb.proxy.ProxyHelper.narrow (orb.string_to_object (url)));
        }
        catch (Exception e)
        {
            Debug.output (1, "ProxyClientForwardInterceptor failed to resolve Proxy URL: " + url);
            throw new org.omg.CORBA.BAD_PARAM ();
        }
    }

/**
 * Calculate network and netmask from configured properties
 */	

    private void getSubnet ()
        throws org.omg.CORBA.BAD_PARAM
    {
        localNetmask = ipToInt (Environment.getProperty ("jacorb.ProxyServer.Netmask", ""));
        localNetwork = ipToInt (Environment.getProperty ("jacorb.ProxyServer.Network", ""));
        localNetwork = localNetwork & localNetmask;
    }

/**
 * Convert dotted decimal ip address to long equivalent
 */

    private static long ipToInt (String ipString)
        throws org.omg.CORBA.BAD_PARAM
    {
        long result = 0;
        StringTokenizer tok = new StringTokenizer (ipString, ".");

        if (ipString.length () > 0)
        {
            if (tok.countTokens () == 4)
            {
                try
                {
                    while (tok.hasMoreTokens ())
                    {
                        result += Integer.parseInt (tok.nextToken ());
                        result <<= 8;
                    }
                }
                catch (NumberFormatException ex)
                {
                    Debug.output (1, "ProxyClientForwardInterceptor Invalid address: " + ipString);
                    throw new org.omg.CORBA.BAD_PARAM ();
                }
            }
            else
            {
                Debug.output (1, "ProxyClientForwardInterceptor Invalid address: " + ipString);
                throw new org.omg.CORBA.BAD_PARAM ();
            }
        }

        return result;
    }
}
