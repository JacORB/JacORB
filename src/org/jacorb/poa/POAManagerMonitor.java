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

import org.jacorb.config.Configurable;

/**
 * The POAManager monitor interface, known to the POAManager 
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.01, 12/08/99, RT
 */
public interface POAManagerMonitor extends Configurable
{
	void addPOA(String name);
	void closeMonitor();
	void init(POAManager poaManager);
	void openMonitor();
	void printMessage(String str);
	void removePOA(String name);
	void setToActive();
	void setToDiscarding(boolean wait);
	void setToHolding(boolean wait);
	void setToInactive(boolean wait, boolean etherialize);
}

