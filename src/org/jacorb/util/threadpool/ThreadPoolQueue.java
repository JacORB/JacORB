package org.jacorb.util.threadpool;
/**
 * ThreadPoolQueue.java
 *
 *
 * Created: Fri Jun  9 15:18:43 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */

public interface ThreadPoolQueue
{
    public boolean add( Object job );
    public Object removeFirst();

    public boolean isEmpty();
} // ThreadPoolQueue
