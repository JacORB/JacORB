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
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;

/**
 * a TypeCodeCompactor implementation that caches previously compacted TypeCodes.
 * after a TypeCode was initially compacted further requests for an equal TypeCode
 * are answered from the cache instead of compacting the TypeCode over and over again.
 *
 * @author Alphonse Bendt
 */
public abstract class AbstractTypeCodeCompactor implements TypeCodeCompactor
{
    protected final Map cache = newCache();

    protected abstract Map newCache();

    public TypeCode getCompactTypeCode(TypeCode originalTypeCode)
    {
        final String id = getIdOrNull(originalTypeCode);

        if (id == null)
        {
            return originalTypeCode;
        }

        return fetchFromCache(id, originalTypeCode);
    }

    /**
     * this method is NOT thread-safe
     */
    protected TypeCode fetchFromCache(String id, TypeCode original)
    {
        TypeCode result = (TypeCode) cache.get(id);

        if (result == null)
        {
            result = original.get_compact_typecode();
            cacheCompactedTypeCode(id, result);
        }

        return result;
    }

    protected abstract void cacheCompactedTypeCode(String id, TypeCode compactedTypeCode);

    private String getIdOrNull(TypeCode typeCode)
    {
        final String result;

        try
        {
            switch (typeCode.kind().value())
            {
                case TCKind._tk_objref:   //14
                case TCKind._tk_struct:   //15
                case TCKind._tk_union:    //16
                case TCKind._tk_enum:     //17
                {
                    result = typeCode.id();
                    break;
                }
                case TCKind._tk_string:   //18
                case TCKind._tk_sequence: //19
                case TCKind._tk_array:    //20
                {
                    result = null;
                    break;
                }
                case TCKind._tk_alias:    //21
                case TCKind._tk_except:   //22
                {
                    result = typeCode.id();
                    break;
                }
                case TCKind._tk_longlong:  // 23
                case TCKind._tk_ulonglong: // 24
                case TCKind._tk_longdouble:// 25
                case TCKind._tk_wchar:     // 26
                case TCKind._tk_wstring:   // 27
                case TCKind._tk_fixed:     // 28
                {
                    result = null;
                    break;
                }
                case TCKind._tk_value:     // 29
                case TCKind._tk_value_box: // 30
                {
                    result = typeCode.id();
                    break;
                }
                case TCKind._tk_native:    // 31
                {
                    result = null;
                    break;
                }
                case TCKind._tk_abstract_interface: //32
                case TCKind._tk_local_interface:    //33
                {
                    result = typeCode.id();
                    break;
                }
                default:
                {
                    result = null;
                    break;
                }
            }
        }
        catch(BadKind e)
        {
            assert false;
            throw new RuntimeException(e);
        }

        return result;
    }
}
