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
import org.jacorb.orb.CDROutputStream;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.TypeCodePackage.Bounds;

/**
 * @author Alphonse Bendt
 */
public abstract class ComplexTypeCodeWriter implements TypeCodeWriter
{
    public void writeTypeCode(TypeCode typeCode, CDROutputStream out,
            Map recursiveTCMap, Map repeatedTCMap)
    {
        if (out.isIndirectionEnabled() && repeatedTCMap.containsKey(typeCode))
        {
            writeIndirectionMarker(out, typeCode, repeatedTCMap);
        }
        else
        {
            try
            {
                tryWriteTypeCode(typeCode, out, recursiveTCMap, repeatedTCMap);
            }
            catch (BadKind e)
            {
                assert false;
                throw new RuntimeException(e);
            }
            catch (Bounds e)
            {
                assert false;
                throw new RuntimeException(e);
            }
        }
    }

    private void tryWriteTypeCode(TypeCode typeCode, CDROutputStream out, Map recursiveTCMap, Map repeatedTCMap) throws BadKind, Bounds
    {
        final int kind = typeCode.kind().value();
        out.write_long( kind  );

        // remember tc start pos before we start writing it
        // out
        final Integer startPosition = Integer.valueOf(out.get_pos());

        recursiveTCMap.put( typeCode.id(), startPosition );

        out.beginEncapsulation();

        doWriteTypeCodeParameters(typeCode, out, recursiveTCMap, repeatedTCMap);

        out.endEncapsulation();

        recursiveTCMap.remove(typeCode.id());

        // add typecode to cache not until here to account for
        // recursive TCs
        repeatedTCMap.put(typeCode, startPosition);
    }

    protected abstract void doWriteTypeCodeParameters(TypeCode typeCode, CDROutputStream out, Map recursiveTCMap, Map repeatedTCMap) throws BadKind, Bounds;

    private final void writeIndirectionMarker(final CDROutputStream out,
                                              final Object key,
                                              final Map indirectionTCMap)
    {
        out.write_long( -1 ); // recursion marker
        int negative_offset =
            ((Integer) indirectionTCMap.get(key)).intValue() - out.get_pos() - 4;

        out.write_long( negative_offset );
    }
}
