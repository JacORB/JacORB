package org.jacorb.ir.gui.typesystem.remote;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
 */

import java.util.Hashtable;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.jacorb.ir.gui.typesystem.AbstractContainer;
import org.jacorb.ir.gui.typesystem.ModelBuilder;
import org.jacorb.ir.gui.typesystem.NodeMapper;
import org.jacorb.ir.gui.typesystem.TypeAssociator;
import org.jacorb.ir.gui.typesystem.TypeSystem;
import org.jacorb.ir.gui.typesystem.TypeSystemNode;
import org.omg.CORBA.Contained;
import org.omg.CORBA.ContainedHelper;
import org.omg.CORBA.DefinitionKind;
import org.omg.CORBA.IRObject;
import org.omg.CORBA.IRObjectHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ParameterDescription;
import org.omg.CORBA.Repository;
import org.omg.CORBA.RepositoryHelper;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.UnionMember;
import org.omg.CORBA.ORBPackage.InvalidName;

/**
 * @author Joerg v. Frantzius
 * @author Gerald Brose.
 * $Id$
 */

public class RemoteTypeSystem
    extends TypeSystem
{
    private final Repository rep;
    private final ORB orb = ORB.init( new String[0], null);
    private static final Hashtable knownIRObjects = new Hashtable();

    private static String test = "";

    public RemoteTypeSystem () throws InvalidName
    {
        rep  =
            RepositoryHelper.narrow(orb.resolve_initial_references("InterfaceRepository"));

    }

    public RemoteTypeSystem (String ior)
    {
        rep = RepositoryHelper.narrow(orb.string_to_object(ior));
    }

    /**
     *  Creates a TreeModel that contains only root enthält. To expand
     *  nodes,     the    TreeExpansionListener    returned    from
     *  getTreeExpansionListener(treeModel)  needs  to be  registered
     *  with JTree.
     * @return javax.swing.tree.DefaultTreeModel
     */

    public DefaultTreeModel createTreeModelRoot()
    {
        if (treeModel!=null)
        {
            return treeModel;
        }

        IRRepository startNode  = new IRRepository(rep);
        treeModel =
            ModelBuilder.getSingleton().createTreeModelRoot(startNode);
        return treeModel;
    }

    /**
     * @return org.jacorb.ir.gui.typesystem.TypeSystemNode
     * @param obj org.omg.CORBA.IRObject
     */

    public static TypeSystemNode createTypeSystemNode(java.lang.Object obj)
    {
        if (obj == null)
        {
            //Debug.output (3, "A reference from the Repository is null... (but it should not)");
            return null;
        }
        IRObject irObject = null;
        TypeSystemNode result = null;

        System.out.flush();

        // Typ-Unterscheidung für obj vornehmen und korrespondierendes
        // org.jacorb.ir.gui.typesystem-Objekt erzeugen.
        // knownIRObjects: zu jedem Objekt des IR wird das
        // korrespondierende org.jacorb.ir.gui.typesystem-Objekt
        // festgehalten, damit letzteres nicht mehrfach für
        /// das selbe IR-Objekt erzeugt wird
        // (die Abbildung von IR-Objekten auf
        // org.jacorb.ir.gui.typesystem-Objekte wird sozusagen injektiv gehalten)

        // Je nach Typ wird ein anderer Hashcode verwendet,
        // um das obj in knownIRObjects abzulegen:
        // die von Object geerbte hashcode() Methode reicht
        // hier nicht, weil sie equals() verwendet und
        // diese Methode nicht für alle möglichen Typen von
        ///  obj korrekt redefiniert wurde (testet nur auf
        // Objekt-Identität)

        if ( obj instanceof IRObject )
        {
            try
            {
                irObject = IRObjectHelper.narrow((org.omg.CORBA.Object)obj);
            }
            catch( org.omg.CORBA.BAD_PARAM bp )
            {
            }
        }
        if( irObject != null )
        {
            // insbesondere "echte" IRObjects können beim Aufbau
            // des Trees mehrmals referenziert
            // und dieser Methode als Argument übergeben werden
            // if (knownIRObjects.get(ORB.init().object_to_string((org.omg.CORBA.Object)irObject))!=null) {
            //			return (TypeSystemNode)knownIRObjects.get(ORB.init().object_to_string((org.omg.CORBA.Object)irObject));
            //		}

            result = (TypeSystemNode)knownIRObjects.get(irObject);

            if( result != null )
            {
//                 Debug.output(2, result.getInstanceNodeTypeName()+" "+
//                              result.getAbsoluteName()+" (cached)");
                return result;
            }

            // try again using Repository-ID
            try
            {
                Contained contained =
                    ContainedHelper.narrow((org.omg.CORBA.Object)irObject);

                result = (TypeSystemNode)knownIRObjects.get(contained.id());
                if (result != null)
                {
//                     Debug.output(2,
//                                  result.getInstanceNodeTypeName()+" "+
//                                  result.getAbsoluteName()+" (cached by id)");
                    return result;
                }
            }
            catch( org.omg.CORBA.BAD_PARAM bp )
            {}

            try
            {
                switch(irObject.def_kind().value())
                {
                    // create IRObjects
                    case DefinitionKind._dk_Module:
                        result = new IRModule(irObject);
                        break;
                    case DefinitionKind._dk_Interface:
                        result = new IRInterface(irObject);
                        break;
                    case DefinitionKind._dk_Constant:
                        result = new IRConstant(irObject);
                        break;
                    case DefinitionKind._dk_Attribute:
                        result = new IRAttribute(irObject);
                        break;
                    case DefinitionKind._dk_Operation:
                        result = new IROperation(irObject);
                        break;
                        /*   Typedef   ist   eine  abstrakte   Oberklasse,
                             theoretisch   dürfte   es   kein  Objekt   mit
                             DefinitionKind._dk_Typedef      geben     case
                             DefinitionKind._dk_Typedef:   result   =   new
                             IRTypedef(irObject); break; */
                    case DefinitionKind._dk_Exception:
                        result = new IRException(irObject);
                        break;
                    case DefinitionKind._dk_Struct:
                        result = new IRStruct(irObject);
                        break;
                    case DefinitionKind._dk_Union:
                        result = new IRUnion(irObject);
                        break;
                    case DefinitionKind._dk_Primitive:
                        result = new IRPrimitive(irObject);
                        break;
                    case DefinitionKind._dk_Fixed:
                        result = new IRFixed(irObject);
                        break;
                    case DefinitionKind._dk_String:
                        result = new IRString(irObject);
                        break;
                    case DefinitionKind._dk_Wstring:
                        result = new IRWstring(irObject);
                        break;
                    case DefinitionKind._dk_Alias:
                        result = new IRAlias(irObject);
                        break;
                    case DefinitionKind._dk_Sequence:
                        result = new IRSequence(irObject);
                        break;
                    case DefinitionKind._dk_Enum:
                        result = new IREnum(irObject);
                        break;
                    case DefinitionKind._dk_Array:
                        result = new IRArray(irObject);
                        break;
                    case DefinitionKind._dk_ValueBox:
                        result = new IRValueBox(irObject);
                        break;
                    case DefinitionKind._dk_Value:
                        result = new IRValue(irObject);
                        break;
                    case DefinitionKind._dk_ValueMember:
                        result = new IRValueMember(irObject);
                        break;
                    default:
                        System.out.println("Unknown/senseless DefinitionKind returned from Repository: "+irObject.def_kind().value());
                        break;
                } // switch
            }
            catch( Exception exc )
            {
                //Debug.output( 3, exc );
            }

            if ( result instanceof IRInterface &&
                 ((IRInterface)result).getName().equals("Container"))
            {
                if (test.equals(((IRInterface)result).getAbsoluteName()))
                {
                    System.out.println("bug!");
                }
                test = ((IRInterface)result).getAbsoluteName();
            }

            if (result != null)
            {
                // knownIRObjects.put(ORB.init().object_to_string((org.omg.CORBA.Object)irObject),result);
                knownIRObjects.put(irObject,result);

                if (knownIRObjects.get(irObject) == null)
                {
                    System.out.println( "wasislos?");
                }

                if (result instanceof IRNode &&
                    (!((IRNode)result).repositoryID.equals("")))
                {
                    knownIRObjects.put(((IRNode)result).repositoryID,result);
                }
            }
        }	// if (irObjectHelper.narrow...)
        else
        {
            // kein IRObject sondern lokales Objekt
            // members von Structs, Unions und Enums können nicht
            // von anderen IRObjects referenziert werden,
            // wir wollen trotzdem für mögliche mehrfache Aufrufe
            // das selbe org.jacorb.ir.gui.typesystem-Objekt zurückgeben
            if (knownIRObjects.get(obj)!=null) {
                return (TypeSystemNode)knownIRObjects.get(obj);
            }

            if (obj instanceof StructMember)
            {
                // als Hash-Key nehmen wir einen IR-weit eindeutigen String
                StructMember structMember = (StructMember)obj;
                if (knownIRObjects.get("structmember" + structMember.name +
                                       structMember.type.kind().toString())!=null)
                {
                    return (TypeSystemNode)knownIRObjects.get("structmember" +
                                                              structMember.name +
                                                              structMember.type.kind().toString());
                }
                result = new IRStructMember((StructMember)obj);
                knownIRObjects.put("structmember" + structMember.name +
                                   structMember.type.kind().toString(),result);
            }
            else if (obj instanceof UnionMember)
            {
                UnionMember unionMember = (UnionMember)obj;
                if (knownIRObjects.get("unionmember" +
                                       unionMember.name +
                                       unionMember.type.kind().toString())!=null)
                {
                    return (TypeSystemNode)knownIRObjects.get("unionmember" +
                                                              unionMember.name +
                                                              unionMember.type.kind().toString());
                }
                result = new IRUnionMember((UnionMember)obj);
                knownIRObjects.put("unionmember" + unionMember.name +
                                   unionMember.type.kind().toString(),result);
            }
            else
                if (obj instanceof ParameterDescription)
                {
                    ParameterDescription parDesc = (ParameterDescription)obj;
                    if (knownIRObjects.get("parameter" + parDesc.name +
                                           parDesc.type.kind().toString())!=null)
                    {
                        return (TypeSystemNode)knownIRObjects.get("parameter" +
                                                                  parDesc.name +
                                                                  parDesc.type.kind().toString());
                    }
                    result = new IRParameter(parDesc);
                    knownIRObjects.put("parameter" + parDesc.name +
                                       parDesc.type.kind().toString(),result);
                }
                else if (obj instanceof String)
                {
                    if (knownIRObjects.get(obj)!=null)
                    {
                        return (IREnumMember)knownIRObjects.get(obj);
                    }
                    result = new IREnumMember((String)obj);
                    knownIRObjects.put(obj,result);
                }
        }	// else (obj war kein IRObject)

        if( result != null )
        {
//             Debug.output( 2, result.getInstanceNodeTypeName()+" "+
//                           result.getAbsoluteName());
        }
//         else
//             Debug.output( 2, "result is null ");
        return result;
    }

    /**
     * @return TableModel
     * @param treeNode org.jacorb.ir.gui.typesystem.TypeSystemNode
     */

    public DefaultTableModel getTableModel(DefaultMutableTreeNode treeNode)
    {
        DefaultTableModel tableModel = new DefaultTableModel();
        java.lang.Object[] colIdentifiers = {"Item","Type","Name"};

        tableModel.setColumnIdentifiers(colIdentifiers);

        if (treeNode!=null)
        {
            if (treeNode.getUserObject() instanceof AbstractContainer)
            {
                for (int i=0; i<treeModel.getChildCount(treeNode); i++)
                {
                    TypeSystemNode childNode =
                        (TypeSystemNode)((DefaultMutableTreeNode)(treeNode.getChildAt(i))).getUserObject();
                    String type = "";
                    if (childNode instanceof TypeAssociator)
                    {
                        type = ((TypeAssociator)childNode).getAssociatedType();
                    }
                    java.lang.Object[] row = {new NodeMapper(childNode,childNode.getInstanceNodeTypeName()),
                                              new NodeMapper(childNode,type),
                                              new NodeMapper(childNode,childNode.getName())};
                    tableModel.addRow(row);
                }
            }
        }
        return tableModel;
    }

    /**
     * @return javax.swing.event.TreeExpansionListener
     * @param treeModel javax.swing.tree.DefaultTreeModel
     */
    public javax.swing.event.TreeExpansionListener getTreeExpansionListener(TreeModel treeModel) {
        return ModelBuilder.getSingleton().getTreeExpansionListener(treeModel);
    }

    /**
     * @return javax.swing.tree.TreeModel
     */

    public TreeModel getTreeModel()
    {
        if (treeModel!=null)
        {
            return treeModel;
        }

        try
        {
            IRRepository startNode 	= new IRRepository(rep);
            treeModel = ModelBuilder.getSingleton().buildTreeModelAsync(startNode);
            return treeModel;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
