/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
package org.jacorb.notification.engine;

/*
 *        JacORB - a free Java ORB
 */

/**
 * Task.java
 *
 *
 * Created: Thu Nov 14 18:33:57 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public interface Task extends Runnable {

    public static int DELIVERED = 0;
    public static int NEW = 1;
    public static int PROXY_CONSUMER_FILTERED = 2;
    public static int CONSUMER_ADMIN_FILTERED = 3;
    public static int SUPPLIER_ADMIN_FILTERED = 4;
    public static int PROXY_SUPPLIER_FILTERED = 5;

    public int getStatus();
}// Task
