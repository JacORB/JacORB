/*
 * @(#)HttpHeaderElement.java				0.3-2 18/06/1999
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


/**
 * This class holds a description of an http header element. It is used
 * by <code>HTTPClient.Util.parseHeader()</code>.
 *
 * @see Util#parseHeader(java.lang.String)
 * @see Util#getElement(java.util.Vector, java.lang.String)
 * @see Util#assembleHeader(java.util.Vector)
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

public class HttpHeaderElement
{
    /** element name */
    private String name;

    /** element value */
    private String value;

    /** element parameters */
    private NVPair[] parameters;


    // Constructors

    /**
     * Construct an element with the given name. The value and parameters
     * are set to null. This can be used when a dummy element is constructed
     * for comparison or retrieval purposes.
     *
     * @param name   the name of the element
     */
    public HttpHeaderElement(String name)
    {
	this.name  = name;
	this.value = null;
	parameters = new NVPair[0];
    }


    /**
     * @param name   the first token in the element
     * @param value  the value part, or null
     * @param params the parameters
     */
    public HttpHeaderElement(String name, String value, NVPair[] params)
    {
	this.name  = name;
	this.value = value;
	if (params != null)
	{
	    parameters = new NVPair[params.length];
	    System.arraycopy(params, 0, parameters, 0, params.length);
	}
	else
	    parameters = new NVPair[0];
    }


    // Methods

    /**
     * @return the name
     */
    public String getName()
    {
	return name;
    }


    /**
     * @return the value
     */
    public String getValue()
    {
	return value;
    }


    /**
     * @return the parameters
     */
    public NVPair[] getParams()
    {
	return parameters;
    }


    /**
     * Two elements are equal if they have the same name. The comparison is
     * <em>case-insensitive</em>.
     *
     * @param obj the object to compare with
     * @return true if <var>obj</var> is an HttpHeaderElement with the same
     *         name as this element.
     */
    public boolean equals(Object obj)
    {
	if ((obj != null) && (obj instanceof HttpHeaderElement))
	{
	    String other = ((HttpHeaderElement) obj).name;
	    return name.equalsIgnoreCase(other);
	}

	return false;
    }


    /**
     * @return a string containing the HttpHeaderElement formatted as it
     *         would appear in a header
     */
    public String toString()
    {
	StringBuffer buf = new StringBuffer();
	appendTo(buf);
	return buf.toString();
    }


    /**
     * Append this header element to the given buffer. This is basically a
     * more efficient version of <code>toString()</code> for assembling
     * multiple elements.
     *
     * @param buf the StringBuffer to append this header to
     * @see #toString()
     */
    public void appendTo(StringBuffer buf)
    {
	buf.append(name);

	if (value != null)
	{
	    if (Util.needsQuoting(value))
	    {
		buf.append("=\"");
		buf.append(Util.quoteString(value, "\\\""));
		buf.append('"');
	    }
	    else
	    {
		buf.append('=');
		buf.append(value);
	    }
	}

	for (int idx=0; idx<parameters.length; idx++)
	{
	    buf.append(";");
	    buf.append(parameters[idx].getName());
	    String pval = parameters[idx].getValue();
	    if (pval != null)
	    {
		if (Util.needsQuoting(pval))
		{
		    buf.append("=\"");
		    buf.append(Util.quoteString(pval, "\\\""));
		    buf.append('"');
		}
		else
		{
		    buf.append('=');
		    buf.append(pval);
		}
	    }
	}
    }
}

