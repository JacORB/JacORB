package org.jacorb.orb;

import java.util.*;

import org.omg.ETF.*;
import org.omg.IOP.*;
import org.omg.IIOP.*;
import org.omg.SSLIOP.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class InternetIOPProfile extends _ProfileLocalBase
                                implements Cloneable
{
    private org.omg.GIOP.Version version = null;
    private IIOPAddress          primaryAddress = null; 
    private byte[]               objectKey = null;
    private TaggedComponentList  components = null;
    
    public InternetIOPProfile (byte[] data)
    {
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
        TaggedComponent[] allComponents = null;
        CDROutputStream profileDataStream = null;
        
        if (components == null)
        {
            components = new TaggedComponentSeqHolder (new TaggedComponent[0]);
        }

        switch( version.minor )
        {
            case 2 :
            {
                //same as IIOP 1.1
            }
            case 1:
            {
                // create IIOP 1.1 profile

                // concatenate the two component lists
                
                allComponents = new TaggedComponent[   this.components.size() 
                                                     + components.value.length ];
                System.arraycopy( this.components.asArray(), 0,
                                  allComponents, 0, this.components.size() );
                System.arraycopy( components.value, 0,
                                  allComponents, this.components.size(),
                                  components.value.length );

                ProfileBody_1_1 pb1 = new ProfileBody_1_1
                (
                    new org.omg.IIOP.Version( version.major, version.minor ),
                    primaryAddress.getHost(),
                    (short)primaryAddress.getPort(),
                    objectKey,
                    allComponents
                );

                // serialize the profile id 1, leave idx 0 for v.1.0 profile
                profileDataStream = new CDROutputStream();
                profileDataStream.beginEncapsulatedArray();
                ProfileBody_1_1Helper.write( profileDataStream, pb1 );

                tagged_profile.value = new TaggedProfile
                (
                    TAG_INTERNET_IOP.value,
                    profileDataStream.getBufferCopy()
                );
            }
            case 0:
            {
                // create IIOP 1.0 profile
                ProfileBody_1_0 pb0 = new ProfileBody_1_0
                (
                    new org.omg.IIOP.Version( version.major, version.minor ),
                    primaryAddress.getHost(),
                    (short)primaryAddress.getPort(),
                    objectKey
                );

                profileDataStream = new CDROutputStream();
                profileDataStream.beginEncapsulatedArray();
                ProfileBody_1_0Helper.write( profileDataStream, pb0 );

                tagged_profile.value = new TaggedProfile
                (
                    TAG_INTERNET_IOP.value,
                    profileDataStream.getBufferCopy()
                );
            }
        }
    }

    public int hash()
    {
        return hashCode();
    }

    public Profile copy()
    {
        try
        {
            return (Profile)this.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException ("error cloning profile: " + e);
        }
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        InternetIOPProfile result = (InternetIOPProfile)super.clone();

        result.version = new org.omg.GIOP.Version (this.version.major,
                                                   this.version.minor);
        if (this.objectKey != null)
        {
            result.objectKey = new byte [this.objectKey.length];
            System.arraycopy (this.objectKey, 0, result.objectKey, 0,
                              this.objectKey.length);
        }
        
        if (this.components != null)
        {
            result.components = (TaggedComponentList)this.components.clone();   
        }
        
        return result;
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
        TaggedProfileHolder result = new TaggedProfileHolder();
        this.marshal (result, null);
        return result.value;
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
