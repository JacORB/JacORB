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

import javax.swing.tree.*;
import javax.swing.*;
import java.awt.Component;
import org.jacorb.imr.*;
/**
 * This class sets the tooltip text for the tree cells,
 * and, if possible, enhances the test with HTML.
 *
 * @author Nicolas Noffke
 * 
 * $Log$
 * Revision 1.4  2002/03/19 09:25:11  nicolas
 * updated copyright to 2002
 *
 * Revision 1.3  2002/03/19 11:08:02  brose
 * *** empty log message ***
 *
 * Revision 1.2  2002/03/17 18:44:02  brose
 * *** empty log message ***
 *
 * Revision 1.4  1999/11/25 16:05:49  brose
 * cosmetics
 *
 * Revision 1.3  1999/11/21 20:15:52  noffke
 * GUI data is now updated periodically by a thread
 *
 * Revision 1.2  1999/11/14 17:15:41  noffke
 * Cosmetics and commenting
 *
 *
 */

public class ImRTreeCellRenderer extends DefaultTreeCellRenderer {
    private boolean m_use_html_labels = false;
    
    public ImRTreeCellRenderer() {
	super();
	
	String _html_labels = org.jacorb.util.Environment.getProperty("jacorb.imr.html_labels");
	
	try{
	    m_use_html_labels = (Boolean.valueOf(_html_labels)).booleanValue();
	}catch (Exception _e){
	    _e.printStackTrace();
	}
    }

    /**
     * Set the tooltip text and overwrite the labels with HTML.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
						  boolean sel, boolean expanded,
						  boolean leaf, int row,
						  boolean hasFocus) {
	
	super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

	Object _node = ((DefaultMutableTreeNode) value).getUserObject();

	if (_node instanceof ImRInfo) {
	    setText("Repository");
	    setToolTipText("Port: " + ((ImRInfo) _node).port + 
			   ", Host: " + ((ImRInfo) _node).host);
	} 
	else if (_node instanceof POAInfo){
	    POAInfo _poa = (POAInfo) _node;
	    setToolTipText("POA is " 
			   + ((_poa.active)? "active" : "inactive")); 

	    if (m_use_html_labels){
		String _color = (_poa.active)? "green" : "red";
		setText("<html> <font color=" + _color + ">" + _poa.name + "</font></html>");
	    }
	    else
		setText(_poa.name);
	} 
	else if (_node instanceof ServerInfo){
	    ServerInfo _server = (ServerInfo) _node;

	    setToolTipText("Server is " 
			   + ((_server.active)? "active" : "down")
			   + ((_server.holding)? "and holding" : ""));
	   
	    if (m_use_html_labels){
		String _color = (_server.active)? "green" : "red";
		setText("<tml> <font color=" + _color + ">" + 
			((_server.holding)? "<blink>" : "") +
			_server.name + 
			((_server.holding)? "</blink>" : "") +
			"</font></html>");
	    }
	    else
		setText(_server.name);
	} 
	return this;
    }
} // ImRTreeCellRenderer








