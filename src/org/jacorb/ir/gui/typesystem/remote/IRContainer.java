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

import org.jacorb.ir.gui.typesystem.AbstractContainer;
import org.omg.CORBA.Contained;
import org.omg.CORBA.Container;
import org.omg.CORBA.ContainerHelper;
import org.omg.CORBA.DefinitionKind;
import org.omg.CORBA.IRObject;

/**
 * Abstrakte Oberklasse für alle Klassen, die in unserem Baum Children
 * haben sollen.  Neben  den "echten" CORBA-Container-Klassen soll das
 * z.B.  auch StructDef  sein.  Letztere Klassen  sollen also  bei uns
 * konzeptionell  Container sein, weil sie member  besitzen (auch wenn
 * sie nicht von CORBA::Container erben)
 *
 */

public abstract class IRContainer
    extends IRNode
    implements AbstractContainer
{

    /**
     * AbstractContainer constructor comment.
     */
    protected IRContainer() {
    super();
    }

   /**
    * @param irObject org.omg.CORBA.IRObject
    */

    protected IRContainer ( IRObject irObject)
    {
    super(irObject);
    }

    /**
     * Erzeugt   TypeSystemNodes   für   alle   contained   Objekte.
     * Default-Implementierung,  die  für   "echte"  CORBA-Container
     * funktionert.  Für  andere Klassen  (z.B. IRStruct),  die keine
     * echten  CORBA-Container sind, wird  diese Methode überschrieben
     * mit individuellem Code zum Auslesen der members.
     * @return org.omg.CORBA.Object
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











