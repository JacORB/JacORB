/*
 * @(#)Util.java					0.3-2 18/06/1999
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996-1999  Ronald Tschalär
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *
 */

package HTTPClient;

import java.net.URL;
import java.util.Date;
import java.util.BitSet;
import java.util.Vector;
import java.util.Hashtable;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * This class holds various utility methods.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

public class Util
{
    private static final BitSet Separators = new BitSet(128);
    private static final BitSet TokenChar = new BitSet(128);
    private static final BitSet UnsafeChar = new BitSet(128);
    private static SimpleDateFormat http_format;

    static
    {
	// rfc-2068 tspecial
	Separators.set('(');
	Separators.set(')');
	Separators.set('<');
	Separators.set('>');
	Separators.set('@');
	Separators.set(',');
	Separators.set(';');
	Separators.set(':');
	Separators.set('\\');
	Separators.set('"');
	Separators.set('/');
	Separators.set('[');
	Separators.set(']');
	Separators.set('?');
	Separators.set('=');
	Separators.set('{');
	Separators.set('}');
	Separators.set(' ');
	Separators.set('\t');

	// rfc-2068 token
	for (int ch=32; ch<127; ch++)  TokenChar.set(ch);
	TokenChar.xor(Separators);

	// rfc-1738 unsafe characters, including CTL and SP, and excluding
	// "#" and "%"
	for (int ch=0; ch<32; ch++)  UnsafeChar.set(ch);
	UnsafeChar.set(' ');
	UnsafeChar.set('<');
	UnsafeChar.set('>');
	UnsafeChar.set('"');
	UnsafeChar.set('{');
	UnsafeChar.set('}');
	UnsafeChar.set('|');
	UnsafeChar.set('\\');
	UnsafeChar.set('^');
	UnsafeChar.set('~');
	UnsafeChar.set('[');
	UnsafeChar.set(']');
	UnsafeChar.set('`');
	UnsafeChar.set(127);

	// rfc-1123 date format (restricted to GMT, as per rfc-2068)
	/* This initialization has been moved to httpDate() because it
	 * takes an awfully long time and is often not needed
	 *
	http_format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'",
					   Locale.US);
	http_format.setTimeZone(new SimpleTimeZone(0, "GMT"));
	*/
    }


    // Constructors

    /**
     * This class isn't meant to be instantiated.
     */
    private Util() {}


    // Methods

    // this doesn't work!!! Aaaaarrgghhh!
    final static Object[] resizeArray(Object[] src, int new_size)
    {
	Object tmp[] = new Object[new_size];
	System.arraycopy(src, 0, tmp, 0,
			(src.length < new_size ? src.length : new_size));
	return tmp;
    }

    final static NVPair[] resizeArray(NVPair[] src, int new_size)
    {
	NVPair tmp[] = new NVPair[new_size];
	System.arraycopy(src, 0, tmp, 0,
			(src.length < new_size ? src.length : new_size));
	return tmp;
    }

    final static AuthorizationInfo[] resizeArray(AuthorizationInfo[] src,
						 int new_size)
    {
	AuthorizationInfo tmp[] = new AuthorizationInfo[new_size];
	System.arraycopy(src, 0, tmp, 0,
			(src.length < new_size ? src.length : new_size));
	return tmp;
    }

    final static Cookie[] resizeArray(Cookie[] src, int new_size)
    {
	Cookie tmp[] = new Cookie[new_size];
	System.arraycopy(src, 0, tmp, 0,
			(src.length < new_size ? src.length : new_size));
	return tmp;
    }

    final static String[] resizeArray(String[] src, int new_size)
    {
	String tmp[] = new String[new_size];
	System.arraycopy(src, 0, tmp, 0,
			(src.length < new_size ? src.length : new_size));
	return tmp;
    }

    final static boolean[] resizeArray(boolean[] src, int new_size)
    {
	boolean tmp[] = new boolean[new_size];
	System.arraycopy(src, 0, tmp, 0,
			(src.length < new_size ? src.length : new_size));
	return tmp;
    }

