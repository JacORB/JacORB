package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.*;
import org.omg.CORBA.UserException;

/**
 * AbstractInterceptorIterator.java
 *
 *
 * @version $Id$
 */

public abstract class AbstractInterceptorIterator  
{
    protected Interceptor[] interceptors = null;
    protected int index = 0;
    protected int increment = 1;

    public AbstractInterceptorIterator(Interceptor[] interceptors) 
    {
        this.interceptors = interceptors;
    }
    
    /**
     * Tests, if there are more elements available.
     */
    protected boolean hasMoreElements() 
    {
        return ( index >= 0 ) && ( index < interceptors.length );
    }
  
    /**
     * Returns the next element in the enumeration
     */

    protected Interceptor nextElement() 
    {
        Interceptor _tmp = interceptors[index];
        index += increment;
        return _tmp;
    }

    protected void iterate()
        throws UserException
    {
        while( hasMoreElements() )
            invoke( nextElement() );
    }

    abstract protected void invoke(Interceptor interceptor)
        throws UserException;

} // AbstractInterceptorIterator






