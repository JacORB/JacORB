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

package org.jacorb.orb.util;

/**
 * @author Gerald Brose
 * @version $Id$
 */


import java.io.*;
import java.util.*;
import org.jacorb.orb.ORB;
import org.jacorb.orb.iiop.IIOPProfile;
import org.omg.ETF.Profile;

public class CorbaLoc
{
    private ORB orb;
    private String keyString;
    private byte[] key;
    private String bodyString;
    private boolean is_rir;

    public Profile[] profileList;

    public CorbaLoc(ORB o, String addr)
    {
        orb = o;
        is_rir = false;
        parse(addr);
    }

    public boolean rir()
    {
        return is_rir;
    }

    public String toString()
    {
        return "corbaloc:" + body();
    }

    public String body()
    {
        StringBuffer sb = new StringBuffer();

        sb.append(bodyString);

        if (keyString != null)
            sb.append("/" + keyString);

        return sb.toString();
    }

    public String getKeyString()
    {
        return keyString;
    }

    public byte[] getKey()
    {
        return key;
    }

    public void defaultKeyString(String s)
    {
        if( keyString == null )
            keyString = s;
        else
            throw new RuntimeException("KeyString not empty, cannot default to " + s );
    }

    public String toCorbaName(String str_name)
    {
        if (getKeyString() == null)
            defaultKeyString("NameService");

        if (str_name != null && str_name.length() > 0)
        {
            try
            {
                return "corbaname:" + body() + "#" + str_name;
            }
            catch (Exception e)
            {
                return null;
            }
        }
        else
            return "corbaname:" + body();
    }

    /**
     * parses a string representing a corbaloc: reference
     */
    private void parse(String addr)
    {
        if( addr == null || !addr.startsWith("corbaloc:"))
            throw new IllegalArgumentException("URL must start with \'corbaloc:\'");

        String sb;
        if( addr.indexOf('/') == -1 )
        {
            sb = new String( addr.substring( addr.indexOf(':')+1 ) );
            if (addr.startsWith("corbaloc:rir:"))
            {
                is_rir = true;
                // default key string for rir protocol
                keyString = "NameService";
            }
            else
            {
                keyString = null;
            }
            key = new byte[0];
        }
        else
        {
            sb = new String( addr.substring( addr.indexOf(':')+1, addr.indexOf('/') ) );
            keyString = addr.substring(  addr.indexOf('/')+1 );
            key = parseKey( keyString );
        }

        if( sb.indexOf(',') > 0 )
        {
            StringTokenizer tokenizer = new StringTokenizer( sb, "," );
            profileList = new Profile[tokenizer.countTokens()];
            int pIndex = 0;
            for( int i = 0; i < profileList.length; i++ )
            {
                Profile p = parseAddress(tokenizer.nextToken());
                if (p == null)
                    continue;
                profileList[pIndex] = p;
                pIndex++;
            }
            while (pIndex < profileList.length)
            {
                profileList[pIndex] = null;
                pIndex++;
            }

        }
        else
            profileList = new Profile[]{ parseAddress(sb) };

        bodyString = sb;
    }

    private Profile parseAddress(String addr)
    {
        int colon = addr.indexOf(':');
        if (colon == -1)
            throw new IllegalArgumentException(
                "Illegal object address format: " + addr);
        if (addr.equals("rir:"))
        {
            is_rir = true;
            /* resolve initials references protocol */
            return null;
        }

        Profile result = null;
        if (orb == null 
            && (colon == 0
                || addr.startsWith("iiop:")
                || addr.startsWith("ssliop:")))
            result = new IIOPProfile(addr);
        else if (orb != null)
        {
            String token = addr.substring(0, colon);
            List factories = orb.getTransportManager().getFactoriesList();
            for (Iterator i = factories.iterator();
                 result == null && i.hasNext();)
            {
                org.omg.ETF.Factories f = (org.omg.ETF.Factories)i.next();
                result = f.decode_corbaloc(addr);
            }
        }
        if (result == null)
            throw new IllegalArgumentException(
                "Unknown protocol in object address format: " + addr);
        return result;
    }

    private static boolean legalChar(char c)
    {
        if(( c >= '0' && c <= '9') ||
           ( c >= 'a' && c <= 'z') ||
           ( c >= 'A' && c <= 'Z' ))
            return true;
        else
            return ( c == ';' || c == '/' ||c == ':' || c == '?' ||
                     c == '@' || c == '&' ||c == '=' || c == '+' ||
                     c == '$' || c == ',' ||c == '_' || c == '.' ||
                     c == '!' || c == '~' ||c == '*' || c == '\'' ||
                     c == '-' || c == '(' || c == ')' );
    }

    private static byte hexValue(char c)
    {
        return (byte)((c >= 'a') ? (10 + c - 'a') :
                      ((c >= 'A') ? (10 + c - 'A') : (c - '0'))
                      );
    }

    private static char hexDigit(byte b)
    {
        if( (b & 0xf0) != 0 )
            throw new IllegalArgumentException("Hex digit out of range " + b);

        return (char)( b < 10 ? '0' + (char)b :  'A' + (char)b - 10 ) ;
    }

    private static boolean isHex(char c)
    {
        return ( ( c >= '0' && c <= '9') ||
                 ( c >= 'a' && c <='f')  ||
                 ( c >= 'A' && c <='F'));
    }

    public static byte[] parseKey(String s)
    {
        char[] tmp = s.toCharArray();
        int count = tmp.length;

        for( int i = 0; i < tmp.length; i++ )
        {
            if( !legalChar(tmp[i]) )
            {
                if( tmp[i] == '%' )
                {
                    if( isHex(tmp[i+1]) && isHex(tmp[i+2]))
                    {
                        count -= 2;
                        i+=2;
                    }
                    else
                        throw new IllegalArgumentException("Illegal escape in URL character");
                }
                else
                    throw new IllegalArgumentException("URL character out of range: " + tmp[i]);
            }
        }

        byte[] result = new byte[count];
        int idx = 0;

        for( int i = 0; i < count; i++ )
        {
            if( legalChar( tmp[idx]))
                result[i] = (byte)tmp[idx++];
            else
            {
                result[i] = (byte)( (hexValue(tmp[idx+1]))<<4 | hexValue(tmp[idx+2]) );
                idx += 3;
            }
        }
        return result;
    }

    public static String parseKey(byte[] key)
    {
        StringBuffer sb = new StringBuffer();

        for( int i = 0; i < key.length; i++ )
        {
            if( !legalChar((char)key[i]) )
            {
                sb.append( '%' );
                // Mask the bytes before shift to ensure 10001001 doesn't get
                // shifted to 11111000 but 00001000 (linden java faq).
                sb.append( hexDigit( (byte)((key[i] & 0xff) >> 4 )));
                sb.append( hexDigit( (byte)( key[i] & 0x0f )));
            }
            else
            {
                sb.append( (char)key[i]);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args)
    {
        String [] noarg = new String[]{};
        ORB orb = (org.jacorb.orb.ORB)ORB.init(noarg,null);
        for( int i = 0; i < args.length; i++ )
        {
            System.out.println( new CorbaLoc(orb, args[i] ).toString());
        }
    }

}
