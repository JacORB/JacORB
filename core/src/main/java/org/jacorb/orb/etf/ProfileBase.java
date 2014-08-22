/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import java.util.Collection;
import java.util.Collections;

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.TaggedComponentList;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.MARSHAL;
import org.omg.ETF.Profile;
import org.omg.ETF._ProfileLocalBase;
import org.omg.IIOP.ListenPoint;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedComponentHelper;
import org.omg.IOP.TaggedComponentSeqHolder;
import org.omg.IOP.TaggedProfile;
import org.omg.IOP.TaggedProfileHolder;
import org.slf4j.Logger;

/**
 * @author Andre Spiegel
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
    protected Logger logger;

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
       if( configuration == null )
       {
          throw new ConfigurationException("ProfileBase: given configuration was null");
       }

       this.configuration = configuration;
    }



    /**
    * ETF defined operation to set the object key on this profile.
    */
    @Override
    public void set_object_key(byte[] key)
    {
        this.objectKey = key;
    }

    /**
    * ETF defined operation to get the object key from this profile.
    */
    @Override
    public byte[] get_object_key()
    {
        return objectKey;
    }

    /**
    * ETF defined read-only accessor for the GIOP version.
    */
    @Override
    public org.omg.GIOP.Version version()
    {
        return version;
    }

    /**
    * ETF defined read-only accessor for the GIOP tag.
    */
    @Override
    public abstract int tag();

    /**
     * Profiles use this method for taking alternative address values
     * for replacement, such as when an IOR proxy or IMR is in use.
     * This is a concrete method here to not break existing profiles
     * that may not be interested in this behavior.
     */
    public void patchPrimaryAddress(ProtocolAddressBase replacement)
    {
        // nothing to do
    }

    /**
    * ETF defined function to marshal the appropriate information for this
    * transport into the tagged profile.  ORBs will typically need
    * to call the IOR interception points before calling marshal().
    * <p>
    * This particular implementation *should* work for any IOP
    * type protocol that encodes its profile_data as a CDR encapsulated
    * octet array as long as you have correctly implemented
    * the {@link #encapsulation()}, {@link #writeAddressProfile(CDROutputStream)}, and
    * {@link #readAddressProfile(CDRInputStream)} methods. But, feel free to override
    * it for the purpose of optimisation or whatever. It should however,
    * remain consistent with your implementation
    * of the above mentioned methods.
    */
    @Override
    public void marshal (final TaggedProfileHolder tagged_profile,
            final TaggedComponentSeqHolder componentSequence)
    {
        TaggedComponentSeqHolder compSeq = componentSequence;

        if (encapsulation() != 0)
        {
            // You're going to have to define your own marshal operation
            // for littleEndian profiles.
            // The CDROutputStream only does big endian currently.
            throw new BAD_PARAM("We can only marshal big endian style profiles !!");
        }

        // Start a CDR encapsulation for the profile_data
        final CDROutputStream profileDataStream = new CDROutputStream();
        try
        {
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
                case 1:
                    // fallthrough
                case 2:
                {
                    // Assume minor != 0 means 1.1 onwards and encode the TaggedComponents
                    if (compSeq == null)
                    {
                        compSeq = new TaggedComponentSeqHolder (new TaggedComponent[0]);
                    }
                    // Write the length of the TaggedProfile sequence.
                    profileDataStream.write_long(this.components.size() + compSeq.value.length);

                    // Write the TaggedProfiles (ours first, then the ORB's)
                    final TaggedComponent[] ourTaggedProfiles = components.asArray();
                    for (int i = 0; i < ourTaggedProfiles.length; i++)
                    {
                        TaggedComponentHelper.write(profileDataStream, ourTaggedProfiles[i]);
                    }
                    for (int i = 0; i < compSeq.value.length; i++)
                    {
                        TaggedComponentHelper.write(profileDataStream, compSeq.value[i]);
                    }
                    break;
                }
                default:
                {
                    throw new INTERNAL("Unknown GIOP version tag " + version.minor + " when marshalling for IIOPProfile");
                }
            }

            // Populate the TaggedProfile for return.
            tagged_profile.value = new TaggedProfile
            (
                    this.tag(),
                    profileDataStream.getBufferCopy()
            );
        }
        finally
        {
            profileDataStream.close();
        }
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
        if (components != null)
        {
            components.value = getComponents().asArray();
        }
    }

    /**
    * Indicates the encapsulation that will be used by this profile
    * when encoding its AddressProfile bytes, and which should subsequently
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
    @Override
    public Profile copy()
    {
        try
        {
            return (Profile)this.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException("error cloning profile: " + e); // NOPMD
        }
    }

    /**
    * Used from the byte[] constructor and the demarshal method. Relies
    * on subclasses having satisfactorily implemented the
    * {@link #readAddressProfile(CDRInputStream)} method.
    */
    protected void initFromProfileData(byte[] data)
    {
        final CDRInputStream in = new CDRInputStream(data);

        try
        {
            in.openEncapsulatedArray();

            readAddressProfile(in);

            int length = in.read_ulong();

            if (in.available() < length)
            {
                throw new MARSHAL("Unable to extract object key. Only " + in.available() + " available and trying to assign " + length);
            }

            objectKey = new byte[length];
            in.read_octet_array(objectKey, 0, length);

            components = (version != null && version.minor > 0) ? new TaggedComponentList(in)
                    : new TaggedComponentList();
        }
        finally
        {
            in.close();
        }
    }

    /**
     * @return a Collection of ListenPoints that represent the endpoints contained in this IIOPProfile.
     */
    public Collection<ListenPoint> asListenPoints()
    {
        return Collections.emptyList();
    }

    /**
     * This function will search and remove the components, whose tag matches
     * the given tag, from the components list.  Removing tags are needed in
     * the case the the ImR is used.
     * @param tags
     */
    public void removeComponents (int tags)
    {
        if (components != null) {
            components.removeComponents(tags);
        }
    }
}
