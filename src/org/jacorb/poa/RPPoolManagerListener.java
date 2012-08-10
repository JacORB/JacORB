package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

/**
 * A poa event listener interface for receiving request processor pool manager events. 
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.01, 06/20/99, RT
 */
public interface RPPoolManagerListener extends EventListener {
	void processorAddedToPool(RequestProcessor processor, int pool_count, int pool_size);
	void processorRemovedFromPool(RequestProcessor processor, int pool_count, int pool_size);
}







