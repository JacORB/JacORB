package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.jacorb.orb.util.CorbaLoc;

import org.apache.avalon.framework.logger.Logger;

import org.jacorb.util.ObjectUtil;
import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.CONV_FRAME.CodeSetComponentInfoHelper;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.MARSHAL;
import org.omg.CosNaming.*;
import org.omg.GIOP.*;
import org.omg.IOP.*;
import org.omg.ETF.*;

/**
 * Class to convert IOR strings into IOR structures
 *
 * @author Gerald Brose
 * @version $Id$
 */

public class ParsedIOR
{
    //for byte -> hexchar
    private static final char[] lookup =
    new char[]{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private Profile effectiveProfile = null;
    private final List profiles = new ArrayList();

    /** top-level tagged components, i.e. NOT part of IOP components. Other
     *  tagged components may be part of the profile bodies
     */
    private TaggedComponentList components = new TaggedComponentList();
    private ProfileSelector profileSelector;

    protected boolean endianness = false;
    private String ior_str = null;
    private IOR ior = null;

    private final ORB orb;

    private CodeSetComponentInfo cs_info = null;
    private Integer orbTypeId = null;
    private final Logger logger;

    /* static part */

    /**
     * factory method
     */

    public static IOR createObjectIOR(org.omg.ETF.Profile profile)
    {
        String repId = "IDL:omg.org/CORBA/Object:1.0";
        TaggedComponentList components = new TaggedComponentList();

        final CDROutputStream out = new CDROutputStream();
        try
        {
            out.beginEncapsulatedArray();
            out.write_long(ORBConstants.JACORB_ORB_ID);
            components.addComponent
            (
                    new TaggedComponent
                    (
                            TAG_ORB_TYPE.value,
                            out.getBufferCopy()
                    )
            );
        }
        finally
        {
            out.close();
        }

        List taggedProfileList = new ArrayList();
        TaggedProfileHolder tp = new TaggedProfileHolder();
        TaggedComponentSeqHolder tcs = new TaggedComponentSeqHolder();
        tcs.value = components.asArray();

        profile.marshal(tp, tcs);
        taggedProfileList.add(tp.value);

        // copy the profiles into the IOR

        TaggedProfile[] tps = new TaggedProfile[taggedProfileList.size()];
        taggedProfileList.toArray(tps);

        return new IOR(repId, tps);
    }

    /**
    * This method replaces the unfiyTargetAddress method.
    * <P>
    * It will extract an object key from any given GIOP::TargetAddress
    * assuming an appropriate ETF::Factories implementation is availble
    * for the profile in use.
    */
    public static byte[] extractObjectKey(TargetAddress addr, ORB orb)
    {
        TaggedProfile tp = null;
        switch (addr.discriminator())
        {
            case KeyAddr.value:
                return addr.object_key();
            case ProfileAddr.value:
                tp = new TaggedProfile(addr.profile().tag, addr.profile().profile_data);
                break;
            case ReferenceAddr.value:
                IORAddressingInfo info = addr.ior();
                tp = new TaggedProfile(info.ior.profiles[info.selected_profile_index].tag,
                                       info.ior.profiles[info.selected_profile_index].profile_data);
                break;
        }
        TaggedProfileHolder profile = new TaggedProfileHolder(tp);
        org.omg.ETF.Factories profileFactory = orb.getTransportManager().getFactories(tp.tag);
        if (profileFactory != null)
        {
            return profileFactory.demarshal_profile(profile, new TaggedComponentSeqHolder()).get_object_key();
        }
        return null;
    }

    /**
     * Returns the value of the TAG_JAVA_CODEBASE component from this IOR,
     * or null if no such component exists.  The component is first searched
     * in the effective profile, if that is an IIOPProfile, and failing that,
     * in the MULTIPLE_COMPONENTS list.
     */
    public String getCodebaseComponent()
    {
        return getStringComponent (TAG_JAVA_CODEBASE.value);
    }

    /**
     * Creates a new <code>ParsedIOR</code> instance.
     *
     * @param object_reference a <code>String</code> value
     * @param orb an <code>org.omg.CORBA.ORB</code> value
     * @exception IllegalArgumentException if an error occurs
     */

    public ParsedIOR( String object_reference, org.omg.CORBA.ORB orb, Logger logger)
        throws IllegalArgumentException
    {
        this(orb, logger);
        parse( object_reference );
    }

    /**
     * Creates a new <code>ParsedIOR</code> instance.
     *
     * @param object_reference a <code>String</code> value
     * @param orb an <code>org.jacorb.orb.ORB</code> value
     * @exception IllegalArgumentException if an error occurs
     */
    public ParsedIOR( String object_reference, org.jacorb.orb.ORB orb, Logger logger )
        throws IllegalArgumentException
    {
        this(orb, logger);
        parse( object_reference );
    }

    public ParsedIOR( IOR _ior, org.jacorb.orb.ORB orb, Logger logger )
    {
        this(orb, logger);
        decode( _ior );
    }

    private ParsedIOR(org.omg.CORBA.ORB orb, Logger logger)
    {
        if (!(orb instanceof org.jacorb.orb.ORB))
        {
            throw new IllegalArgumentException("Construct ParsedIOR with full ORB not Singleton");
        }
        if (logger == null)
        {
            throw new IllegalArgumentException();
        }
        this.orb = (org.jacorb.orb.ORB)orb;
        this.logger = logger;
    }

    /**
     * <code>equals</code> contract is that they have the same IOR string and the
     * same effective profile. i.e. if one profile is SSL enabled then this will
     * return false.
     *
     * @param other an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean equals( Object other )
    {
        if (other == null)
        {
            return false;
        }

        return
        (
            (other instanceof ParsedIOR)                                &&
            ((ParsedIOR)other).getIORString().equals(getIORString())    &&
            effectiveProfile != null                                &&
            effectiveProfile.is_match (((ParsedIOR)other).effectiveProfile)
        );
    }

    public int hashCode()
    {
        return getIORString().hashCode();
    }

    /**
     * When multiple internet IOP tags are present, they will probably
     * have different versions, we will use the highest version
     * between 0 and 1.
     */
    public void decode( IOR _ior )
    {
        for( int i = 0; i < _ior.profiles.length; i++ )
        {
            int tag = _ior.profiles[i].tag;

            switch( tag )
            {
                case TAG_MULTIPLE_COMPONENTS.value :
                {
                    components = new TaggedComponentList
                                           (_ior.profiles[i].profile_data);
                    break;
                }
                default:
                {
                    org.omg.ETF.Factories factories =
                        orb.getTransportManager().getFactories (tag);
                    if (factories != null)
                    {
                        TaggedProfileHolder tp =
                            new TaggedProfileHolder (_ior.profiles[i]);
                        profiles.add
                            (factories.demarshal_profile
                                (tp,
                                 new TaggedComponentSeqHolder()));
                    }
                    else
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("No transport available for profile tag " + tag);
                    }
                    break;
                }
            }
        }

        ior = _ior;

        setEffectiveProfile ();
    }

