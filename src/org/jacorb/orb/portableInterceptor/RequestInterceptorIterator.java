package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.*;
import org.jacorb.orb.SystemExceptionHelper;

/**
 * This class is an iterator over an array
 * of RequestInterceptors.
 *
 * @author Nicolas Noffke
 * @version  $Id$
 */

public abstract class RequestInterceptorIterator 
    extends AbstractInterceptorIterator
{

    private boolean reversed = false;
    private boolean forward = true;

    protected short op = -1;
    protected Exception interceptor_ex = null;

    public RequestInterceptorIterator(Interceptor[] interceptors) 
    {
	super(interceptors);
    }

    /**
     * Reverses the direction in which the list is traversed.
     * The element returned by the next call to nextElement()
     * is the one previous to that returned by the last 
     * call to nextElement(). <br>
     * The direction can only be reversed one time and only if 
     * the starting direction was "forward".
     */

    protected void reverseDirection()
    {
	if (! reversed && forward)
        {
	    increment *= -1;
	    index += (2 * increment);
      
	    reversed = true;
	}
    }

    protected void setDirection(boolean forward)
    {
	this.forward = forward;

	if (forward){
	    index = 0;
	    increment = 1;
	}
	else{
	    index = interceptors.length - 1;;
	    increment = -1;
	}
    }
} // RequestInterceptorIterator






