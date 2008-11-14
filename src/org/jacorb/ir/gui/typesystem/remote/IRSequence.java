/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.omg.CORBA.SequenceDef;
import org.omg.CORBA.SequenceDefHelper;

/**
 * 
 */

public class IRSequence 
    extends IRNodeWithType 
{
   
    /**
     * IRSequence constructor comment.
     */

    protected IRSequence() 
    {
	super();
    }

    /**
     * IRSequence constructor comment.
     * @param irObject org.omg.CORBA.IRObject
     */

    protected IRSequence(org.omg.CORBA.IRObject irObject)
    {
	super(irObject);
	SequenceDef sequenceDef = SequenceDefHelper.narrow((org.omg.CORBA.Object)irObject);
	setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(sequenceDef.element_type_def()));
	setName("sequence<"+getAssociatedType()+">");
    }
}











