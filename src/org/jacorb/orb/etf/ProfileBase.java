/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */
package org.jacorb.orb.etf;

import org.apache.avalon.framework.configuration.*;

import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.TaggedComponentList;

import org.omg.ETF.*;
import org.omg.IOP.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public abstract class ProfileBase
    extends _ProfileLocalBase
    implements Cloneable, Configurable
{
    protected org.omg.GIOP.Version version = null;
    protected byte[]               objectKey = null;
    protected TaggedComponentList  components = null;

    protected org.jacorb.config.Configuration configuration;
    protected String corbalocStr = null;

    public ProfileBase()
    {
    }
    
    /**
    * ETF defined operation to set the object key on this profile.
    */
    public void set_object_key(byte[] key)
    {
        this.objectKey = key;
    }

    /**
    * ETF defined operation to get the object key from this profile.
    */
    public byte[] get_object_key()
    {
        return objectKey;
    }
    
    /**
    * ETF defined read-only accessor for the GIOP version.
    */
    public org.omg.GIOP.Version version()
    {
        return version;
    }
    
    /**
    * ETF defined read-only accessor for the GIOP tag.
    */
    public abstract int tag();
    
    /**
    * ETF defined function to marshal the appropriate information for this
    * transport into the tagged profile.  ORBs will typically need 
    * to call the IOR interception points before calling marshal().
    * <p>
    * This particular implementation *should* work for any IOP
    * type protocol that encodes its profile_data as a CDR encapsulated
    * octet array as long as you have correctly implemented 
    * the {@link #encapsulation()}, {@link #writeAddressProfile()}, and
    * {@link #readAddressProfile()} methods. But, feel free to override 
    * it for the purpose of optimisation or whatever. It should however, 
    * remain consistent with your implementation
    * of the above mentioned methods.
    */
    public void marshal (TaggedProfileHolder tagged_profile,
                         TaggedComponentSeqHolder components)
    {
        if (encapsulation() != 0)
        {
            // You're going to have to define your own marshal operation 
            // for littleEndian profiles.
            // The CDROutputStream only does big endian currently.
            throw new Error("We can only marshal big endian stylee profiles !!");
        }
        
        // Start a CDR encapsulation for the profile_data
        CDROutputStream profileDataStream = new CDROutputStream();
        profileDataStream.beginEncapsulatedArray();
        
        // Write the opaque AddressProfile bytes for this profile...
        writeAddressProfile(profileDataStream);
        
        // ... then the object key
        profileDataStream.write_long(objectKey.length);
        profileDataStream.write_octet_array(objectKey,0,objectKey.length);
        
        switch( version.minor )
        {
            case 0 :
                // For GIOP 1.0 there were no tagged components
                break;
            default :
                // Assume minor != 0 means 1.1 onwards and encode the TaggedComponents
                if (components == null)
                {
                    components = new TaggedComponentSeqHolder (new TaggedComponent[0]);
                }
                // Write the length of the TaggedProfile sequence.
                profileDataStream.write_long(this.components.size() + components.value.length);
                
                // Write the TaggedProfiles (ours first, then the ORB's)
                for (int i = 0; i < this.components.asArray().length; i++)
                {
                    TaggedComponentHelper.write(profileDataStream, this.components.asArray()[i]);
                }
                for (int i = 0; i < components.value.length; i++)
                {
                    TaggedComponentHelper.write(profileDataStream, components.value[i]);
                }        
        }
        
        // Populate the TaggedProfile for return.
        tagged_profile.value = new TaggedProfile
        (
            this.tag(),
            profileDataStream.getBufferCopy()
        );
    }
    
    /**
    * Method to mirror the marshal method.
    */
    public void demarshal(TaggedProfileHolder tagged_profile,
                          TaggedComponentSeqHolder components)
    {
        if (tagged_profile.value.tag != this.tag())
        {
            throw new org.omg.CORBA.BAD_PARAM 
                ("Wrong tag for Transport, tag: " 
                 + tagged_profile.value.tag);
        }
        initFromProfileData(tagged_profile.value.profile_data);
        components.value = getComponents().asArray();
    }
    
    /**
    * Indicates the encapsulation that will be used by this profile
    * when encoding it's AddressProfile bytes, and which should subsequently
    * be used when marshalling all the rest of the TaggedProfile.profile_data.
    * Using the default CDROutputStream for a transport profile encapsulation 
    * this should always be 0.
    */
    public short encapsulation()
    {
        return 0; // i.e. Big endian TAG_INTERNET_IOP style
    }
    
    /**
    * Write the AddressProfile to the supplied stream.
    * Implementors can assume an encapsulation is already open.
    */
    public abstract void writeAddressProfile(CDROutputStream stream);
    
    /**
    * Read the ETF::AddressProfile from the supplied stream.
    */
    public abstract void readAddressProfile(CDRInputStream stream);
    
    /**
    * Accessor for the TaggedComponents of the Profile.
    */
    public TaggedComponentList getComponents()
    {
        return components;
    }

    public Object getComponent(int tag, Class helper)
    {
        return components.getComponent(tag, helper);
    }

    public void addComponent(int tag, Object data, Class helper)
    {
        components.addComponent(tag, data, helper);
    }

    public void addComponent(int tag, byte[] data)
    {
        components.addComponent(tag, data);
    }

    public TaggedProfile asTaggedProfile()
    {
        TaggedProfileHolder result = new TaggedProfileHolder();
        this.marshal(result, null);
        return result.value;
    }

    /**
     * This function shall return an equivalent, deep-copy of the profile
     * on the free store.
     */
    public Profile copy()
    {
        try
        {
            return (Profile)this.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException("error cloning profile: " + e);
        }
    }
    
    /**
    * Used from the byte[] constructor and the demarshal method. Relies
    * on subclasses having satisfactorily implemented the 
    * {@link #readAddressProfile()} method.
    */
    protected void initFromProfileData(byte[] data)
    {
        CDRInputStream in = new CDRInputStream(null, data);
        in.openEncapsulatedArray();

        readAddressProfile(in);
        
        int length = in.read_ulong();
        
        objectKey = new byte[length];
        in.read_octet_array(objectKey, 0, length);
        
        components = (version != null && version.minor > 0) ? new TaggedComponentList(in)
                                                            : new TaggedComponentList();
    }
}
