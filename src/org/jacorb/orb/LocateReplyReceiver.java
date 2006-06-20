package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import org.jacorb.orb.giop.LocateReplyInputStream;

import org.omg.CORBA.portable.RemarshalException;

/**
 * A special ReplyPlaceholder that receives LocateReplies.
 *
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */
public class LocateReplyReceiver
    extends org.jacorb.orb.giop.ReplyPlaceholder
{
    public LocateReplyReceiver(ORB orb)
    {
        super(orb);
    }

    /**
     * This method blocks until a reply becomes available.
     */
    public synchronized LocateReplyInputStream getReply()
    throws RemarshalException
    {
        return (LocateReplyInputStream)getInputStream(false);
    }
}
