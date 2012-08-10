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

import java.util.Arrays;
import java.util.Map;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.slf4j.Logger;

/**
 * @author Alphonse Bendt
 */
public abstract class AbstractTypeCodeCache implements TypeCodeCache, Configurable
{
    protected final Map cache;
    private Logger logger;

    public AbstractTypeCodeCache()
    {
        cache = newCache();
    }

    protected abstract Map newCache();

    public void cacheTypeCode(String repositoryID, TypeCodeCache.Pair[] entries)
    {
        final TypeCodeCache.Pair[] copy = copy(entries);
        put(repositoryID, copy);

        if (logger.isDebugEnabled())
        {
            logger.debug("put into cache: " + repositoryID + " => " + Arrays.asList(copy));
        }
    }

    protected abstract void put(String repositoryID, final TypeCodeCache.Pair[] entries);

    public TypeCodeCache.Pair[] getCachedTypeCodes(String repositoryID)
    {
        final TypeCodeCache.Pair[] fromCache = (TypeCodeCache.Pair[]) cache.get(repositoryID);

        if (fromCache == null)
        {
            return null;
        }

        TypeCodeCache.Pair[] copy = copy(fromCache);

        if (logger.isDebugEnabled())
        {
            logger.debug("cache hit: " + repositoryID + " => " + Arrays.asList(copy));
        }

        return copy;
    }

    private TypeCodeCache.Pair[] copy(TypeCodeCache.Pair[] fromCache)
    {
        final TypeCodeCache.Pair[] copy = new TypeCodeCache.Pair[fromCache.length];
        for (int i = 0; i < fromCache.length; i++)
        {
            copy[i] = fromCache[i];

        }
        return copy;
    }

    public void configure(Configuration configuration) throws ConfigurationException
    {
        logger = ((org.jacorb.config.Configuration)configuration).getLogger("jacorb.orb.cdr");
    }
}