    public CodeSetComponentInfo getCodeSetComponentInfo()
    {
        return cs_info;
    }

    public Integer getORBTypeId()
    {
        return orbTypeId;
    }

    public IOR getIOR()
    {
        return ior;
    }

    public String getIORString()
    {
        if( ior_str == null )
        {
            try
            {
                CDROutputStream out = new CDROutputStream( orb );
                out.beginEncapsulatedArray();

                IORHelper.write(out,ior);

                byte bytes[] = out.getBufferCopy();

                StringBuffer sb = new StringBuffer("IOR:");

                for (int j = 0; j < bytes.length; j++)
                {
                    sb.append( lookup[ (bytes[j] >> 4) & 0xF ] );
                    sb.append( lookup[ (bytes[j]     ) & 0xF ] );
                }

                ior_str = sb.toString();
            }
            catch (Exception e)
            {
                if (logger.isErrorEnabled())
                    logger.error(e.getMessage());
                throw new org.omg.CORBA.UNKNOWN("Error in building IIOP-IOR");
            }
        }

        return ior_str;
    }

    public byte[] get_object_key()
    {
        return effectiveProfile.get_object_key();
    }

    public List getProfiles()
    {
        return profiles;
    }

    public Profile getEffectiveProfile()
    {
        return effectiveProfile;
    }

    private void setEffectiveProfile ()
    {
        effectiveProfile = getProfileSelector().selectProfile
           (profiles, orb.getClientConnectionManager());
        ior_str = getIORString();

        if (effectiveProfile != null)
        {
            cs_info = (CodeSetComponentInfo) getComponent
               (TAG_CODE_SETS.value, CodeSetComponentInfoHelper.class);
            orbTypeId = getLongComponent (TAG_ORB_TYPE.value);
        }
    }

    public String getTypeId()
    {
        return ior.type_id;
    }

    public String getIDString ()
    {
        StringBuffer buff = new StringBuffer(getTypeId ());
        buff.append (":");
        byte[] key = get_object_key ();

        for (int j = 0; j < key.length; j++)
        {
            buff.append(lookup [(key[j] >> 4) & 0xF]);
            buff.append(lookup [(key[j]     ) & 0xF]);
        }

        return (buff.toString ());
    }

    public TaggedComponentList getMultipleComponents()
    {
        return components;
    }

    public boolean isNull()
    {
        return ior.type_id.equals("") && ior.profiles.length == 0;
    }

