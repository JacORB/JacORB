package org.jacorb.orb;
/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.buffermanager.BufferManagerExpansionPolicy;
import org.jacorb.orb.buffermanager.DefaultExpansionPolicy;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.NO_MEMORY;

/**
 * A BufferManager is used to share a pool of buffers and to implement
 * a buffer  allocation policy.  This  reduces the  number of  memory
 * allocations and deallocations and the overall memory footprint.
 * Buffers are generally created on demand.
 *
 * @author Gerald Brose
*/

public class BufferManager extends AbstractBufferManager
{
   /**
    * Smallest size of arrays that will be cached for reuse. This equates to
    * 1023 (calculated by (1 << ( 9 + 1 ) ) - 1 = 1023 )
    */
   private static final int MIN_CACHE = 9;

   /**
    * Precompute Log2
    */
   private static final double LOG2 = Math.log (2.0);

    /**
     * The buffer pool
     */
    protected final Collection[] bufferPool;

    /**
     * The 'extra-large' buffer cache.
     */
    private byte[] bufferMax = null;

   /**
    * The maximal buffer size managed since the buffer pool is ordered by buffer
    * size in log2 steps.
    *
    * The default size of 22 means that it will manage 2^(22) = 4Mb.
    */
    private final int maxManagedBufferSize;

    /**
     * Max number of buffers of the same size held in pool.
     *
     * Default value is 20.
     */
    private final int threshold;

    /**
     * Purge thread for QoS purging of the bufferMax cache.
     */
    private Reaper reaper;

    /**
     * <code>time</code> denotes whether the maxCache will be active:
     * -1: Not active
     * 0 : Active, never flushed
     * >0: Active with reaper flush thread.
     */
    private final int time;

    /**
     * <code>expansionPolicy</code> defines how buffer sizes
     * will be calculated
     */
    private BufferManagerExpansionPolicy expansionPolicy;

    /**
     * used to create the singleton ORB buffermanager
     * @param configuration
     */
    public BufferManager(Configuration configuration)
    {
        try
        {
           this.time = configuration.getAttributeAsInteger("jacorb.bufferManagerMaxFlush", 0);
           this.maxManagedBufferSize = configuration.getAttributeAsInteger("jacorb.maxManagedBufSize", 22);
           this.threshold = configuration.getAttributeAsInteger("jacorb.bufferManagerThreshold", 20);
        }
        catch (ConfigurationException ex)
        {
           configuration.getLogger ("buffer").error ("Error configuring the BufferManager", ex);
           throw new INTERNAL ("Unable to configure the BufferManager");
        }

        try
        {
            expansionPolicy = (BufferManagerExpansionPolicy)
                configuration.getAttributeAsObject ("jacorb.buffermanager.expansionpolicy",
                                                    DefaultExpansionPolicy.class.getName ());
            if (expansionPolicy instanceof Configurable)
            {
                ((Configurable)expansionPolicy).configure (configuration);
            }
        }
        catch (ConfigurationException e)
        {
            this.expansionPolicy = null;
        }

        bufferPool = initBufferPool(configuration, maxManagedBufferSize);

        // Partly prefill the cache with some buffers.
        int sizes [] = new int [] {1023, 2047};

        for (int i = 0; i < sizes.length; i++)
        {
            for( int min = 0; min < 10; min++ )
            {
                int position = calcLog(sizes[i]) - MIN_CACHE ;
                storeBuffer(position, new byte[sizes[i]]);
            }
        }

        if ( time > 0)
        {
            if (reaper != null)
            {
                // this is the case when
                // the BufferManager is re-configured
                reaper.dispose();
            }

            // create new reaper
            reaper = new Reaper(time);
            reaper.setName ("BufferManager MaxCache Reaper");
            reaper.setDaemon (true);
            reaper.start();
        }
    }

    protected void storeBuffer(final int position, final byte[] buffer)
    {
        bufferPool[ position ].add(buffer);
    }

    protected Collection[] initBufferPool(Configuration configuration, int maxSize)
    {
        final List[] bufferPool = new List[maxSize];

        for( int i = 0; i < bufferPool.length; i++)
        {
            bufferPool[ i ] = (List) new ArrayList (threshold);
        }

        return bufferPool;
    }


    /**
     * Calculate log2 of given value.
     *
     * Incorporates shortcut for known memory buffer size and non-cached values.
     *
     * @param value
     * @return
     */
    private static final int calcLog (int value)
    {
        // Shortcut for uncached_data_length
        if (value <= 1023 )
        {
            return MIN_CACHE;
        }
        else
        {
            return (int)(Math.floor (Math.log (value) / LOG2));
        }
    }


