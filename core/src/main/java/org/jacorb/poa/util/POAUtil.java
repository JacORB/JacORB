package org.jacorb.poa.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import java.util.ArrayList;
import java.util.List;
import org.jacorb.poa.POAConstants;
import org.jacorb.poa.except.POAInternalError;
import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;
import org.omg.PortableServer.ID_UNIQUENESS_POLICY_ID;
import org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID;
import org.omg.PortableServer.IdAssignmentPolicy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicy;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LIFESPAN_POLICY_ID;
import org.omg.PortableServer.LifespanPolicy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID;
import org.omg.PortableServer.RequestProcessingPolicy;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;
import org.omg.PortableServer.ServantRetentionPolicy;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.THREAD_POLICY_ID;
import org.omg.PortableServer.ThreadPolicy;
import org.omg.PortableServer.ThreadPolicyValue;

/**
 * This class collects some useful routines for the POA.
 *
 * @author Reimo Tiedemann, FU Berlin
 */

public final class POAUtil
{
    static private final int bytesPerLine = 20;
    private static final char[] lookup =
        new char[]{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private POAUtil () {}

    /**
     * <code>convert</code> outputs a byte oid in a hex string dump formatted
     * like e.g.:
     * 49 6d 52              ImR
     * ....
     *
     * @param data a <code>byte</code> value
     * @return a <code>String</code> value
     */
    public static String convert( byte[] data )
    {
        StringBuffer result = new StringBuffer ();
        int k = 0;

        for (int j = 0; j < data.length; j++)
        {
            result.append( toHex(data[j]));

            boolean lastLine = (j >= (data.length - 1));

            if (lastLine)
            {
                for (int p = 0;
                     p < (bytesPerLine - (j % bytesPerLine) - 1);
                     p++)
                {
                    result.append ("   ");
                }
            }

            if (((j % bytesPerLine) == (bytesPerLine - 1)) || lastLine)
            {
                for (; k <= j; k++)
                {
                    result.append ((data[k] < 32) ? '.' : (char) data[k]);
                }
            }
        }
        return result.toString ();
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
            if (object_key[i] == POAConstants.OBJECT_KEY_SEP_BYTE
                && (i==0 || object_key[i-1] != POAConstants.MASK_BYTE))
            {
                return unmaskStr( object_key, 0, i );
            }
        }
        throw new POAInternalError("error extracting impl name from object_key: "+
                                   convert(object_key));
    }


    /**
     * extracts the oid from a specified object key
     */

    public static byte[] extractOID(byte[] object_key)
    {
        for (int i=object_key.length-1; i>=0; i--)
        {
            if (object_key[i] == POAConstants.OBJECT_KEY_SEP_BYTE
                && (i==0 || object_key[i-1] != POAConstants.MASK_BYTE))
            {
                ++i;
                return unmaskId(object_key, i, object_key.length - i);
            }
        }
        throw new POAInternalError("error extracting oid from object_key: "+
                                   convert(object_key));
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
            if (object_key[i] == POAConstants.OBJECT_KEY_SEP_BYTE
                && (i==0 || object_key[i-1] != POAConstants.MASK_BYTE))
            {
                begin = i;
                break;
            }
        }
        for (int i=object_key.length-1; i>=0; i--)
        {
            if (object_key[i] == POAConstants.OBJECT_KEY_SEP_BYTE
                && (i==0 || object_key[i-1] != POAConstants.MASK_BYTE))
            {
                end = i;
                break;
            }
        }
        if (begin > end)
        {
            throw new POAInternalError("error extracting poa name from object_key: "+convert(object_key));
        }
        if (begin == end)
        {
            return "";
        }

        begin++;
        return new String(object_key, begin, end-begin);
    }


    /**
     * <code>extractScopedPOANames</code> returns a list containing the
     * poa_names. This method is faster than using a StringTokenizer.
     *
     * @param poa_name is a <code>String</code> value which may contain
     * poa_names separated by
     * {@link POAConstants#OBJECT_KEY_SEPARATOR OBJECT_KEY_SEPARATOR}
     * @return a <code>Vector</code> value
     */
    public static List<String> extractScopedPOANames (String poa_name)
    {
        final List<String> scopes = new ArrayList<String>();
        final int length = poa_name.length();

        if (length > 0)
        {
            // Fill in the list with the poa_names.
            int previous = 0, current=0;

            for ( ; current < length; ++current)
            {
                // If we've found a separator skip over it and add to the vector
                if (poa_name.charAt (current) == POAConstants.OBJECT_KEY_SEPARATOR)
                {
                    scopes.add (poa_name.substring (previous, current));
                    ++current;
                    previous = current;
                }
            }
            // Add the final POA name
            scopes.add (poa_name.substring (previous, current));
        }
        return scopes;
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
     * unmasks the object key separator chars
     */

    public static String unmaskStr(String str)
    {
        return new String(unmaskId(str.getBytes()));
    }

    private static String unmaskStr(byte[] data, int offset, int length)
    {
        return new String(unmaskId(data, offset, length));
    }

    /**
     * unmasks the object key separator bytes
     */

    public static byte[] unmaskId(final byte[] id)
    {
        final int altered = getAltered(id, 0, id.length);

        if (altered == id.length)
        {
            return id;
        }
        else
        {
            return unmaskId(id, 0, id.length, altered);
        }
    }

    /**
     * unmasks the object key separator bytes
     */

    private static byte[] unmaskId(final byte[] id, final int start, final int length)
    {
        int altered = getAltered(id, start, length);

        return unmaskId(id, start, length, altered);
    }

    private static int getAltered(final byte[] id, final int start, final int length)
    {
        int altered = length;
        for (int i=start; i<start + length; i++)
        {
            if (id[i] == POAConstants.MASK_BYTE)
            {
                altered--;
                i++;
            }
        }
        return altered;
    }

    private static byte[] unmaskId(final byte[] id, final int start, final int length, final int altered)
    {
        final byte[] result = new byte[altered];

        for (int i=start, resultIdx = 0; i< start + length; ++i, ++resultIdx)
        {
            if (id[i] == POAConstants.MASK_BYTE)
            {
                if (id[i+1] == POAConstants.MASK_MASK_BYTE)
                {
                    result[resultIdx] = POAConstants.MASK_BYTE;
                }
                else if (id[i+1] == POAConstants.SEPA_MASK_BYTE)
                {
                    result[resultIdx] = POAConstants.OBJECT_KEY_SEP_BYTE;
                }
                else
                {
                    throw new POAInternalError("error: forbidden byte sequence \""
                                               +POAConstants.MASK_BYTE+id[i+1]+"\" (unmaskId)");
                }
                ++i;
            }
            else
            {
                result[resultIdx] = id[i];
            }
        }
        return result;
    }


    /**
     * <code>toHex</code> converts a byte into a readable string.
     *
     * @param b a <code>byte</code> value
     * @return a <code>String</code> value
     */

    public static final String toHex(byte b)
    {
        StringBuffer sb = new StringBuffer();

        int upper = (b >> 4) & 0x0F;
        sb.append( lookup[upper] );

        int lower = b & 0x0F;
        sb.append( lookup[lower] );

        sb.append( ' ' );

        return sb.toString();
    }
}
