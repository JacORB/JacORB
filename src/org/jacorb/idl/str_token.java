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


package org.jacorb.idl;

/** This subclass of token represents symbols that need to maintain one
 *  String value plus the line and the position this value was found in
 *  as attributes.  It maintains that value in the public
 *  field str_val.
 *
 * @see java_cup.runtime.str_token
 * @version $Id$
 * @author  Gerald Brose
 */

public class str_token
        extends java_cup.runtime.str_token
        implements java.io.Serializable
{

    public String str_val;
    public String line_val;
    public int line_no;
    public int char_pos;
    public String pragma_prefix = "";
    public String fileName = "";

    /** Full constructor. */
    public str_token( int term_num, String v,
                      PositionInfo p, String _fileName )
    {
        /* super class does most of the work */
        super( term_num );

        str_val = v;
        line_val = p.line;
        line_no = p.line_no;
        char_pos = p.line_pos;
        pragma_prefix = p.pragma_prefix;
        fileName = _fileName;
    }

    /** Constructor for value defaulting to an empty string. */
    public str_token( int term_num )
    {
        this( term_num, "", lexer.getPosition(),
                GlobalInputStream.currentFile().getName() );
    }


}
