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

package org.jacorb.notification.servant;

import org.jacorb.notification.IContainer;
import org.omg.CosNotifyChannelAdmin.EventChannel;

/**
 * Internal Interface used by Admins to query the settings of the EventChannel they belong to.
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */
public interface IEventChannel extends IContainer
{
    /**
     * @return the id the Admin should use.
     */
    int getAdminID();

    /**
     * @return the id of the parent Channel.
     */
    int getChannelID();

    /**
     * @return the parent Channel.
     */
    EventChannel getEventChannel();
    
    String getChannelMBean();
}