    final static byte[] resizeArray(byte[] src, int new_size)
    {
	byte tmp[] = new byte[new_size];
	System.arraycopy(src, 0, tmp, 0,
			(src.length < new_size ? src.length : new_size));
	return tmp;
    }

    final static char[] resizeArray(char[] src, int new_size)
    {
	char tmp[] = new char[new_size];
	System.arraycopy(src, 0, tmp, 0,
			(src.length < new_size ? src.length : new_size));
	return tmp;
    }

    final static int[] resizeArray(int[] src, int new_size)
    {
	int tmp[] = new int[new_size];
	System.arraycopy(src, 0, tmp, 0,
			(src.length < new_size ? src.length : new_size));
	return tmp;
    }


    /**
     * Split a property into an array of Strings, using "|" as the
     * separator.
     */
    static String[] splitProperty(String prop)
    {
	if (prop == null)  return new String[0];

	StringTokenizer tok = new StringTokenizer(prop, "|");
	String[] list = new String[tok.countTokens()];
	for (int idx=0; idx<list.length; idx++)
	    list[idx] = tok.nextToken().trim();

	return list;
    }


    /**
     * Helper method for context lists used by modules. Returns the
     * list associated with the context if it exists; otherwise it creates
     * a new list and adds it to the context list.
     *
     * @param cntxt_list the list of lists indexed by context
     * @param cntxt the context
     */
    final static Hashtable getList(Hashtable cntxt_list, Object cntxt)
    {
	Hashtable list = (Hashtable) cntxt_list.get(cntxt);
	if (list == null)
	{
	    synchronized(cntxt_list)	// only synch if necessary
	    {
		list = (Hashtable) cntxt_list.get(cntxt);
		if (list == null)	// verify nobody else beat us to it
		{
		    list = new Hashtable();
		    cntxt_list.put(cntxt, list);
		}
	    }
	}

	return list;
    }


    /**
     * Creates an array of distances to speed up the search in findStr().
     * The returned array should be passed as the second argument to
     * findStr().
     *
     * @param search the search string (same as the first argument to
     *               findStr()).
     * @return an array of distances (to be passed as the second argument to
     *         findStr()).
     */
    final static int[] compile_search(byte[] search)
    {
	int[] cmp = {0, 1, 0, 1, 0, 1};
	int   end;

	for (int idx=0; idx<search.length; idx++)
	{
	    for (end=idx+1; end<search.length; end++)
	    {
		if (search[idx] == search[end])  break;
	    }
	    if (end < search.length)
	    {
		if ((end-idx) > cmp[1])
		{
		    cmp[4] = cmp[2];
		    cmp[5] = cmp[3];
		    cmp[2] = cmp[0];
		    cmp[3] = cmp[1];
		    cmp[0] = idx;
		    cmp[1] = end - idx;
		}
		else if ((end-idx) > cmp[3])
		{
		    cmp[4] = cmp[2];
		    cmp[5] = cmp[3];
		    cmp[2] = idx;
		    cmp[3] = end - idx;
		}
		else if ((end-idx) > cmp[3])
		{
		    cmp[4] = idx;
		    cmp[5] = end - idx;
		}
	    }
	}

	cmp[1] += cmp[0];
	cmp[3] += cmp[2];
	cmp[5] += cmp[4];
	return cmp;
    }

