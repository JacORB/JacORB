package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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
 
import java.util.*;
import org.jacorb.util.*;

/**
 * A BufferManager is used to share a pool of buffers and to implement
 *  a buffer  allocation policy.  This  reduces the  number of  memory
 * allocations and deallocations and the overall memory footprint.
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$ 
*/

public class BufferManager
{
    private static BufferManager singleton = new BufferManager();

    /** the buffer pool */
    private Stack[] bufferPool;

    /** the maximal buffer size managed since the buffer
	pool is ordered by buffer size in log2 steps */

    private static int MAX;

    /** the buffer at pos n has size 2**(n+MIN_OFFSET)
	so the smallest available buffer is 2**MIN_OFFSET,
        the largest buffers managed are 2**(MIN_OFFSET+MAX-1)
    */

    private static final int MIN_OFFSET = 5;

    /** max number of buffers of the same size held in pool */

    private static final int THREASHOLD = 50;

    private int hits = 0;
    private int calls = 0;

    private BufferManager()
    {
        MAX = Environment.getMaxManagedBufSize();
	bufferPool = new Stack[ MAX ];

	for( int i = 0; i < MAX; i++)
        {
	    bufferPool[ i ] = new Stack();	
        }
    }

    public static BufferManager getInstance()
    {
        return singleton;
    }

    /**
     * Log 2, rounded up
     */

    private static final int log2up(int n)
    {
	int l =0;
	int nn = n-1;
	while( (nn >>l) != 0 )
	    l++;

	return l;
    }


    /**
     * Log 2, rounded down
     */

    private static final int log2down(int n)
    {
	int l =0;
	int nn = n;
	while( (nn >>l) != 0 )
	    l++;

	return l-1;
    }


   /**
     * @param required_capacity - so many more bytes are needed
     * @returns a buffer large enough to hold required_capacity
     */

    public synchronized byte[] getBuffer(int initial)
    {
      //org.jacorb.util.Debug.output( 2, "get buffer: " + initial + " bytes");

	calls++;

	int log = log2up(initial);
	
	if( log >= MAX+MIN_OFFSET  )
	    return new byte[initial];

	Stack s = bufferPool[log > MIN_OFFSET ? log-MIN_OFFSET : 0 ];

	if( ! s.isEmpty() )
	{
	    hits++;
	    Object o = s.pop();
            byte[] result = (byte [])o;
	    return result;
	}
	else
	{
            byte[] b = new byte[log > MIN_OFFSET ? 1<<log : 1 << MIN_OFFSET ];
            return b;
	}
    }

    public synchronized void returnBuffer(byte[] current)
    {
	int log_curr = log2down(current.length);

	/*
	org.jacorb.util.Debug.output( 4, "return buffer: " + current.length + 
				      " bytes, log2: " + log_curr );
	*/

	if( log_curr >= MIN_OFFSET )//+MIN_OFFSET )
	{
	    if( log_curr > MAX )
	    {
		return; // we don't keep anything bigger than 2**MAX
	    }

	    Stack s = bufferPool[ log_curr-MIN_OFFSET ];
	    if( s.size() < THREASHOLD )
	    {
		s.push( current );
	    }
	}
    }

    public void printStatistics()
    {
	System.out.println( "BufferManager statistics:");
	System.out.println("\t get Buffer called: " +  calls);
	System.out.println("\t buffers found in pool: " + hits);
	System.out.println( "\t buffers size: ");

	for( int i= MAX; i > 0; )
	{
	    i--;
	    System.out.println( "\t size 2**" + (5+i) + " # " + bufferPool[i].size());;
	}
    }


    public void release()
    {
	for( int i= MAX; i > 0; )
	{
	    i--;
	    bufferPool[i].removeAllElements();
	}
    }


    public static void main(String[] args)
    {
	for( int i = 0; i < args.length; i++ )
	{
	    int l = Integer.parseInt(args[i]);
	    System.out.println("log2up(" + l + "): " + log2up(l));
	    System.out.println("log2down(" + l + "): " + log2down(l));
	}
    }
}

