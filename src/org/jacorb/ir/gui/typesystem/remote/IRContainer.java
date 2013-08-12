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

import org.jacorb.ir.gui.typesystem.AbstractContainer;
import org.omg.CORBA.Contained;
import org.omg.CORBA.Container;
import org.omg.CORBA.ContainerHelper;
import org.omg.CORBA.DefinitionKind;
import org.omg.CORBA.IRObject;

/**
 * Abstract superclass for all classes that should have children in our tree.
 * In addition to the "real" CORBA container classes, that should also include,
 * e.g. StructDef.  The latter classes should therefore be containers here,
 * conceptually, since they have members (although they don't inherit from
 * CORBA::Container).
 */
public abstract class IRContainer
    extends IRNode
    implements AbstractContainer
{

    protected IRContainer() {
    super();
    }

    protected IRContainer ( IRObject irObject)
    {
    super(irObject);
    }

    /**
     * Creates TypeSystemNodes for all contained objects.
     * Default implementation, which works for "real" CORBA containers.
     * For other classes, such as IRStruct, which are not real CORBA
     * containers, this method is overridden by specific code to
     * read the members.
     */
    public org.jacorb.ir.gui.typesystem.ModelParticipant[] contents ()
    {
    Container container =
            ContainerHelper.narrow((org.omg.CORBA.Object)this.irObject);
    Contained[] contents =
            container.contents(DefinitionKind.dk_all, true);

    org.jacorb.ir.gui.typesystem.TypeSystemNode[] result =
            new org.jacorb.ir.gui.typesystem.TypeSystemNode[contents.length];

    for (int i=0; i<contents.length; i++)
        {
            result[i] = RemoteTypeSystem.createTypeSystemNode(contents[i]);
    }
    return result;
    }
}
