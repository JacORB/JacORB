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
import org.jacorb.poa.util.*;
 
import org.jacorb.util.Environment;
import org.jacorb.orb.dsi.ServerRequest;

/**
 * A lightweight implementation of a POA monitor
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.02, 12/08/99, RT
 */
public class POAMonitorLightImpl implements POAMonitor {
	private POA poaModel;
	private AOM aomModel;
	private RequestQueue queueModel;
	private RPPoolManager pmModel;	
	private LogTrace logTrace;
	private String prefix;	
	private boolean isSystemId;
	public void changeState(String state) {
	}
	public void closeMonitor() {
	}
	public void init(POA poa, AOM aom, RequestQueue queue, RPPoolManager pm, 
					String _prefix, boolean _isSystemId, LogTrace _logTrace) {
		poaModel = poa;
		aomModel = aom;
		queueModel = queue;
		pmModel = pm;
		prefix = prefix;
		isSystemId = _isSystemId;
		logTrace = _logTrace;
	}
	public void openMonitor() {
		if (Environment.isMonitoringOn()) {
			try {
				POAMonitor newMonitor = (POAMonitor)Class.forName("jacorb.poa.POAMonitorImpl").newInstance();
				newMonitor.init(poaModel, aomModel, queueModel, pmModel, prefix, isSystemId, logTrace);
				poaModel.setMonitor(newMonitor);
				newMonitor.openMonitor();
			} catch (Throwable exception) {
				logTrace.printLog(0, "Exception occurred in openMonitor() of POAMonitorLightImpl");
				logTrace.printLog(0, exception);				
			}
		}
	}
}







