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

package org.jacorb.notification.filter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public abstract class AbstractFilterUsageDecorator
{
    long lastUsage_;
    
    long matchCount_ = 0;

    long matchStructuredCount_ = 0;

    long matchTypedCount_ = 0;
    
    final FilterInvocationHandler invocationHandler_;
    
    private final Date created_ = new Date();
    
    protected class FilterInvocationHandler implements InvocationHandler
    {
        private final Object delegate_;

        public FilterInvocationHandler(Object delegate)
        {
            delegate_ = delegate;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            updateUsage(method);

            return method.invoke(delegate_, args);
        }
        
        private void updateUsage(Method method)
        {
            lastUsage_ = System.currentTimeMillis();

            // this will work for MappingFilters and Filters as methods have the same names.
            if (method.getName().equals("match"))
            {
                ++matchCount_;
            }
            else if (method.getName().equals("match_structured"))
            {
                ++matchStructuredCount_;
            }
            else if (method.getName().equals("match_typed"))
            {
                ++matchTypedCount_;
            }
        }
    }

    public AbstractFilterUsageDecorator(Object delegate)
    {
        super();

        invocationHandler_ = new FilterInvocationHandler(delegate);
    }

    public Date getLastUsage()
    {
        return new Date(lastUsage_);
    }

    public Date getCreationDate()
    {
        return created_;
    }
    
    public long getMatchCount()
    {
        return matchCount_;
    }

    public long getMatchStructuredCount()
    {
        return matchStructuredCount_;
    }

    public long getMatchTypedCount()
    {
        return matchTypedCount_;
    }
}
