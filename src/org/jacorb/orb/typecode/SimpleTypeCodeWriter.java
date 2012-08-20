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
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;

/**
 * @author Alphonse Bendt
 */
public class SimpleTypeCodeWriter implements TypeCodeWriter
{
    public TCKind[] getSupportedTypeCodes()
    {
        return new TCKind[] {
                TCKind.tk_fixed,
                TCKind.tk_string,
                TCKind.tk_wstring
        };
    }

    public void writeTypeCode(TypeCode typeCode,
            CDROutputStream out,
            Map recursiveTCMap,
            Map repeatedTCMap)
    {
        final int kind = typeCode.kind().value();
        out.write_long(kind);

        try
        {
            switch(kind)
            {
                case TCKind._tk_fixed:
                {
                    out.write_ushort( typeCode.fixed_digits() );
                    out.write_short( typeCode.fixed_scale() );
                    break;
                }
                case TCKind._tk_string:
                    // fallthrough
                case TCKind._tk_wstring:
                {
                    out.write_long(typeCode.length());
                    break;
                }
                default:
                {
                    assert false;
                    throw new RuntimeException();
                }
            }
        }
        catch(BadKind e)
        {
            assert false;
            throw new RuntimeException();
        }
    }
}
