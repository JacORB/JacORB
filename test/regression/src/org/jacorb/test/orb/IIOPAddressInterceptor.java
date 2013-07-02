package org.jacorb.test.orb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jacorb.orb.iiop.IIOPAddress;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;

/**
 * An IOR Interceptor that adds alternate addresses to IIOP Profiles.
 * 
 * @author Andre Spiegel
 */
public class IIOPAddressInterceptor
    extends LocalObject
    implements IORInterceptor
{
	public static List alternateAddresses = new ArrayList();
	
    public void establish_components(IORInfo info)
    {
		for (Iterator i = alternateAddresses.iterator(); i.hasNext();)
		{
			IIOPAddress addr = (IIOPAddress)i.next();
			info.add_ior_component_to_profile
			(
				new TaggedComponent
				(
					TAG_ALTERNATE_IIOP_ADDRESS.value,
					addr.toCDR()
				),
				TAG_INTERNET_IOP.value
			);
		}
    }

    public String name()
    {
		return "IIOPAddressInterceptor";
    }

    public void destroy()
    {
		alternateAddresses.clear();
    }

}
