package org.jacorb.util.threadpool;
/**
 * LinkedListQueue.java
 *
 *
 * Created: Thu Dec 21 11:35:52 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */
import java.util.Vector;

public class LinkedListQueue
    implements ThreadPoolQueue
{
    private ListElement first = null;
    private ListElement last = null;

    private ListElementPool element_pool = null;

    public LinkedListQueue( int pool_size )
    {
        element_pool = new ListElementPool( pool_size );
    }

    public LinkedListQueue()
    {
        this( 100 );
    }
    
    public synchronized boolean add( Object job )
    {
        ListElement el = element_pool.getElement();

        if( first == null )
        {
            first = el;
            last = el;
        }
        else
        {
            last.setNext( el );
            last = el;
        }
        
        el.setData( job );
        
        return true;
    }

    public Object removeFirst()
    {
        ListElement el = first;
        Object data = el.getData();

        if( first == last )
        {
            first = null;
            last = null;
        }
        else
        {
            first = first.getNext();
        }

        element_pool.returnElement( el );        
        return data;
    }

    public boolean isEmpty()
    {
        return first == null;
    }
} // LinkedListQueue






