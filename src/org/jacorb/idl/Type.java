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

package org.jacorb.idl;

/**
 * This class delegates calls to another Type-object to
 * mimick inheritance.
 * 
 * @author Gerald Brose
 * @version $Id$	
 */


public interface Type 
    extends java.lang.Cloneable
{
    public String typeName();
    
    public String signature();
    
    public boolean basic();

    public String getTypeCodeExpression();
    
    public String holderName();
    
    public String printReadExpression(String streamname);

    public String printReadStatement(String var_name, String streamname);

    public String printWriteStatement(String var_name, String streamname);

}


















