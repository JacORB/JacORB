/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 The JacORB project.
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

package org.jacorb.orb.typecode;

import java.util.Map;
import org.jacorb.orb.CDRInputStream;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ValueMember;

/**
 * @author Alphonse Bendt
 */
public class ValueTypeCodeReader extends ComplexTypeCodeReader
{
    protected TypeCode doReadTypeCodeInternal(CDRInputStream in,
            Map recursiveTCMap, Map repeatedTCMap, Integer startPosition,
            int kind, String repositoryID)
    {
        final String name = validateName (in.read_string());

        final short type_modifier = in.read_short();
        final org.omg.CORBA.TypeCode concrete_base_type = in.read_TypeCode(recursiveTCMap, repeatedTCMap);
        final int member_count = in.read_long();
        final ValueMember[] vMembers = new ValueMember[member_count];

        for( int i = 0; i < member_count; i++)
        {
            vMembers[i] = new ValueMember
            (
                in.read_string(),
                null, // id
                null, // defined_in
                null, // version
                in.read_TypeCode(recursiveTCMap, repeatedTCMap),
                null, // type_def
                in.read_short()
            );
        }

        return orb.create_value_tc(repositoryID, name, type_modifier, concrete_base_type, vMembers);
    }
}