    /**
     * <code>parse</code> decodes the object_reference passed to ParsedIOR.
     *
     * @param object_reference a <code>String</code> value.
     * @exception IllegalArgumentException if object_reference is null or the
     * designated resource cannot be found.
     */
    protected void parse(String object_reference)
        throws IllegalArgumentException
    {
        if (object_reference == null)
        {
            throw new IllegalArgumentException("Null object reference");
        }

        if (object_reference.startsWith("IOR:"))
        {
            parse_stringified_ior(object_reference);
        }
        else if (object_reference.startsWith("corbaloc:"))
        {
            parse_corbaloc(object_reference);
        }
        else if (object_reference.startsWith("corbaname:"))
        {
            parse_corbaname(object_reference);
        }
        else if (object_reference.startsWith("resource:"))
        {
            parse_resource(object_reference.substring(9));
        }
        else if (object_reference.startsWith("jndi:"))
        {
            parse_jndi(object_reference.substring(5));
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Trying to resolve URL/IOR from: " + object_reference);
            }

            String content = null;
            try
            {
                content = ObjectUtil.readURL(object_reference);
            }
            catch(java.io.IOException ioe)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Error reading IOR/URL: ", ioe);
                // ignore;
            }
            if (content == null)
            {
                throw new IllegalArgumentException("Invalid or unreadable URL/IOR: " + object_reference);
            }
            parse(content);
        }
        ior_str = getIORString();
    }

    // parser helper methods

    private void parse_stringified_ior(String object_reference)
    {
        int length = object_reference.length();
        int cnt    = (length - 4) / 2;

        if ( ( length % 2 ) != 0 )
        {
            throw new BAD_PARAM("Odd number of characters within object reference");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        for (int j = 0; j < cnt; j++)
        {
            char c1 = object_reference.charAt(j * 2 + 4);
            char c2 = object_reference.charAt(j * 2 + 5);
            int i1 =
                (c1 >= 'a')
                    ? (10 + c1 - 'a')
                    : ((c1 >= 'A') ? (10 + c1 - 'A') : (c1 - '0'));
            int i2 =
                (c2 >= 'a')
                    ? (10 + c2 - 'a')
                    : ((c2 >= 'A') ? (10 + c2 - 'A') : (c2 - '0'));
            bos.write((i1 * 16 + i2));
        }

        final CDRInputStream in_;

        if (orb == null)
        {
            in_ = new CDRInputStream(org.omg.CORBA.ORB.init(), bos.toByteArray());
        }
        else
        {
            in_ = new CDRInputStream(orb, bos.toByteArray());
        }

        endianness = in_.read_boolean();
        if (endianness)
        {
            in_.setLittleEndian(true);
        }

        try
        {
            IOR _ior = IORHelper.read(in_);

            decode(_ior);
        }
        catch (MARSHAL e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Caught ", e);
            }
            throw new BAD_PARAM("Invalid IOR " + e, 10, CompletionStatus.COMPLETED_NO);
        }
    }

    private void parse_corbaloc(String object_reference)
    {
        CorbaLoc corbaLoc = new CorbaLoc(orb, object_reference);
        IOR ior = null;
        if (corbaLoc.rir())
        {
            try
            {
                org.omg.CORBA.Object obj =
                    orb.resolve_initial_references(corbaLoc.getKeyString());

                if (obj == null)
                {
                    throw new IllegalArgumentException(
                        "Unable to resolve reference for "
                            + corbaLoc.getKeyString());
                }

                ior =
                    ((Delegate) ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate()).getIOR();
            }
            catch (Exception e)
            {
                if (logger.isErrorEnabled())
                    logger.error(e.getMessage());
                throw new IllegalArgumentException("Invalid corbaloc: URL");
            }
        }
        else
        {
            Profile profile = corbaLoc.profileList[0];
            if (profile == null)
                return; // could not decode any address in list

            profile.set_object_key(corbaLoc.getKey());
            ior = createObjectIOR(profile);
        }

        decode(ior);
    }

    private void parse_corbaname(String object_reference)
    {
        String corbaloc = "corbaloc:";
        String name = "";
        int colon = object_reference.indexOf(':');
        int pound = object_reference.indexOf('#');

        if (pound == -1)
        {
            corbaloc += object_reference.substring(colon + 1);
        }
        else
        {
            corbaloc += object_reference.substring(colon + 1, pound);
            name = object_reference.substring(pound + 1);
        }

        /* empty key string in corbaname becomes NameService */
        if (corbaloc.indexOf('/') == -1)
        {
            corbaloc += "/NameService";
        }

        logger.debug(corbaloc);

        try
        {
            NamingContextExt n =
                NamingContextExtHelper.narrow(orb.string_to_object(corbaloc));
            IOR ior = null;
            // If the name hasn't been set - which is possible if we're just
            // resolving the root context down try to use name.
            if (name.length() > 0)
            {
                org.omg.CORBA.Object target = n.resolve_str(name);
                ior =
                    ((Delegate) ((org.omg.CORBA.portable.ObjectImpl)target)
                        ._get_delegate())
                        .getIOR();
            }
            else
            {
                ior =
                    ((Delegate) ((org.omg.CORBA.portable.ObjectImpl)n)
                        ._get_delegate())
                        .getIOR();
            }
            decode(ior);
        }
        catch (Exception e)
        {
            logger.error("Invalid object reference", e);

            throw new IllegalArgumentException("Invalid object reference: " +
                                               object_reference);
        }
    }

    private void parse_resource(String resourceName)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Trying to resolve URL/IOR from resource: " + resourceName);
        }

        URL url = ObjectUtil.getResource(resourceName);

        if (url == null)
        {
            throw new IllegalArgumentException(
                "Failed to get resource: " + resourceName);
        }

        try
        {
            String content = ObjectUtil.readURL(url.toString());
            parse(content);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Failed to read resource: " +
                    resourceName);
        }
    }

    private void parse_jndi(String jndiName)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Trying to resolve JNDI/IOR from name: " + jndiName);
        }

        java.lang.Object obj = null;
        try
        {
            // javax.naming.Context initialContext =
            //     new javax.naming.InitialContext ();
            //                obj = initialContext.lookup (jndiName);

            // Replaced lines above with reflected equivalent so will compile
            // under JDK < 1.3 which do not include javax.naming classes. For
            // jndi based name resolution to work obviously javax.naming
            // classes must be in CLASSPATH.
            //
            Class[] types = new Class[1];
            java.lang.Object[] params = new java.lang.Object[1];

            Class cls = ObjectUtil.classForName("javax.naming.InitialContext");
            java.lang.Object initialContext = cls.newInstance();

            types[0] = String.class;
            params[0] = jndiName;

            java.lang.reflect.Method method = cls.getMethod("lookup", types);
            obj = method.invoke(initialContext, params);
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException(
                "Failed to lookup JNDI/IOR: " + ex);
        }

        if (obj == null)
        {
            throw new IllegalArgumentException("Null JNDI/IOR: " + jndiName);
        }
        parse(obj.toString());
    }

    /**
     * Returns the component with the given tag, searching the effective
     * profile's components first (this is only possible with org.jacorb.orb.etf.ProfileBase implementations),
     * and then the MULTIPLE_COMPONENTS profile, if one exists.  If no
     * component with the given tag exists, this method returns null.
     */
    private Object getComponent (int tag, Class helper)
    {
        Object result = null;
        if (effectiveProfile instanceof org.jacorb.orb.etf.ProfileBase)
        {
            // TODO Should there be a component access mechanism for all
            //      ETF profiles?  Clarify with OMG.
            result = ((org.jacorb.orb.etf.ProfileBase)effectiveProfile).getComponent (tag, helper);
        }

        if (result != null)
        {
            return result;
        }
        return components.getComponent (tag, helper);
    }

    private static class LongHelper
    {
        public static Integer read (org.omg.CORBA.portable.InputStream in)
        {
            return new Integer (in.read_long());
        }
    }

    /**
     * Works like getComponent(), but for component values of CORBA type long.
     */
    private Integer getLongComponent (int tag)
    {
        return (Integer)getComponent (tag, LongHelper.class);
    }

    private static class StringHelper
    {
        public static String read (org.omg.CORBA.portable.InputStream in)
        {
            return in.read_string();
        }
    }

    /**
     * Works like getComponent(), but for component values of type string.
     */
    private String getStringComponent (int tag)
    {
        return (String)getComponent (tag, StringHelper.class);
    }

    /**
     * <code>isParsableProtocol</code> returns true if ParsedIOR can handle the
     * protocol within the string.
     *
     * @param check a <code>String</code> a string containing a protocol.
     * @return a <code>boolean</code> denoting whether ParsedIOR can handle this
     * protocol
     */
    public static boolean isParsableProtocol( String check )
    {
        if (check.startsWith( "IOR:" ) ||
            check.startsWith( "corbaloc:" ) ||
            check.startsWith( "corbaname:" ) ||
            check.startsWith( "resource:" ) ||
            check.startsWith( "jndi:" ) ||
            check.startsWith( "file:" ) ||
            check.startsWith( "http:" )
           )
        {
            return true;
        }
        return false;
    }

    public void setProfileSelector(ProfileSelector sel)
    {
        profileSelector = sel;
        setEffectiveProfile();
    }

    private ProfileSelector getProfileSelector()
    {
       if (profileSelector == null)
       {
           return orb.getTransportManager().getProfileSelector ();
       }
       return profileSelector;
    }
}
