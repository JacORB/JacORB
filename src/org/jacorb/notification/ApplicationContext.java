/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
package org.jacorb.notification;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;


/*
 *        JacORB - a free Java ORB
 */

/**
 * ApplicationContext.java
 *
 *
 * Created: Sat Nov 30 16:02:04 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class ApplicationContext {

    public ApplicationContext() {
	
    }

    ORB orb;
    POA poa;

    /**
     * Get the Orb value.
     * @return the Orb value.
     */
    public ORB getOrb() {
	return orb;
    }

    /**
     * Set the Orb value.
     * @param newOrb The new Orb value.
     */
    public void setOrb(ORB newOrb) {
	this.orb = newOrb;
    }

    /**
     * Get the Poa value.
     * @return the Poa value.
     */
    public POA getPoa() {
	return poa;
    }

    /**
     * Set the Poa value.
     * @param newPoa The new Poa value.
     */
    public void setPoa(POA newPoa) {
	this.poa = newPoa;
    }

    public String toString() {
	StringBuffer _b = new StringBuffer();
	_b.append("orb: " + orb + "\n");
	_b.append("poa: " + poa + "\n");
	return _b.toString();
    }
    
}// ApplicationContext
