package org.jacorb.notification.filter.etcl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import org.jacorb.notification.AbstractFilter;
import org.jacorb.notification.ApplicationContext;
import org.jacorb.notification.filter.FilterConstraint;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.InvalidConstraint;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class ETCLFilter extends AbstractFilter {

    public final static String CONSTRAINT_GRAMMAR = "EXTENDED_TCL";

    public ETCLFilter(ApplicationContext applicationContext)
    {
        super(applicationContext, CONSTRAINT_GRAMMAR);
    }


    public FilterConstraint newFilterConstraint(ConstraintExp constraintExp) throws InvalidConstraint{
        return new ETCLFilterConstraint(constraintExp);
    }

}
