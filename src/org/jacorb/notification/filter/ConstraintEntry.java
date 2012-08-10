package org.jacorb.notification.filter;

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

import org.jacorb.notification.EventTypeWrapper;
import org.omg.CosNotifyFilter.ConstraintInfo;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ConstraintEntry
{
    private final FilterConstraint filterConstraint_;

    private final ConstraintInfo constraintInfo_;

    ////////////////////////////////////////

    public ConstraintEntry( FilterConstraint filterConstraint,
                     ConstraintInfo constraintInfo )
    {
        filterConstraint_ = filterConstraint;
        constraintInfo_ = constraintInfo;
    }

    ////////////////////////////////////////
    
    public EventTypeWrapper getEventTypeWrapper( int index )
    {
        return new EventTypeWrapper( constraintInfo_.constraint_expression.event_types[ index ] );
    }

    public int getEventTypeCount()
    {
        return constraintInfo_.constraint_expression.event_types.length;
    }

    public int getConstraintId()
    {
        return constraintInfo_.constraint_id;
    }


    public ConstraintInfo getConstraintInfo()
    {
        return constraintInfo_;
    }

    public String getConstraintExpression()
    {
        return constraintInfo_.constraint_expression.constraint_expr;
    }

    public FilterConstraint getFilterConstraint()
    {
        return filterConstraint_;
    }
    
    public void appendToBuffer(StringBuffer buffer)
    {
        buffer.append("Constraint #");
        buffer.append(getConstraintId());
        buffer.append(": ");
        buffer.append(EventTypeWrapper.toString(constraintInfo_.constraint_expression.event_types));
        buffer.append("\n\t");
        buffer.append(getConstraintExpression());
        buffer.append("\n");
    }
}
