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

package org.jacorb.orb.dii;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

import java.util.Vector;

public class ExceptionList 
	extends org.omg.CORBA.ExceptionList 
{
	private Vector list = new java.util.Vector();

	public int count()
	{
		return list.size();
	}

	public void add(org.omg.CORBA.TypeCode tc )
	{
		list.addElement( tc );
	}

	public org.omg.CORBA.TypeCode item(int index) 
		throws org.omg.CORBA.Bounds
	{
		try 
		{
			return (org.omg.CORBA.TypeCode)list.elementAt(index);
		}
		catch ( ArrayIndexOutOfBoundsException e )
		{
			throw new org.omg.CORBA.Bounds();
		}
	}

	public void remove(int index) 
		throws org.omg.CORBA.Bounds
	{
		try 
		{
			list.removeElementAt(index);
		} 
		catch ( ArrayIndexOutOfBoundsException e )
		{
			throw new org.omg.CORBA.Bounds();
		}
	}
}


