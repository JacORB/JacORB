package org.jacorb.poa.gui;

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
 
/**
 * Defines an interface of a POAManager monitor gui class.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.00, 06/11/99, RT
 */
public interface POAManagerMonitorView {
	void _addPOA(String name);
	void _destroy();
	void _printMessage(String text);
	void _removePOA(String name);
	void _resetState();
	void _setToActive();
	void _setToDiscarding(boolean wait);
	void _setToHolding(boolean wait);
	void _setToInactive(boolean wait, boolean etherialize);
	void _setVisible(boolean visible);
}


