package org.jacorb.util.threadpool;
/**
 * ElementPool.java
 *
 *
 * Created: Thu Jan  4 14:56:29 2001
 *
 * @author Nicolas Noffke
 * $Id$
 */
import java.util.Stack;

public class ListElementPool  
{
    private Stack pool = null;
    private int max_size = 0;
    
    public ListElementPool( int max_size ) 
    {
        pool = new Stack();
        this.max_size = max_size;
    }

    public ListElement getElement()
    {
        if( pool.size() == 0 )
        {
            return new ListElement();
        }
        else
        {
            return (ListElement) pool.pop();
        }
    }
    
    public void returnElement( ListElement el )
    {
        if( pool.size() < max_size )
        {
            //clean up
            el.setNext( null );
            el.setData( null );
            
            pool.push( el );
        }
        //else throw away        
    }
} // ListElementPool
