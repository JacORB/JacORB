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
import org.omg.CORBA.UnionMember;

/**
 * @author Alphonse Bendt
 */
public class UnionTypeCodeReader extends ComplexTypeCodeReader
{
    protected org.omg.CORBA.TypeCode doReadTypeCodeInternal(CDRInputStream in, Map recursiveTCMap,
            Map repeatedTCMap, Integer startPosition, int kind, String id)
    {
        final String name = validateName (in.read_string());

        org.omg.CORBA.TypeCode discriminator_type = in.read_TypeCode(recursiveTCMap, repeatedTCMap);
        // Use the dealiased discriminator type for the label types.
        // This works because the JacORB IDL compiler ignores any aliasing
        // of label types and only the discriminator type is passed on the
        // wire.
        org.omg.CORBA.TypeCode orig_disc_type =
            org.jacorb.orb.TypeCode.originalType(discriminator_type);

        final int default_index = in.read_long();
        final int member_count = in.read_long();

        UnionMember[] union_members = new UnionMember[member_count];
        for( int i = 0; i < member_count; i++)
        {
            org.omg.CORBA.Any label = orb.create_any();

            if( i == default_index )
            {
                // Default discriminator
                label.insert_octet( in.read_octet());
            }
            else
            {
                // use the dealiased discriminator type to construct labels
                label.read_value( in, orig_disc_type );
            }

            union_members[i] = new UnionMember
            (
                    in.read_string(),
                    label,
                    in.read_TypeCode(recursiveTCMap, repeatedTCMap),
                    null
            );
        }

        return orb.create_union_tc(id, name, discriminator_type, union_members, false);
    }
}