    /**
     * Search for a string. Use compile_search() to first generate the second
     * argument. This uses a Knuth-Morris-Pratt like algorithm.
     *
     * @param search  the string to search for.
     * @param cmp     the the array returned by compile_search.
     * @param str     the string in which to look for <var>search</var>.
     * @param beg     the position at which to start the search in
     *                <var>str</var>.
     * @param end     the position at which to end the search in <var>str</var>,
     *                noninclusive.
     * @return the position in <var>str</var> where <var>search</var> was
     *         found, or -1 if not found.
     */
    final static int findStr(byte[] search, int[] cmp, byte[] str,
				     int beg, int end)
    {
	int c1f  = cmp[0],
	    c1l  = cmp[1],
	    d1   = c1l - c1f,
	    c2f  = cmp[2],
	    c2l  = cmp[3],
	    d2   = c2l - c2f,
	    c3f  = cmp[4],
	    c3l  = cmp[5],
	    d3   = c3l - c3f;

	Find: while (beg+search.length <= end)
	{
	    if (search[c1l] == str[beg+c1l])
	    {
		/* This is correct, but Visual J++ can't cope with it...
		Comp: if (search[c1f] == str[beg+c1f])
		{
		    for (int idx=0; idx<search.length; idx++)
			if (search[idx] != str[beg+idx])  break Comp;

		    break Find;		// we found it
		}
		*  so here is the replacement: */
		if (search[c1f] == str[beg+c1f])
		{
		    boolean same = true;

		    for (int idx=0; idx<search.length; idx++)
			if (search[idx] != str[beg+idx])
			{
				same = false;
				break;
			}

		    if (same)
			break Find;         // we found it
		}

		beg += d1;
	    }
	    else if (search[c2l] == str[beg+c2l])
		beg += d2;
	    else if (search[c3l] == str[beg+c3l])
		beg += d3;
	    else
		beg++;
	}

	if (beg+search.length > end)
	    return -1;
	else
	    return beg;
    }


    /**
     * Replace quoted characters by their unquoted version. Quoted characters
     * are characters preceded by a slash. E.g. "\c" would be replaced by "c".
     * This is used in parsing http headers where quoted-characters are
     * allowed in quoted-strings and often used to quote the quote character
     * &lt;"&gt;.
     *
     * @param str the string do dequote
     * @return the string do with all quoted characters replaced by their
     *         true value.
     */
    public final static String dequoteString(String str)
    {
	if (str.indexOf('\\') == -1)  return str;

	char[] buf = str.toCharArray();
	int pos = 0, num_deq = 0;
	while (pos < buf.length)
	{
	    if (buf[pos] == '\\'  &&  pos+1 < buf.length)
	    {
		System.arraycopy(buf, pos+1, buf, pos, buf.length-pos-1);
		num_deq++;
	    }
	    pos++;
	}

	return new String(buf, 0, buf.length-num_deq);
    }


    /**
     * Replace given characters by their quoted version. Quoted characters
     * are characters preceded by a slash. E.g. "c" would be replaced by "\c".
     * This is used in generating http headers where certain characters need
     * to be quoted, such as the quote character &lt;"&gt;.
     *
     * @param str   the string do quote
     * @param qlist the list of characters to quote
     * @return the string do with all characters replaced by their
     *         quoted version.
     */
    public final static String quoteString(String str, String qlist)
    {
	char[] list = qlist.toCharArray();
	int idx;
	for (idx=0; idx<list.length; idx++)
	    if (str.indexOf(list[idx]) != -1)  break;
	if (idx == list.length)  return str;

	int len = str.length();
	char[] buf = new char[len*2];
	str.getChars(0, len, buf, 0);
	int pos = 0;
	while (pos < len)
	{
	    if (qlist.indexOf(buf[pos], 0) != -1)
	    {
		if (len == buf.length)
		    buf = Util.resizeArray(buf, len+str.length());

		System.arraycopy(buf, pos, buf, pos+1, len-pos);
		len++;
		buf[pos++] = '\\';
	    }
	    pos++;
	}

	return new String(buf, 0, len);
    }


    /**
     * This parses the value part of a header. All quoted strings are
     * dequoted.
     *
     * @see #parseHeader(java.lang.String, boolean)
     * @param header  the value part of the header.
     * @return a Vector containing all the elements; each entry is an
     *         instance of <var>HttpHeaderElement</var>.
     * @exception ParseException if the syntax rules are violated.
     */
    public final static Vector parseHeader(String header)  throws ParseException
    {
	return parseHeader(header, true);
    }


