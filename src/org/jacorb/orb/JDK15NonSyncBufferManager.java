package org.jacorb.orb;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jacorb.config.Configuration;

public class JDK15NonSyncBufferManager extends BufferManager
{
    public JDK15NonSyncBufferManager(Configuration configuration)
    {
        super(configuration);
    }

    protected Collection[] initBufferPool(Configuration configuration, int maxSize)
    {
        Collection[] list = new ConcurrentLinkedQueue[maxSize];

        for (int i = 0; i < list.length; i++)
        {
            list[i] = new ConcurrentLinkedQueue();
        }

        return list;
    }

    protected byte[] doFetchBuffer(Collection list)
    {
        SoftReference entry;

        while( (entry = (SoftReference) ((ConcurrentLinkedQueue)list).poll()) != null)
        {
            byte[] result = (byte[]) entry.get();
            if (result != null)
            {
                return result;
            }
        }

        return null;
    }

    protected void doReturnBuffer(Collection list, byte[] buffer, int threshold)
    {
        if (list.size() < threshold)
        {
            list.add(new SoftReference(buffer));
        }
    }

    protected void storeBuffer(int position, byte[] buffer)
    {
        bufferPool[ position ].add(new SoftReference(buffer));
    }
}
