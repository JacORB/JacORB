/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
package org.jacorb.imr.util;

import javax.swing.*;
import java.awt.Component;
/**
 * This class sets the combo box in the server table
 * and preselects the correct host name for a server.
 *
 * @author Nicolas Noffke
 * 
 * $Id$
 */

public class ImRTableCellEditor extends DefaultCellEditor {
    
    public ImRTableCellEditor(JComboBox box){
	super(box);
    }

    /**
     * Return HostSelector JComboBox.
     */
    public Component getTableCellEditorComponent(JTable table,
						 Object value,
						 boolean isSelected,
						 int row,
						 int column){

	super.getTableCellEditorComponent(table, value, isSelected, row, column);
	
	JComboBox _box = (JComboBox) editorComponent;
	
	// preselect host name for server
	for(int _i = 0; _i < _box.getItemCount(); _i++){
	    if(((String) value).equals((String) _box.getItemAt(_i))){
		_box.setSelectedIndex(_i);
		break;
	    }
	}

	return _box;
    }
    
} // ImRTableCellEditor








