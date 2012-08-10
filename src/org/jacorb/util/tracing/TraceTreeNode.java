package org.jacorb.util.tracing;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import java.util.Vector;

/**
 * TraceTreeNode.java
 *
 * @author Nicolas Noffke
 */
public class TraceTreeNode
{
    protected Vector subtraces = null;

    protected int tracer_id = 0;
    protected String operation = null;
    protected long client_time = 0;
    protected long server_time = 0;


    public TraceTreeNode(int tracer_id)
    {
        this.tracer_id = tracer_id;
        subtraces = new Vector();
    }
}
