/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

package org.jacorb.ir.gui.typesystem;


/**
 * Exists only for being inserted into a TableModel as a cell value.
 * This is the only way for the GUI client to find out the corresponding
 * TypeSystemNode for a selected row. (Unfortunately the DefaultTableModel
 * is not prepared to associate an object with each row.)
 */
public class NodeMapper {
    TypeSystemNode node;
    String string;



/**
 * This method was created by a SmartGuide.
 * @param node org.jacorb.ir.gui.typesystem.TypeSystemNode
 */
public NodeMapper ( TypeSystemNode node, String string) {
    this.node = node;
    this.string = string;
}
/**
 * This method was created by a SmartGuide.
 * @return org.jacorb.ir.gui.typesystem.TypeSystemNode
 */
public TypeSystemNode getNode() {
    return node;
}
/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String toString() {
    return string;
}
}

