package org.jacorb.util.tracing;

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

import java.util.Calendar;
import java.util.Hashtable;

public class Timer
{
    private Calendar date;
    private Hashtable tableTable;

    public Timer()
    {
        date = Calendar.getInstance();
        tableTable = new Hashtable();
    }

    public void start(int rid, Object target)
    {
        try
        {
            Integer id = new Integer( rid );

            Hashtable table = (Hashtable)tableTable.get( target );
            if( table == null )
            {
                table = new Hashtable();
                tableTable.put( target, table );
            }
            table.put( id, new Long( System.currentTimeMillis()));
        }
        catch( Exception e)
	{
            e.printStackTrace();
        }
    }

    /**
     * @return difference between start and stop time for
     * request rif `
     */

    public long stop(int rid, Object target)
    {
        long t = System.currentTimeMillis();

        Hashtable table = (Hashtable)tableTable.get( target );
        if( table == null )
            System.err.println("errorin timer: no request table for object");

        Long startTime  =
            (Long)table.remove( new Integer(rid ));

        if( startTime != null )
        {
            return t - startTime.longValue();
        }
        else
            return -1;
    }
}






