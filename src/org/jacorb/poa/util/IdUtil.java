package org.jacorb.poa.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2000  Gerald Brose.
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
 
/**
 * This class collects some oid related basic routines.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.05, 21/01/00, RT
 */

public final class IdUtil 
{

    public static byte[] concat(byte[] first, byte[] second) 
    {
	byte[] result = new byte[first.length+second.length];
	System.arraycopy(first, 0, result, 0, first.length);
	System.arraycopy(second, 0, result, first.length, second.length);
	return result;
    
}
    /**
     * creates an id as a concatenation of the current time in msec
     * and random_len random bytes
     */

    public static byte[] createId(int random_len) 
    {
	byte[] time = toId(System.currentTimeMillis());		
	long range = (long) Math.pow(10, random_len)-1;
	byte[] random = toId((long)(Math.random()*range));
	return concat(time, random);
    }

    public static boolean equals(byte[] first, byte[] second) {
	if (first.length != second.length) return false;
	for (int i=0; i<first.length; i++) {
	    if (first[i] != second[i]) return false;
	}
	return true;
    }

    /**
     * compares first len bytes of two byte arrays
     */

    public static boolean equals(byte[] first, byte[] second, int len) 
    {
	if (first.length < len || second.length < len) return false;
	for (int i=0; i<len; i++) 
	{
	    if (first[i] != second[i]) return false;
	}
	return true;
    }

    /**
     * extracts len bytes from id, the first byte to be copied is at index offset
     */

    public static byte[] extract(byte[] id, int offset, int len) 
    {
	byte[] result = new byte[len];
	System.arraycopy(id, offset, result, 0, len);
	return result;
    }

    /**
     * converts the number l into a byte array
     */

    public static byte[] toId(long l) 
    {
	/*
	  String str = "" + l;
	  return str.getBytes();
		
	  String str = "" + l;
	*/
	String str = Long.toOctalString(l);
	if (str.length() % 2 != 0) str = "0" + str;
	byte[] result = new byte[str.length()/2];
	StringBuffer number = new StringBuffer("xx");
	for (int i=0; i<result.length; i++) {
	    number.setCharAt(0, str.charAt(i*2));
	    number.setCharAt(1, str.charAt(i*2+1));
	    result[i] = new Integer(number.toString()).byteValue();
	}
	for (int i=0; i<result.length; i++) {
	    if (result[i] == org.jacorb.poa.POAConstants.OBJECT_KEY_SEP_BYTE) {
		result[i] = (byte) 48;     // char 0 , hex 30
				
	    } else if (result[i] == org.jacorb.poa.POAConstants.MASK_BYTE) {
		result[i] = (byte) 19;     // char !!, hex 13
	    }
	}
	return result;
    }
}






