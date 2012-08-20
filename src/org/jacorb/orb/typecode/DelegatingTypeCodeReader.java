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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jacorb.orb.CDRInputStream;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.TypeCode;
import org.slf4j.Logger;

/**
 * this class is the entry point into the TypeCode reading logic.
 *
 * @author Alphonse Bendt
 */
public class DelegatingTypeCodeReader
{
    private final Map readerMap = new HashMap();

    public DelegatingTypeCodeReader()
    {
        readerMap.put(Integer.valueOf(-1), new IndirectionTypeCodeReader());

        final TypeCodeReader primitiveReader = new PrimitiveTypeCodeReader();

        for (int x=0; x<14; ++x)
        {
            readerMap.put(Integer.valueOf(x), primitiveReader);
        }
        readerMap.put(Integer.valueOf(23), primitiveReader);
        readerMap.put(Integer.valueOf(24), primitiveReader);
        readerMap.put(Integer.valueOf(26), primitiveReader);

        readerMap.put(Integer.valueOf(14), new ObjectReferenceTypeCodeReader());
        readerMap.put(Integer.valueOf(15), new StructTypeCodeReader());
        readerMap.put(Integer.valueOf(16), new UnionTypeCodeReader());
        readerMap.put(Integer.valueOf(17), new EnumTypeCodeReader());
        readerMap.put(Integer.valueOf(21), new AliasTypeCodeReader());
        readerMap.put(Integer.valueOf(22), new ExceptTypeCodeReader());
        readerMap.put(Integer.valueOf(29), new ValueTypeCodeReader());
        readerMap.put(Integer.valueOf(30), new ValueBoxTypeCodeReader());
        readerMap.put(Integer.valueOf(32), new AbstractInterfaceTypeCodeReader());

        final TypeCodeReader simpleReader = new SimpleTypeCodeReader();
        readerMap.put(Integer.valueOf(18), simpleReader);
        readerMap.put(Integer.valueOf(27), simpleReader);
        readerMap.put(Integer.valueOf(28), simpleReader);

        final TypeCodeReader sequenceReader = new SequenceTypeCodeReader();
        readerMap.put(Integer.valueOf(19), sequenceReader);
        readerMap.put(Integer.valueOf(20), sequenceReader);
    }

    /**
     * entry method to the TypeCode reader logic
     * @param logger used to log informational/debug information
     * @param in the InputStream from which should be read from
     * @param recursiveTCMap Map that should be used to store the buffer positions of not completely read in TypeCodes
     * @param repeatedTCMap Map that should be used to store the buffer positions of completely read in TypeCodes
     */
    public TypeCode readTypeCode(Logger logger, CDRInputStream in, Map recursiveTCMap, Map repeatedTCMap)
    {
        in.mark(0);

        final int kind = in.read_long();
        final int start_pos = in.get_pos() - 4;

        try
        {
            in.reset();
        }
        catch(IOException e)
        {
            assert false;
            throw new RuntimeException("should not happen");
        }

        if (logger.isDebugEnabled())
        {
            logger.debug(in.getIndentString() + "read TypeCode kind " + kind + " at startposition " + start_pos);
        }

        final TypeCode result = doReadTypeCode(in, recursiveTCMap, repeatedTCMap, kind);

        if (logger.isDebugEnabled())
        {
            logger.debug(in.getIndentString() + "return " + result + " (" + result.getClass().getName() + "@" + System.identityHashCode(result) + ")");
        }

        return result;
    }

    private TypeCode doReadTypeCode(CDRInputStream in, Map recursiveTCMap, Map repeatedTCMap, int kind)
    {
        final TypeCodeReader delegate = (TypeCodeReader) readerMap.get(Integer.valueOf(kind));

        if (delegate == null)
        {
            throw new MARSHAL("cannot handle TypeCode with kind=" + kind);
        }
        else
        {
            return delegate.readTypeCode(in, recursiveTCMap, repeatedTCMap);
        }
    }
}
