package org.jacorb.poa.util;

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

import org.jacorb.poa.*;
import org.jacorb.poa.except.*;
import org.omg.PortableServer.*;

import java.util.Calendar;

/**
 * This class collects some useful routines for the POA.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version $Id$
 */

public final class POAUtil 
{
    /**
     * converts an oid into a string, if the inHex flag is set
     * the string contains an hex dump
     */

    public static String convert(byte[] objectId, boolean inHex) 
    {
        if (inHex) 
        {
            String result = "";
            for (int i=0; i<objectId.length; i++) 
            {
                int n1 = (objectId[i] & 0xff) / 16;
                int n2 = (objectId[i] & 0xff) % 16;
                char c1 = (char)(n1>9 ? ('A'+(n1-10)) : ('0'+n1));
                char c2 = (char)(n2>9 ? ('A'+(n2-10)) : ('0'+n2));
                result = result + ( c1 + (c2 + " "));
            }
            return result;
			
        } 
        else 
        {
            return objectId_to_string(objectId).replace('\n', ' ');
        }
    }

    /**
     * reads the policy value from the specified policy and
     * converts it into a string
     */

    public static String convert(org.omg.CORBA.Policy policy, int policy_type) 
    {		
        switch (policy_type) 
        {       		
        case THREAD_POLICY_ID.value:
            if (policy == null || ((ThreadPolicy) policy).value() == ThreadPolicyValue.ORB_CTRL_MODEL) return "ORB_CTRL_MODEL";
            else if (((ThreadPolicy) policy).value() == ThreadPolicyValue.SINGLE_THREAD_MODEL) return "SINGLE_THREAD_MODEL";
            break;

        case LIFESPAN_POLICY_ID.value:
            if (policy == null || ((LifespanPolicy) policy).value() == LifespanPolicyValue.TRANSIENT) return "TRANSIENT";
            else if (((LifespanPolicy) policy).value() == LifespanPolicyValue.PERSISTENT) return "PERSISTENT";
            break;
				
        case ID_UNIQUENESS_POLICY_ID.value:
            if (policy == null || ((IdUniquenessPolicy) policy).value() == IdUniquenessPolicyValue.UNIQUE_ID) return "UNIQUE_ID";
            else if (((IdUniquenessPolicy) policy).value() == IdUniquenessPolicyValue.MULTIPLE_ID) return "MULTIPLE_ID";
            break;

        case ID_ASSIGNMENT_POLICY_ID.value:
            if (policy == null || ((IdAssignmentPolicy) policy).value() == IdAssignmentPolicyValue.SYSTEM_ID) return "SYSTEM_ID";
            else if (((IdAssignmentPolicy) policy).value() == IdAssignmentPolicyValue.USER_ID) return "USER_ID";
            break;
				
        case SERVANT_RETENTION_POLICY_ID.value:
            if (policy == null || ((ServantRetentionPolicy) policy).value() == ServantRetentionPolicyValue.RETAIN) return "RETAIN";
            else if (((ServantRetentionPolicy) policy).value() == ServantRetentionPolicyValue.NON_RETAIN) return "NON_RETAIN";
            break;

        case REQUEST_PROCESSING_POLICY_ID.value:
            if (policy == null || ((RequestProcessingPolicy) policy).value() == RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY) return "USE_ACTIVE_OBJECT_MAP_ONLY";
            else if (((RequestProcessingPolicy) policy).value() == RequestProcessingPolicyValue.USE_SERVANT_MANAGER) return "USE_SERVANT_MANAGER";
            else if (((RequestProcessingPolicy) policy).value() == RequestProcessingPolicyValue.USE_DEFAULT_SERVANT) return "USE_DEFAULT_SERVANT";
            break;

        case IMPLICIT_ACTIVATION_POLICY_ID.value:
            if (policy == null || ((ImplicitActivationPolicy) policy).value() == ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION) return "NO_IMPLICIT_ACTIVATION";
            else if (((ImplicitActivationPolicy) policy).value() == ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION) return "IMPLICIT_ACTIVATION";
            break;				
        }
        return "unknown";
    }

    /**
     * converts the state into a string
     */

    public static String convert(org.omg.PortableServer.POAManagerPackage.State state) 
    {
        if (state.value() == org.omg.PortableServer.POAManagerPackage.State._ACTIVE)
            return "active";
        if (state.value() == org.omg.PortableServer.POAManagerPackage.State._HOLDING)
            return "holding";
        if (state.value() == org.omg.PortableServer.POAManagerPackage.State._DISCARDING)
            return "discarding";
        if (state.value() == org.omg.PortableServer.POAManagerPackage.State._INACTIVE)
            return "inactive";
		
        return "unknown";
    }

    /**
     * extracts the impl name from a specified object key
     */

    public static String extractImplName(byte[] object_key) 
    {
        for (int i = 0; i < object_key.length; i++) 
        {
            if( object_key[i] == POAConstants.OBJECT_KEY_SEP_BYTE ) 
            {
                byte[] result = IdUtil.extract(object_key, 0, i);
                return unmaskStr( new String(result) );
            }
        }
        throw new POAInternalError("error extracting impl name from object_key: "+
                                   convert(object_key, false));
    }


    /**
     * extracts the oid from a specified object key
     */

    public static byte[] extractOID(byte[] object_key) 
    {
        for (int i=object_key.length-1; i>=0; i--) 
        {
            if (object_key[i] == POAConstants.OBJECT_KEY_SEP_BYTE) 
            {
                i++;
                byte[] result = 
                    IdUtil.extract(object_key, i, object_key.length - i);
                //				jacorb.orb.Environment.output(6, "extractOID, \t\tObject-Key: " + convert(object_key, false) + " \tOID: "+convert(result, true));
                return unmaskId(result);
            }
        }
        throw new POAInternalError("error extracting oid from object_key: "+
                                   convert(object_key, false));
    }

