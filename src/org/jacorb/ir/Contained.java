package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import org.omg.CORBA.*;
import org.omg.PortableServer.POA;

import java.lang.reflect.*;

import org.apache.avalon.framework.logger.Logger;

/**
 * @version $Id$
 */

public abstract class Contained
    extends IRObject
    implements org.omg.CORBA.ContainedOperations
{
    protected String id;

    /* IDL name from the root of the class path to the leaf */
    protected String absolute_name;
    /* from the root of the class path to the leaf */
    String full_name;
    protected String version = "1.0";

    protected org.omg.CORBA.Container defined_in;
    protected org.omg.CORBA.Repository containing_repository;

    private static Class intfClass;
    private static Class idlClass;
    private static Class stubClass;
    private static Class exceptClass;
    private static boolean class_init;

    public Contained()
    {
    }

    public Contained( String _id,
                      String _name,
                      String _version,
                      org.omg.CORBA.Container _defined_in,
                      String _absolute_name,
                      org.omg.CORBA.Repository _containing_repository )
    {
        id = _id;
        name = _name;
        version = _version;
        defined_in = _defined_in;
        absolute_name = _absolute_name;
        containing_repository = _containing_repository;
    }


    public static Contained createContained( Class c,
                                             String path,
                                             org.omg.CORBA.Container _defined_in,
                                             org.omg.CORBA.Repository ir,
                                             Logger logger,
                                             ClassLoader loader,
                                             POA poa )
    {
        if( !class_init )
        {
            try
            {
                intfClass =
                    loader.loadClass("org.omg.CORBA.Object");
                idlClass =
                    loader.loadClass("org.omg.CORBA.portable.IDLEntity");
                stubClass =
                    loader.loadClass("org.omg.CORBA.portable.ObjectImpl");
                exceptClass =
                    loader.loadClass("org.omg.CORBA.UserException");
                class_init = true;
            }
            catch ( ClassNotFoundException cnf )
            {
                // debug:  cnf.printStackTrace();
            }
        }

        if( stubClass.isAssignableFrom( c ) )
        {
            return null; // don't care for stubs
        }
        else if( c.isInterface())
        {
            if( intfClass.isAssignableFrom( c ) )
            {
                try
                {
                    Class helperClass =
                        loader.loadClass(c.getName() + "Helper");

                    if( helperClass == null )
                    {
                        return null;
                    }

                    org.jacorb.ir.InterfaceDef idef =
                        new org.jacorb.ir.InterfaceDef( c,
                                                        helperClass,
                                                        path,
                                                        _defined_in,
                                                        ir,
                                                        loader,
                                                        poa,
                                                        logger);

                    return idef;
                }
                catch ( ClassNotFoundException e )
                {
                    // debug: e.printStackTrace();
                    return null;
                }
            }
            else
            {
                try
                {
                    Field f = c.getDeclaredField("value");
                    return new org.jacorb.ir.ConstantDef( c,
                                                          _defined_in,
                                                          ir,
                                                          logger,
                                                          poa);
                }
                catch( NoSuchFieldException nsfe )
                {
                    //                    org.jacorb.util.Debug.output(2, nsfe );
                    return null;
                }
            }
        }
        else if( exceptClass.isAssignableFrom( c ))
        {
            /*
            try
            {
            */
                return new org.jacorb.ir.ExceptionDef(c,
                                                      _defined_in,
                                                      ir,
                                                      loader,
                                                      poa,
                                                      logger);
                /*
            }
            catch ( Exception e )
            {
                // debug:
                e.printStackTrace();
                return null;
            }
                */
        }
        else if( idlClass.isAssignableFrom( c ) )
        {
            try
            {
                Class helperClass =
                    loader.loadClass( c.getName()+"Helper");

                org.omg.CORBA.TypeCode tc =
                    (org.omg.CORBA.TypeCode)helperClass.getDeclaredMethod("type", (Class[]) null).invoke(null, (java.lang.Object[]) null);
                switch( tc.kind().value())
                {
                case org.omg.CORBA.TCKind._tk_struct:
                    return new org.jacorb.ir.StructDef( c,
                                                        path,
                                                        _defined_in,
                                                        ir,
                                                        logger,
                                                        loader,
                                                        poa );
                case org.omg.CORBA.TCKind._tk_enum:
                    return new org.jacorb.ir.EnumDef(
                                                     c,
                                                     _defined_in,
                                                     ir,
                                                     loader );
                case org.omg.CORBA.TCKind._tk_union:
                    return new org.jacorb.ir.UnionDef( c,
                                                       path,
                                                       _defined_in,
                                                       ir,
                                                       loader,
                                                       logger,
                                                       poa);
                default:
                    return null;
                }
            }
            catch( ClassNotFoundException  e )
            {
                // may happen for pseudo IDL
                logger.warn("Caught Exception", e);
            }
            catch( Exception  e )
            {
                logger.error("Caught Exception", e);
            }
            return null;
        }
        else if( c.getName().endsWith("Helper"))
        {
            try
            {
                org.omg.CORBA.TypeCode tc =
                    (org.omg.CORBA.TypeCode)c.getDeclaredMethod("type", (Class[]) null).invoke(null, (java.lang.Object[]) null);
                if( tc.kind() == org.omg.CORBA.TCKind.tk_alias )
                {
                    return new AliasDef(tc,
                                        _defined_in,
                                        ir,
                                        logger,
                                        poa);
                }
            }
            catch( Exception  e )
            {
            }
            return null;
        }
        else
        {
            return null;
        }
    }

    public static org.omg.CORBA.Contained createContainedReference(
        Contained containedObject,
        Logger logger,
        POA poa )
    {
        if ( containedObject == null)
        {
            throw new INTF_REPOS ("Precondition violated in Contained createContainedReference");
        }

        org.omg.PortableServer.Servant servant = null;

        switch ( containedObject.def_kind().value() )
        {
        case org.omg.CORBA.DefinitionKind._dk_Interface:
            servant =
                new org.omg.CORBA.InterfaceDefPOATie( (org.omg.CORBA.InterfaceDefOperations)containedObject );
            break;
        case org.omg.CORBA.DefinitionKind._dk_Exception:
            servant =
                new org.omg.CORBA.ExceptionDefPOATie( (org.omg.CORBA.ExceptionDefOperations)containedObject );
            break;
        case org.omg.CORBA.DefinitionKind._dk_Struct:
            servant =
                new org.omg.CORBA.StructDefPOATie( (org.omg.CORBA.StructDefOperations)containedObject );
            break;
        case org.omg.CORBA.DefinitionKind._dk_Enum:
            servant =
                new org.omg.CORBA.EnumDefPOATie( (org.omg.CORBA.EnumDefOperations)containedObject );
            break;
        case org.omg.CORBA.DefinitionKind._dk_Union:
            servant =
                new org.omg.CORBA.UnionDefPOATie( (org.omg.CORBA.UnionDefOperations)containedObject );
            break;
        case org.omg.CORBA.DefinitionKind._dk_Module:
            servant =
                new org.omg.CORBA.ModuleDefPOATie( (org.omg.CORBA.ModuleDefOperations)containedObject );
            break;
        case org.omg.CORBA.DefinitionKind._dk_Alias:
            servant =
                new org.omg.CORBA.AliasDefPOATie( (org.omg.CORBA.AliasDefOperations)containedObject );
            break;
        case org.omg.CORBA.DefinitionKind._dk_Constant:
            servant =
                new org.omg.CORBA.ConstantDefPOATie( (org.omg.CORBA.ConstantDefOperations)containedObject );
            break;
        default:
            logger.warn("WARNING, createContainedReference returns null for dk " +
                        containedObject.def_kind().value() );
            return null;
        }

        try
        {
            org.omg.CORBA.Contained containedRef =
                org.omg.CORBA.ContainedHelper.narrow(poa.servant_to_reference( servant ));

            containedObject.setReference( containedRef );
            return containedRef;
        }
        catch( Exception e )
        {
            logger.error("unexpected exception", e);
            return null;
        }
    }


    public java.lang.String id()
    {
        return id;
    }
    public void id(java.lang.String a)
    {
        id = a;
    }

    public java.lang.String name()
    {
        return name;
    }

    public void name(java.lang.String a)
    {
        name = a;
    }

    public java.lang.String version()
    {
        return version;
    }

    public void version(java.lang.String a)
    {
        version = a;
    }

    public org.omg.CORBA.Container defined_in()
    {
        return defined_in;
    }

    public java.lang.String absolute_name()
    {
        return absolute_name;
    }

    public org.omg.CORBA.Repository containing_repository()
    {
        return containing_repository;
    }

    public abstract org.omg.CORBA.ContainedPackage.Description describe();


    public void move(org.omg.CORBA.Container new_container,
                     java.lang.String new_name,
                     java.lang.String new_version)
    {
        if( defined_in != null )
            ; // remove this object

        defined_in = new_container;
        version = new_version;
        name = new_name;
    }
}
