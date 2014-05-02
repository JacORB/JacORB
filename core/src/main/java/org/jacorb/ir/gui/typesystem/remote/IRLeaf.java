/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.ir.gui.typesystem.remote;

/**
 * Abstract superclass for elements of the IR for which there is no corresponding IRObject,
 * or one which does not inherit from Contained, but which should nonetheless show up in
 * our tree (e.g. StructMember).  Is instantiated by classes which have corresponding
 * members() operations (e.g. StructDef).
 * (Other methods would be added if editing the IR would be supported.)
 */
public abstract class IRLeaf extends org.jacorb.ir.gui.typesystem.TypeSystemNode {


/**
 * IRLeaf constructor comment.
 */
protected IRLeaf() {
    super();
}
}











