package org.jacorb.orb;

import org.omg.ETF.*;
import org.omg.IOP.*;

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

    public void marshal (TaggedProfileHolder tagged_profile,
                         TaggedComponentSeqHolder components)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public int hash()
    {
        return 0;  // allowable implementation, no hashing supported
    }

    public Profile copy()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean is_match(Profile prof)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.GIOP.Version version()
    {
        return version;
    }

    public IIOPAddress getAddress()
    {
        return primaryAddress;
    }

    public byte[] getObjectKey()
    {
        return objectKey;
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
}
