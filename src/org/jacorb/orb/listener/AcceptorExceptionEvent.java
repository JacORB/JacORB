/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) The JacORB project, 1997-2006.
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

package org.jacorb.orb.listener;

import org.omg.CORBA.ORB;
import java.util.EventObject;

/**
 * <code>AcceptorExceptionEvent</code> defines an event state object for a
 * Acceptor Exception events.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class AcceptorExceptionEvent extends EventObject
{
    /**
     * <code>orb</code> is a reference to the ORB.
     */
    private final ORB orb;

    /**
     * <code>th</code> is the stack trace of the thrown exception.
     */
    private final Throwable th;


    /**
     * Creates a new <code>AcceptorExceptionEvent</code> instance passing in the
     * source of the event and relevant connection data.
     *
     * @param source an <code>Object</code> value
     * @param orb an <code>ORB</code> value
     * @param th a <code>Throwable</code> value
     */
    public AcceptorExceptionEvent
        (Object source, ORB orb, Throwable th)
    {
        super (source);

        this.orb = orb;
        this.th  = th;
    }


    /**
     * <code>getORB</code> is an accessor for the ORB.
     *
     * @return a <code>ORB</code> value
     */
    public ORB getORB()
    {
        return orb;
    }


    /**
     * <code>getException</code> is an accessor for the stack trace.
     *
     * @return a <code>Throwable</code> value
     */
    public Throwable getException()
    {
        return th;
    }

    /**
     * Returns a String representation of this EventObject.
     *
     * @return  A String representation of this EventObject.
     */
    public String toString()
    {
        return (super.toString() + " with ORB " + orb + " and trace " + th.toString());
    }
}
