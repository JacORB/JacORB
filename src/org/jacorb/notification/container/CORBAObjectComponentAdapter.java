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

import java.lang.reflect.Method;

import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.PicoVerificationException;
import org.picocontainer.defaults.AbstractComponentAdapter;
import org.picocontainer.defaults.AssignabilityRegistrationException;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class CORBAObjectComponentAdapter extends AbstractComponentAdapter
{
    private final org.omg.CORBA.Object delegate_;

    /**
     * Component Adapter to specify a reference to a CORBA Object.
     * 
     * @param service
     *            CORBA Interface the delegate offers
     * @param delegate
     *            CORBA object that offers the service
     */
    public CORBAObjectComponentAdapter(Class service, org.omg.CORBA.Object delegate)
    {
        super(service, service);

        final String _interfaceName = service.getName();
        final String _helperClassName = _interfaceName + "Helper";
        boolean _notAssignable = false;

        try
        {
            Class _helperClass = Class.forName(_helperClassName);
            Method _idMethod = _helperClass.getMethod("id", new Class[0]);
            String _id = (String) _idMethod.invoke(null, null);

            if (!delegate._is_a(_id))
            {
                _notAssignable = true;
            }
        } catch (Exception e)
        {
            _notAssignable = true;
        }

        if (_notAssignable)
        {
            throw new AssignabilityRegistrationException(service, delegate.getClass());
        }

        delegate_ = delegate;
    }

    public Object getComponentInstance(PicoContainer container) throws PicoInitializationException,
            PicoIntrospectionException
    {
        return delegate_;
    }

    public void verify(PicoContainer container) throws PicoVerificationException
    {
        // no op
    }
}