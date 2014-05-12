/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import org.jacorb.notification.filter.CurrentTimeUtil;
import org.omg.CORBA.ORB;
import org.omg.CosTime.TimeService;
import org.omg.CosTime.TimeServiceHelper;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;
import org.picocontainer.defaults.DecoratingComponentAdapter;
import org.picocontainer.defaults.DefaultPicoContainer;

public class CurrentTimeUtilComponentAdapter extends DecoratingComponentAdapter
{
	private static final long serialVersionUID = 1L;

    public CurrentTimeUtilComponentAdapter()
    {
        super(new ConstructorInjectionComponentAdapter(CurrentTimeUtil.class, CurrentTimeUtil.class));
    }

    public Object getComponentInstance(PicoContainer container) throws PicoInitializationException,
            PicoIntrospectionException
    {
        ORB orb = (ORB) container.getComponentInstanceOfType(ORB.class);

        MutablePicoContainer tempContainer = new DefaultPicoContainer(container);
        try
        {
            TimeService timeService = TimeServiceHelper.narrow(orb.resolve_initial_references("TimeService"));
            tempContainer.registerComponent(new CORBAObjectComponentAdapter(TimeService.class, timeService));
        } catch (Exception e)
        {
            // ignored
        }

        return super.getComponentInstance(tempContainer);
    }

    public void verify(PicoContainer container) throws PicoIntrospectionException
    {
        // no op
    }
}
