/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

package org.jacorb.orb.connection;

import java.util.List;

/**
 * This class is used to select a transport for closing. The transport
 * should be idle (although this is checked again later when
 * closing). The given list of transports is synchronized, so no
 * additional synchronization is necessary.
 *
 * @author Nicolas Noffke
 * @version $Id$ */

public interface SelectionStrategy
{
    /**
     * Select a transport for closing. If none is idle, null should be
     * returned.  
     */
    public Transport selectForClose( List transports );
}



