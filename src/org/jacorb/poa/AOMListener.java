package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-98  Gerald Brose.
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
 
import org.omg.PortableServer.Servant;

/**
 * A poa event listener interface for receiving aom events. 
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.00, 06/09/99, RT
 */
public interface AOMListener extends EventListener {
	void objectActivated(byte[] oid, Servant servant, int size);
	void objectDeactivated(byte[] oid, Servant servant, int size);
	void servantEtherialized(byte[] oid, Servant servant);
	void servantIncarnated(byte[] oid, Servant servant);
}







