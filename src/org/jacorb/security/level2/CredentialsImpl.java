package org.jacorb.security.level2;

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


import org.omg.Security.*;
import org.omg.SecurityLevel2.*;

import java.io.*;
import java.util.*;
import java.math.BigInteger;


/**
 *  JacORB implementation of security Credentials
 *
 *  $Id$
 *
 */

public class CredentialsImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.SecurityLevel2.Credentials, 
               Serializable //for making a deep copy
{
    private SecAttribute[] my_attributes;
  
    private short accepting_options_supported;
    private short accepting_options_required;
    private short invocation_options_supported;
    private short invocation_options_required;

    private AuthenticationStatus authStatus = null;
    private InvocationCredentialsType type = null; 

    /** SecurityFeature is an enum, its integer value is the index */
    private boolean [] securityFeaturesForRequests;
    private boolean [] securityFeaturesForReplies;


    //private BigInteger hash = null;

    private SecAttributeManager attrib_mgr = null;

    private boolean dirty = true;
        
    public CredentialsImpl( SecAttribute[] attributes,
                            AuthenticationStatus status,
                            InvocationCredentialsType type )    
    {
        this.authStatus = status;
        this.my_attributes = attributes;
        this.type = type;

        attrib_mgr = SecAttributeManager.getInstance();
    }

//      public int hashCode()
//      {
//          return hash.hashCode();
//      }

//      private void rehash()
//      {
//          MessageDigest md = MessageDigest.getInstance("MD5");
        
//          for( int i = 0; i < my_attributes.length; i++ )
//          {
//              KeyAndCert certs = 
//                  attrib_mgr.getAttribValue( my_attributes[i] );
            
//              md.update( certs.chain[1].getFingerprint() );
//          }
        
//          hash = new BigInteger( md.digest() );
//      }

    public Credentials copy()
    { 
        try{
            PipedOutputStream pipe_out = new PipedOutputStream();
            PipedInputStream pipe_in = new PipedInputStream(pipe_out);

            ObjectOutputStream out = new ObjectOutputStream(pipe_out);
            out.writeObject(this);
            out.flush();
            out.close();

            ObjectInputStream in = new ObjectInputStream(pipe_in);
            CredentialsImpl creds = (CredentialsImpl) in.readObject();
            in.close();

            pipe_in.close();
            pipe_out.close();

            //creds.authenticator = authenticator;

            return creds;
        }catch (Exception e){
            org.jacorb.util.Debug.output(3, e);
        }
        return null; 
    }
  
    public InvocationCredentialsType credentials_type()
    {
        return type;
    }
    
    public AuthenticationStatus authentication_state()
    {
        return authStatus;
    }
    
    public String mechanism()
    {
        return null;
    }

    public short accepting_options_supported()
    {
        return accepting_options_supported;
    }

    public void accepting_options_supported(short arg)
    {
        accepting_options_supported = arg;
    }

    public short accepting_options_required()
    {
        return accepting_options_required;
    }

    public void accepting_options_required(short arg)
    {
        accepting_options_required = arg;
    }

    public short invocation_options_supported()
    {
        return invocation_options_supported;
    }

    public void invocation_options_supported(short arg)
    {
        invocation_options_supported = arg;
    }

    public short invocation_options_required()
    {
        return invocation_options_required;
    }

    public void invocation_options_required(short arg)
    {
        invocation_options_required = arg;
    }

    /**
     * can be used in access control decisions or auditing
     *
     * @param attributes - the set of attributes whose values are desired. 
     *                     If this list is empty, all attributes are returned
     * 
     * @return The requested set of attributes reflecting the state 
     *         of the credentials
     */
    public SecAttribute[] get_attributes(AttributeType[] types)
    {
        if( types == null || types.length == 0 )
            return my_attributes;
      
        /* sort out the requested attributes */
        Vector v = new Vector();
        for( int i = 0; i < types.length; i++ )
        {
            for( int j = 0; j < my_attributes.length; j++ )
            {

                if(( my_attributes[j].attribute_type.attribute_family.family == 
                    types[i].attribute_family.family) &&
                   ( my_attributes[j].attribute_type.attribute_type == 
                     types[i].attribute_type ))
                {                    
                    v.addElement(my_attributes[j]);
                }
            } 
        }

        SecAttribute[] result = new SecAttribute[ v.size() ];
        v.copyInto( result );
      
        return result; 
    }
  
    public  void destroy()
    {
    }
  
    public void set_security_feature(CommunicationDirection direction, 
                                     SecurityFeature[] security_features)
    {
        switch( direction.value() )
        {
        case  CommunicationDirection._SecDirectionRequest:
            setFeatures( securityFeaturesForRequests, security_features);

        case CommunicationDirection._SecDirectionReply:
            setFeatures( securityFeaturesForReplies, security_features);

        case CommunicationDirection._SecDirectionBoth:
            setFeatures( securityFeaturesForRequests, security_features);
            setFeatures( securityFeaturesForReplies, security_features);
        }
    }

    private void setFeatures( boolean[] target, SecurityFeature[] features )
    {
        if( features.length > target.length )
            throw new java.lang.IllegalArgumentException("Too many features");

        for( int i = 0; i < features.length; i++ )
        {
            int value =  features[i].value();
            if( value > target.length || value < 0)
            {
                throw new java.lang.IllegalArgumentException("SecurityFeatureValue out of range");
            }
            target[ value ] = true;
        }
    }

  
    public boolean get_security_feature(CommunicationDirection direction,
                                        org.omg.Security.SecurityFeature feature)
    {
        switch( direction.value() )
        {
        case  CommunicationDirection._SecDirectionRequest:
            return securityFeaturesForRequests[feature.value()];

        case CommunicationDirection._SecDirectionReply:
            return securityFeaturesForReplies[feature.value()];

        default: //  CommunicationDirection._SecDirectionBoth:
            return securityFeaturesForRequests[feature.value()] && 
                securityFeaturesForReplies[feature.value()];

        }
    }
  
    /**
     * force_commit is ignored. Attributes are always set at once.
     * Currently only such SecAttributes are accepted, that have
     * been generated by the SecAttributeManager.
     */
    public  boolean set_privileges(boolean force_commit, 
                                   SecAttribute[] requested_privileges, 
                                   AttributeListHolder/*out*/ actual_privileges)
    {
        //check if attribs have been created by SecAttributeManager
        for( int i = 0; i < requested_privileges.length; i++ )
        {
            if( attrib_mgr.getAttributeValue( requested_privileges[i] )
                == null )
            {
                throw new Error( "SecAttribute not created by Manager" );
            }
        }
        
        //filter out requested privileges that are allowed
        Vector additional_privileges = new Vector();

        for (int i = 0; i < requested_privileges.length; i++)
            if ( requested_privileges[i].attribute_type.attribute_family.family ==
                (short) 1 ) //privilege attributes
            {
                additional_privileges.addElement( requested_privileges[i] );
            }
          
        if (additional_privileges.size() > 0)
        {
            SecAttribute[] tmp = new SecAttribute[my_attributes.length + 
                                                 additional_privileges.size()];

            //copy existing privileges into new array
            System.arraycopy( my_attributes, 0, 
                              tmp, 0, 
                              my_attributes.length );
        
            //copy additional privileges into new array
            for ( int i = 0; i < additional_privileges.size(); i++ )
            {
                SecAttribute attrib = (SecAttribute) 
                    additional_privileges.elementAt( i );

                tmp[my_attributes.length + i] = attrib;          
            }

            my_attributes = tmp;
            actual_privileges.value = tmp;

            dirty = true;

            return true;
        }
        else
            return false; 
    }
  
  
    public boolean is_valid(org.omg.Security.UtcTHolder/*out*/ expiry_time)
    { 
        return false; 
    }
  
    public  boolean refresh(byte[] refresh_data)
    { 
        return false; 
    }

    /*
     * Own methods
     */
    
    public boolean isDirty()
    {
        return dirty;
    }

    public void clearDirtyFlag()
    {
        dirty = false;
    }
}









