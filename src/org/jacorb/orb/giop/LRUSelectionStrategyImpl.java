/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

package org.jacorb.orb.giop;

import java.util.List;
import java.util.Iterator;

/**
 * @author Nicolas Noffke
 * @version $Id$
 */

public class LRUSelectionStrategyImpl
    implements SelectionStrategy
{
    public ServerGIOPConnection selectForClose( List connections )
    {
        ServerGIOPConnection oldest = null;
        long last_usage = Long.MAX_VALUE;

        for( Iterator it = connections.iterator();
             it.hasNext();
             )
        {
            ServerGIOPConnection conn = (ServerGIOPConnection) it.next();
            
            if( ! conn.hasPendingMessages() )
            {
                LRUStatisticsProviderImpl sp = (LRUStatisticsProviderImpl)
                    conn.getStatisticsProvider();
  
                if( sp.getLastUsage() < last_usage )
                {
                    oldest = conn;
                    last_usage = sp.getLastUsage();
                }
            }
        }

        return oldest;
    }
}



