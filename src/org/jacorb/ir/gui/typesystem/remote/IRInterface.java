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

/**
 *
 */

import java.util.Enumeration;
import java.util.Vector;
import org.jacorb.ir.gui.typesystem.Interface;
import org.jacorb.ir.gui.typesystem.ModelParticipant;
import org.jacorb.ir.gui.typesystem.TypeSystemNode;
import org.omg.CORBA.IRObject;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.InterfaceDefHelper;

public class IRInterface
    extends IRContainer
    implements org.jacorb.ir.gui.typesystem.Interface
{
    private IRInterface[] superInterfaces = null;
    private IRAttribute[] allFields = null;
    private IROperation[] allOperations = null;

    /**
     * Default-Konstruktor: wird von TypeSystem.createNode(...) benutzt
     */
    public IRInterface ( ) {
    super();
    }

    /**
     * This method was created by a SmartGuide.
     * @param irObject org.omg.CORBA.IRObject
     */
    public IRInterface ( IRObject irObject) {
    super(irObject);
    }

    /**
     * This method was created by a SmartGuide.
     * @return java.lang.String[]
     */
    public String[] allowedToAdd() {
    String[] result = {	IRAttribute.nodeTypeName(),
                                IROperation.nodeTypeName(),
                                IRConstant.nodeTypeName(),
                                IRTypedef.nodeTypeName(),
                                IRException.nodeTypeName()};
    return result;
    }

    /**
     * @return java.lang.String
     */

    public String description()
    {
    String result = super.description();
    Interface[] superinterfaces = getSuperInterfaces();

    if (superinterfaces.length>0)
        {
            result = result + "\nSuper-Interfaces:\t ";
            for (int i = 0; i<superinterfaces.length; i++)
            {
                result = result + ((TypeSystemNode)superinterfaces[i]).getAbsoluteName();
                if (!(i==superinterfaces.length-1))
                {
                    result = result + ", ";
                }
            }
    }
    else
        {
            result = result	+ "\nSuper-Interfaces:\t:none";
    }
    return result;
    }

    /**
     * Gibt alle Fields inkl. der Fields der Super-Interfaces zurück
     * @return org.jacorb.ir.gui.typesystem.TypeSystemNode[]
     */

    public TypeSystemNode[] getAllFields()
    {
        if (this.allFields==null)
        {
            Vector fields = new Vector();
            // erstmal die Fields der superInterfaces sammeln
            Interface[] superInterfaces = this.getSuperInterfaces();
            for (int i=0; i<superInterfaces.length; i++)
            {
                TypeSystemNode[] nextFields = superInterfaces[i].getAllFields();
                for (int n=0; n<nextFields.length; n++) {
                    fields.addElement(nextFields[n]);
                }
            }
            // dann unsere eigenen Fields (also die Attributes des Interfaces)
            ModelParticipant[] contents = this.contents();

            for (int i=0; i<contents.length; i++)
            {
                if (contents[i] instanceof IRAttribute) {
                    fields.addElement(contents[i]);
                }
            }

            this.allFields = new IRAttribute[fields.size()];
            int i = 0;
            for (Enumeration e = fields.elements(); e.hasMoreElements(); )
            {
                allFields[i] = (IRAttribute)e.nextElement();
                i++;
            }
    }	// if (allFields==null)
    return allFields;
    }

    /**
     * Gibt alle Fields inkl. der Fields der Super-Interfaces zurück
     * @return org.jacorb.ir.gui.typesystem.TypeSystemNode[]
     */

    public TypeSystemNode[] getAllOperations()
    {
    if (this.allOperations==null)
        {
            Vector operations = new Vector();
            // erstmal die Operationen der superInterfaces sammeln
            Interface[] superInterfaces = this.getSuperInterfaces();
            for (int i=0; i<superInterfaces.length; i++)
            {
                TypeSystemNode[] nextOperations = superInterfaces[i].getAllOperations();
                for (int n=0; n<nextOperations.length; n++)
                {
                    operations.addElement(nextOperations[n]);
                }
            }

            // dann unsere eigenen Operationen

            ModelParticipant[] contents = this.contents();
            for (int i=0; i<contents.length; i++)
            {
                if (contents[i] instanceof IROperation)
                {
                    operations.addElement(contents[i]);
                }
            }

            this.allOperations = new IROperation[operations.size()];
            int i = 0;
            for (Enumeration e = operations.elements(); e.hasMoreElements(); )
            {
                allOperations[i] = (IROperation)e.nextElement();
                i++;
            }
    }	// if (allOperations==null)
    return allOperations;
    }

    /**
     * This method was created by a SmartGuide.
     * @return org.jacorb.ir.gui.typesystem.Interface[]
     */

    public Interface[] getSuperInterfaces()
    {
    if (superInterfaces==null)
        {
            // superInterfaces in unserem dazugehörigen Field speichern
            InterfaceDef interfaceDef = InterfaceDefHelper.narrow((org.omg.CORBA.Object)irObject);
            InterfaceDef[] baseInterfaces = interfaceDef.base_interfaces(); // base interfaces aus IR holen
            this.superInterfaces = new IRInterface[baseInterfaces.length];
            for (int i=0; i<baseInterfaces.length; i++)
            {
                // für alle base interfaces die zugehörige TypeSystemNode holen
                IRInterface superInterface = (IRInterface)RemoteTypeSystem.createTypeSystemNode(baseInterfaces[i]);
                this.superInterfaces[i] = superInterface;
            }
    }
    return superInterfaces;
    }

    /**
     * @return java.lang.String
     */

    public static String nodeTypeName()
    {
    return "interface";
    }

    /**
     * @param irObject org.omg.CORBA.IRObject
     */

    protected void setIRObject(org.omg.CORBA.IRObject irObject)
    {
    super.setIRObject(irObject);
    }
}











