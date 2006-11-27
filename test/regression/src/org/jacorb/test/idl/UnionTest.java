package org.jacorb.test.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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


import org.jacorb.test.common.ORBTestCase;

/**
 * Union.java
 *
 * IDL parse tests.
 */

public class UnionTest extends ORBTestCase
{
	// need to initialize ORB as there's a static dependency between the
	// singleton ORB and the standard ORB. otherwise BufferManager wouldn't
	// be intialized properly.
   public void testUnionCheckTypeCode() throws Exception
   {
	   org.omg.CORBA.TypeCode type = org.jacorb.test.bugs.BasicConfig.MOidpairHelper.type();

	   assertEquals("value0",(((org.jacorb.orb.TypeCode)type).member_name(0)));
   }
}
