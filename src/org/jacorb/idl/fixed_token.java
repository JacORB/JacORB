/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-98  Gerald Brose.
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
 * @see java_cup.runtime.long_token
 * @version $Id$
 * @author  Gerald Brose
 */

public class fixed_token 
    extends java_cup.runtime.long_token 
    implements java.io.Serializable
{
    public java.math.BigDecimal fixed_val;

    /** 
     * Full constructor. 
     */

    public fixed_token(int term_num, java.math.BigDecimal f)
    {
	super(term_num);
	fixed_val = f;
    }

}




