    /**
     * This parses the value part of a header. The result is a Vector of
     * HttpHeaderElement's. The syntax the header must conform to is:
     *
     * <PRE>
     * header  = [ element ] *( "," [ element ] )
     * element = name [ "=" [ value ] ] *( ";" [ param ] )
     * param   = name [ "=" [ value ] ]
     * 
     * name    = token
     * value   = ( token | quoted-string )
     * 
     * token         = 1*&lt;any char except "=", ",", ";", &lt;"&gt; and
     *                       white space&gt;
     * quoted-string = &lt;"&gt; *( text | quoted-char ) &lt;"&gt;
     * text          = any char except &lt;"&gt;
     * quoted-char   = "\" char
     * </PRE>
     *
     * Any amount of white space is allowed between any part of the header,
     * element or param and is ignored. A missing value in any element or
     * param will be stored as the empty string; if the "=" is also missing
     * <var>null</var> will be stored instead.
     *
     * @param header  the value part of the header.
     * @param dequote if true all quoted strings are dequoted.
     * @return a Vector containing all the elements; each entry is an
     *         instance of <var>HttpHeaderElement</var>.
     * @exception ParseException if the above syntax rules are violated.
     * @see HTTPClient.HttpHeaderElement
     */
    public final static Vector parseHeader(String header, boolean dequote)
	    throws ParseException
    {
	if (header == null)  return null;
	char[]  buf    = header.toCharArray();
	Vector  elems  = new Vector();
	boolean first  = true;
	int     beg = -1, end = 0, len = buf.length, abeg[] = new int[1];
	String  elem_name, elem_value;


	elements: while (true)
	{
	    if (!first)				// find required ","
	    {
		beg = skipSpace(buf, end);
		if (beg == len)  break;
		if (buf[beg] != ',')
		    throw new ParseException("Bad header format: '" + header +
					     "'\nExpected \",\" at position " +
					     beg);
	    }
	    first = false;

	    beg = skipSpace(buf, beg+1);
	    if (beg == len)  break elements;
	    if (buf[beg] == ',')		// skip empty elements
	    {
		end = beg;
		continue elements;
	    }

	    if (buf[beg] == '='  ||  buf[beg] == ';'  ||  buf[beg] == '"')
		throw new ParseException("Bad header format: '" + header +
					 "'\nEmpty element name at position " +
					 beg);

	    end = beg+1;			// extract element name
	    while (end < len  &&  !Character.isSpace(buf[end])  &&
		   buf[end] != '='  &&  buf[end] != ','  &&  buf[end] != ';')
		end++;
	    elem_name = new String(buf, beg, end-beg);

	    beg = skipSpace(buf, end);
	    if (beg < len  &&  buf[beg] == '=')	// element value
	    {
		abeg[0] = beg+1;
		elem_value = parseValue(buf, abeg, header, dequote);
		end = abeg[0];
	    }
	    else
	    {
		elem_value = null;
		end = beg;
	    }

	    NVPair[] params = new NVPair[0];
	    params: while (true)
	    {
		String param_name, param_value;

		beg = skipSpace(buf, end);	// expect ";"
		if (beg == len  ||  buf[beg] != ';')
		    break params;

		beg = skipSpace(buf, beg+1);
		if (beg == len  ||  buf[beg] == ',')
		{
		    end = beg;
		    break params;
		}
		if (buf[beg] == ';')		// skip empty parameters
		{
		    end = beg;
		    continue params;
		}

		if (buf[beg] == '='  ||  buf[beg] == '"')
		    throw new ParseException("Bad header format: '" + header +
					 "'\nEmpty parameter name at position "+
					 beg);

		end = beg+1;			// extract param name
		while (end < len  &&  !Character.isSpace(buf[end])  &&
		       buf[end] != '='  &&  buf[end] != ','  && buf[end] != ';')
		    end++;
		param_name = new String(buf, beg, end-beg);

		beg = skipSpace(buf, end);
		if (beg < len  &&  buf[beg] == '=')	// element value
		{
		    abeg[0] = beg+1;
		    param_value = parseValue(buf, abeg, header, dequote);
		    end = abeg[0];
		}
		else
		{
		    param_value = null;
		    end = beg;
		}

		params = Util.resizeArray(params, params.length+1);
		params[params.length-1] = new NVPair(param_name, param_value);
	    }

	    elems.addElement(
		      new HttpHeaderElement(elem_name, elem_value, params));
	}

	return elems;
    }


