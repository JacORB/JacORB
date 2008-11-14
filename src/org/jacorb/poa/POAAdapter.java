package org.jacorb.poa;

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
 
import org.jacorb.orb.dsi.ServerRequest;
import org.omg.PortableServer.Servant;

/**
 * The adapter which can receives poa events. It implements all poa related 
 * listener interfaces. The methods in this class are empty.
 * This class is provided as a convenience for easily creating poa listeners 
 * by extending this class and overriding only the methods of interest.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.01, 06/20/99, RT
 */
abstract public class POAAdapter 
	implements AOMListener, POAListener, RequestQueueListener, RPPoolManagerListener {
	public void objectActivated(byte[] oid, Servant servant, int aom_size) {
	}
	public void objectDeactivated(byte[] oid, Servant servant, int aom_size) {
	}
	public void poaCreated(POA poa) {
	}
	public void poaStateChanged(POA poa, int new_state) {
	}
	public void processorAddedToPool(RequestProcessor processor, int pool_count, int pool_size) {
	}
	public void processorRemovedFromPool(RequestProcessor processor, int pool_count, int pool_size) {
	}
	public void referenceCreated(org.omg.CORBA.Object object) {
		/* reference creation will not propagated in this version */ 
	}
	public void requestAddedToQueue(ServerRequest request, int queue_size) {
	}
	public void requestRemovedFromQueue(ServerRequest request, int queue_size) {
	}
	public void servantEtherialized(byte[] oid, Servant servant) {
	}
	public void servantIncarnated(byte[] oid, Servant servant) {
	}
}







