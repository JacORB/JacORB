package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose and the JacORB team
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.jacorb.orb.giop.ReplyPlaceholder;

public class ReplyGroup
{
    private boolean is_open = true;
    private Set<ReplyPlaceholder> replies = null;
    private org.omg.ETF.Profile profile;
    private Delegate owner;

    ReplyGroup (Delegate d, org.omg.ETF.Profile p)
    {
        owner = d;
        profile = p;
    }

    synchronized void postInit ()
    {
        replies = new HashSet<ReplyPlaceholder>();
    }

    synchronized void lockBarrier()
    {
        is_open = false;
    }

    synchronized void openBarrier()
    {
        is_open = true;
        this.notifyAll();
    }

    synchronized void waitOnBarrier()
    {
        while (! is_open)
        {
            try
            {
                this.wait();
            }
            catch ( InterruptedException e )
            {
                //ignore
            }
        }
    }

    public Set<ReplyPlaceholder> getReplies()
    {
        return replies;
    }

    void retry()
    {
        synchronized ( replies )
        {
            for ( Iterator<ReplyPlaceholder> i = replies.iterator();
                  i.hasNext(); )
            {
                ReplyPlaceholder p = i.next();
                p.retry();
            }
        }
    }

    void addHolder (ReplyPlaceholder holder)
    {
        synchronized (replies)
        {
            replies.add (holder);
        }
    }

    void removeHolder (ReplyPlaceholder holder)
    {
        synchronized (replies)
        {
            replies.remove (holder);
        }
    }
}
