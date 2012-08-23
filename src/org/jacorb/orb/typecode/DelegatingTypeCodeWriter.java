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

import java.util.HashMap;
import java.util.Map;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.TypeCode;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCodePackage.BadKind;

/**
 * @author Alphonse Bendt
 */
public class DelegatingTypeCodeWriter
{
    private final Map writerMap = new HashMap();

    public DelegatingTypeCodeWriter()
    {
        registerTypeCodeWriter(writerMap, new PrimitiveTypeCodeWriter());
        registerTypeCodeWriter(writerMap, new SimpleTypeCodeWriter());
        registerTypeCodeWriter(writerMap, new ObjectReferenceTypeCodeWriter());
        registerTypeCodeWriter(writerMap, new StructTypeCodeWriter());
        registerTypeCodeWriter(writerMap, new UnionTypeCodeWriter());
        registerTypeCodeWriter(writerMap, new EnumTypeCodeWriter());
        registerTypeCodeWriter(writerMap, new SequenceTypeCodeWriter());
        registerTypeCodeWriter(writerMap, new AliasTypeCodeWriter());
        registerTypeCodeWriter(writerMap, new ExceptTypeCodeWriter());
        registerTypeCodeWriter(writerMap, new ValueTypeCodeWriter());
        registerTypeCodeWriter(writerMap, new ValueBoxTypeCodeWriter());
        registerTypeCodeWriter(writerMap, new AbstractInterfaceTypeCodeWriter());
    }

    private void registerTypeCodeWriter(Map map, TypeCodeWriter typeCodeWriter)
    {
        TCKind[] supportedTypeCodes = typeCodeWriter.getSupportedTypeCodes();

        for (int i = 0; i < supportedTypeCodes.length; i++)
        {
            map.put(supportedTypeCodes[i], typeCodeWriter);
        }
    }

    public void writeTypeCode(org.omg.CORBA.TypeCode typeCode,
                              CDROutputStream out,
                              Map recursiveTCMap,
                              Map repeatedTCMap)
    {
        try
        {
            if(TypeCode.isRecursive(typeCode) && recursiveTCMap.containsKey(typeCode.id()))
            {
                writeIndirectionMarker(out, typeCode.id(), recursiveTCMap);
            }
            else
            {
                final TCKind kind = typeCode.kind();
                final TypeCodeWriter delegate = (TypeCodeWriter) writerMap.get(kind);

                if (delegate == null)
                {
                    throw new MARSHAL("Cannot handle TypeCode with kind: " + kind);
                }

                delegate.writeTypeCode(typeCode, out, recursiveTCMap, repeatedTCMap);
            }
        }
        catch (BadKind e)
        {
            assert false;
            throw new RuntimeException(e);
        }
    }

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

