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
public class NullTypeCodeCache implements TypeCodeCache
{
    private final static TypeCodeCache instance = new NullTypeCodeCache();
    private final TypeCodeCache.Pair[] emptyResult = new TypeCodeCache.Pair[0];

    public static TypeCodeCache getInstance()
    {
        return instance;
    }

    private NullTypeCodeCache()
    {
        super();
    }

    public void cacheTypeCode(String repositoryID, TypeCodeCache.Pair[] entries)
    {
    }

    public TypeCodeCache.Pair[] getCachedTypeCodes(String repositoryID)
    {
        return emptyResult;
    }
}
