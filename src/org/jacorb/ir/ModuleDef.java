package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import java.util.*;

import org.omg.CORBA.INTF_REPOS;
import org.omg.PortableServer.POA;
import org.apache.avalon.framework.logger.Logger;

public class ModuleDef
    extends Contained
    implements org.omg.CORBA.ModuleDefOperations, ContainerType
{
    private static char 	fileSeparator =
        System.getProperty("file.separator").charAt(0);

    private Container           delegate;
    private String 		path = null;
    private Logger logger;

    public ModuleDef( String path,
                      String full_name,
                      org.omg.CORBA.Container def_in,
                      org.omg.CORBA.Repository ir,
                      ClassLoader loader,
                      POA poa,
                      Logger logger )
        throws INTF_REPOS
    {
        this.logger = logger;
        this.path = path;
        this.full_name = full_name.replace(fileSeparator,'/');

        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("New ModuleDef " + full_name + " path: " + path);
        }

        def_kind = org.omg.CORBA.DefinitionKind.dk_Module;
        if (ir == null)
        {
           throw new INTF_REPOS ("No repository!");
        }

        containing_repository = ir;
        defined_in = def_in;

        try
        {
            id( RepositoryID.toRepositoryID( full_name, false, loader ));
            if( full_name.indexOf( fileSeparator ) > 0 )
            {
                name( full_name.substring( full_name.lastIndexOf( fileSeparator ) + 1 ));

                if( defined_in instanceof org.omg.CORBA.Contained)
                {
                    absolute_name =
                        ((org.omg.CORBA.Contained)defined_in).absolute_name() +
                        "::" + name();

                    if (this.logger.isDebugEnabled())
                    {
                        this.logger.debug("New ModuleDef 1a) name " +
                                          name() + " absolute: " + 
                                          absolute_name);
                    }
                }
                else
                {
                    absolute_name = "::" + name();

                    if (this.logger.isDebugEnabled())
                    {
                        this.logger.debug("New ModuleDef 1b) name " +
                                          name() + " absolute: " +
                                          absolute_name + " defined_in : " +
                                          defined_in.getClass().getName());
                    }
                }
            }
            else
            {
                defined_in = containing_repository;
                name( full_name );
                absolute_name = "::" + name();

                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("New ModuleDef 2) name " +
                                      name() +
                                      " absolute:" + absolute_name);
                }
            }
            delegate = new Container( this, path, full_name, loader, poa, this.logger );

        }
        catch ( Exception e )
        {
            this.logger.error("Caught Exception", e);
            throw new INTF_REPOS( ErrorMsg.IR_Not_Implemented,
                                  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }
    }


    public String path()
    {
        return path;
    }


    public void loadContents()
    {
        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("Module " + name() +  " loading...");
        }

        delegate.loadContents();

        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("Module " + name() +  " loaded");
        }
    }

    void define()
    {
        delegate.define();
    }

    public  org.omg.CORBA.Contained lookup(/*ScopedName*/ String name)
    {
        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("Module " + this.name + " lookup " + name);
        }

        return delegate.lookup(name);
    }

    public org.omg.CORBA.Contained[] contents(
        org.omg.CORBA.DefinitionKind limit_type,
        boolean exclude_inherited)
    {
        return delegate.contents(limit_type, exclude_inherited);
    }

    public org.omg.CORBA.Contained[] lookup_name(String search_name,
                                                 int levels_to_search,
                                                 org.omg.CORBA.DefinitionKind limit_type,
                                                 boolean exclude_inherited)
    {
        return delegate.lookup_name( search_name, levels_to_search,
                                     limit_type, exclude_inherited );
    }

    public org.omg.CORBA.ContainerPackage.Description[] describe_contents(
                                             org.omg.CORBA.DefinitionKind limit_type,
                                             boolean exclude_inherited,
                                             int max_returned_objs)
    {
        return delegate.describe_contents( limit_type,
                                           exclude_inherited,
                                           max_returned_objs );
    }

    public org.omg.CORBA.ModuleDef create_module(String id, String name, String version)
    {
        return delegate.create_module( id,  name,  version);
    }

    public org.omg.CORBA.ConstantDef create_constant( String id,
                                                      String name,
                                                      String version,
                                                      org.omg.CORBA.IDLType type,
                                                      org.omg.CORBA.Any value)
    {
        return delegate.create_constant(  id,  name, version, type, value );
    }

    public org.omg.CORBA.StructDef create_struct( String id,
                                                  String name,
                                                  String version,
                                                  org.omg.CORBA.StructMember[] members)
    {
        return delegate.create_struct( id, name, version, members);
    }

    public org.omg.CORBA.UnionDef create_union( String id,
                                                String name,
                                                String version,
                                                org.omg.CORBA.IDLType discriminator_type,
                                                org.omg.CORBA.UnionMember[] members)
    {
        return delegate.create_union( id,  name, version, discriminator_type, members);
    }

    public org.omg.CORBA.EnumDef create_enum( String id,
                                              String name,
                                              String version,
                                              String[] members)
    {
        return delegate.create_enum(  id,  name, version, members);
    }

    public org.omg.CORBA.AliasDef create_alias( String id,
                                                String name,
                                                String version,
                                                org.omg.CORBA.IDLType original_type)
    {
        return delegate.create_alias(  id,  name, version, original_type);
    }

    public org.omg.CORBA.ExceptionDef create_exception( String id,
                                                        String name,
                                                        String version,
                                                        org.omg.CORBA.StructMember[] member )
    {
        return delegate.create_exception(id, name, version, member);
    }

    /**
     * not supported
     */

    public org.omg.CORBA.InterfaceDef create_interface( String id,
                                                        String name,
                                                        String version,
                                                        org.omg.CORBA.InterfaceDef[] base_interfaces,
                                                        boolean is_abstract )
    {
        return delegate.create_interface( id,  name,  version,
                    base_interfaces, is_abstract );
    }

    /**
     * not supported
     */

    public org.omg.CORBA.ValueBoxDef create_value_box(String id, String name, String version,
                                                      org.omg.CORBA.IDLType type)
    {
        return delegate.create_value_box(id, name, version, type);
    }

    /**
     * not supported
     */

    public  org.omg.CORBA.ValueDef create_value(
                                     String id,
                                     String name,
                                     String version,
                                     boolean is_custom,
                                     boolean is_abstract,
                                     org.omg.CORBA.ValueDef base_value,
                                     boolean is_truncatable,
                                     org.omg.CORBA.ValueDef[] abstract_base_values,
                                     org.omg.CORBA.InterfaceDef[] supported_interfaces,
                                     org.omg.CORBA.Initializer[] initializers)
    {
        return delegate.create_value( id,
                                      name,
                                      version,
                                      is_custom,
                                      is_abstract,
                                      base_value,
                                      is_truncatable,
                                      abstract_base_values,
                                      supported_interfaces,
                                      initializers);
    }


    /**
     * not supported
     */

    public org.omg.CORBA.NativeDef create_native(java.lang.String id,
                                                 java.lang.String name,
                                                 java.lang.String version)
    {
        return delegate.create_native( id,  name,  version);
    }


    // from Contained

    public org.omg.CORBA.ContainedPackage.Description describe()
    {
        org.omg.CORBA.Any a = orb.create_any();
        String defined_in_id = null;

        if( defined_in instanceof org.omg.CORBA.Contained )
            defined_in_id = ((org.omg.CORBA.Contained)defined_in).id();
        else
            defined_in_id = "IR";

        org.omg.CORBA.ModuleDescription m =
            new org.omg.CORBA.ModuleDescription(
                name, id, defined_in_id, version);

        org.omg.CORBA.ModuleDescriptionHelper.insert( a, m );
        return new org.omg.CORBA.ContainedPackage.Description(
            org.omg.CORBA.DefinitionKind.dk_Module, a);
    }

    public void destroy()
    {
        delegate.destroy();
    }


}
