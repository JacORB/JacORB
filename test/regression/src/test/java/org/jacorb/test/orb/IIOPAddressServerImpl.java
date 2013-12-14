package org.jacorb.test.orb;

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.test.IIOPAddressServerPOA;
import org.jacorb.test.Sample;
import org.jacorb.test.SampleHelper;
import org.omg.PortableServer.POA;

/**
 * @author Andre Spiegel
 */
public class IIOPAddressServerImpl extends IIOPAddressServerPOA
    implements Configurable
{
    private Configuration config = null;

    public void configure (Configuration c)
    {
        config = c;
    }

    public void setSocketAddress(String host, int port)
    {
        config.setAttribute ("OAIAddr",host);
        if (port != -1)
        {
            config.setAttribute ("OAPort", port);
        }

        try
        {
            config.getORB().configure(config);
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }
    }

    public void setSocketProtAddr(String protaddr)
    {
        config.setAttribute ("OAAddress",protaddr);
        try
        {
            config.getORB().configure(config);
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }
    }

    public void clearSocketAddress()
    {
        config.setAttribute ("OAIAddr", null);
        config.setAttribute ("OAPort", -1);
        config.setAttribute ("OAAddress",null);

        try
        {
            config.getORB().configure(config);
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }
    }

    public void setIORAddress(String host, int port)
    {
        config.setAttribute ("jacorb.ior_proxy_host", host);

        if (port != -1)
        {
            config.setAttribute ("jacorb.ior_proxy_port", port);
        }

        try
        {
            config.getORB().configure(config);
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }
    }

    public void setIORProtAddr(String protaddr)
    {
        config.setAttribute ("jacorb.ior_proxy_address", protaddr);

        try
        {
            config.getORB().configure(config);
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }
    }

    public void clearIORAddress()
    {
        config.setAttribute ("jacorb.ior_proxy_host", null);
        config.setAttribute ("jacorb.ior_proxy_port", -1);
        config.setAttribute ("jacorb.ior_proxy_address", null);
        try
        {
            config.getORB().configure(config);
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }
    }

    public void addAlternateAddress(String host, int port)
    {
        IIOPAddressInterceptor.alternateAddresses.add (new IIOPAddress (host, port));
    }

    public void clearAlternateAddresses()
    {
        IIOPAddressInterceptor.alternateAddresses.clear();
    }

    /**
     * Returns a sample object, using a new POA so that the address settings
     * we made are incorporated into the IOR.
     */
    public Sample getObject()
    {
        try
        {
            SampleImpl result = new SampleImpl();
            POA poa = _default_POA().create_POA("poa-" + System.currentTimeMillis(),
                                                null, null);
            poa.the_POAManager().activate();
            poa.activate_object(result);
            org.omg.CORBA.Object obj = poa.servant_to_reference (result);
            return SampleHelper.narrow (obj);
        }
        catch (Exception ex)
        {
            throw new RuntimeException ("Exception creating result object: "
                                        + ex);
        }
    }
}
