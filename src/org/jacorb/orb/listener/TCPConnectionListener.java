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
 * The <code>TCPConnectionListener</code> interface defines methods for a
 * developer to implement in order to receive notifications of socket
 * events from JacORB.
 *
 * @author Nick Cross
 * @version $Id$
 */
public interface TCPConnectionListener extends EventListener
{
    boolean isListenerEnabled();

    /**
     * <code>connectionOpened</code> will be called whenever a socket
     * is opened.
     *
     * @param e a <code>TCPConenctionEvent</code> value
     */
    void connectionOpened(TCPConnectionEvent e);


    /**
     * <code>connectionClosed</code> will be called whenever a socket
     * is closed.
     *
     * @param e a <code>TCPConenctionEvent</code> value
     */
    void connectionClosed(TCPConnectionEvent e);
}
