/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import org.jacorb.orb.ParsedIOR;
import java.io.*;
import java.util.*;

public class CorbaLoc
{
    private String keyString;
    private byte[] key;
    private String bodyString;

    public static class ObjectAddress
    {
	/** protocol type information */
	public String protocol_identifier = "iiop";

	/** protocol version information  */
	public int major = 1;
	public int minor = 0;

	/** IP address information */
	public String host = "127.0.0.1";
	public int port = 2809; // corbaloc default port number

	public org.omg.IIOP.Version getVersion()
	{
	    return new org.omg.IIOP.Version((byte)major, (byte)minor);
	}
    }

    public ObjectAddress[] objectAddressList;

    public CorbaLoc(String addr)
    {
	parse(addr);
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
	    sb = new String( addr.substring( addr.indexOf(':')+1));
	    keyString = null;
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
	    objectAddressList = new ObjectAddress[tokenizer.countTokens()];

	    for( int i = 0; i < objectAddressList.length; i++ )
		objectAddressList[i] = parseObjectAddress(tokenizer.nextToken());
	}
	else
	    objectAddressList = new ObjectAddress[]{ parseObjectAddress(sb) };	    
	
	bodyString = sb;
    }

    private ObjectAddress parseObjectAddress(String addr)
    {
	if( addr.indexOf(':') == -1)
	    throw new IllegalArgumentException("Illegal object address format: " + addr);

	ObjectAddress result = new ObjectAddress();

	if( addr.equals("rir:"))
	{
	    /* resolve initials references protocol */
	    result.protocol_identifier = "rir";
	    return result;
	}
	else if ( addr.indexOf(':') == 0 ||
		  addr.startsWith("iiop:") ||
                  addr.startsWith("ssliop:")) 
	{
	    if( addr.indexOf(':') != 0 )
	    {
		//protocol exclicitely specified
		result.protocol_identifier = addr.substring( 0, addr.indexOf(':') );
	    }
	    //else: use default "iiop"

	    String version_and_host = addr.substring( addr.indexOf(':')+1);
	    if( version_and_host.length() == 0)
		throw new IllegalArgumentException("Illegal IIOP protocol format in object address format: " + addr);

	    /* IIOP */
	    String host;
	    if( version_and_host.indexOf( '@' ) != -1 ) 
	    {		
		String version = version_and_host.substring(0, version_and_host.indexOf( '@' ));
		if( version.indexOf('.') != -1 )
		{
		    try
		    {
			result.major = Integer.parseInt(version.substring(0,version.indexOf('.')));
			result.minor = Integer.parseInt(version.substring(version.indexOf('.')+1));
		    }
		    catch( NumberFormatException nfe )
		    {
			throw new IllegalArgumentException("Illegal version format for IIOP protocol in object address format: " + addr);
		    }
		}
		host = version_and_host.substring( version_and_host.indexOf( '@' )+1);
	    }
	    else
		host = version_and_host;
	    if( host.indexOf(':') != -1 )
	    {
		try
		{
		    result.host = host.substring(0, host.indexOf(':'));
		    result.port = Integer.parseInt( host.substring( host.indexOf(':')+1));
		}
		catch( NumberFormatException ill )
		{
		    throw new IllegalArgumentException("Illegal port number in IIOP object address format: " + addr);
		}
	    }
	    else
		result.host = host;
	    return result;  
	}
	else
	    throw new IllegalArgumentException("Illegal protocol in object address format: " + addr);
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

    private byte[] parseKey(String s)
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
		sb.append( hexDigit(  (byte)(key[i] >> 4 )));
                sb.append( hexDigit( (byte)( key[i] & 0x0f )));
	    }
	    else
	    {
		sb.append( (char)key[i]);
	    }
	}
	return sb.toString();
    }


    public String toString()
    {
	return "corbaloc:" + body();
    }

    public String body()
    {
	StringBuffer sb = new StringBuffer();

	sb.append(bodyString);

	if( keyString != null )
	    sb.append("/" + keyString );

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
	if( getKeyString() == null )
	    defaultKeyString("NameService");
	    
	if( str_name != null && str_name.length() > 0 )
	{
	    try
	    {
		return "corbaname:" + 
		    body() + "#" + str_name;
	    }
	    catch( Exception e)
	    {
		return null;
	    }
	}
	else
	    return "corbaname:" + body();	

    }

    public static void main(String[] args)
    {
	for( int i = 0; i < args.length; i++ )
	{
	    System.out.println( new CorbaLoc( args[i] ).toString());
	}
	
    }


}






