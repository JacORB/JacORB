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

package org.jacorb.notification.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.CallbackingDisposable;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.DecoratingComponentAdapter;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class NonCachingRememberingComponentAdapter extends DecoratingComponentAdapter
{
    final List activeInstances_ = new ArrayList();

    public NonCachingRememberingComponentAdapter(ComponentAdapter delegate)
    {
        super(delegate);
    }

    public Object getComponentInstance(PicoContainer container) throws PicoInitializationException,
            PicoIntrospectionException
    {
        final Object instance = super.getComponentInstance(container);
        activeInstances_.add(instance);

        if (instance instanceof CallbackingDisposable)
        {
            CallbackingDisposable d = (CallbackingDisposable) instance;
            
            d.addDisposeHook(new Disposable()
            {
                public void dispose()
                {
                    activeInstances_.remove(instance);
                }
            });
        }

        return instance;
    }

    public List getActiveInstances()
    {
        return Collections.unmodifiableList(activeInstances_);
    }
}