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
 * $Id$
 */

public class ImRTreeCellRenderer extends DefaultTreeCellRenderer {
    private boolean m_use_html_labels = false;
    
    public ImRTreeCellRenderer() {
        super();
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








