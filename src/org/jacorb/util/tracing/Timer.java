package org.jacorb.util.tracing;

import java.util.*;
import java.io.*;

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
     * @returns difference between start and stop time for
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
