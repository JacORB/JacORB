package org.jacorb.poa.gui.beans;

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
 * Defines an interface for an double list dialog item.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/10/99
 * @see		jacorb.poa.gui.beans.DoubleListDialog
 */
public interface DoubleListItem {
public void _correctWidth(int diff_width);
public void _setContainer(DoubleListDialog _container);
public void _setSelected(boolean selected);

}



