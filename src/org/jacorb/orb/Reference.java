package org.jacorb.orb;
/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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
 * 
 * @version $Id$
 */

public class Reference
    extends javax.rmi.CORBA.Stub implements java.rmi.Remote
{
    private String[] ids = {"","IDL:omg.org/CORBA/Object:1.0"};
    public String[] _ids()
    {
	return ids;
    }

    public Reference(String typeId)
    {
	ids[0] = typeId;
    }

}