    /**
     * Parse the value part. Accepts either token or quoted string.
     */
    private static String parseValue(char[] buf, int[] abeg, String header,
				     boolean dequote)
		throws ParseException
    {
	int beg = abeg[0], end = beg, len = buf.length;
	String value;


	beg = skipSpace(buf, beg);

	if (beg < len  &&  buf[beg] == '"')	// it's a quoted-string
	{
	    beg++;
	    end = beg;
	    char[] deq_buf = null;
	    int    deq_pos = 0, lst_pos = beg;

	    while (end < len  &&  buf[end] != '"')
	    {
		if (buf[end] == '\\')
		{
		    if (dequote)	// dequote char
		    {
			if (deq_buf == null)
			    deq_buf = new char[buf.length];
			System.arraycopy(buf, lst_pos, deq_buf, deq_pos,
					 end-lst_pos);
			deq_pos += end-lst_pos;
			lst_pos = ++end;
		    }
		    else
			end++;		// skip quoted char
		}

		end++;
	    }
	    if (end == len)
		throw new ParseException("Bad header format: '" + header +
					 "'\nClosing <\"> for quoted-string"+
					 " starting at position " +
					 (beg-1) + " not found");
	    if (deq_buf != null)
	    {
		System.arraycopy(buf, lst_pos, deq_buf, deq_pos, end-lst_pos);
		deq_pos += end-lst_pos;
		value = new String(deq_buf, 0, deq_pos);
	    }
	    else
		value = new String(buf, beg, end-beg);
	    end++;
	}
	else					// it's a simple token value
	{
	    end = beg;
	    while (end < len  &&  !Character.isSpace(buf[end])  &&
		   buf[end] != ','  &&  buf[end] != ';')
		end++;

	    value = new String(buf, beg, end-beg);
	}

	abeg[0] = end;
	return value;
    }


    /**
     * Determines if the given header contains a certain token. The header
     * must conform to the rules outlined in parseHeader().
     *
     * @see #parseHeader(java.lang.String)
     * @param header the header value.
     * @param token  the token to find; the match is case-insensitive.
     * @return true if the token is present, false otherwise.
     * @exception ParseException if this is thrown parseHeader().
     */
    public final static boolean hasToken(String header, String token)
	    throws ParseException
    {
	if (header == null)
	    return false;
	else
	    return parseHeader(header).contains(new HttpHeaderElement(token));
    }


    /**
     * Get the HttpHeaderElement with the name <var>name</var>.
     *
     * @param header a vector of HttpHeaderElement's, such as is returned
     *               from <code>parseHeader()</code>
     * @param name   the name of element to retrieve; matching is
     *               case-insensitive
     * @return the request element, or null if none found.
     * @see #parseHeader(java.lang.String)
     */
    public final static HttpHeaderElement getElement(Vector header, String name)
    {
	int idx = header.indexOf(new HttpHeaderElement(name));
	if (idx == -1)
	    return null;
	else
	    return (HttpHeaderElement) header.elementAt(idx);
    }


    /**
     * retrieves the value associated with the parameter <var>param</var> in
     * a given header string. It parses the header using
     * <code>parseHeader()</code> and then searches the first element for the
     * given parameter. This is used especially in headers like
     * 'Content-type' and 'Content-Disposition'.
     *
     * <P>quoted characters ("\x") in a quoted string are dequoted.
     *
     * @see #parseHeader(java.lang.String)
     * @param  param  the parameter name
     * @param  hdr    the header value
     * @return the value for this parameter, or null if not found.
     * @exception ParseException if the above syntax rules are violated.
     */
    public final static String getParameter(String param, String hdr)
	    throws ParseException
    {
	NVPair[] params = ((HttpHeaderElement) parseHeader(hdr).firstElement()).
			    getParams();

	for (int idx=0; idx<params.length; idx++)
	{
	    if (params[idx].getName().equalsIgnoreCase(param))
		return params[idx].getValue();
	}

	return null;
    }


