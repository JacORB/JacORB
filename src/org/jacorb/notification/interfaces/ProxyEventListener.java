package org.jacorb.notification.interfaces;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public interface ProxyEventListener {

    /**
     * This event is fired if a Admin wants to create a new Proxy. A
     * Listener may throw AdminLimitExceeded if the creation of more
     * Proxies exeeds a limit.
     */
    void actionProxyCreationRequest( ProxyEvent event )
        throws AdminLimitExceeded;

    void actionProxyDisposed(ProxyEvent event);

    void actionProxyCreated(ProxyEvent event);
}
