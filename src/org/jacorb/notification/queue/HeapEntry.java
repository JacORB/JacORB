package org.jacorb.notification.queue;

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

import org.jacorb.notification.interfaces.Message;

/**
 * Single entry within a Heap. An entry consists of a payload (the
 * Notification) and order of simple int type. The order member
 * is useful to keep track in which order the single elements were
 * inserted in a Heap.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

class HeapEntry {

    Message event_;
    long order_;

    HeapEntry(Message event,
              long order) {
        event_ = event;
        order_ = order;
    }

    public String toString() {
        return "[" + order_ + "/" + event_.toString() + "]";
    }
}

