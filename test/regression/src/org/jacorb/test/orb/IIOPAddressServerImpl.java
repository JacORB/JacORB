package org.jacorb.test.orb;

import org.omg.PortableServer.*;

import org.jacorb.util.Environment;
import org.jacorb.orb.IIOPAddress;

import org.jacorb.test.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class IIOPAddressServerImpl extends IIOPAddressServerPOA
{
    public void setSocketAddress(String host, int port)
    {
        Environment.setProperty ("OAIAddr", host);
        if (port != -1)
            Environment.setProperty ("OAPort", Integer.toString(port));
    }
    
    public void clearSocketAddress()
    {
        Environment.setProperty ("OAIAddr", null);
        Environment.setProperty ("OAPort", null);
    }

    public void setIORAddress(String host, int port)
    {
		Environment.setProperty ("jacorb.ior_proxy_host", host);
        if (port != -1)
    		Environment.setProperty ("jacorb.ior_proxy_port", Integer.toString(port));
    }
    
    public void clearIORAddress()
    {
        Environment.setProperty ("jacorb.ior_proxy_host", null);
        Environment.setProperty ("jacorb.ior_proxy_port", null);
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
