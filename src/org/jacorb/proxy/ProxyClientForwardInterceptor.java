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
 * @author Nicolas Noffke, Sebastian Müller
 */

public class ProxyClientForwardInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
{
    private org.jacorb.proxy.Proxy proxy = null;
    private org.jacorb.orb.ORB orb = null;
    private long network = 0;
    private long netmask = 0;

    public ProxyClientForwardInterceptor (ORBInitInfo info)
    {
        orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB ();

        calculateSubnet ();

        String url = Environment.getProperty ("jacorb.ProxyServerURL", "");
        if (url.length () == 0)
        {
            Debug.output (1, "ProxyClientForwardInterceptor failed to resolve Appligator URL");
        }
        else
        {
            try
            {
                proxy = org.jacorb.proxy.ProxyHelper.narrow (orb.string_to_object (url));
            }
            catch (Exception e)
            {
                Debug.output (1, "ProxyClientForwardInterceptor failed to resolve Proxy from URL");
            }
        }
    }

    public String name ()
    {
        return "JacORB.ProxyClientForwardInterceptor";
    }

/**
 * Throws a ForwardRequest, if target is not local to subnet
 * and not proxy
 */

    public void send_request (ClientRequestInfo ri)
        throws ForwardRequest
    {
        String originalTarget;
        ServiceContext ctx;
        org.omg.CORBA.Object target = ri.target ();

        if (proxy != null)
        {
            if (proxy._is_equivalent (target))
            {
                // If calling to proxy send original target in service context

                originalTarget = ri.effective_target().toString ();
                ctx = new ServiceContext (1245790978, originalTarget.getBytes ());
                ri.add_request_service_context (ctx, false);
            }
            else
            {
                // If not calling to proxy see if should redirect to proxy

                if (redirect (ri.target ()))
                {
                    throw new ForwardRequest ("ProxyForward", proxy);
                }
            }
        }
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
    }

/**
 * Returns whether to redirect a call to a particular target to the proxy
 */

    private boolean redirect (org.omg.CORBA.Object target)
    {
        boolean result = false;
        ParsedIOR pior = new ParsedIOR (target.toString ());
        String host = pior.getHost ();

        if (orb.isApplet ())
        {
            // If applet, redirect if target is not local host

            result = ! orb.getApplet().getCodeBase().getHost().equals (host);
        }
        else
        {
            if (network != 0)
            {
                // Redirect if target not within local subnet

                result = ((ipToInt (host) & netmask) != network);
            }
            else
            {
                // If subnet not configured always redirect

                result = true;
            }
        }

        if (result)
        {
            Debug.output (1, "ProxyClientForwardInterceptor forwarding to " + host);
        }

        return result;
    }

/**
 * Calculate network and netmask from configured properties
 */	

    private void calculateSubnet ()
    {
        netmask = ipToInt (Environment.getProperty ("jacorb.ProxyServerNetmask", ""));
        network = netmask & ipToInt (Environment.getProperty ("jacorb.ProxyServerNetwork", ""));
    }

/**
 * Convert dotted decimal ip address to long equivalent
 */

    private static long ipToInt (String ipString)
    {
        long result = 0;
        StringTokenizer tok = new StringTokenizer (ipString, ".");

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
                result = 0;
            }
        }
        else
        {
            Debug.output (1, "ProxyClientForwardInterceptor Invalid address: " + ipString);
        }

        return result;
    }
}
