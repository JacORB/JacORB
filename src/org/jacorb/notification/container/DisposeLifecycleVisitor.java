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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DecoratingComponentAdapter;
import org.picocontainer.defaults.LifecycleVisitor;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class DisposeLifecycleVisitor extends LifecycleVisitor
{
    private final Class type_;

    public DisposeLifecycleVisitor(Method method, Class ofType, boolean visitInInstantiationOrder)
    {
        super(method, ofType, visitInInstantiationOrder);
        type_ = ofType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.picocontainer.PicoVisitor#visitContainer(org.picocontainer.PicoContainer)
     */
    public void visitContainer(PicoContainer pico)
    {
        List list = pico.getComponentAdaptersOfType(type_);

        final List toBeVisited = new ArrayList();

        Iterator i = list.iterator();

        while (i.hasNext())
        {
            ComponentAdapter ca = (ComponentAdapter) i.next();

            addComponentInstances(pico, ca, toBeVisited);
        }

        visitTemporaryContainer(toBeVisited);
    }

    private void visitTemporaryContainer(final List toBeVisited)
    {
        InvocationHandler handler = new InvocationHandler()
        {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
                if (!method.getName().equals("getComponentInstancesOfType"))
                {
                    throw new RuntimeException("uninteded usage");
                }
                return toBeVisited;
            }
        };

        PicoContainer container = (PicoContainer) Proxy.newProxyInstance(getClass()
                .getClassLoader(), new Class[] { PicoContainer.class }, handler);
        
        super.visitContainer(container);
    }

    private void addComponentInstances(PicoContainer container, ComponentAdapter ca, List list)
    {
        if (ca instanceof NonCachingRememberingComponentAdapter)
        {
            list.addAll(((NonCachingRememberingComponentAdapter) ca).getActiveInstances());
        }
        else if (ca instanceof DecoratingComponentAdapter)
        {
            addComponentInstances(container, ((DecoratingComponentAdapter) ca).getDelegate(), list);
        }
        else
        {
            list.add(ca.getComponentInstance(container));
        }
    }
}