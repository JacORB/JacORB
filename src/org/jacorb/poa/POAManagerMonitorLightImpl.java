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
 
import org.jacorb.poa.gui.*;

import org.jacorb.util.Environment;

/**
 * A lightweight implementation of a POAManager monitor
 * 
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.03, 12/08/99, RT
 */
public class POAManagerMonitorLightImpl implements POAManagerMonitor {
	private POAManager model = null;
	public void addPOA(String name) {
	}
	public void closeMonitor() {
	}
	public void init(POAManager poaManager) {
		model = poaManager;
	}
	public void openMonitor() {
		if (Environment.isMonitoringOn()) {
			try {
				POAManagerMonitor newMonitor = (POAManagerMonitor)Class.forName("jacorb.poa.POAManagerMonitorImpl").newInstance();
				newMonitor.init(model);				
				model.setMonitor(newMonitor);
				newMonitor.openMonitor();
			} catch (Throwable exception) {
				org.jacorb.util.Debug.output(0, "Exception occurred in closeMonitor() of POAManagerMonitorLightImpl");
				org.jacorb.util.Debug.output(0, exception);
			}
		}
	}
	public void printMessage(String str) {
	}
	public void removePOA(String name) {
	}
	public void setToActive() {
	}
	public void setToDiscarding(boolean wait) {
	}
	public void setToHolding(boolean wait) {
	}
	public void setToInactive(boolean wait, boolean etherialize) {
	}
}







