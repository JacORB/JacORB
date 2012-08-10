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

package org.jacorb.notification.filter;

import org.jacorb.config.*;
import org.jacorb.notification.interfaces.Disposable;
import org.omg.CORBA.Any;
import org.omg.CosNotifyFilter.InvalidGrammar;

/**
 * @author Alphonse Bendt
 */
public interface IFilterFactoryDelegate extends Disposable
{
    AbstractFilter create_filter_servant(String grammar) throws InvalidGrammar;

    MappingFilterImpl create_mapping_filter_servant(Configuration config,
            String grammar, Any any) throws InvalidGrammar;

}
