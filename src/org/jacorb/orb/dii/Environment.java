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
 *
 * This class is used to hold exception information for
 * DII requests.
 * 
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class Environment
	extends org.omg.CORBA.Environment
{
	private java.lang.Exception ex;

	public Environment(){}


	public void exception( java.lang.Exception e)
	{
		ex = e;
	}

	public java.lang.Exception exception()
	{
		return ex;
	}

	public void clear()
	{
		ex = null;
	}
}








