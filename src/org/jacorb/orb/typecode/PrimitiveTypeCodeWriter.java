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

/**
 * @author Alphonse Bendt
 */
public class PrimitiveTypeCodeWriter implements TypeCodeWriter
{
    public TCKind[] getSupportedTypeCodes()
    {
        return new TCKind[] {
             TCKind.tk_null,
             TCKind.tk_void,
             TCKind.tk_short,
             TCKind.tk_long,
             TCKind.tk_ushort,
             TCKind.tk_ulong,
             TCKind.tk_float,
             TCKind.tk_double,
             TCKind.tk_boolean,
             TCKind.tk_char,
             TCKind.tk_octet,
             TCKind.tk_any,
             TCKind.tk_TypeCode,
             TCKind.tk_Principal,
             TCKind.tk_longlong,
             TCKind.tk_ulonglong,
             TCKind.tk_wchar,
        };
    }

    public void writeTypeCode(TypeCode typeCode, CDROutputStream out,
            Map recursiveTCMap, Map repeatedTCMap)
    {
        out.write_long(typeCode.kind().value());
    }
}
