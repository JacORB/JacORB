/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.orb;

import org.jacorb.config.Configuration;

/**
 * BufferManagerFactory that'll return a non caching BufferManager (e.g. a BufferManager
 * that'll create a new array of byte for every invocation).
 *
 * @author Alphonse Bendt
 */

public class NonCachingBufferManagerFactory implements BufferManagerFactory
{
    private final static IBufferManager bufferManager = new AbstractBufferManager()
    {
        public byte[] getBuffer(int size)
        {
            return new byte[size];
        }
    };

    public IBufferManager newBufferManager(IBufferManager parent, Configuration config)
    {
        return bufferManager;
    }

    public IBufferManager newSingletonBufferManager(Configuration config)
    {
        return bufferManager;
    }
}

