package org.jacorb.notification.engine;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import java.util.Iterator;
import java.util.List;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.jacorb.notification.KeyedListEntry;
import org.jacorb.notification.NotificationEvent;
import org.jacorb.notification.interfaces.FilterStage;
import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;

/**
 *
 *
 * Created: Thu Nov 14 20:34:23 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

class FilterTaskUtils
{

    private static Logger sLogger_ =
        Hierarchy.getDefaultHierarchy().getLoggerFor( FilterTaskUtils.class.getName() );

    static boolean filterEvent( FilterStage destination, NotificationEvent event )
    {
	if ( sLogger_.isDebugEnabled() )
            {
                sLogger_.debug( "filterEvent(" + destination + ", " + event );
            }
	
	switch ( event.getType() )
            {
		
            case NotificationEvent.TYPE_ANY:
                Any _anyEvent = event.toAny();
                return filterEvent( destination, _anyEvent );
		
            case NotificationEvent.TYPE_STRUCTURED:
                StructuredEvent _structEvent = event.toStructuredEvent();
                return filterEvent( destination, _structEvent );
		
            default:
                // bug
                throw new RuntimeException();
            }
    }

    static boolean filterEvent( FilterStage destination, StructuredEvent event )
    {
        List _filterList = destination.getFilters();

        if ( _filterList.isEmpty() )
        {
            return true;
        }

        Iterator _allFilters = _filterList.iterator();

        while ( _allFilters.hasNext() )
        {
            try
            {
                Filter _filter = ( Filter )
                                 ( ( KeyedListEntry ) _allFilters.next() ).getValue();

                if ( _filter.match_structured( event ) )
                {
                    return true;
                }
            }
            catch ( UnsupportedFilterableData ufd )
            {
                // error means false
            }
        }

        return false;
    }

    static boolean filterEvent( FilterStage destination, Any any )
    {
        List _filterList = destination.getFilters();

        if ( _filterList.isEmpty() )
        {
            return true;
        }

        Iterator _allFilters = _filterList.iterator();

        while ( _allFilters.hasNext() )
        {
            try
            {

                Filter _filter = ( Filter )
                                 ( ( KeyedListEntry ) _allFilters.next() ).getValue();

                if ( _filter.match( any ) )
                {
                    return true;
                }
            }
            catch ( UnsupportedFilterableData ufd )
            {
                // error means false
            }
        }

        return false;
    }
}
