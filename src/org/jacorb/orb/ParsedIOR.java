package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
import java.net.URL;
import java.util.*;

import org.jacorb.orb.iiop.*;
import org.jacorb.orb.util.CorbaLoc;
import org.jacorb.util.Debug;
import org.jacorb.util.Environment;
import org.jacorb.util.ObjectUtil;
import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.CONV_FRAME.CodeSetComponentInfoHelper;
import org.omg.CSIIOP.*;
import org.omg.CosNaming.*;
import org.omg.GIOP.*;
import org.omg.IIOP.*;
import org.omg.IOP.*;
import org.omg.SSLIOP.*;
import org.omg.ETF.*;

/**
 * Class to convert IOR strings into IOR structures
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class ParsedIOR
{
    //for byte -> hexchar
    private static final char[] lookup =
    new char[]{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private IIOPProfile effectiveProfile = null;
    private List profiles = new ArrayList();

    /** top-level tagged components, i.e. NOT part of IOP components. Other
     *  tagged components may be part of the profile bodies
     */
    private TaggedComponentList components = new TaggedComponentList();

    protected boolean endianness = false;
    private String ior_str = null;
    private IOR ior = null;

    private ORB orb = null;

    private IIOPAddress iiopAddress = null;
    private boolean use_ssl = false;
    private boolean use_sas = false;

    private CodeSetComponentInfo cs_info = null;
    private Integer orbTypeId = null;

    /* static part */

    /**
     * factory method
     */


    public static IOR createObjectIOR( String host,
                                       short port,
                                       byte[] object_key,
                                       int giop_minor )
    {
        String repId = "IDL:org.omg/CORBA/Object:1.0";
        Vector components_v = new Vector();
        TaggedComponent[] components = null;

        // set the ORB type ID component to JacORB

        CDROutputStream orbIDComponentDataStream = new CDROutputStream();
        orbIDComponentDataStream.beginEncapsulatedArray();
        orbIDComponentDataStream.write_long( ORBConstants.JACORB_ORB_ID );

        TaggedComponent orbIDComponent = new TaggedComponent
        (
            TAG_ORB_TYPE.value,
            orbIDComponentDataStream.getBufferCopy()
        );
        components_v.addElement( orbIDComponent );

        // all components for the profiles must be created by now

        components = new TaggedComponent[ components_v.size() ];
        components_v.copyInto( components );

        CDROutputStream profileDataStream = new CDROutputStream();
        profileDataStream.beginEncapsulatedArray();

        TaggedProfile tp = null;
        Vector taggedProfileVector = new Vector();

        if( giop_minor == 0 )
        {
            ProfileBody_1_0 pb1_0 = new ProfileBody_1_0
            (
                new org.omg.IIOP.Version( (byte) 1, (byte) 0 ),
                host,
                port,
                object_key
            );
            ProfileBody_1_0Helper.write( profileDataStream, pb1_0 );

            tp = new TaggedProfile
            (
                TAG_INTERNET_IOP.value,
                profileDataStream.getBufferCopy()
            );
            taggedProfileVector.addElement( tp );

            // now fill the last IOR profile with components

            profileDataStream = new CDROutputStream();
            profileDataStream.beginEncapsulatedArray();
            MultipleComponentProfileHelper.write( profileDataStream, components );

            tp = new TaggedProfile
            (
                TAG_MULTIPLE_COMPONENTS.value,
                profileDataStream.getBufferCopy()
            );
            taggedProfileVector.addElement( tp );
        }
        else //GIOP 1.1 or 1.2
        {
            ProfileBody_1_1 pb1_1 = new ProfileBody_1_1
            (
                new org.omg.IIOP.Version( (byte) 1, (byte) giop_minor ),
                host,
                port,
                object_key,
                components
            );
            ProfileBody_1_1Helper.write( profileDataStream, pb1_1 );

            tp = new TaggedProfile
            (
                TAG_INTERNET_IOP.value,
                profileDataStream.getBufferCopy()
            );
            taggedProfileVector.addElement( tp );
        }

        // copy the profiles into the IOR

        TaggedProfile[] tps = new TaggedProfile[ taggedProfileVector.size() ];
        taggedProfileVector.copyInto( tps );

        return new IOR( repId, tps );
    }

    public static IOR createObjectIOR( String host,
                                       short port,
                                       byte[] object_key,
                                       int giop_minor,
                                       TaggedComponent[] components )
    {
        String repId = "IDL:org.omg/CORBA/Object:1.0";
        boolean orbTypeSet = false;
        Vector components_v = new Vector();
        TaggedComponent[] allComponents = null;

        // search for the ORB type ID component

        for( int i = 0; i < components.length; i++ )
        {
            if( components[i].tag == TAG_ORB_TYPE.value )
            {
                orbTypeSet = true;
            }
            components_v.addElement( components[i] );
        }

        if( ! orbTypeSet )
        {
            // set the ORB type ID component to JacORB

            CDROutputStream orbIDComponentDataStream = new CDROutputStream();
            orbIDComponentDataStream.beginEncapsulatedArray();
            orbIDComponentDataStream.write_long( ORBConstants.JACORB_ORB_ID );

            TaggedComponent orbIDComponent = new TaggedComponent
            (
                TAG_ORB_TYPE.value,
                orbIDComponentDataStream.getBufferCopy()
            );
            components_v.addElement( orbIDComponent );
        }

        // all components for the profiles must be created by now

        allComponents = new TaggedComponent[ components_v.size() ];
        components_v.copyInto( allComponents );

        CDROutputStream profileDataStream = new CDROutputStream();
        profileDataStream.beginEncapsulatedArray();

        TaggedProfile tp = null;
        Vector taggedProfileVector = new Vector();

        if( giop_minor == 0 )
        {
            ProfileBody_1_0 pb1_0 = new ProfileBody_1_0
            (
                new org.omg.IIOP.Version( (byte) 1, (byte) 0 ),
                host,
                port,
                object_key
            );
            ProfileBody_1_0Helper.write( profileDataStream, pb1_0 );

            tp = new TaggedProfile
            (
                TAG_INTERNET_IOP.value,
                profileDataStream.getBufferCopy()
            );
            taggedProfileVector.addElement( tp );

            // now fill the last IOR profile with components

            profileDataStream = new CDROutputStream();
            profileDataStream.beginEncapsulatedArray();
            MultipleComponentProfileHelper.write( profileDataStream, allComponents );

            tp = new TaggedProfile
            (
                TAG_MULTIPLE_COMPONENTS.value,
                profileDataStream.getBufferCopy()
            );
            taggedProfileVector.addElement( tp );
        }
        else //GIOP 1.1 or 1.2
        {
            ProfileBody_1_1 pb1_1 = new ProfileBody_1_1
            (
                new org.omg.IIOP.Version( (byte) 1, (byte) giop_minor ),
                host,
                port,
                object_key,
                allComponents
            );
            ProfileBody_1_1Helper.write( profileDataStream, pb1_1 );

            tp = new TaggedProfile
            (
                TAG_INTERNET_IOP.value,
                profileDataStream.getBufferCopy()
            );
            taggedProfileVector.addElement( tp );
        }

        // copy the profiles into the IOR

        TaggedProfile[] tps = new TaggedProfile[ taggedProfileVector.size() ];
        taggedProfileVector.copyInto( tps );

        return new IOR( repId, tps );
    }


    /**
     * The TargetAddress struct provides three different ways of
     * transporting the object key. Since most of jacorbs structure
     * only wants to access the object_key, we transform the original
     * target address to the object_key type.
     */
    public static void unfiyTargetAddress( TargetAddress addr )
    {
        if( addr.discriminator() == ProfileAddr.value )
        {
            IIOPProfile p =
                new IIOPProfile (addr.profile().profile_data);
            addr.object_key( p.getObjectKey() );
        }
        else if( addr.discriminator() == ReferenceAddr.value )
        {
            IORAddressingInfo info = addr.ior();

            ParsedIOR pior = new ParsedIOR( info.ior );
            pior.effectiveProfile =
              (IIOPProfile)pior.profiles.get (info.selected_profile_index);

            addr.object_key( pior.get_object_key() );
        }
    }

    public String getCodebaseComponent()
    {
        return components.getStringComponent (TAG_JAVA_CODEBASE.value);
    }

    /**
     * Creates a new <code>ParsedIOR</code> instance.
     *
     * @param object_reference a <code>String</code> value
     * @param orb an <code>org.omg.CORBA.ORB</code> value
     * @exception IllegalArgumentException if an error occurs
     */
    public ParsedIOR( String object_reference, org.omg.CORBA.ORB orb)
        throws IllegalArgumentException
    {
        if (orb instanceof ORB)
        {
            this.orb = (org.jacorb.orb.ORB)orb;
            parse( object_reference );
        }
        else
        {
            throw new IllegalArgumentException
                ("Construct ParsedIOR with full ORB not Singleton");
        }
    }

    /**
     * Creates a new <code>ParsedIOR</code> instance.
     *
     * @param object_reference a <code>String</code> value
     * @param orb an <code>org.jacorb.orb.ORB</code> value
     * @exception IllegalArgumentException if an error occurs
     */
    public ParsedIOR( String object_reference, ORB orb )
        throws IllegalArgumentException
    {
        this.orb = orb;
        parse( object_reference );
    }

    public ParsedIOR( IOR _ior )
    {
        decode( _ior );
    }

    public boolean equals( Object o )
    {
        return o instanceof ParsedIOR &&
            ((ParsedIOR) o).ior_str.equals( ior_str );
    }

    /**
     * Init must be deferred from the constructor because it must be
     * possible to have IORs without an effective profile. The
     * exception may only be thrown on the first invocation of that
     * reference.
     */

    public void init()
    {
        if( isNull() )
            throw new org.omg.CORBA.INV_OBJREF( "Trying to use NULL reference" );

        if (effectiveProfile == null)
        {
            throw new org.omg.CORBA.INV_OBJREF( "No TAG_INTERNET_IOP found in object_reference" );
        }

        int port = effectiveProfile.getAddress().getPort();

        CompoundSecMechList sas
            = (CompoundSecMechList)effectiveProfile.getComponent
                                           (TAG_CSI_SEC_MECH_LIST.value,
                                            CompoundSecMechListHelper.class);
        if (sas != null)
            use_sas = true;

        SSL ssl = (SSL)effectiveProfile.getComponent
                                           (TAG_SSL_SEC_TRANS.value,
                                            SSLHelper.class);
        if( sas != null &&
            ssl != null )
        {
            ssl.target_requires |= sas.mechanism_list[0].target_requires;
        }

        // SSL usage is decided the following way: At least one side
        // must require it. Therefore, we first check if it is
        // supported by both sides, and then if it is required by at
        // least one side. The distinction between
        // EstablishTrustInTarget and EstablishTrustInClient is
        // handled at the socket factory layer.

        //the following is used as a bit mask to check, if any of
        //these options are set
        int minimum_options =
            Integrity.value |
            Confidentiality.value |
            DetectReplay.value |
            DetectMisordering.value |
            EstablishTrustInTarget.value |
            EstablishTrustInClient.value;

        int client_required = 0;
        int client_supported = 0;

        //only read in the properties if ssl is really supported.
        if(  Environment.isPropertyOn( "jacorb.security.support_ssl" ))
        {
            client_required =
                Environment.getIntProperty( "jacorb.security.ssl.client.required_options", 16 );

            client_supported =
                Environment.getIntProperty( "jacorb.security.ssl.client.supported_options", 16 );
        }

        if( ssl != null && // server knows about ssl...
            ((ssl.target_supports & minimum_options) != 0) && //...and "really" supports it
            Environment.isPropertyOn( "jacorb.security.support_ssl" ) && //client knows about ssl...
            ((client_supported & minimum_options) != 0 )&& //...and "really" supports it
            ( ((ssl.target_requires & minimum_options) != 0) || //server ...
              ((client_required & minimum_options) != 0))) //...or client require it
        {
            Debug.output( 1, "Selecting SSL for connection");
            use_ssl = true;
            port = ssl.port;
        }
        //prevent client policy violation, i.e. opening plain TCP
        //connections when SSL is required
        else if( ssl == null && // server doesn't know ssl...
                 Environment.isPropertyOn( "jacorb.security.support_ssl" ) && //client knows about ssl...
                 ((client_required & minimum_options) != 0)) //...and requires it
        {
            throw new org.omg.CORBA.NO_PERMISSION( "Client-side policy requires SSL, but server doesn't support it" );
        }
        else
        {
            use_ssl = false;
        }

        iiopAddress = new IIOPAddress (effectiveProfile.getAddress().getHost(),
                                       port);
    }

    public boolean useSSL()
    {
        return use_ssl;
    }

    public boolean useSAS()
    {
        return use_sas;
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
            //Debug.output( 4, "Parsing IOR, found profile id: " +
            //              _ior.profiles[i].tag );

            switch( _ior.profiles[i].tag )
            {
                case TAG_MULTIPLE_COMPONENTS.value :
                {
                    components = new TaggedComponentList
                                           (_ior.profiles[i].profile_data);
                    break;
                }
                case TAG_INTERNET_IOP.value :
                {
                    profiles.add (new IIOPProfile
                                         (_ior.profiles[i].profile_data));
                    break;
                }
            }
        }

        /* Select the effective profile. We take the one with the
           highest minor version number. */
        if (profiles.size() > 0)
        {
            effectiveProfile = (IIOPProfile)profiles.get(0);
            for (int i=1; i<profiles.size(); i++)
            {
                Profile p = (Profile)profiles.get(i);
                if (p.version().minor > effectiveProfile.version().minor)
                    effectiveProfile = (IIOPProfile)p;
            }
        }

        ior = _ior;
        ior_str = getIORString();

        if( effectiveProfile != null )
        {
            cs_info   = (CodeSetComponentInfo)getComponent
                                            (TAG_CODE_SETS.value,
                                             CodeSetComponentInfoHelper.class);
            orbTypeId = getLongComponent (TAG_ORB_TYPE.value);
        }
    }

    /**
     * initialize this ParsedIOR by decoding a CorbaLoc address
     */

    public void decode( CorbaLoc corbaLoc )
    {
        IOR ior = null;
        CorbaLoc.ObjectAddress address = corbaLoc.objectAddressList[0];

        if( address.protocol_identifier.equals("rir"))
        {
            try
            {
                org.omg.CORBA.Object obj =
                    orb.resolve_initial_references(corbaLoc.getKeyString());

                if (obj == null)
                {
                    throw new IllegalArgumentException
                        ("Unable to resolve reference for " + corbaLoc.getKeyString());
                }

                ior =
                    ((Delegate)((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate()).getIOR();
            }
            catch( Exception e )
            {
                Debug.output(2, e );
                throw new IllegalArgumentException("Invalid corbaloc: URL");
            }
        }
        else if( address.protocol_identifier.equals("iiop"))
        {
            ior = createObjectIOR( address.host,
                                   (short) address.port,
                                   orb.mapObjectKey (corbaLoc.getKey ()),
                                   address.minor );
        }
        else if( address.protocol_identifier.equals("ssliop") )
        {
            SSL ssl = new SSL();
            ssl.port = (short) address.port;

            String supported_str =
                Environment.getProperty( "jacorb.security.ssl.corbaloc_ssliop.supported_options" );

            if( (supported_str != null) &&
                (! supported_str.equals( "" )) )
            {
                try
                {
                    ssl.target_supports = (short)
                        Integer.parseInt( supported_str, 16 );
                }
                catch( NumberFormatException nfe )
                {
                    Debug.output( 0, "WARNING: Unable to create int from string >>" +
                                  supported_str + "<<" );
                    Debug.output( 0, "Please check property \"jacorb.security.ssl.corbaloc_ssliop.supported_options\"" );

                    ssl.target_supports = EstablishTrustInTarget.value;
                }
            }
            else
            {
                //For the time being, we only uses EstablishTrustInTarget,
                //because we don't handle any of the other options anyway.
                ssl.target_supports = EstablishTrustInTarget.value;
            }


            String required_str =
                Environment.getProperty( "jacorb.security.ssl.corbaloc_ssliop.required_options" );

            if( (required_str != null) &&
                (! required_str.equals( "" )) )
            {
                try
                {
                    ssl.target_supports = (short)
                        Integer.parseInt( required_str, 16 );
                }
                catch( NumberFormatException nfe )
                {
                    Debug.output( 0, "WARNING: Unable to create int from string >>" +
                                  required_str + "<<" );
                    Debug.output( 0, "Please check property \"jacorb.security.ssl.corbaloc_ssliop.required_options\"" );

                    ssl.target_supports = EstablishTrustInTarget.value;
                }
            }
            else
            {
                //For the time being, we only uses EstablishTrustInTarget,
                //because we don't handle any of the other options anyway
                ssl.target_requires = EstablishTrustInTarget.value;
            }

            //create the tagged component containing the ssl struct
            CDROutputStream out = new CDROutputStream();
            out.beginEncapsulatedArray();

            SSLHelper.write( out, ssl );

            TaggedComponent ssl_c =
                new TaggedComponent( TAG_SSL_SEC_TRANS.value,
                                     out.getBufferCopy() );

            ior =  createObjectIOR( address.host,
                                    (short) address.port,
                                    orb.mapObjectKey (corbaLoc.getKey ()),
                                    address.minor,
                                    new TaggedComponent[]{ssl_c});
        }

        decode( ior );
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
                Debug.output(2,e);
                throw new org.omg.CORBA.UNKNOWN("Error in building IIOP-IOR");
            }
        }

        return ior_str;
    }

    public byte[] get_object_key()
    {
        return effectiveProfile.getObjectKey();
    }

    public List getProfiles()
    {
        return profiles;
    }

    public Profile getEffectiveProfile()
    {
        return effectiveProfile;
    }

    public IIOPAddress getIIOPAddress()
    {
        return iiopAddress;
    }

    public String getTypeId()
    {
        return ior.type_id;
    }

    public String getIDString ()
    {
        StringBuffer buff = new StringBuffer (getTypeId ());
        buff.append (":");
        byte[] key = get_object_key ();

        for (int j = 0; j < key.length; j++)
        {
            buff.append (lookup [(key[j] >> 4) & 0xF]);
            buff.append (lookup [(key[j]     ) & 0xF]);
        }

        return (buff.toString ());
    }

    public TaggedComponentList getMultipleComponents()
    {
        return components;
    }

    public boolean isNull()
    {
        return ( ior.type_id.equals("") && ( ior.profiles.length == 0 ));
    }

    /**
     * <code>parse</code> decodes the object_reference passed to ParsedIOR.
     *
     * @param object_reference a <code>String</code> value.
     * @exception IllegalArgumentException if object_reference is null or the
     * designated resource cannot be found.
     */
    protected void parse (String object_reference)
        throws IllegalArgumentException
    {
        if (object_reference == null)
        {
            throw new IllegalArgumentException ("Null object reference");
        }

        if (object_reference.startsWith ("IOR:"))
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream ();
            int cnt = (object_reference.length () - 4) / 2;
            for (int j = 0; j < cnt; j++)
            {
                char c1 = object_reference.charAt (j*2+4);
                char c2 = object_reference.charAt (j*2+5);
                int i1 = (c1 >= 'a') ? (10 + c1 - 'a') :
                    ((c1 >= 'A') ? (10 + c1 - 'A') :
                     (c1 - '0'));
                int i2 = (c2 >= 'a') ? (10 + c2 - 'a') :
                    ((c2 >= 'A') ? (10 + c2 - 'A') :
                     (c2 - '0'));
                bos.write ((i1*16+i2));
            }

            CDRInputStream in_ = null;

            if (orb == null)
            {
                in_ = new CDRInputStream (org.omg.CORBA.ORB.init(),
                                          bos.toByteArray ());
            }
            else
            {
                in_ = new CDRInputStream (orb, bos.toByteArray());
            }

            endianness = in_.read_boolean ();
            if (endianness)
            {
                in_.setLittleEndian (true);
            }

            IOR _ior = IORHelper.read (in_);
            decode (_ior);
        }
        else if (object_reference.startsWith ("corbaloc:"))
        {
            decode (new CorbaLoc (object_reference));
        }
        else if (object_reference.startsWith ("corbaname:"))
        {
            String corbaloc = "corbaloc:";
            String name = "";

            if (object_reference.indexOf('#') == -1)
            {
                corbaloc +=
                    object_reference.substring (object_reference.indexOf (':') + 1);
            }
            else
            {
                corbaloc +=
                    object_reference.substring (object_reference.indexOf (':') + 1,
                                                object_reference.indexOf ('#'));
                name =
                    object_reference.substring (object_reference.indexOf ('#') + 1);
            }

            /* empty key string in corbaname becomes NameService */
            if (corbaloc.indexOf ('/') == -1)
            {
                corbaloc += "/NameService";
            }

            Debug.output (4,corbaloc);

            try
            {
                NamingContextExt n =
                    NamingContextExtHelper.narrow (orb.string_to_object (corbaloc));
                IOR ior = null;

                // If the name hasn't been set - which is possible if we're just
                // resolving the root context down try to use name.
                if ( name.length() > 0 )
                {
                    org.omg.CORBA.Object target = n.resolve_str (name);
                    ior = ((Delegate)((org.omg.CORBA.portable.ObjectImpl)target)._get_delegate()).getIOR();
                }
                else
                {
                    ior = ((Delegate)((org.omg.CORBA.portable.ObjectImpl)n)._get_delegate()).getIOR();
                }
                decode (ior);
            }
            catch (Exception e)
            {
                Debug.output (4, e);
                throw new IllegalArgumentException ("Invalid object reference: " + object_reference);
            }
        }
        else if (object_reference.startsWith ("resource:"))
        {
            String resourceName = object_reference.substring (9);
            Debug.output (2, "Trying to resolve URL/IOR from resource: " + resourceName);

            ClassLoader cl = getClass().getClassLoader ();
            if (cl == null)
            {
                //#ifjdk 1.2
                    cl = ClassLoader.getSystemClassLoader ();
                //#else
                //# throw new RuntimeException ("couldn't find class loader");
                //#endif
            }

            URL url = cl.getResource (resourceName);
            if (url == null)
            {
                throw new IllegalArgumentException ("Failed to get resource: " + resourceName);
            }

            String content = ObjectUtil.readURL (url.toString ());
            if (content == null)
            {
                throw new IllegalArgumentException ("Failed to read resource: " + resourceName);
            }

            parse (content);
        }
        else if (object_reference.startsWith ("jndi:"))
        {
            String jndiName = object_reference.substring (5);
            Debug.output (2, "Trying to resolve JNDI/IOR from name: " + jndiName);

            java.lang.Object obj = null;
            try
            {
                //                javax.naming.Context initialContext = new javax.naming.InitialContext ();
                //                obj = initialContext.lookup (jndiName);
                //
                // Replaced lines above with reflected equivalent so will compile under JDK < 1.3
                // which do not include javax.naming classes. For jndi based name resolution to
                // work obviously javax.naming classes must be in CLASSPATH.
                //
                Class[] types = new Class [1];
                java.lang.Object[] params = new java.lang.Object[1];

                Class cls = Class.forName ("javax.naming.InitialContext");
                java.lang.Object initialContext = cls.newInstance ();

                types[0] = String.class;
                params[0] = jndiName;

                java.lang.reflect.Method method = cls.getMethod ("lookup", types);
                obj = method.invoke (initialContext, params);
            }
            catch (Exception ex)
            {
                throw new IllegalArgumentException ("Failed to lookup JNDI/IOR: " + ex);
            }

            if (obj == null)
            {
                throw new IllegalArgumentException ("Null JNDI/IOR: " + object_reference);
            }

            parse (obj.toString ());
        }
        else
        {
            Debug.output (2, "Trying to resolve URL/IOR from: " + object_reference);
            String content = ObjectUtil.readURL (object_reference);
            if (content == null)
            {
                throw new IllegalArgumentException ("Invalid or unreadable URL/IOR: " + object_reference);
            }
            parse (content);
        }
        ior_str = getIORString ();
    }

    /**
     * Returns the component with the given tag, searching
     * in the top level components first, then in the
     * components of the effective profile.  If no component
     * with the given tag exists, return null.
     */
    private Object getComponent (int tag, Class helper)
    {
        Object result = components.getComponent (tag, helper);
        if (result != null)
            return result;
        else
            return effectiveProfile.getComponent (tag, helper);
    }

    private static class LongHelper
    {
        public static Integer read (org.omg.CORBA.portable.InputStream in)
        {
            return new Integer (in.read_long());
        }
    }

    private Integer getLongComponent (int tag)
    {
        Object result = components.getComponent (tag, LongHelper.class);
        if (result != null)
            return (Integer)result;
        else
            return (Integer)effectiveProfile.getComponent (tag, LongHelper.class);
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
        else
        {
            return false;
        }
    }
}
