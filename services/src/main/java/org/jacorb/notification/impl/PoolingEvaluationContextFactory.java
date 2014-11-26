/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

package org.jacorb.notification.impl;

import org.jacorb.config.*;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.interfaces.EvaluationContextFactory;
import org.jacorb.notification.util.AbstractObjectPool;
import org.jacorb.notification.util.AbstractPoolable;

/**
 * @author Alphonse Bendt
 */
public class PoolingEvaluationContextFactory implements EvaluationContextFactory
{
    private final AbstractObjectPool evaluationContextPool_;
    
    public PoolingEvaluationContextFactory(final Configuration configuration,
            	final EvaluationContextFactory delegate)
    {
        evaluationContextPool_ = new AbstractObjectPool("EvaluationContextPool")
        {
            public Object newInstance()
            {
                return delegate.newEvaluationContext();
            }

            public void doActivateObject(Object o)
            {
                AbstractPoolable obj = (AbstractPoolable) o;
                obj.reset();
                obj.setObjectPool(this);
            }
        };
        
        evaluationContextPool_.configure(configuration);
    }

    public EvaluationContext newEvaluationContext()
    {
        return ( EvaluationContext ) evaluationContextPool_.lendObject();
    }
}