package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.io.*;
import java.net.*;
import java.util.*;

import org.jacorb.orb.util.*;
import org.jacorb.util.*;

import org.omg.IOP.*;
import org.omg.GIOP.*;
import org.omg.IIOP.*;
import org.omg.CosNaming.*;
import org.omg.CONV_FRAME.*;

/**
 * Class to convert IOR strings into IOR structures
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class ParsedIOR 
{
    private int effectiveProfileBody = 0;
    protected ProfileBody_1_1[] profileBodies = null; 

    /** top-level tagged componenents, i.e. not part of IOP components    */
    public TaggedComponent[] taggedComponents = new TaggedComponent[0];
    public TaggedProfile[]  effectiveProfile;

    protected boolean endianness = false;
    protected String ior_str = null;
    private IOR ior = null;
    
    private org.jacorb.orb.ORB orb;

    private CodeSetComponentInfo cs_info = null;

    /* static part */

    /** 
     * factory method
     */

    public static IOR createIOR( String typeId, 
                                 ProfileBody_1_0 profileBody )
    {
	IOR ior = new IOR();
	ior.type_id = typeId;
	ior.profiles = new TaggedProfile[1];
	ior.profiles[0] = new TaggedProfile();
	ior.profiles[0].tag = 0; // IIOP

	CDROutputStream out = new CDROutputStream();
        out.beginEncapsulatedArray();
	ProfileBody_1_0Helper.write( out, profileBody );

	ior.profiles[0].profile_data = out.getBufferCopy();

	return ior;
    }

    public static IOR createIOR( String typeId, 
                                 ProfileBody_1_1 profileBody )
    {
	IOR ior = new IOR();
	ior.type_id = typeId;
	ior.profiles = new TaggedProfile[1];
	ior.profiles[0] = new TaggedProfile();
	ior.profiles[0].tag = 0; // IIOP

	CDROutputStream out = new CDROutputStream();
        out.beginEncapsulatedArray();
	ProfileBody_1_1Helper.write( out, profileBody );

	ior.profiles[0].profile_data = out.getBufferCopy();

	return ior;
    }


    public static ProfileBody_1_1 getProfileBody( byte[] profile, 
                                                  int min_minor )
    {
	ProfileBody_1_1 _profile_body = null;
	CDRInputStream in = new CDRInputStream((org.omg.CORBA.ORB)null, profile);
	try
        {
	    // look for all profiles, if we found TaggedComponents
	    // we'll extract them
	    in.openEncapsulatedArray();
      
	    // mark position because we will pre-read version from stream
	    in.mark(0); 
      
	    // first read the version and observe if we have already
	    // decoded newer version of IIOP IOR
	    int minor = org.omg.IIOP.VersionHelper.read(in).minor;
      
	    if( ( minor < min_minor) || (minor > 2) ) 
		return null;
      
	    // return to start of profile body stream
	    in.reset();
      
	    switch(minor){
	    case 2: // 1.2 is compatible with 1.1
	    case 1:
		_profile_body = ProfileBody_1_1Helper.read(in);
		break;
	    case 0:
		// convert profile body 1.0 -> 1.1 by adding empty or existing tagged
		// components (should be always empty because if we already read >1.0
		// profile version, we should never encounter these lines)
		ProfileBody_1_0 pb0;
		pb0 = ProfileBody_1_0Helper.read(in);
		_profile_body = new ProfileBody_1_1(pb0.iiop_version,
								 pb0.host,
								 pb0.port,
								 pb0.object_key,
								 new TaggedComponent[0]);
		// taggedComponents);
		if( _profile_body.port < 0 )
		    _profile_body.port += 65536;
		break;
	    }
	}
        catch ( Exception ex )
        {
	    Debug.output( 2, ex );
	    throw new org.omg.CORBA.INV_OBJREF();
	};

	return _profile_body;  
    }

    public static org.omg.SSLIOP.SSL getSSLTaggedComponent( TaggedComponent[] components )
    {
        boolean found_ssl = false;
        for ( int i = 0; i < components.length; i++ )
        {
            if( components[i].tag == 20 ) //TAG_SSL_SEC_TRANS
            {
                found_ssl = true;

                CDRInputStream in =
                    new CDRInputStream( (org.omg.CORBA.ORB)null, 
                                        components[ i ].component_data );
                try
                {
                    in.openEncapsulatedArray();
                    return org.omg.SSLIOP.SSLHelper.read( in );
                } 
                catch ( Exception ex ) 
                { 
                    return null; 
                }
            }
        }
        return null;
    }


    public static org.omg.SSLIOP.SSL getSSLTaggedComponent( 
                                   ProfileBody_1_1 profileBody )
    {
        if ( profileBody == null ||
             profileBody.iiop_version == null ||
             ( char )profileBody.iiop_version.minor == (( char ) 0 ) ||
             profileBody.components == null
             )
        {
            return null;
        }

        /* else: */

        boolean found_ssl = false;
        for ( int i = 0; i < profileBody.components.length; i++ )
        {
            if( profileBody.components[i].tag == 20 ) //TAG_SSL_SEC_TRANS
            {
                found_ssl = true;

                Debug.output( 8, "Component data",
                              profileBody.components[ i ].component_data);

                CDRInputStream in =
                    new CDRInputStream((org.omg.CORBA.ORB)null, 
                                       profileBody.components[ i ].component_data );
                try
                {
                    in.openEncapsulatedArray();
                    return org.omg.SSLIOP.SSLHelper.read( in );
                } 
                catch ( Exception ex ) 
                {
                    return null; 
                }
            }
        }
        return null;
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
            TaggedProfile p = addr.profile();
            
            ProfileBody_1_1 body = 
                getProfileBody( addr.profile().profile_data, 0 );
            
            addr.object_key( body.object_key );
        }
        else if( addr.discriminator() == ReferenceAddr.value )
        {
            IORAddressingInfo info = addr.ior();
            
            ParsedIOR pior = new ParsedIOR( info.ior );
            pior.effectiveProfileBody = info.selected_profile_index;
            
            addr.object_key( pior.get_object_key() );
        }
    }

    /* instance part */

    public ParsedIOR( String object_reference )
    {
	parse( object_reference );
    }

    public ParsedIOR( String object_reference, ORB orb )
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
     * When multiple internet IOP tags are present, they will probably
     * have different versions, we will use the highest version
     * between 0 and 1.  
     */
    public void decode( IOR _ior) 
    {
	boolean iiopFound = false;

        Vector internetProfiles = new Vector();
        Vector multipleComponentsProfiles = new Vector();

	for ( int i = 0; i <_ior.profiles.length; i++ )
	{
	    Debug.output( 4, "Parsing IOR, found profile id: " +
                                      _ior.profiles[i].tag );
	    switch( _ior.profiles[i].tag ) 
	    {
                case TAG_MULTIPLE_COMPONENTS.value :
                {
                    Debug.output( 4, "TAG_MULTIPLE_COMPONENTS found in IOR" );
                    
                    CDRInputStream in = 
                        new CDRInputStream( (org.omg.CORBA.ORB)null,
                                            _ior.profiles[i].profile_data );
                    in.openEncapsulatedArray();

                    taggedComponents = MultipleComponentProfileHelper.read( in );
                    break;	
                }
                case TAG_INTERNET_IOP.value :
                {
                    Debug.output(4, "TAG_INTERNET_IOP found in IOR");
                    // decode Internet IOP profile

                    ProfileBody_1_1 body = 
                        getProfileBody( _ior.profiles[i].profile_data, 0 );
          
                    if ( body != null )
                    {
                        internetProfiles.addElement( body );
                        Debug.output( 4, "IOP 1.1 decoded" ); 
                    }
                    
                    iiopFound = true;
                    break;
                }
            } 
        }

        profileBodies = 
            new ProfileBody_1_1[ internetProfiles.size() ];
        internetProfiles.copyInto( profileBodies );

        effectiveProfileBody = 0;
        
        /* select the effective profile. We take the one with the
           highest minor version  number */
        for( int b = 1; b < profileBodies.length; b++)
        {
            if( profileBodies[b].iiop_version.minor > 
                profileBodies[ effectiveProfileBody ].iiop_version.minor )
            {
                effectiveProfileBody = b;
            }
        }

	ior = _ior;
	ior_str = getIORString();

        //retrieve the codeset component
        for( int i = 0; i < taggedComponents.length; i++ )
        {
	    if( taggedComponents[i].tag == TAG_CODE_SETS.value )
            {
                // get server cs from IOR 
                CDRInputStream is =
                    new CDRInputStream( orb, 
                                        taggedComponents[i].component_data);
                
                is.openEncapsulatedArray();
                
                cs_info = CodeSetComponentInfoHelper.read( is );
            
                break;
            }
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
		org.omg.CORBA.Object obj = orb.resolve_initial_references(corbaLoc.getKeyString());
		ior = ((Delegate)((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate()).getIOR();
	    }
	    catch( Exception e )
	    {
		Debug.output(2, e );
		throw new IllegalArgumentException("Invalid corbaloc: URL");
	    }
	}
	else if( address.protocol_identifier.equals("iiop"))
	{
	    ProfileBody_1_0 profile_body = 
		new ProfileBody_1_0( address.getVersion(),
                                     address.host,
                                     (short) address.port,
                                     corbaLoc.getKey());

	    ior = createIOR( "IDL:org.omg/CORBA/Object:1.0", profile_body);
	}
	decode( ior );
    }

    public CodeSetComponentInfo getCodeSetComponentInfo()
    {
        return cs_info;
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
		CDROutputStream out = new CDROutputStream();

       		// endianness = false, big-endian
		out.write_boolean(false);
		IORHelper.write(out,ior);

		byte bytes[] = out.getBufferCopy();
		StringBuffer sb = new StringBuffer("IOR:");
		for (int j=0; j<bytes.length; j++)
		{
		    int b = bytes[j];
		    if(b<0) b+= 256;
		    int n1 = (0xff & b) / 16;
		    int n2 = (0xff & b) % 16;
		    int c1 = (n1 < 10) ? ('0' + n1) : ('a' + (n1 - 10));
		    int c2 = (n2 < 10) ? ('0' + n2) : ('a' + (n2 - 10));
		    //java.lang.System.out.println("" + b +","+ n1 +","+ n2 +","+ (char)c1 + (char)c2);
		    sb.append((char)c1);
		    sb.append((char)c2);
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

    public String getObjKey()
    {
	return new String( profileBodies[ effectiveProfileBody ].object_key );
    }

    public byte[] get_object_key()
    {
	return profileBodies[ effectiveProfileBody ].object_key;
    }

    public ProfileBody_1_1 getProfileBody() // chg by devik
    {
        if( profileBodies.length > effectiveProfileBody )
        {
            return profileBodies[ effectiveProfileBody ];
        }
        else
        {
            return null;
        }            
    }

    public ProfileBody_1_1[] getProfileBodies()
    {
        return profileBodies;
    }

    public TaggedProfile getEffectiveProfile() // chg by bnv
    {
        if( profileBodies.length > effectiveProfileBody )
        {
            return ior.profiles[ effectiveProfileBody ];
        }
        else
        {
            return null;
        }            
    }

    public String getAddress()
    {
	int port = profileBodies[ effectiveProfileBody ].port;
	if( port < 0 )
	    port += 65536;
	return profileBodies[ effectiveProfileBody ].host + ":" + port;
    }

    public String getTypeId() 
    {
	return ior.type_id;
    }

    public boolean isNull()
    {
	return ( ior.type_id.equals("") && ( ior.profiles.length == 0 ));
    }

    protected void parse( String object_reference )
    {
	if (object_reference.startsWith("IOR:"))
	{
	    ior_str = object_reference;
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    int cnt = (object_reference.length()-4) / 2;
	    for(int j=0; j<cnt; j++)
	    {
		char c1 = object_reference.charAt(j*2+4);
		char c2 = object_reference.charAt(j*2+5);
		int i1 = (c1 >= 'a') ? (10 + c1 - 'a') :
		    ((c1 >= 'A') ? (10 + c1 - 'A') :
		     (c1 - '0'));
		int i2 = (c2 >= 'a') ? (10 + c2 - 'a') :
		    ((c2 >= 'A') ? (10 + c2 - 'A') :
		     (c2 - '0'));
		bos.write((i1*16+i2));
	    }

	    CDRInputStream in_ = null;
            
            if( orb == null )
            {
                in_ = new CDRInputStream( org.omg.CORBA.ORB.init(), 
                                          bos.toByteArray() );
            }
            else
            {
                in_ = new CDRInputStream( orb, bos.toByteArray() );
            }                
	    
	    endianness = in_.read_boolean();
	    if(endianness)
		in_.setLittleEndian(true);
	    
	    IOR _ior =  IORHelper.read(in_);
	    decode( _ior );
	}
	else if (object_reference.startsWith("corbaloc:"))
	{
	    decode( new CorbaLoc( object_reference ));
	}
	else if (object_reference.startsWith("http://") || 
                 object_reference.startsWith("file:/") )
	{
	    parse( ObjectUtil.readURL(object_reference));
	}
	else if(object_reference.startsWith("corbaname:") )
	{
	    String corbaloc;
	    String name = "";

	    if( object_reference.indexOf('#') == -1 )
		corbaloc = "corbaloc:" + object_reference.substring(object_reference.indexOf(':')+1 );
	    else
	    {
		corbaloc = "corbaloc:" + object_reference.substring(object_reference.indexOf(':')+1, 
								    object_reference.indexOf('#'));
		name = object_reference.substring(object_reference.indexOf('#')+1);
	    }

	    /* empty key string in corbaname becomes NameService */
	    if( corbaloc.indexOf('/') == -1 )
		corbaloc += "/NameService";

	    Debug.output(4,corbaloc);

	    try
	    {
		NamingContextExt n = NamingContextExtHelper.narrow( orb.string_to_object(corbaloc));
		org.omg.CORBA.Object target = n.resolve_str( name );
		IOR ior = ((Delegate)((org.omg.CORBA.portable.ObjectImpl)target)._get_delegate()).getIOR();
		decode(ior);
	    }
	    catch( Exception e )
	    {
		Debug.output(4, e );
		throw new RuntimeException("Invalid object reference: " + object_reference);
	    }
	}
	else
	    throw new RuntimeException("Invalid IOR format: " + object_reference );
    }




}


