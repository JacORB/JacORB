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
package org.jacorb.poa;

import org.jacorb.orb.dsi.ServerRequest;
import org.omg.PortableServer.Servant;

/**
 * This class will manage an structure of a chain of poa event listeners 
 * and will dispatch events to those listeners.
 *
 * @version 1.01, 06/20/99, RT
 */
public class EventMulticaster
	implements AOMListener, RequestQueueListener, RPPoolManagerListener, POAListener, EventListener {
	protected EventListener one, two;
	protected EventMulticaster(EventListener _one, EventListener _two) {
		one = _one;
		two = _two;
	}
	protected static AOMListener add(AOMListener _one, AOMListener _two) {
		return (AOMListener) add_(_one, _two);
	}
	protected static POAListener add(POAListener _one, POAListener _two) {
		return (POAListener) add_(_one, _two);
	}
	protected static RequestQueueListener add(RequestQueueListener _one, RequestQueueListener _two) {
		return (RequestQueueListener) add_(_one, _two);
	}
	protected static RPPoolManagerListener add(RPPoolManagerListener _one, RPPoolManagerListener _two) {
		return (RPPoolManagerListener) add_(_one, _two);
	}
	protected static EventListener add_(EventListener _one, EventListener _two) {
		if (_one == null)  return _two;
		if (_two == null)  return _one;
		return new EventMulticaster(_one, _two);
	}
	public void objectActivated(byte[] oid, Servant servant, int size) {
		((AOMListener)one).objectActivated(oid, servant, size);
		((AOMListener)two).objectActivated(oid, servant, size);
	}
	public void objectDeactivated(byte[] oid, Servant servant, int size) {
		((AOMListener)one).objectDeactivated(oid, servant, size);
		((AOMListener)two).objectDeactivated(oid, servant, size);
	}
	public void poaCreated(POA poa) {
		((POAListener)one).poaCreated(poa);
		((POAListener)two).poaCreated(poa);
	}
	public void poaStateChanged(POA poa, int new_state) {
		((POAListener)one).poaStateChanged(poa, new_state);
		((POAListener)two).poaStateChanged(poa, new_state);
	}
	public void processorAddedToPool(RequestProcessor processor, int pool_count, int pool_size) {
		((RPPoolManagerListener)one).processorAddedToPool(processor, pool_count, pool_size);
		((RPPoolManagerListener)two).processorAddedToPool(processor, pool_count, pool_size);
	}
	public void processorRemovedFromPool(RequestProcessor processor, int pool_count, int pool_size) {
		((RPPoolManagerListener)one).processorRemovedFromPool(processor, pool_count, pool_size);
		((RPPoolManagerListener)two).processorRemovedFromPool(processor, pool_count, pool_size);
	}
	public void referenceCreated(org.omg.CORBA.Object object) {
		((POAListener)one).referenceCreated(object);
		((POAListener)two).referenceCreated(object);
	}
	protected static AOMListener remove(AOMListener l, AOMListener old) {
		return (AOMListener) remove_(l, old);
	}
	protected EventListener remove(EventListener l) {
		if (l == one) return two;
		if (l == two) return one;
		EventListener a = remove_(one, l);
		EventListener b = remove_(two, l);
		if (a == one && b == two) {
			return this;
		}
		return add_(a, b);
	}
	protected static POAListener remove(POAListener l, POAListener old) {
		return (POAListener) remove_(l, old);
	}
	protected static RequestQueueListener remove(RequestQueueListener l, RequestQueueListener old) {
		return (RequestQueueListener) remove_(l, old);
	}
	protected static RPPoolManagerListener remove(RPPoolManagerListener l, RPPoolManagerListener old) {
		return (RPPoolManagerListener) remove_(l, old);
	}
	protected static EventListener remove_(EventListener l, EventListener old) {
		if (l == old || l == null) {
			return null;
		} else if (l instanceof EventMulticaster) {
			return ((EventMulticaster) l).remove(old);
		} else {
			return l;
		}
	}
	public void requestAddedToQueue(ServerRequest request, int queue_size) {
		((RequestQueueListener)one).requestAddedToQueue(request, queue_size);
		((RequestQueueListener)two).requestAddedToQueue(request, queue_size);
	}
	public void requestRemovedFromQueue(ServerRequest request, int queue_size) {
		((RequestQueueListener)one).requestRemovedFromQueue(request, queue_size);
		((RequestQueueListener)two).requestRemovedFromQueue(request, queue_size);
	}
	public void servantEtherialized(byte[] oid, Servant servant) {
		((AOMListener)one).servantEtherialized(oid, servant);
		((AOMListener)two).servantEtherialized(oid, servant);
	}
	public void servantIncarnated(byte[] oid, Servant servant) {
		((AOMListener)one).servantIncarnated(oid, servant);
		((AOMListener)two).servantIncarnated(oid, servant);
	}
}







