package org.jacorb.orb.portableInterceptor;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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
 *
 */

import org.omg.PortableInterceptor.Interceptor;

/**
 * This class is an iterator over an array
 * of RequestInterceptors.
 *
 * @author Nicolas Noffke
 */

public abstract class RequestInterceptorIterator
    extends AbstractInterceptorIterator
{
    private boolean reversed = false;
    private boolean forward = true;

    protected short op = -1;
    protected Exception interceptor_ex = null;

    protected boolean intermediatePoint = false;

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
            /**
             * If we are at an intermediate interception point e.g. receive_request then
             * any failure results in all previous interceptors that have completed being
             * called.  See OMG 16.4.10.1 Server-side flow rules and in particular the
             * scenario where an exception is raised at the B.receive_request interception
             * point - page 374.  The intermediatePoint flag is true if receive_request so
             * we call all the interceptors in reverse order.
             */
            if (! intermediatePoint)
            {
            increment *= -1;
            index += (2 * increment);
            }
            else
            {
                index = interceptors.length - 1;
                increment = -1;
            }

            reversed = true;
        }
    }

    protected void setDirection(boolean forward)
    {
        this.forward = forward;

        if (forward)
        {
            index = 0;
            increment = 1;
        }
        else
        {
            index = interceptors.length - 1;
            increment = -1;
        }
    }

    protected void setIntermediatePoint(boolean intermediatePoint)
    {
        this.intermediatePoint = intermediatePoint;
    }

} // RequestInterceptorIterator
