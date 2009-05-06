package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
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

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.jacorb.config.NullLogger;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.UnionMember;

/**
 * <code>CreateTypeCodesTest</code> tests validation of names when typecodes
 * are created that include members.  The member name can be blank or null
 * in certain circumstances.  When user created the name must not be null.
 * Related to bugjac10.
 *
 * @author Carol Jordan
 * @version $Id$
 */
public class CreateTypeCodesTest extends TestCase
{
    /**
     * for these tests we need an ORBSingleton not a full ORB
     */
    private final ORB orb = ORB.init();

    protected void setUp() throws Exception
    {
        Field logger = orb.getClass().getDeclaredField("logger");
        logger.setAccessible(true);
        logger.set(orb, new NullLogger());
    }

    public void testCreateStructTC () throws Exception
    {
        String id = null;
        String name = null;
        StructMember [] members;
        TypeCode testTC = null;

        testTC = orb.create_string_tc (10);
        id = "IDL:testCreateStructTC";
        name = "Struct";

        members = new StructMember [2];

        members[0] = new StructMember ("StructMember1", testTC, null);
        members[1] = new StructMember ("StructMember2", testTC, null);

        /* Test valid name */
        orb.create_struct_tc (id, name, members);

        /* Test blank name */
        members[0] = new StructMember ("", testTC, null);
        members[1] = new StructMember ("StructMember2", testTC, null);

        try
        {
            orb.create_struct_tc (id, name, members);

            fail("Excepted exception blank member name in create_struct_tc");
        }
        catch (BAD_PARAM ex)
        {
            // Do nothing - expecting exception}
        }

        /* Test null name */
        members[0] = new StructMember ("StructMember1", testTC, null);
        members[1] = new StructMember (null, testTC, null);

        try
        {
            orb.create_struct_tc (id, name, members);

            fail("Excepted exception null member name in create_struct_tc");
        }
        catch (BAD_PARAM ex)
        {
            // Do nothing - expecting exception}
        }
    }

    /**
     * <code>testCreateEnumTC</code>
     */
    public void testCreateEnumTC () throws Exception
    {
        String id = null;
        String name = null;
        String [] members;

        orb.create_string_tc (10);
        id = "IDL:testCreateEnumTC";
        name = "Enum";

        members = new String [2];

        members[0] = new String ("Member1");
        members[1] = new String ("Member2");

        orb.create_enum_tc (id, name, members);

        /* Test blank name */
        members[0] = new String ("");
        members[1] = new String ("Member2");

        try
        {
            orb.create_enum_tc (id, name, members);

            fail("Excepted exception blank member in create_enum_tc");
        }
        catch (BAD_PARAM ex)
        {
            // Do nothing - expecting exception}
        }

        /* Test null name */
        members[0] = new String ("Member1");
        members[1] = null;

        try
        {
            orb.create_enum_tc (id, name, members);

            fail("Excepted exception null member in create_enum_tc");
        }
        catch (BAD_PARAM ex)
        {
            // Do nothing - expecting exception}
        }
    }

    /**
     * <code>testCreateExceptTC</code>
     */
    public void testCreateExceptTC ()
    {
        String id = null;
        String name = null;
        StructMember [] members;
        TypeCode testTC = null;

        testTC = orb.create_string_tc (10);
        id = "IDL:testCreateExceptTC";
        name = "Except";

        members = new StructMember [2];

        members[0] = new StructMember ("StructMember1", testTC, null);
        members[1] = new StructMember ("StructMember2", testTC, null);

        /* Test valid name */
        orb.create_exception_tc (id, name, members);

        /* Test blank name */
        members[0] = new StructMember ("", testTC, null);
        members[1] = new StructMember ("StructMember2", testTC, null);

        try
        {
            orb.create_exception_tc (id, name, members);

            fail("Excepted exception blank member name in create_except_tc");
        }
        catch (BAD_PARAM ex)
        {
            // Do nothing - expecting exception}
        }

        /* Test null name */
        members[0] = new StructMember ("StructMember1", testTC, null);
        members[1] = new StructMember (null, testTC, null);

        try
        {
            orb.create_exception_tc (id, name, members);

            fail("Excepted exception null member name in create_except_tc");
        }
        catch (BAD_PARAM ex)
        {
            // Do nothing - expecting exception}
        }
    }

    /**
     * <code>testCreateExceptTC</code>
     */
    public void testCreateUnionTC ()
    {
        String id = null;
        String name = null;
        UnionMember [] members;
        TypeCode testTC = null;
        Any label;
        TypeCode discriminator = null;


        testTC = orb.get_primitive_tc (TCKind.from_int (3));
        id = "IDL:testCreateUnionTC";
        name = "Union";
        label= orb.create_any();
        label.insert_long (123);

        discriminator = orb.get_primitive_tc (TCKind.from_int (3));

        members = new UnionMember [2];

        members[0] = new UnionMember ("UnionMember1", label, testTC, null);

        label = orb.create_any();
        label.insert_long(321);

        members[1] = new UnionMember ("UnionMember2", label, testTC, null);

        /* Test valid name */
        orb.create_union_tc (id, name, discriminator, members);


        /* Test blank name */
        members[0] = new UnionMember ("", label, testTC, null);

        label = orb.create_any();
        label.insert_long(123);

        members[1] = new UnionMember ("Member2", label, testTC, null);

        try
        {
            orb.create_union_tc (id, name, discriminator, members);

            fail("Excepted exception blank member name in create_union_tc");
        }
        catch (BAD_PARAM ex)
        {
            // Do nothing - expecting exception}
        }

        /* Test null name */
        members[0] = new UnionMember ("Member1", label, testTC, null);

        label = orb.create_any();
        label.insert_long (321);

        members[1] = new UnionMember (null, label, testTC, null);

        try
        {
            orb.create_union_tc (id, name, discriminator, members);

            fail("Excepted exception null member name in create_union_tc");
        }
        catch (BAD_PARAM ex)
        {
            // Do nothing - expecting exception}
        }
    }
}
