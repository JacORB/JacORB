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

/**
 * @author Alphonse Bendt
 */
public interface TypeCodeCache
{
    /**
     * storage class used to put TypeCode's into implementations of the interface TypeCodeCache
     */
    static final class Pair
    {
        /**
         * the cached TypeCode
         */
        public final org.omg.CORBA.TypeCode typeCode;

        /**
         * position of the cached TypeCode relative to the start of the parent TypeCode (is 0 for toplevel TypeCode's).
         */
        public final Integer position;

        /**
         * Create a new <code>Pair</code>.
         *
         * @param typeCode an <code>org.omg.CORBA.TypeCode</code> value
         * @param position an <code>Integer</code> value
         */
        public Pair( org.omg.CORBA.TypeCode typeCode, Integer position )
        {
            this.typeCode = typeCode;
            this.position = position;
        }

        /**
         * <code>toString</code> used for debugging ONLY.
         *
         * @return a <code>String</code> value
         */
        public String toString()
        {
            return ("TypeCode: " + typeCode + " / Position: " + position );
        }
    }

    void cacheTypeCode(String repositoryID, TypeCodeCache.Pair[] entries);

    TypeCodeCache.Pair[] getCachedTypeCodes(String repositoryID);
}
