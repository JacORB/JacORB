package org.jacorb.imr;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */


/**
 * This class provides shared or exclusive access to a ressource.
 * It preferes the exclusive access, i.e. if threads are waiting for 
 * exclusive access, shared locks can't be gained.
 *
 * @author Nicolas Noffke
 * 
 * $Id$
 *
 */

public class ResourceLock 
    implements java.io.Serializable 
{
    private int shared;
    private int exclusive;
    private boolean exclusives_waiting = false;

    /**
     * The constructor.
     */

    public ResourceLock() 
    {
        shared = 0;
        exclusive = 0;
    }

    /**
     * This method tries to aquire a shared lock. It blocks
     * until the exclusive lock is released.
     */

    public synchronized void gainSharedLock()
    {
        while(exclusive > 0 && exclusives_waiting)
        {
            try
            {
                wait();
            }
            catch (java.lang.Exception _e)
            {
            }
        }
        shared++;
    }

    /**
     * Release the shared lock. Unblocks threads waiting for
     * access.
     */

    public synchronized void releaseSharedLock()
    {
        if (--shared == 0)
            notifyAll();
    }

    /**
     * This method tries to aquire an exclusive lock. It blocks until
     * all shared locks have been released.
     */

    public synchronized void gainExclusiveLock()
    {
        while(shared > 0 || exclusive > 0)
        {
            try
            {
                exclusives_waiting = true;
                wait();
            }
            catch (java.lang.Exception _e)
            {
            }
        }
        exclusive++;
        exclusives_waiting = false;
    }

    /**
     * Releases the exclusive lock. Unblocks all threads waiting
     * for access.
     */

    public synchronized void releaseExclusiveLock()
    {
        if (--exclusive == 0)
            notifyAll();
    }

} // ResourceLock



