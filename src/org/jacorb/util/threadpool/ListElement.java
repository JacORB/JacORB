package org.jacorb.util.threadpool;
/**
 * ListElement.java
 *
 *
 * Created: Thu Jan  4 14:33:48 2002
 *
 * @author Nicolas Noffke
 * $Id$
 */

public class ListElement  
{
    private ListElement next = null;
    private Object data = null;

    public ListElement() 
    {        
    }
        
    
    /**
     * Get the value of data.
     * @return Value of data.
     */
    public Object getData() 
    {
        return data;
    }
    
    /**
     * Set the value of data.
     * @param v  Value to assign to data.
     */
    public void setData( Object v ) 
    {
        this.data = v;
    }
    
    /**
     * Get the value of next.
     * @return Value of next.
     */
    public ListElement getNext() 
    {
        return next;
    }
    
    /**
     * Set the value of next.
     * @param v  Value to assign to next.
     */
    public void setNext( ListElement v ) 
    {
        this.next = v;
    }       
} // ListElement