    /**
     * extracts the oid from a specified object reference
     */

    public static byte[] extractOID(org.omg.CORBA.Object reference) 
    {
        return ((org.jacorb.orb.Delegate) ((org.omg.CORBA.portable.ObjectImpl) reference)._get_delegate()).getObjectId();
    }

    /**
     * extracts the poa name from a specified object key
     */

    public static String extractPOAName(byte[] object_key) 
    {
        int begin = object_key.length;
        int end = 0;
        for (int i=0; i<object_key.length; i++) 
        {
            if (object_key[i] == POAConstants.OBJECT_KEY_SEP_BYTE) 
            {
                begin = i;
                break;
            }
        }
        for (int i=object_key.length-1; i>=0; i--) 
        {
            if (object_key[i] == POAConstants.OBJECT_KEY_SEP_BYTE) 
            {
                end = i;
                break;
            }
        }
        if (begin > end) 
        {
            throw new POAInternalError("error extracting poa name from object_key: "+convert(object_key, false));
        }
        if (begin == end) 
        {
            return "";
        } 
        else 
        {
            begin++;
            return new String(IdUtil.extract(object_key, begin, end-begin));
        }
    }

    /**
     * returns the policy with the specified policy_type from a policy list  
     */

    public static org.omg.CORBA.Policy getPolicy(org.omg.CORBA.Policy[] policies, int policy_type) 
    {
        if (policies != null) 
        {
            for (int i = 0; i < policies.length; i++) 
            {
                if (policies[i].policy_type() == policy_type) 
                {
                    return policies[i];
                }
            }
        }
        return null;
    }


    public static boolean isActive(org.omg.PortableServer.POAManagerPackage.State state) {
        return state.value() == org.omg.PortableServer.POAManagerPackage.State._ACTIVE ? true : false;
    }


    public static boolean isDiscarding(org.omg.PortableServer.POAManagerPackage.State state) {
        return state.value() == org.omg.PortableServer.POAManagerPackage.State._DISCARDING ? true : false;
    }


    public static boolean isHolding(org.omg.PortableServer.POAManagerPackage.State state) {
        return state.value() == org.omg.PortableServer.POAManagerPackage.State._HOLDING ? true : false;
    }


    public static boolean isInactive(org.omg.PortableServer.POAManagerPackage.State state) {
        return state.value() == org.omg.PortableServer.POAManagerPackage.State._INACTIVE ? true : false;
    }

    /**
     * masks the object key separator bytes 
     */

    public static byte[] maskId(byte[] id) 
    {
        int altered = id.length;
        for (int i=0; i<id.length; i++) 
        {
            if (id[i] == POAConstants.OBJECT_KEY_SEP_BYTE) 
            {
                altered++;
            } 
            else if (id[i] == POAConstants.MASK_BYTE) 
            {
                altered++;
            }
        }
        if (altered == id.length) return id;
		
        byte[] result = new byte[altered];

        altered = 0;
        for (int i=0; i<id.length; i++) {
            if (id[i] == POAConstants.OBJECT_KEY_SEP_BYTE) {
                result[altered] = POAConstants.MASK_BYTE;
                result[altered+1] = POAConstants.SEPA_MASK_BYTE;
                altered += 2;
				
            } else if (id[i] == POAConstants.MASK_BYTE) {
                result[altered] = POAConstants.MASK_BYTE;
                result[altered+1] = POAConstants.MASK_MASK_BYTE;
                altered += 2;
				
            } else {
                result[altered] = id[i];
                altered ++;
            }			
        }
        return result;
    }

    /**
     * masks the object key separator chars
     */

    public static String maskStr(String str) 
    {
        return new String(maskId(str.getBytes()));
    }
    /**
     * converts an iod into a string
     */
    public static String objectId_to_string(byte[] oid) {
        return new String(oid);
    }
    /**
     * converts a string into an oid
     */
    public static byte[] string_to_objectId(String str) {	
        return str.getBytes();
    }

    /**
     * unmasks the object key separator bytes 
     */

    public static byte[] unmaskId(byte[] id) 
    {
        int altered = id.length;
        for (int i=0; i<id.length; i++) 
        {
            if (id[i] == POAConstants.MASK_BYTE) 
            {
                altered--;
                i++;
            }
        }
        if (altered == id.length) return id;
		
        byte[] result = new byte[altered];

        altered = 0;
        for (int i=0; i<id.length; i++) 
        {			
            if (id[i] == POAConstants.MASK_BYTE) 
            {
                if (id[i+1] == POAConstants.MASK_MASK_BYTE) 
                {
                    result[altered] = POAConstants.MASK_BYTE;
                } 
                else if (id[i+1] == POAConstants.SEPA_MASK_BYTE) 
                {
                    result[altered] = POAConstants.OBJECT_KEY_SEP_BYTE;
					
                } 
                else 
                {
                    throw new POAInternalError("error: forbidden byte sequence \""
                                               +POAConstants.MASK_BYTE+id[i+1]+"\" (unmaskId)");
                }
                i++;
				
            } 
            else 
            {
                result[altered] = id[i];
            }
            altered++;
        }
        return result;
    }


    /**
     * unmasks the object key separator chars
     */

    public static String unmaskStr(String str) 
    {
        return new String(unmaskId(str.getBytes()));
    }
}


