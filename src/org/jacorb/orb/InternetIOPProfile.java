package org.jacorb.orb;

import java.util.*;

import org.omg.ETF.*;
import org.omg.IOP.*;
import org.omg.SSLIOP.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class InternetIOPProfile extends _ProfileLocalBase
{
    private byte[] data = null;  // raw data
    
    private org.omg.GIOP.Version version = null;
    private IIOPAddress          primaryAddress = null; 
    private byte[]               objectKey = null;
    private TaggedComponentList  components = null;
    
    public InternetIOPProfile (byte[] data)
    {
        this.data = data;
        
        CDRInputStream in = new CDRInputStream (null, data);
        in.openEncapsulatedArray();

        org.omg.IIOP.Version iiopVersion =
            org.omg.IIOP.VersionHelper.read(in);
        this.version = new org.omg.GIOP.Version (iiopVersion.major,
                                                 iiopVersion.minor);
                                                 
        this.primaryAddress = IIOPAddress.read (in);
        
        int length = in.read_ulong();
        objectKey = new byte[length];
        in.read_octet_array (objectKey, 0, length);
        
        components = (version.minor > 0) ? new TaggedComponentList(in)
                                         : new TaggedComponentList();
    }

    public InternetIOPProfile (IIOPAddress address, byte[] objectKey)
    {
        this.version        = new org.omg.GIOP.Version ((byte)1, (byte)2);
        this.primaryAddress = address;
        this.objectKey      = objectKey;
        this.components     = new TaggedComponentList();
    }

    public void marshal (TaggedProfileHolder tagged_profile,
                         TaggedComponentSeqHolder components)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public int hash()
    {
        return hashCode();
    }

    public Profile copy()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean is_match(Profile prof)
    {
        if (prof instanceof InternetIOPProfile)
        {
            InternetIOPProfile other = (InternetIOPProfile)prof;
            return this.primaryAddress.equals (other.primaryAddress)
               &&  this.getAlternateAddresses().equals(other.getAlternateAddresses());
        }
        else
            return false;
    }

    public org.omg.GIOP.Version version()
    {
        return version;
    }

    public IIOPAddress getAddress()
    {
        return primaryAddress;
    }

	public List getAlternateAddresses()
	{
		return components.getComponents(TAG_ALTERNATE_IIOP_ADDRESS.value,
		                                IIOPAddress.class);
	}

    public byte[] getObjectKey()
    {
        return objectKey;
    }

    public SSL getSSL()
    {
        return (SSL)components.getComponent( TAG_SSL_SEC_TRANS.value,
                                             SSLHelper.class );
    }

    public TaggedComponentList getComponents()
    {
        return components;
    }

    public Object getComponent (int tag, Class helper)
    {
        return components.getComponent (tag, helper);
    }
    
    public TaggedProfile asTaggedProfile()
    {
        return new TaggedProfile (TAG_INTERNET_IOP.value,
                                  data);
    }
    
    public boolean equals (Object other)
    {
        if (other instanceof org.omg.ETF.Profile)
            return is_match ((org.omg.ETF.Profile)other);
        else
            return false;
    }
    
    public int hashCode()
    {
        return primaryAddress.hashCode();
    }
    
    public String toString()
    {
        return primaryAddress.toString();
    }
}
