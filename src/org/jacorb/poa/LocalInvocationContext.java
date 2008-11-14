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
 
import org.omg.CORBA.ORB;
import org.omg.PortableServer.Servant;

/**
 * Stores the context informations for local object calls
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.00, 09/20/99, RT
 */
public class LocalInvocationContext implements InvocationContext {
	private ORB orb;
	private POA poa;
	private Servant servant;
	private byte[] oid;
	
	private LocalInvocationContext() {
	}
	public LocalInvocationContext(ORB _orb, POA _poa, byte[] _oid, Servant _servant) {
		orb = _orb;
		poa = _poa;
		oid = _oid;
		servant = _servant;
	}
	public byte[] getObjectId() {
		return oid;
	}
	public ORB getORB() {
		return orb;
	}
	public POA getPOA() {
		return poa;
	}
	public Servant getServant() {
		return servant;
	}
}