    /**
     * Assembles a Vector of HttpHeaderElements into a full header string.
     * The individual header elements are seperated by a ", ".
     *
     * @param the parsed header
     * @return a string containing the assembled header
     */
    public final static String assembleHeader(Vector pheader)
    {
	StringBuffer hdr = new StringBuffer(200);
	int len = pheader.size();

	for (int idx=0; idx<len; idx++)
	{
	    ((HttpHeaderElement) pheader.elementAt(idx)).appendTo(hdr);
	    hdr.append(", ");
	}
	hdr.setLength(hdr.length()-2);

	return hdr.toString();
    }


    /**
     * returns the position of the first non-space character in a char array
     * starting a position pos.
     *
     * @param str the char array
     * @param pos the position to start looking
     * @return the position of the first non-space character
     */
    final static int skipSpace(char[] str, int pos)
    {
	int len = str.length;
	while (pos < len  &&  Character.isSpace(str[pos]))  pos++;
	return pos;
    }

    /**
     * returns the position of the first space character in a char array
     * starting a position pos.
     *
     * @param str the char array
     * @param pos the position to start looking
     * @return the position of the first space character, or the length of
     *         the string if not found
     */
    final static int findSpace(char[] str, int pos)
    {
	int len = str.length;
	while (pos < len  &&  !Character.isSpace(str[pos]))  pos++;
	return pos;
    }

    /**
     * returns the position of the first non-token character in a char array
     * starting a position pos.
     *
     * @param str the char array
     * @param pos the position to start looking
     * @return the position of the first non-token character, or the length
     *         of the string if not found
     */
    final static int skipToken(char[] str, int pos)
    {
	int len = str.length;
	while (pos < len  &&  TokenChar.get(str[pos]))  pos++;
	return pos;
    }


    /**
     * Does the string need to be quoted when sent in a header? I.e. does
     * it contain non-token characters?
     *
     * @param str the string
     * @return true if it needs quoting (i.e. it contains non-token chars)
     */
    final static boolean needsQuoting(String str)
    {
	int len = str.length(), pos = 0;

	while (pos < len  &&  TokenChar.get(str.charAt(pos)))  pos++;
	return (pos < len);
    }


    /**
     * Compares two http urls for equality. This exists because the method
     * <code>java.net.URL.sameFile()</code> is broken (an explicit port 80
     * doesn't compare equal to an implicit port, and it doesn't take
     * escapes into account).
     *
     * <P>Two http urls are considered equal if they have the same protocol
     * (case-insensitive match), the same host (case-insensitive), the
     * same port and the same file (after decoding escaped characters).
     *
     * @param url1 the first url
     * @param url1 the second url
     * @return true if <var>url1</var> and <var>url2</var> compare equal
     */
    public final static boolean sameHttpURL(URL url1, URL url2)
    {
	if (!url1.getProtocol().equalsIgnoreCase(url2.getProtocol()))
	    return false;

	if (!url1.getHost().equalsIgnoreCase(url2.getHost()))
	    return false;

	int port1 = url1.getPort(), port2 = url2.getPort();
	if (port1 == -1)  port1 = URI.defaultPort(url1.getProtocol());
	if (port2 == -1)  port2 = URI.defaultPort(url1.getProtocol());
	if (port1 != port2)
	    return false;

	try
	    { return URI.unescape(url1.getFile()).equals(URI.unescape(url2.getFile())); }
	catch (ParseException pe)
	    { return url1.getFile().equals(url2.getFile());}
    }


    /**
     * Return the default port used by a given protocol.
     *
     * @param protocol the protocol
     * @return the port number, or 0 if unknown
     * @deprecated use URI.defaultPort() instead
     * @see HTTPClient.URI#defaultPort(java.lang.String)
     */
    public final static int defaultPort(String protocol)
    {
	return URI.defaultPort(protocol);
    }


