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

import java.util.EventListener;

/**
 * A callback interface to notify client code that the acceptor caught an
 * exception. The implementation can then decide how to handle the exception,
 * e.g. shutdown the ORB.
 *
 * @author Nick Cross
 * @version $Id$
 */
public interface AcceptorExceptionListener extends EventListener
{
    /**
     * Throwable <code>th</code> has been caught by the acceptor thread.
     */
    public void exceptionCaught(AcceptorExceptionEvent ae);
}