    public byte[] getExpandedBuffer( int size )
    {
        if (size < 0)
        {
           throw new INTERNAL ("Unable to cache and create buffer of negative size. Possible overflow issue.");
        }

        // Use the expansion policy if available
        if (expansionPolicy != null)
        {
            size = expansionPolicy.getExpandedSize (size);
        }

        return getBuffer (size);
    }

    /**
     * <code>getBuffer</code> returns a new buffer.
     *
     * @param size an <code>int</code> value
     * @return a <code>byte[]</code> value
     */

    public byte[] getBuffer( int size )
    {
        byte [] result = null;

        if (size < 0)
        {
           throw new INTERNAL ("Unable to cache and create buffer of negative size. Possible overflow issue.");
        }

        final int log = calcLog(size);

        if (log > maxManagedBufferSize)
        {
            try
            {
                if (time < 0)
                {
                    // Defaults to returning asked for size
                    result = new byte[size];
                }
                else
                {
                    synchronized(this)
                    {
                        // Using cache so do below determination
                        if (bufferMax == null || bufferMax.length < size)
                        {
                            // Autocache really large values for speed
                            bufferMax = new byte[size];
                        }
                        // Else return the cached buffer
                        result = bufferMax;
                        bufferMax = null;
                    }
                }
            }
            catch (OutOfMemoryError e)
            {
                throw new NO_MEMORY(e.toString());
            }
        }
        else
        {
            int index = (log > MIN_CACHE ? log - MIN_CACHE : 0);
            final Collection s = bufferPool[index];
            result = doFetchBuffer(s);

            if (result == null)
            {
                // .. = 1 << MIN_CACHE + 1
                // 64 = 1 << 5 + 1
                // 128 = 1 << 6 + 1
                // 255 = 1 << 7 + 1
                // 512 = 1 << 8 + 1
                // 1024 = 1 << 9 + 1
                // 2048 = 1 << 10 + 1
                result = new byte[ ( log > MIN_CACHE ? 1 << log + 1 : 1024 ) - 1];
            }
        }
        return result;
    }

    protected byte[] doFetchBuffer(Collection list)
    {
        synchronized(list)
        {
            final int size = list.size();

            if (size > 0)
            {
                // pop least recently added buffer from the list
                return (byte[])((AbstractList)list).remove(size-1);
            }
        }

        return null;
    }

    /**
     * Describe <code>returnBuffer</code> method here.
     *
     * @param current a <code>byte[]</code> value
     * @param cdrStr a <code>boolean</code> value value to denote if CDROuputStream is
     *               caller (may use cache in this situation)
     */
    public void returnBuffer(byte[] current, boolean cdrStr)
    {
        if (current != null)
        {
           int log_curr = calcLog(current.length);

            if( log_curr >= MIN_CACHE)
            {
                if( log_curr > maxManagedBufferSize )
                {
                    synchronized(this)
                    {
                        // Only cache if CDROutputStream is called, cache is enabled &
                        // the new value is > than the cached value.
                        if (cdrStr &&
                                (time >= 0 &&
                                        (bufferMax == null || bufferMax.length < current.length)))
                        {
                            bufferMax = current;
                        }
                        return;
                    }
                }

                final Collection s = bufferPool[ log_curr-MIN_CACHE ];

                doReturnBuffer(s, current, threshold);
            }
        }
    }

    protected void doReturnBuffer(Collection list, byte[] buffer, final int threshold)
    {
        synchronized(list)
        {
            if( list.size() < threshold )
            {
                list.add( buffer );
            }
        }
    }

    public void release()
    {
        for( int i= 0; i < bufferPool.length; ++i)
        {
           bufferPool[i].clear();
        }

        if (reaper != null)
        {
            reaper.dispose();
            reaper = null;
        }
    }

    private final class Reaper extends Thread
    {
        private boolean done = false;
        private int sleepInterval = 0;

        public Reaper (int sleepInterval)
        {
            super("BufferManagerReaper");
            // Convert from seconds to milliseconds
            this.sleepInterval = (sleepInterval * 1000);
        }

        public void run()
        {
            long time;

            while (true)
            {
                // Sleep (note time check on wake to catch premature awakening bug)
                time = sleepInterval + System.currentTimeMillis();
                synchronized(this)
                {
                    while(!done && System.currentTimeMillis() <= time)
                    {
                        try
                        {
                            wait(sleepInterval);
                        }
                        catch (InterruptedException ex)
                        {
                            // ignored
                        }
                    }
                }

                // Check not shutting down

                synchronized(this)
                {
                    if(done)
                    {
                        break;
                    }
                }

                synchronized(BufferManager.this)
                {
                    bufferMax = null;
                }
            }
        }

        public synchronized void dispose()
        {
            done = true;

            interrupt();

            // Only one thread waiting so safe to use notify rather than notifyAll.
            notify();
        }
    }
}
