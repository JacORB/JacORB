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

// TODO Initializers:
// Currently initializers are completely ignored.
// You cannot see them, even if defined in the IR.

/**
 *
 */

import java.util.Enumeration;
import java.util.Vector;
import org.jacorb.ir.gui.typesystem.Interface;
import org.jacorb.ir.gui.typesystem.ModelParticipant;
import org.jacorb.ir.gui.typesystem.TypeSystemNode;
import org.jacorb.ir.gui.typesystem.Value;
import org.omg.CORBA.IRObject;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.ValueDef;
import org.omg.CORBA.ValueDefHelper;

public class IRValue
    extends IRContainer
    implements org.jacorb.ir.gui.typesystem.Value
{
    private Value baseValue = null;
    private boolean lookedUpBaseValue = false;
    private Value[] abstractBaseValues = null;
    private IRInterface[] interfaces = null;
    private IRAttribute[] allFields = null;
    private IROperation[] allOperations = null;
    private IRValueMember[] allMembers = null;

    /**
     * Default-Konstruktor: wird von TypeSystem.createNode(...) benutzt
     */
    public IRValue ( ) {
    super();
    }

    /**
     * @param irObject org.omg.CORBA.IRObject
     */
    public IRValue ( IRObject irObject) {
    super(irObject);
    }

    /**
     * @return An array of the node-type names of node-types
     *         that can be added here.
     */
    public String[] allowedToAdd() {
    String[] result = {	IRAttribute.nodeTypeName(),
                                IROperation.nodeTypeName(),
                                IRConstant.nodeTypeName(),
                                IRTypedef.nodeTypeName(),
                                IRException.nodeTypeName(),
                                IRValueMember.nodeTypeName()
                          };
    return result;
    }

    /**
     * @return A textual description of this value type.
     */
    public String description()
    {
    String result = super.description();

        Value base = getBaseValue();
        if (base != null)
            result = result + "\nBase-Value:\t "
                            + ((IRValue)base).getAbsoluteName();
    else
            result = result + "\nBase-Value:\t:none";

    Interface[] implemented = getInterfaces();
    if (implemented.length > 0) {
            result = result + "\nImplemented-Interfaces:\t ";
            for (int i = 0; i<implemented.length; i++) {
                result = result + ((TypeSystemNode)implemented[i]).getAbsoluteName();
                if (i != implemented.length-1)
                    result = result + ", ";
            }
    } else
            result = result + "\nImplemented-Interfaces:\t:none";

        Value[] abstractBases = getAbstractBaseValues();
        if (abstractBases.length > 0) {
            result = result + "\nAbstract-Base-Values:\t ";
            for (int i = 0; i < abstractBases.length; i++) {
                result = result + ((TypeSystemNode)abstractBases[i]).getAbsoluteName();
                if (i != abstractBases.length-1)
                    result = result + ", ";
            }
    } else
            result = result + "\nAbstract-Base-Values:\t:none";

    return result;
    }

    /**
     * Returns all fields defined here, including fields from
     * the base value and interfaces.
     */
    public TypeSystemNode[] getAllFields()
    {
        if (allFields==null) {
            Vector fields = new Vector();

            // erstmal die Fields der interfaces sammeln
            Interface[] interfaces = getInterfaces();
            for (int i=0; i<interfaces.length; i++) {
                TypeSystemNode[] nextFields = interfaces[i].getAllFields();
                for (int n=0; n<nextFields.length; n++)
                    fields.addElement(nextFields[n]);
            }

            Value[] abstractBases = getAbstractBaseValues();
            for (int i = 0; i < abstractBases.length; i++) {
                TypeSystemNode[] nextFields = abstractBases[i].getAllFields();
                for (int n=0; n<nextFields.length; n++)
                    if (nextFields[n] instanceof IRAttribute)
                        fields.addElement(nextFields[n]);
            }

            // dann unsere eigenen Fields (also die Attributes des Interfaces)
            ModelParticipant[] contained = contents();
            for (int i=0; i<contained.length; i++)
                if (contained[i] instanceof IRAttribute)
                    fields.addElement(contained[i]);

            // convert into an array
            allFields = new IRAttribute[fields.size()];
            int i = 0;
            for (Enumeration e = fields.elements(); e.hasMoreElements(); ++i)
                allFields[i] = (IRAttribute)e.nextElement();
    }
    return allFields;
    }

    /**
     * Returns all operations defined here, including operations from
     * the base value and interfaces, but excluding initializers.
     */
    public TypeSystemNode[] getAllOperations()
    {
    if (this.allOperations==null)
        {
            Vector operations = new Vector();
            // erstmal die Operationen der interfaces sammeln
            Interface[] interfaces = this.getInterfaces();
            for (int i=0; i<interfaces.length; i++)
            {
                TypeSystemNode[] nextOperations = interfaces[i].getAllOperations();
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
     *  Return the concrete base value of this value, or null
     *  if this base value has no base value.
     */
    public Value getBaseValue()
    {
    if (!lookedUpBaseValue) {
            ValueDef valueDef = ValueDefHelper.narrow((org.omg.CORBA.Object)irObject);
            ValueDef base = valueDef.base_value();
            if (base != null)
                baseValue = (Value)RemoteTypeSystem.createTypeSystemNode(base);
            lookedUpBaseValue = true;
    }
    return baseValue;
    }

    /**
     *  Return the abstract base values of this value.
     */
    public Value[] getAbstractBaseValues()
    {
    if (abstractBaseValues == null) {
            ValueDef valueDef = ValueDefHelper.narrow((org.omg.CORBA.Object)irObject);
            ValueDef[] abstractBases = valueDef.abstract_base_values();
            abstractBaseValues = new Value[abstractBases.length];
            for (int i = 0; i < abstractBases.length; ++i)
                abstractBaseValues[i] = (Value)RemoteTypeSystem.createTypeSystemNode(abstractBases[i]);
    }
    return abstractBaseValues;
    }

    /**
     * Returns all value members defined here, including value members from
     * the base value.
     */
    public TypeSystemNode[] getAllMembers()
    {
    if (allMembers == null) {
            Vector members = new Vector();

            // first collect value members of our base value
            Value base = getBaseValue();
            if (base != null) {
                TypeSystemNode[] baseMembers = base.getAllMembers();

                for (int i = 0; i < baseMembers.length; i++)
                    members.addElement(baseMembers[i]);
            }

            // add members from abstract_base_values
            Value[] abstractBases = getAbstractBaseValues();
            for (int i = 0; i < abstractBases.length; i++) {
                TypeSystemNode[] nextMembers = abstractBases[i].getAllMembers();
                for (int n=0; n<nextMembers.length; n++)
                    members.addElement(nextMembers[n]);
            }


            // then our own value members
            ModelParticipant[] contents = this.contents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] instanceof IRValueMember)
                    members.addElement(contents[i]);
            }

            allMembers = new IRValueMember[members.size()];
            int i = 0;
            for (Enumeration e = members.elements(); e.hasMoreElements(); ) {
                allMembers[i] = (IRValueMember)e.nextElement();
                i++;
            }
    }
    return allMembers;
    }


    /**
     * Get the interfaces implemented by this value type.
     * This will create the <code>interfaces</code> array, fill it in with
     * the <code>InterfaceDef</code> of the interfaces implemented by the
     * value type, and return the array.
     *
     * @return A reference to the <code>interfaces</code> field.
     */
    public Interface[] getInterfaces()
    {
    if (interfaces==null) {
            // interfaces in unserem dazugehörigen Field speichern
            ValueDef valueDef = ValueDefHelper.narrow((org.omg.CORBA.Object)irObject);
            InterfaceDef[] supportedInterfaces = valueDef.supported_interfaces();
            interfaces = new IRInterface[supportedInterfaces.length];
            for (int i=0; i<supportedInterfaces.length; i++) {
                // für alle base interfaces die zugehörige TypeSystemNode holen
                IRInterface intf = (IRInterface)RemoteTypeSystem.createTypeSystemNode(supportedInterfaces[i]);
                interfaces[i] = intf;
            }
    }
    return interfaces;
    }

    /**
     * @return A string denoting the node type implemented here.
     */
    public static String nodeTypeName()
    {
    return "value";
    }

    /**
     * Set the CORBA reference of the IR object we represent.
     *
     * @param irObject The CORBA reference to be set.
     */
    protected void setIRObject(org.omg.CORBA.IRObject irObject)
    {
    super.setIRObject(irObject);
    }
}

