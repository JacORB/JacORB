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
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;

/**
 * @author Alphonse Bendt
 */
public class SequenceTypeCodeReader extends AbstractTypeCodeReader
{
    protected TypeCode doReadTypeCode(CDRInputStream in, Map recursiveTCMap,
            Map repeatedTCMap, Integer startPosition, int kind)
    {
        in.openEncapsulation();
        final TypeCode element_type = in.read_TypeCode(recursiveTCMap, repeatedTCMap);
        final TypeCode result;

        final int length = in.read_long();

        switch(kind)
        {
            case TCKind._tk_sequence:       // 19
            {
                result = orb.create_sequence_tc(length, element_type);
                break;
            }
            case TCKind._tk_array:          // 20
            {
                result = orb.create_array_tc(length, element_type );
                break;
            }
            default:
            {
                throw new IllegalArgumentException();
            }
        }

        repeatedTCMap.put(startPosition, result);

        in.closeEncapsulation();

        return result;
    }
}