    /**
     * This returns a string containing the date and time in <var>date</var>
     * formatted according to a subset of RFC-1123. The format is defined in
     * the HTTP/1.0 spec (RFC-1945), section 3.3, and the HTTP/1.1 spec
     * (RFC-2068), section 3.3.1. Note that Date.toGMTString() is close, but
     * is missing the weekday and supresses the leading zero if the day is
     * less than the 10th. Instead we use the SimpleDateFormat class.
     *
     * <P>Some versions of JDK 1.1.x are bugged in that their GMT uses
     * daylight savings time... Therefore we use our own timezone
     * definitions.
     *
     * @param date the date and time to be converted
     * @return a string containg the date and time as used in http
     */
    public final static String httpDate(Date date)
    {
	if (http_format == null)
	{
	    synchronized(HTTPClient.Util.class)
	    {
		if (http_format == null)
		{
		    http_format =
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'",
					     Locale.US);
		    http_format.setTimeZone(new SimpleTimeZone(0, "GMT"));
		}
	    }
	}

	return http_format.format(date);
    }


    /**
     * Escape unsafe characters in a path.
     *
     * @param path the original path
     * @return the path with all unsafe characters escaped
     */
    final static String escapeUnsafeChars(String path)
    {
	int len = path.length();
	char[] buf = new char[3*len];

	int dst = 0;
	for (int src=0; src<len; src++)
	{
	    char ch = path.charAt(src);
	    if (ch >= 128  ||  UnsafeChar.get(ch))
	    {
		buf[dst++] = '%';
		buf[dst++] = hex_map[(ch & 0xf0) >>> 4];
		buf[dst++] = hex_map[ch & 0x0f];
	    }
	    else
		buf[dst++] = ch;
	}

	if (dst > len)
	    return new String(buf, 0, dst);
	else
	    return path;
    }

    static final char[] hex_map =
	    {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};


    /**
     * Extract the path from an http resource.
     *
     * <P>The "resource" part of an HTTP URI can contain a number of parts,
     * some of which are not always of interest. These methods here will
     * extract the various parts, assuming the following syntanx (taken from
     * RFC-2068):
     *
     * <PRE>
     * resource = [ "/" ] [ path ] [ ";" params ] [ "?" query ] [ "#" fragment ]
     * </PRE>
     *
     * @param the resource to split
     * @return the path, including any leading "/"
     * @see #getParams
     * @see #getQuery
     * @see #getFragment
     */
    public final static String getPath(String resource)
    {
	int p, end = resource.length();
	if ((p = resource.indexOf('#')) != -1)			// find fragment
	    end = p;
	if ((p = resource.indexOf('?')) != -1  &&  p < end)	// find query
	    end = p;
	if ((p = resource.indexOf(';')) != -1  &&  p < end)	// find params
	    end = p;
	return resource.substring(0, end);
    }


    /**
     * Extract the params part from an http resource.
     *
     * @param the resource to split
     * @return the params, or null if there are none
     * @see #getPath
     */
    public final static String getParams(String resource)
    {
	int beg, f, q;
	if ((beg = resource.indexOf(';')) == -1)		// find params
	    return null;
	if ((f = resource.indexOf('#')) != -1  &&  f < beg)	// find fragment
	    return null;
	if ((q = resource.indexOf('?')) != -1  &&  q < beg)	// find query
	    return null;
	if (q == -1  &&  f == -1)
	    return resource.substring(beg+1);
	if (f == -1  ||  (q != -1  &&  q < f))
	    return resource.substring(beg+1, q);
	else
	    return resource.substring(beg+1, f);
    }


    /**
     * Extract the query string from an http resource.
     *
     * @param the resource to split
     * @return the query, or null if there was none
     * @see #getPath
     */
    public final static String getQuery(String resource)
    {
	int beg, f;
	if ((beg = resource.indexOf('?')) == -1)		// find query
	    return null;
	if ((f = resource.indexOf('#')) != -1  &&  f < beg)	// find fragment
	    return null;				// '?' is in fragment
	if (f == -1)
	    return resource.substring(beg+1);		// no fragment
	else
	    return resource.substring(beg+1, f);	// strip fragment
    }


    /**
     * Extract the fragment part from an http resource.
     *
     * @param the resource to split
     * @return the fragment, or null if there was none
     * @see #getPath
     */
    public final static String getFragment(String resource)
    {
	int beg;
	if ((beg = resource.indexOf('#')) == -1)	// find fragment
	    return null;
	else
	    return resource.substring(beg+1);
    }
}

