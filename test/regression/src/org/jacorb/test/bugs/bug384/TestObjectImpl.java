package org.jacorb.test.bugs.bug384;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import junit.framework.*;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;

/**
 * Implementation of a test object that can be "ping()-ed"
 *
 * @author Gerald Brose
 * @version $Id$
 */

public class TestObjectImpl
    extends TestObjectPOA
{
    public TestObjectImpl()
    {}

    public void ping()
    {}

    public A[] testMarshall()
    {
        A[] result = new A[2];

        // This will simulate starting and stopping an A server.
        result[0] = AHelper.narrow ((ORB.init( new String[0], null)).string_to_object ("IOR:000000000000002649444C3A6F72672F6A61636F72622F746573742F627567732F6275673338342F413A312E30000000000000020000000000000064000102000000000E3231332E34382E39312E31353700DD770000001041496D706C2F00112A0B024A40214A1E000000020000000000000008000000004A414300000000010000001C00000000000100010000000105010001000101090000000105010001000000010000002C0000000000000001000000010000001C00000000000100010000000105010001000101090000000105010001" ) );
        result[1] = BHelper.narrow ((ORB.init( new String[0], null)).string_to_object ("IOR:000000000000002649444C3A6F72672F6A61636F72622F746573742F627567732F6275673338342F423A312E30000000000000020000000000000064000102000000000E3231332E34382E39312E31353700F0C10000001042496D706C2F00112A0B0704022A0A0E000000020000000000000008000000004A414300000000010000001C00000000000100010000000105010001000101090000000105010001000000010000002C0000000000000001000000010000001C00000000000100010000000105010001000101090000000105010001" ) );

        return result;
    }
}
