/*
 * @(#)Codecs.java					0.3-2 18/06/1999
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


import java.util.BitSet;
import java.util.Vector;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;


/**
 * This class collects various encoders and decoders.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

public class Codecs
{
    private static BitSet  BoundChar;
    private static BitSet  EBCDICUnsafeChar;
    private static byte[]  Base64EncMap, Base64DecMap;
    private static char[]  UUEncMap;
    private static byte[]  UUDecMap;


    private final static String ContDisp = "\r\nContent-Disposition: form-data; name=\"";
    private final static String FileName = "\"; filename=\"";
    private final static String Boundary = "\r\n-----ieoau._._+2_8_GoodLuck8.3-dskdfJwSJKlrWLr0234324jfLdsjfdAuaoei-----";


    // Class Initializer

    static
    {
	// rfc-2046 & rfc-2045: (bcharsnospace & token)
	// used for multipart codings
	BoundChar = new BitSet(256);
	for (int ch='0'; ch <= '9'; ch++)  BoundChar.set(ch);
	for (int ch='A'; ch <= 'Z'; ch++)  BoundChar.set(ch);
	for (int ch='a'; ch <= 'z'; ch++)  BoundChar.set(ch);
	BoundChar.set('+');
	BoundChar.set('_');
	BoundChar.set('-');
	BoundChar.set('.');

	// EBCDIC unsafe characters to be quoted in quoted-printable
	// See first NOTE in section 6.7 of rfc-2045
	EBCDICUnsafeChar = new BitSet(256);
	EBCDICUnsafeChar.set('!');
	EBCDICUnsafeChar.set('"');
	EBCDICUnsafeChar.set('#');
	EBCDICUnsafeChar.set('$');
	EBCDICUnsafeChar.set('@');
	EBCDICUnsafeChar.set('[');
	EBCDICUnsafeChar.set('\\');
	EBCDICUnsafeChar.set(']');
	EBCDICUnsafeChar.set('^');
	EBCDICUnsafeChar.set('`');
	EBCDICUnsafeChar.set('{');
	EBCDICUnsafeChar.set('|');
	EBCDICUnsafeChar.set('}');
	EBCDICUnsafeChar.set('~');

	// rfc-2045: Base64 Alphabet
	byte[] map = {
	    (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F',
	    (byte)'G', (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L',
	    (byte)'M', (byte)'N', (byte)'O', (byte)'P', (byte)'Q', (byte)'R',
	    (byte)'S', (byte)'T', (byte)'U', (byte)'V', (byte)'W', (byte)'X',
	    (byte)'Y', (byte)'Z',
	    (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f',
	    (byte)'g', (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l',
	    (byte)'m', (byte)'n', (byte)'o', (byte)'p', (byte)'q', (byte)'r',
	    (byte)'s', (byte)'t', (byte)'u', (byte)'v', (byte)'w', (byte)'x',
	    (byte)'y', (byte)'z',
	    (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5',
	    (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'+', (byte)'/' };
	Base64EncMap = map;
	Base64DecMap = new byte[128];
	for (int idx=0; idx<Base64EncMap.length; idx++)
	    Base64DecMap[Base64EncMap[idx]] = (byte) idx;

	// uuencode'ing maps
	UUEncMap = new char[64];
	for (int idx=0; idx<UUEncMap.length; idx++)
	    UUEncMap[idx] = (char) (idx + 0x20);
	UUDecMap = new byte[128];
	for (int idx=0; idx<UUEncMap.length; idx++)
	    UUDecMap[UUEncMap[idx]] = (byte) idx;
    }


    // Constructors

    /**
     * This class isn't meant to be instantiated.
     */
    private Codecs() {}


    // Methods

    /**
     * This method encodes the given string using the base64-encoding
     * specified in RFC-2045 (Section 6.8). It's used for example in the
     * "Basic" authorization scheme.
     *
     * @param  str the string
     * @return the base64-encoded <var>str</var>
     */
    public final static String base64Encode(String str)
    {
	if (str == null)  return  null;

	byte data[] = new byte[str.length()];
	str.getBytes(0, str.length(), data, 0);

	return new String(base64Encode(data), 0);
    }


    /**
     * This method encodes the given byte[] using the base64-encoding
     * specified in RFC-2045 (Section 6.8).
     *
     * @param  data the data
     * @return the base64-encoded <var>data</var>
     */
    public final static byte[] base64Encode(byte[] data)
    {
	if (data == null)  return  null;

	int sidx, didx;
	byte dest[] = new byte[((data.length+2)/3)*4];


	// 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
	for (sidx=0, didx=0; sidx < data.length-2; sidx += 3)
	{
	    dest[didx++] = Base64EncMap[(data[sidx] >>> 2) & 077];
	    dest[didx++] = Base64EncMap[(data[sidx+1] >>> 4) & 017 |
					(data[sidx] << 4) & 077];
	    dest[didx++] = Base64EncMap[(data[sidx+2] >>> 6) & 003 |
					(data[sidx+1] << 2) & 077];
	    dest[didx++] = Base64EncMap[data[sidx+2] & 077];
	}
	if (sidx < data.length)
	{
	    dest[didx++] = Base64EncMap[(data[sidx] >>> 2) & 077];
	    if (sidx < data.length-1)
	    {
		dest[didx++] = Base64EncMap[(data[sidx+1] >>> 4) & 017 |
					    (data[sidx] << 4) & 077];
		dest[didx++] = Base64EncMap[(data[sidx+1] << 2) & 077];
	    }
	    else
		dest[didx++] = Base64EncMap[(data[sidx] << 4) & 077];
	}

	// add padding
	for ( ; didx < dest.length; didx++)
	    dest[didx] = (byte) '=';

	return dest;
    }


    /**
     * This method decodes the given string using the base64-encoding
     * specified in RFC-2045 (Section 6.8).
     *
     * @param  str the base64-encoded string.
     * @return the decoded <var>str</var>.
     */
    public final static String base64Decode(String str)
    {
	if (str == null)  return  null;

	byte data[] = new byte[str.length()];
	str.getBytes(0, str.length(), data, 0);

	return new String(base64Decode(data), 0);
    }


    /**
     * This method decodes the given byte[] using the base64-encoding
     * specified in RFC-2045 (Section 6.8).
     *
     * @param  data the base64-encoded data.
     * @return the decoded <var>data</var>.
     */
    public final static byte[] base64Decode(byte[] data)
    {
	if (data == null)  return  null;

	int tail = data.length;
	while (data[tail-1] == '=')  tail--;

	byte dest[] = new byte[tail - data.length/4];


	// ascii printable to 0-63 conversion
	for (int idx = 0; idx <data.length; idx++)
	    data[idx] = Base64DecMap[data[idx]];

	// 4-byte to 3-byte conversion
	int sidx, didx;
	for (sidx = 0, didx=0; didx < dest.length-2; sidx += 4, didx += 3)
	{
	    dest[didx]   = (byte) ( ((data[sidx] << 2) & 255) |
			    ((data[sidx+1] >>> 4) & 003) );
	    dest[didx+1] = (byte) ( ((data[sidx+1] << 4) & 255) |
			    ((data[sidx+2] >>> 2) & 017) );
	    dest[didx+2] = (byte) ( ((data[sidx+2] << 6) & 255) |
			    (data[sidx+3] & 077) );
	}
	if (didx < dest.length)
	    dest[didx]   = (byte) ( ((data[sidx] << 2) & 255) |
			    ((data[sidx+1] >>> 4) & 003) );
	if (++didx < dest.length)
	    dest[didx]   = (byte) ( ((data[sidx+1] << 4) & 255) |
			    ((data[sidx+2] >>> 2) & 017) );

	return dest;
    }


    /**
     * This method encodes the given byte[] using the unix uuencode
     * encding. The output is split into lines starting with the encoded
     * number of encoded octets in the line and ending with a newline.
     * No line is longer than 45 octets (60 characters), not including
     * length and newline.
     *
     * <P><em>Note:</em> just the raw data is encoded; no 'begin' and 'end'
     * lines are added as is done by the unix <code>uuencode</code> utility.
     *
     * @param  data the data
     * @return the uuencoded <var>data</var>
     */
    public final static char[] uuencode(byte[] data)
    {
	if (data == null)      return  null;
	if (data.length == 0)  return  new char[0];

	int line_len = 45;	// line length, in octets

	int sidx, didx;
	char nl[]   = System.getProperty("line.separator", "\n").toCharArray(),
	     dest[] = new char[(data.length+2)/3*4 +
			    ((data.length+line_len-1)/line_len)*(nl.length+1)];

	// split into lines, adding line-length and line terminator
	for (sidx=0, didx=0; sidx+line_len < data.length; )
	{
	    // line length
	    dest[didx++] = UUEncMap[line_len];

	    // 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
	    for (int end = sidx+line_len; sidx < end; sidx += 3)
	    {
		dest[didx++] = UUEncMap[(data[sidx] >>> 2) & 077];
		dest[didx++] = UUEncMap[(data[sidx+1] >>> 4) & 017 |
					    (data[sidx] << 4) & 077];
		dest[didx++] = UUEncMap[(data[sidx+2] >>> 6) & 003 |
					    (data[sidx+1] << 2) & 077];
		dest[didx++] = UUEncMap[data[sidx+2] & 077];
	    }

	    // line terminator
	    for (int idx=0; idx<nl.length; idx++)  dest[didx++] = nl[idx];
	}

	// last line

	// line length
	dest[didx++] = UUEncMap[data.length-sidx];

	// 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
	for (; sidx+2 < data.length; sidx += 3)
	{
	    dest[didx++] = UUEncMap[(data[sidx] >>> 2) & 077];
	    dest[didx++] = UUEncMap[(data[sidx+1] >>> 4) & 017 |
					(data[sidx] << 4) & 077];
	    dest[didx++] = UUEncMap[(data[sidx+2] >>> 6) & 003 |
					(data[sidx+1] << 2) & 077];
	    dest[didx++] = UUEncMap[data[sidx+2] & 077];
	}

	if (sidx < data.length-1)
	{
	    dest[didx++] = UUEncMap[(data[sidx] >>> 2) & 077];
	    dest[didx++] = UUEncMap[(data[sidx+1] >>> 4) & 017 |
					(data[sidx] << 4) & 077];
	    dest[didx++] = UUEncMap[(data[sidx+1] << 2) & 077];
	    dest[didx++] = UUEncMap[0];
	}
	else if (sidx < data.length)
	{
	    dest[didx++] = UUEncMap[(data[sidx] >>> 2) & 077];
	    dest[didx++] = UUEncMap[(data[sidx] << 4) & 077];
	    dest[didx++] = UUEncMap[0];
	    dest[didx++] = UUEncMap[0];
	}

	// line terminator
	for (int idx=0; idx<nl.length; idx++)  dest[didx++] = nl[idx];

	// sanity check
	if (didx != dest.length)
	    throw new Error("Calculated "+dest.length+" chars but wrote "+didx+" chars!");

	return dest;
    }


    /**
     * TBD! How to return file name and mode?
     *
     * @param rdr the reader from which to read and decode the data
     * @exception ParseException if either the "begin" or "end" line are not
     *                           found, or the "begin" is incorrect
     * @exception IOException if the <var>rdr</var> throws an IOException
     */
    private final static byte[] uudecode(BufferedReader rdr)
		throws ParseException, IOException
    {
	String line, file_name;
	int    file_mode;


	// search for beginning

	while ((line = rdr.readLine()) != null  &&  !line.startsWith("begin "))
	    ;
	if (line == null)
	    throw new ParseException("'begin' line not found");


	// parse 'begin' line

	StringTokenizer tok = new StringTokenizer(line);
	tok.nextToken();		// throw away 'begin'
	try				// extract mode
	    { file_mode = Integer.parseInt(tok.nextToken(), 8); }
	catch (Exception e)
	    { throw new ParseException("Invalid mode on line: " + line); }
	try				// extract name
	    { file_name = tok.nextToken(); }
	catch (java.util.NoSuchElementException e)
	    { throw new ParseException("No file name found on line: " + line); }


	// read and parse body

	byte[] body = new byte[1000];
	int    off  = 0;

	while ((line = rdr.readLine()) != null  &&  !line.equals("end"))
	{
	    byte[] tmp = uudecode(line.toCharArray());
	    if (off + tmp.length > body.length)
		body = Util.resizeArray(body, off+1000);
	    System.arraycopy(tmp, 0, body, off, tmp.length);
	    off += tmp.length;
	}

	if (line == null)
	    throw new ParseException("'end' line not found");


	return Util.resizeArray(body, off);
    }


    /**
     * This method decodes the given uuencoded char[].
     *
     * <P><em>Note:</em> just the actual data is decoded; any 'begin' and
     * 'end' lines such as those generated by the unix <code>uuencode</code>
     * utility must not be included.
     *
     * @param  data the uuencode-encoded data.
     * @return the decoded <var>data</var>.
     */
    public final static byte[] uudecode(char[] data)
    {
	if (data == null)  return  null;

	int sidx, didx;
	byte dest[] = new byte[data.length/4*3];


	for (sidx=0, didx=0; sidx < data.length; )
	{
	    // get line length (in number of encoded octets)
	    int len = UUDecMap[data[sidx++]];

	    // ascii printable to 0-63 and 4-byte to 3-byte conversion
	    int end = didx+len;
	    for (; didx < end-2; sidx += 4)
	    {
		byte A = UUDecMap[data[sidx]],
		     B = UUDecMap[data[sidx+1]],
		     C = UUDecMap[data[sidx+2]],
		     D = UUDecMap[data[sidx+3]];
		dest[didx++] = (byte) ( ((A << 2) & 255) | ((B >>> 4) & 003) );
		dest[didx++] = (byte) ( ((B << 4) & 255) | ((C >>> 2) & 017) );
		dest[didx++] = (byte) ( ((C << 6) & 255) | (D & 077) );
	    }

	    if (didx < end)
	    {
		byte A = UUDecMap[data[sidx]],
		     B = UUDecMap[data[sidx+1]];
		dest[didx++] = (byte) ( ((A << 2) & 255) | ((B >>> 4) & 003) );
	    }
	    if (didx < end)
	    {
		byte B = UUDecMap[data[sidx+1]],
		     C = UUDecMap[data[sidx+2]];
		dest[didx++] = (byte) ( ((B << 4) & 255) | ((C >>> 2) & 017) );
	    }

	    // skip padding
	    while (sidx < data.length  &&
		   data[sidx] != '\n'  &&  data[sidx] != '\r')
		sidx++;

	    // skip end of line
	    while (sidx < data.length  &&
		   (data[sidx] == '\n'  ||  data[sidx] == '\r'))
		sidx++;
	}

	return Util.resizeArray(dest, didx);
    }


    /**
     * This method does a quoted-printable encoding of the given string
     * according to RFC-2045 (Section 6.7). <em>Note:</em> this assumes
     * 8-bit characters.
     *
     * @param  str the string
     * @return the quoted-printable encoded string
     */
    public final static String quotedPrintableEncode(String str)
    {
	if (str == null)  return  null;

	char map[] =
	    {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'},
	     nl[]  = System.getProperty("line.separator", "\n").toCharArray(),
	     res[] = new char[(int) (str.length()*1.5)],
	     src[] = str.toCharArray();
	char ch;
	int  cnt  = 0,
	     didx = 1,
	     last = 0,
	     slen = str.length();


	for (int sidx=0; sidx < slen; sidx++)
	{
	    ch = src[sidx];

	    if (ch == nl[0]   &&  match(src, sidx, nl))		// Rule #4
	    {
		if (res[didx-1] == ' ')			// Rule #3
		{
		    res[didx-1] = '=';
		    res[didx++] = '2';
		    res[didx++] = '0';
		}
		else if (res[didx-1] == '\t')		// Rule #3
		{
		    res[didx-1] = '=';
		    res[didx++] = '0';
		    res[didx++] = '9';
		}

		res[didx++] = '\r';
		res[didx++] = '\n';
		sidx += nl.length - 1;
		cnt = didx;
	    }
	    else if (ch > 126  ||  (ch < 32  &&  ch != '\t')  ||  ch == '='  ||
		     EBCDICUnsafeChar.get((int) ch))
	    {							// Rule #1, #2
		res[didx++] = '=';
		res[didx++] = map[(ch & 0xf0) >>> 4];
		res[didx++] = map[ch & 0x0f];
	    }
	    else						// Rule #1
	    {
		res[didx++] = ch;
	    }

	    if (didx > cnt+70)					// Rule #5
	    {
		res[didx++] = '=';
		res[didx++] = '\r';
		res[didx++] = '\n';
		cnt = didx;
	    }

	    if (didx > res.length-5)
		res = Util.resizeArray(res, res.length+500);
	}

	return String.valueOf(res, 1, didx-1);
    }

    private final static boolean match(char[] str, int start, char[] arr)
    {
	if (str.length < start + arr.length)  return false;

	for (int idx=1; idx < arr.length; idx++)
	    if (str[start+idx] != arr[idx])  return false;
	return true;
    }


    /**
     * This method does a quoted-printable decoding of the given string
     * according to RFC-2045 (Section 6.7). <em>Note:</em> this method
     * expects the whole message in one chunk, not line by line.
     *
     * @param  str the message
     * @return the decoded message
     * @exception ParseException If a '=' is not followed by a valid
     *                           2-digit hex number or '\r\n'.
     */
    public final static String quotedPrintableDecode(String str)
	    throws ParseException
    {
	if (str == null)  return  null;

	char res[] = new char[(int) (str.length()*1.1)],
	     src[] = str.toCharArray(),
	     nl[]  = System.getProperty("line.separator", "\n").toCharArray();
	int last   = 0,
	    didx   = 0,
	    slen   = str.length();


	for (int sidx=0; sidx<slen; )
	{
	    char ch = src[sidx++];

	    if (ch == '=')
	    {
		if (sidx >= slen-1)
		    throw new ParseException("Premature end of input detected");

		if (src[sidx] == '\n'  ||  src[sidx] == '\r')
		{					// Rule #5
		    sidx++;

		    if (src[sidx-1] == '\r'  &&
			src[sidx] == '\n')
			sidx++;
		}
		else					// Rule #1
		{
		    char repl;
		    int hi = Character.digit(src[sidx], 16),
			lo = Character.digit(src[sidx+1], 16);

		    if ((hi | lo) < 0)
			throw new ParseException(new String(src, sidx-1, 3) +
						" is an invalid code");
		    else
		    {
			repl = (char) (hi << 4 | lo);
			sidx += 2;
		    }

		    res[didx++] = repl;
		}
		last = didx;
	    }
	    else if (ch == '\n'  ||  ch == '\r')	// Rule #4
	    {
		if (ch == '\r'  &&  sidx < slen  &&  src[sidx] == '\n')
		    sidx++;
		for (int idx=0; idx<nl.length; idx++)
		    res[last++] = nl[idx];
		didx = last;
	    }
	    else					// Rule #1, #2
	    {
		res[didx++] = ch;
		if (ch != ' '  &&  ch != '\t')		// Rule #3
		    last = didx;
	    }

	    if (didx > res.length-nl.length-2)
		res = Util.resizeArray(res, res.length+500);
	}

	return new String(res, 0, didx);
    }


    /**
     * This method urlencodes the given string. This method is here for
     * symmetry reasons and just calls java.net.URLEncoder.encode().
     *
     * @param  str the string
     * @return the url-encoded string
     */
    public final static String URLEncode(String str)
    {
	if (str == null)  return  null;

	return java.net.URLEncoder.encode(str);
    }


    /**
     * This method decodes the given urlencoded string.
     *
     * @param  str the url-encoded string
     * @return the decoded string
     * @exception ParseException If a '%' is not followed by a valid
     *                           2-digit hex number.
     */
    public final static String URLDecode(String str) throws ParseException
    {
	if (str == null)  return  null;

	char[] res  = new char[str.length()];
	int    didx = 0;

	for (int sidx=0; sidx<str.length(); sidx++)
	{
	    char ch = str.charAt(sidx);
	    if (ch == '+')
		res[didx++] = ' ';
	    else if (ch == '%')
	    {
		try
		{
		    res[didx++] = (char)
			Integer.parseInt(str.substring(sidx+1,sidx+3), 16);
		    sidx += 2;
		}
		catch (NumberFormatException e)
		{
		    throw new ParseException(str.substring(sidx,sidx+3) +
					    " is an invalid code");
		}
	    }
	    else
		res[didx++] = ch;
	}

	return String.valueOf(res, 0, didx);
    }


    /**
     * This method decodes a multipart/form-data encoded string.
     *
     * @param     data        the form-data to decode.
     * @param     cont_type   the content type header (must contain the
     *			      boundary string).
     * @param     dir         the directory to create the files in.
     * @return                an array of name/value pairs, one for each part;
     *                        the name is the 'name' attribute given in the
     *                        Content-Disposition header; the value is either
     *                        the name of the file if a filename attribute was
     *                        found, or the contents of the part.
     * @exception IOException If any file operation fails.
     * @exception ParseException If an error during parsing occurs.
     * @see #mpFormDataDecode(byte[], java.lang.String, java.lang.String, HTTPClient.FilenameMangler)
     */
    public final static NVPair[] mpFormDataDecode(byte[] data, String cont_type,
						  String dir)
	    throws IOException, ParseException
    {
	return mpFormDataDecode(data, cont_type, dir, null);
    }


    /**
     * This method decodes a multipart/form-data encoded string. The boundary
     * is parsed from the <var>cont_type</var> parameter, which must be of the
     * form 'multipart/form-data; boundary=...'. Any encoded files are created
     * in the directory specified by <var>dir</var> using the encoded filename.
     *
     * <P><em>Note:</em> Does not handle nested encodings (yet).
     *
     * <P>Examples: If you're receiving a multipart/form-data encoded response
     * from a server you could use something like:
     * <PRE>
     *     NVPair[] opts = Codecs.mpFormDataDecode(resp.getData(),
     *                                  resp.getHeader("Content-type"), ".");
     * </PRE>
     * If you're using this in a Servlet to decode the body of a request from
     * a client you could use something like:
     * <PRE>
     *     byte[] body = new byte[req.getContentLength()];
     *     new DataInputStream(req.getInputStream()).readFully(body);
     *     NVPair[] opts = Codecs.mpFormDataDecode(body, req.getContentType(),
     *                                             ".");
     * </PRE>
     * Assuming the data received looked something like:
     * <PRE>
     * -----------------------------114975832116442893661388290519
     * Content-Disposition: form-data; name="option"
     *                                                         &nbsp;
     * doit
     * -----------------------------114975832116442893661388290519
     * Content-Disposition: form-data; name="comment"; filename="comment.txt"
     *                                                         &nbsp;
     * Gnus and Gnats are not Gnomes.
     * -----------------------------114975832116442893661388290519--
     * </PRE>
     * you would get one file called <VAR>comment.txt</VAR> in the current
     * directory, and opts would contain two elements: {"option", "doit"}
     * and {"comment", "comment.txt"}
     *
     * @param     data        the form-data to decode.
     * @param     cont_type   the content type header (must contain the
     *			      boundary string).
     * @param     dir         the directory to create the files in.
     * @param     mangler     the filename mangler, or null if no mangling is
     *                        to be done. This is invoked just before each
     *                        file is created and written, thereby allowing
     *                        you to control the names of the files.
     * @return                an array of name/value pairs, one for each part;
     *                        the name is the 'name' attribute given in the
     *                        Content-Disposition header; the value is either
     *                        the name of the file if a filename attribute was
     *                        found, or the contents of the part.
     * @exception IOException If any file operation fails.
     * @exception ParseException If an error during parsing occurs.
     */
    public final static NVPair[] mpFormDataDecode(byte[] data, String cont_type,
						  String dir,
						  FilenameMangler mangler)
	    throws IOException, ParseException
    {
	// Find and extract boundary string

	String bndstr = Util.getParameter("boundary", cont_type);
	if (bndstr == null)
	    throw new ParseException("'boundary' parameter not found in Content-type: " + cont_type);

	byte[] srtbndry = new byte[bndstr.length()+4],
	       boundary = new byte[bndstr.length()+6],
	       endbndry = new byte[bndstr.length()+6];

	(    "--" + bndstr + "\r\n").getBytes(0, srtbndry.length, srtbndry, 0);
	("\r\n--" + bndstr + "\r\n").getBytes(0, boundary.length, boundary, 0);
	("\r\n--" + bndstr + "--"  ).getBytes(0, endbndry.length, endbndry, 0);


	// setup search routines

	int[] bs = Util.compile_search(srtbndry),
	      bc = Util.compile_search(boundary),
	      be = Util.compile_search(endbndry);


	// let's start parsing the actual data

	int start = Util.findStr(srtbndry, bs, data, 0, data.length);
	if (start == -1)	// didn't even find the start
	    throw new ParseException("Starting boundary not found: " +
				     new String(srtbndry,0));
	start += srtbndry.length;

	NVPair[] res  = new NVPair[10];
	boolean  done = false;
	int      idx;

	for (idx=0; !done; idx++)
	{
	    // find end of this part

	    int end = Util.findStr(boundary, bc, data, start, data.length);
	    if (end == -1)		// must be the last part
	    {
		end = Util.findStr(endbndry, be, data, start, data.length);
		if (end == -1)
		    throw new ParseException("Ending boundary not found: " +
					     new String(endbndry,0));
		done = true;
	    }

	    // parse header(s)

	    String hdr, name=null, value, filename=null, cont_disp = null;

	    while (true)
	    {
		int next = findEOL(data, start) + 2;
		if (next-2 <= start)  break;	// empty line -> end of headers
		hdr      = new String(data, 0, start, next-2-start);
		start    = next;

		// handle line continuation
		byte ch;
		while (next < data.length-1  &&
		       ((ch = data[next]) == ' '  ||  ch == '\t'))
		{
		    next   = findEOL(data, start) + 2;
		    hdr   += new String(data, 0, start, next-2-start);
		    start  = next;
		}

		if (!hdr.regionMatches(true, 0, "Content-Disposition", 0, 19))
		    continue;
		Vector pcd =
			Util.parseHeader(hdr.substring(hdr.indexOf(':')+1));
		HttpHeaderElement elem = Util.getElement(pcd, "form-data");

		if (elem == null)
		    throw new ParseException("Expected 'Content-Disposition: form-data' in line: "+hdr);

		NVPair[] params = elem.getParams();
		name = filename = null;
		for (int pidx=0; pidx<params.length; pidx++)
		{
		    if (params[pidx].getName().equalsIgnoreCase("name"))
			name = params[pidx].getValue();
		    if (params[pidx].getName().equalsIgnoreCase("filename"))
			filename = params[pidx].getValue();
		}
		if (name == null)
		    throw new ParseException("'name' parameter not found in header: "+hdr);

		cont_disp = hdr;
	    }

	    start += 2;
	    if (start > end)
		throw new ParseException("End of header not found at offset "+end);

	    if (cont_disp == null)
		throw new ParseException("Missing 'Content-Disposition' header at offset "+start);

	    // handle data for this part

	    if (filename != null)			// It's a file
	    {
		if (mangler != null)
		    filename = mangler.mangleFilename(filename, name);
		if (filename != null)
		{
		    File file = new File(dir, filename);
		    FileOutputStream out = new FileOutputStream(file);

		    out.write(data, start, end-start);
		    out.close();
		}

		value = filename;
	    }
	    else					// It's simple data
	    {
		value = new String(data, 0, start, end-start);
	    }

	    if (idx >= res.length)
		res = Util.resizeArray(res, idx+10);
	    res[idx] = new NVPair(name, value);

	    start = end + boundary.length;
	}

	return Util.resizeArray(res, idx);
    }


    /**
     * Searches for the next CRLF in an array.
     *
     * @param  arr the byte array to search.
     * @param  off the offset at which to start the search.
     * @return the position of the CR or (arr.length-2) if not found
     */
    private final static int findEOL(byte[] arr, int off)
    {
	while (off < arr.length-1  &&
	       !(arr[off++] == '\r'  &&  arr[off] == '\n'));
	return off-1;
    }

    /**
     * This method encodes name/value pairs and files into a byte array
     * using the multipart/form-data encoding.
     *
     * @param     opts        the simple form-data to encode (may be null);
     *                        for each NVPair the name refers to the 'name'
     *                        attribute to be used in the header of the part,
     *                        and the value is contents of the part.
     * @param     files       the files to encode (may be null); for each
     *                        NVPair the name refers to the 'name' attribute
     *                        to be used in the header of the part, and the
     *                        value is the actual filename (the file will be
     *                        read and it's contents put in the body of that
     *                        part).
     * @param     cont_type   this returns a new NVPair in the 0'th element
     *                        which contains
     *			      name = "Content-Type",
     *			      value = "multipart/form-data; boundary=..."
     *                        (the reason this parameter is an array is
     *                        because a) that's the only way to simulate
     *                        pass-by-reference and b) you need an array for
     *                        the headers parameter to the Post() or Put()
     *                        anyway).
     * @return                an encoded byte array containing all the opts
     *			      and files.
     * @exception IOException If any file operation fails.
     * @see #mpFormDataEncode(HTTPClient.NVPair[], HTTPClient.NVPair[], HTTPClient.NVPair[], HTTPClient.FilenameMangler)
     */
    public final static byte[] mpFormDataEncode(NVPair[] opts, NVPair[] files,
						NVPair[] cont_type)
	    throws IOException
    {
	return mpFormDataEncode(opts, files, cont_type, null);
    }


    private static NVPair[] dummy = new NVPair[0];

    /**
     * This method encodes name/value pairs and files into a byte array
     * using the multipart/form-data encoding. The boundary is returned
     * as part of <var>cont_type</var>.
     * <BR>Example:
     * <PRE>
     *     NVPair[] opts = { new NVPair("option", "doit") };
     *     NVPair[] file = { new NVPair("comment", "comment.txt") };
     *     NVPair[] hdrs = new NVPair[1];
     *     byte[]   data = Codecs.mpFormDataEncode(opts, file, hdrs);
     *     con.Post("/cgi-bin/handle-it", data, hdrs);
     * </PRE>
     * <VAR>data</VAR> will look something like the following:
     * <PRE>
     * -----------------------------114975832116442893661388290519
     * Content-Disposition: form-data; name="option"
     *                                                         &nbsp;
     * doit
     * -----------------------------114975832116442893661388290519
     * Content-Disposition: form-data; name="comment"; filename="comment.txt"
     *                                                         &nbsp;
     * Gnus and Gnats are not Gnomes.
     * -----------------------------114975832116442893661388290519--
     * </PRE>
     * where the "Gnus and Gnats ..." is the contents of the file
     * <VAR>comment.txt</VAR> in the current directory.
     *
     * @param     opts        the simple form-data to encode (may be null);
     *                        for each NVPair the name refers to the 'name'
     *                        attribute to be used in the header of the part,
     *                        and the value is contents of the part.
     * @param     files       the files to encode (may be null); for each
     *                        NVPair the name refers to the 'name' attribute
     *                        to be used in the header of the part, and the
     *                        value is the actual filename (the file will be
     *                        read and it's contents put in the body of that
     *                        part).
     * @param     cont_type   this returns a new NVPair in the 0'th element
     *                        which contains
     *			      name = "Content-Type",
     *			      value = "multipart/form-data; boundary=..."
     *                        (the reason this parameter is an array is
     *                        because a) that's the only way to simulate
     *                        pass-by-reference and b) you need an array for
     *                        the headers parameter to the Post() or Put()
     *                        anyway).
     * @param     mangler     the filename mangler, or null if no mangling is
     *                        to be done. This allows you to change the name
     *                        used in the <var>filename</var> attribute of the
     *                        Content-Disposition header. Note: the mangler
     *                        will be invoked twice for each filename.
     * @return                an encoded byte array containing all the opts
     *			      and files.
     * @exception IOException If any file operation fails.
     */
    public final static byte[] mpFormDataEncode(NVPair[] opts, NVPair[] files,
						NVPair[] cont_type,
						FilenameMangler mangler)
	    throws IOException
    {
	int len = 0,
	    hdr_len = 2  + 2 + 70 + 2 +  39 + 2 +  2;
	     //       \r\n --  bnd  \r\n C..  \r\n \r\n
	byte[] boundary  = new byte[74],
	       cont_disp = new byte[40],
	       filename  = new byte[13];

        ContDisp.getBytes(0, ContDisp.length(), cont_disp, 0);
	FileName.getBytes(0, FileName.length(), filename, 0);
	Boundary.getBytes(0, Boundary.length(), boundary, 0);

	if (opts == null)   opts  = dummy;
	if (files == null)  files = dummy;


	// Calculate the length of the data

	for (int idx=0; idx<opts.length; idx++)
	    len += hdr_len + opts[idx].getName().length() +
		   opts[idx].getValue().length();

	for (int idx=0; idx<files.length; idx++)
	{
	    File file = new File(files[idx].getValue());
	    String fname = file.getName();
	    if (mangler != null)
		fname = mangler.mangleFilename(fname, files[idx].getName());
	    if (fname != null)
	    {
		len += hdr_len + files[idx].getName().length() + 13;
		len += fname.length() + file.length();
	    }
	}

	len -= 2;			// first CR LF is not written
	len += 2 + 2 + 70 + 2 + 2;	// \r\n -- bnd -- \r\n


	// Now fill array

	byte[] res = new byte[len];
	int    pos = 0;

	NewBound: for (int new_c=0x30303030; new_c!=0x7A7A7A7A; new_c++)
	{
	    pos = 0;

	    // modify boundary in hopes that it will be unique
	    while (!BoundChar.get(new_c     & 0xff)) new_c += 0x00000001;
	    while (!BoundChar.get(new_c>>8  & 0xff)) new_c += 0x00000100;
	    while (!BoundChar.get(new_c>>16 & 0xff)) new_c += 0x00010000;
	    while (!BoundChar.get(new_c>>24 & 0xff)) new_c += 0x01000000;
	    boundary[40] = (byte) (new_c     & 0xff);
	    boundary[42] = (byte) (new_c>>8  & 0xff);
	    boundary[44] = (byte) (new_c>>16 & 0xff);
	    boundary[46] = (byte) (new_c>>24 & 0xff);

	    int off = 2;
	    int[] bnd_cmp = Util.compile_search(boundary);

	    for (int idx=0; idx<opts.length; idx++)
	    {
		System.arraycopy(boundary, off, res, pos, boundary.length-off);
		pos += boundary.length - off;
		off  = 0;
		System.arraycopy(cont_disp, 0, res, pos, cont_disp.length);
		pos += cont_disp.length;

		int nlen = opts[idx].getName().length();
		opts[idx].getName().getBytes(0, nlen, res, pos);
		if (nlen >= boundary.length  &&
		    Util.findStr(boundary, bnd_cmp, res, pos, pos+nlen) != -1)
		    continue NewBound;
		pos += nlen;

		res[pos++] = (byte) '"';
		res[pos++] = (byte) '\r';
		res[pos++] = (byte) '\n';
		res[pos++] = (byte) '\r';
		res[pos++] = (byte) '\n';

		int vlen = opts[idx].getValue().length();
		opts[idx].getValue().getBytes(0, vlen, res, pos);
		if (vlen >= boundary.length  &&
		    Util.findStr(boundary, bnd_cmp, res, pos, pos+vlen) != -1)
		    continue NewBound;
		pos += vlen;
	    }

	    for (int idx=0; idx<files.length; idx++)
	    {
		File file = new File(files[idx].getValue());
		String fname = file.getName();
		if (mangler != null)
		    fname = mangler.mangleFilename(fname, files[idx].getName());
		if (fname == null)  continue;

		System.arraycopy(boundary, off, res, pos, boundary.length-off);
		pos += boundary.length - off;
		off  = 0;
		System.arraycopy(cont_disp, 0, res, pos, cont_disp.length);
		pos += cont_disp.length;

		int nlen = files[idx].getName().length();
		files[idx].getName().getBytes(0, nlen, res, pos);
		if (nlen >= boundary.length  &&
		    Util.findStr(boundary, bnd_cmp, res, pos, pos+nlen) != -1)
		    continue NewBound;
		pos += nlen;

		System.arraycopy(filename, 0, res, pos, filename.length);
		pos += filename.length;

		nlen = fname.length();
		fname.getBytes(0, nlen, res, pos);
		if (nlen >= boundary.length  &&
		    Util.findStr(boundary, bnd_cmp, res, pos, pos+nlen) != -1)
		    continue NewBound;
		pos += nlen;

		res[pos++] = (byte) '"';
		res[pos++] = (byte) '\r';
		res[pos++] = (byte) '\n';
		res[pos++] = (byte) '\r';
		res[pos++] = (byte) '\n';

		nlen = (int) file.length();
		int opos = pos;
		FileInputStream fin = new FileInputStream(file);
		while (nlen > 0)
		{
		    int got = fin.read(res, pos, nlen);
		    nlen -= got;
		    pos += got;
		}
		if (Util.findStr(boundary, bnd_cmp, res, opos, pos) != -1)
		    continue NewBound;
	    }

	    break NewBound;
	}

	System.arraycopy(boundary, 0, res, pos, boundary.length);
	pos += boundary.length;
	res[pos++] = (byte) '-';
	res[pos++] = (byte) '-';
	res[pos++] = (byte) '\r';
	res[pos++] = (byte) '\n';

	if (pos != len)
	    throw new Error("Calculated "+len+" bytes but wrote "+pos+" bytes!");

	/* the boundary parameter should be quoted (rfc-2046, section 5.1.1)
	 * but too many script authors are not capable of reading specs...
	 * So, I give up and don't quote it.
	 */
	cont_type[0] = new NVPair("Content-Type",
				  "multipart/form-data; boundary=" +
				  new String(boundary, 0, 4, 70));

	return res;
    }


    /**
     * Turns an array of name/value pairs into the string
     * "name1=value1&name2=value2&name3=value3". The names and values are
     * first urlencoded. This is the form in which form-data is passed to
     * a cgi script.
     *
     * @param pairs the array of name/value pairs
     * @return a string containg the encoded name/value pairs
     */
    public final static String nv2query(NVPair pairs[])
    {
	if (pairs == null)
	    return null;


	int          idx;
	StringBuffer qbuf = new StringBuffer();

	for (idx = 0; idx < pairs.length; idx++)
	{
	    qbuf.append(URLEncode(pairs[idx].getName()) + "=" +
			URLEncode(pairs[idx].getValue()) + "&");
	}

	if (idx > 0)
	    qbuf.setLength(qbuf.length()-1);	// remove trailing '&'

	return qbuf.toString();
    }


    /**
     * Turns a string of the form "name1=value1&name2=value2&name3=value3"
     * into an array of name/value pairs. The names and values are
     * urldecoded. The query string is in the form in which form-data is
     * received in a cgi script.
     *
     * @param query the query string containing the encoded name/value pairs
     * @return an array of NVPairs
     * @exception ParseException If the '=' is missing in any field, or if
     *				 the urldecoding of the name or value fails
     */
    public final static NVPair[] query2nv(String query)  throws ParseException
    {
	if (query == null) return null;

	int idx = -1,
	    cnt = 1;
	while ((idx = query.indexOf('&', idx+1)) != -1)  cnt ++;
	NVPair[] pairs = new NVPair[cnt];

	for (idx=0, cnt=0; cnt<pairs.length; cnt++)
	{
	    int eq  = query.indexOf('=', idx);
	    int end = query.indexOf('&', idx);

	    if (end == -1)  end = query.length();

	    if (eq == -1  ||  eq >= end)
		throw new ParseException("'=' missing in " +
					 query.substring(idx, end));

	    pairs[cnt] =
		    new  NVPair(URLDecode(query.substring(idx,eq)),
				URLDecode(query.substring(eq+1,end)));

	    idx = end + 1;
	}

	return pairs;
    }


    /**
     * Encodes data used the chunked encoding. <var>last</var> signales if
     * this is the last chunk, in which case the appropriate footer is
     * generated.
     *
     * @param data  the data to be encoded; may be null.
     * @param ftrs  optional headers to include in the footer (ignored if
     *              not last); may be null.
     * @param last  whether this is the last chunk.
     * @return an array of bytes containing the chunk
     */
    public final static byte[] chunkedEncode(byte[] data, NVPair[] ftrs,
					     boolean last)
    {
	return
	    chunkedEncode(data, 0, data == null ? 0 : data.length, ftrs, last);
    }


    /**
     * Encodes data used the chunked encoding. <var>last</var> signales if
     * this is the last chunk, in which case the appropriate footer is
     * generated.
     *
     * @param data  the data to be encoded; may be null.
     * @param off   an offset into the <var>data</var>
     * @param len   the number of bytes to take from <var>data</var>
     * @param ftrs  optional headers to include in the footer (ignored if
     *              not last); may be null.
     * @param last  whether this is the last chunk.
     * @return an array of bytes containing the chunk
     */
    public final static byte[] chunkedEncode(byte[] data, int off, int len,
					     NVPair[] ftrs, boolean last)
    {
	if (data == null)
	{
	    data = new byte[0];
	    len  = 0;
	}
	if (last  &&  ftrs == null) ftrs = new NVPair[0];

	// get length of data as hex-string
	String hex_len = Integer.toString(len, 16);


	// calculate length of chunk

	int res_len = 0;
	if (len > 0)	// len CRLF data CRLF
	    res_len += hex_len.length() + 2 + len + 2;

	if (last)
	{
	    res_len += 1 + 2;	// 0 CRLF
	    for (int idx=0; idx<ftrs.length; idx++)
		res_len += ftrs[idx].getName().length() + 2 +	// name ": "
			   ftrs[idx].getValue().length() + 2;	// value CRLF
	    res_len += 2;	// CRLF
	}

	// allocate result

	byte[] res = new byte[res_len];
	int    r_off = 0;


	// fill result

	if (len > 0)
	{
	    hex_len.getBytes(0, hex_len.length(), res, r_off);
	    r_off += hex_len.length();
	    res[r_off++] = (byte) '\r';
	    res[r_off++] = (byte) '\n';

	    System.arraycopy(data, off, res, r_off, len);
	    r_off += len;
	    res[r_off++] = (byte) '\r';
	    res[r_off++] = (byte) '\n';
	}

	if (last)
	{
	    res[r_off++] = (byte) '0';
	    res[r_off++] = (byte) '\r';
	    res[r_off++] = (byte) '\n';

	    for (int idx=0; idx<ftrs.length; idx++)
	    {
		ftrs[idx].getName().getBytes(0, ftrs[idx].getName().length(),
					     res, r_off);
		r_off += ftrs[idx].getName().length();

		res[r_off++] = (byte) ':';
		res[r_off++] = (byte) ' ';

		ftrs[idx].getValue().getBytes(0, ftrs[idx].getValue().length(),
					      res, r_off);
		r_off += ftrs[idx].getValue().length();

		res[r_off++] = (byte) '\r';
		res[r_off++] = (byte) '\n';
	    }

	    res[r_off++] = (byte) '\r';
	    res[r_off++] = (byte) '\n';
	}

	if (r_off != res.length)
	    throw new Error("Calculated "+res.length+" bytes but wrote "+r_off+" bytes!");

	return res;
    }


    /**
     * Decodes chunked data. The chunks are read from an InputStream, which
     * is assumed to be correctly positioned. Use 'xxx instanceof byte[]'
     * and 'xxx instanceof NVPair[]' to determine if this was data or the
     * last chunk.
     *
     * @param  input  the stream from which to read the next chunk.
     * @return If this was a data chunk then it returns a byte[]; else
     *         it's the footer and it returns a NVPair[] containing the
     *         footers.
     * @exception ParseException If any exception during parsing occured.
     * @exception IOException    If any exception during reading occured.
     */
    public final static Object chunkedDecode(InputStream input)
	    throws ParseException, IOException
    {
	int len = getChunkLength(input);

	if (len > 0)			// it's a chunk
	{
	    byte[] res = new byte[len];

	    int off = 0;
	    while (len != -1  &&  off < res.length)
	    {
		len  = input.read(res, off, res.length-off);
		off += len;
	    }

	    if (len == -1)
		throw new ParseException("Premature EOF while reading chunk;" +
					 "Expected: "+res.length+" Bytes, " +
					 "Received: "+(off+1)+" Bytes");

	    input.read();	// CR
	    input.read();	// LF

	    return res;
	}
	else				// it's the end
	{
	    NVPair[] res = new NVPair[0];

	    DataInputStream datain = new DataInputStream(input);
	    String line;

	    // read and parse footer
	    while ((line = datain.readLine()) != null  &&  line.length() > 0)
	    {
		int colon = line.indexOf(':');
		if (colon == -1)
		    throw new ParseException("Error in Footer format: no "+
					     "':' found in '" + line + "'");
		res = Util.resizeArray(res, res.length+1);
		res[res.length-1] = new NVPair(line.substring(0, colon).trim(),
					       line.substring(colon+1).trim());
	    }

	    return res;
	}

    }


    /**
     * Gets the length of the chunk.
     *
     * @param  input  the stream from which to read the next chunk.
     * @return  the length of chunk to follow (w/o trailing CR LF).
     * @exception ParseException If any exception during parsing occured.
     * @exception IOException    If any exception during reading occured.
     */
    final static int getChunkLength(InputStream input)
	    throws ParseException, IOException
    {
	byte[] hex_len = new byte[8];	// if they send more than 2GB chunks...
	int    off     = 0,
	       ch;


	// read chunk length

	while ((ch = input.read()) != '\r'  &&  ch != '\n'  &&
		ch != ';'  &&  off < hex_len.length)
	    hex_len[off++] = (byte) ch;

	if (ch == ';')		// chunk-ext (ignore it)
	    while ((ch = input.read()) != '\r'  &&  ch != '\n') ;

	if (ch != '\n'  &&  (ch != '\r'  ||  input.read() != '\n'))
	    throw new ParseException("Didn't find valid chunk length: " +
				     new String(hex_len, 0, 0, off));

	// parse chunk length

	int len;
	try
	    { len = Integer.parseInt(new String(hex_len, 0, 0, off).trim(),
				     16); }
	catch (NumberFormatException nfe)
	    { throw new ParseException("Didn't find valid chunk length: " +
					new String(hex_len, 0, 0, off) ); }

	return len;
    }

}

