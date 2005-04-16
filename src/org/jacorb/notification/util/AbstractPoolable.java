package org.jacorb.notification.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.interfaces.Disposable;

/**
 * Interface to indicate that a Object can be pooled. Objects can be pooled to spare ressources.
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractPoolable implements Disposable
{
    private AbstractObjectPool objectPool_;

    protected final Logger logger_ = LogUtil.getLogger(getClass().getName());


    /**
     * The call to this Method indicates that this Object is not needed by the user anymore. After a
     * call to <code>dispose</code> the Object can be returned to its ObjectPool. It's forbidden
     * to use the Object after release has been called as this may cause unexpected behaviour.
     */
    public void dispose()
    {
        if (objectPool_ != null)
        {
            objectPool_.returnObject(this);

            setObjectPool(null);
        }
    }

    /**
     * Set the ObjectPool to which this instance should be returned.
     */
    public synchronized void setObjectPool(AbstractObjectPool pool)
    {
        objectPool_ = pool;
    }

    /**
     * Reset the Object to an initial state. Subclasses should override this method appropiately to
     * reset the instance to an initial state.
     */
    public abstract void reset();
}