/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.bugs.bugjac524;

import java.util.ArrayList;
import java.util.List;
import org.jacorb.orb.listener.TCPConnectionEvent;
import org.jacorb.orb.listener.TCPConnectionListener;

/**
 * @author Alphonse Bendt
 */
public class BugJac524TCPConnectionListener implements TCPConnectionListener
{
    static List<TCPConnectionEvent> open = new ArrayList<TCPConnectionEvent>();
    static List<TCPConnectionEvent> close = new ArrayList<TCPConnectionEvent>();

    static void reset()
    {
        open.clear();
        close.clear();
    }

    public void connectionClosed(TCPConnectionEvent e)
    {
        close.add(e);
    }

    public void connectionOpened(TCPConnectionEvent e)
    {
        open.add(e);
    }

    public boolean isListenerEnabled()
    {
        return true;
    }
}
