package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
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

import java.util.Enumeration;
import java.util.Hashtable;
import org.omg.CORBA.AbstractInterfaceDef;
import org.omg.CORBA.ExtInitializer;
import org.omg.CORBA.ExtValueDef;
import org.omg.CORBA.INTF_REPOS;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.LocalInterfaceDef;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ValueDef;
import org.omg.PortableServer.POA;
import org.slf4j.Logger;

/**
 * @version $Id$
 */

public class ExceptionDef
    extends Contained
    implements org.omg.CORBA.ExceptionDefOperations
{
    private org.omg.CORBA.TypeCode       type;
    private Class                        myClass;
    private Class                        helperClass;
    private org.omg.CORBA.StructMember[] members;
    private Hashtable	                 contained = new Hashtable();
    private Logger logger;
    private ClassLoader loader;
    private POA poa;

    public ExceptionDef(Class c,
                        org.omg.CORBA.Container _defined_in,
                        org.omg.CORBA.Repository ir,
                        ClassLoader loader,
                        POA poa,
                        Logger logger)
    {
        this.logger = logger;
        this.loader = loader;
        this.poa = poa;
        def_kind = org.omg.CORBA.DefinitionKind.dk_Exception;
        containing_repository = ir;
        defined_in = _defined_in;
        if (defined_in == null)
        {
            throw new INTF_REPOS ("defined_in = null");
        }
        if (containing_repository == null)
        {
            throw new INTF_REPOS ("containing_repository = null");
        }

        try
        {
            String classId = c.getName();
            myClass = c;
            version( "1.0" );
            if( classId.indexOf('.') > 0 )
            {
                name( classId.substring( classId.lastIndexOf('.')+1));
                absolute_name =
                    org.omg.CORBA.ContainedHelper.narrow( defined_in ).absolute_name() + "::" + name;
            }
            else
            {
                name( classId );
                absolute_name = "::" + name;
            }

            helperClass = this.loader.loadClass(classId + "Helper") ;
            id( (String)helperClass.getDeclaredMethod("id", (Class[]) null).invoke( null, (Object[]) null ));
            type =
                TypeCodeUtil.getTypeCode(myClass, this.loader, null, classId, this.logger );
            try
            {
                members = new org.omg.CORBA.StructMember[ type.member_count() ];
                for( int i = 0; i < members.length; i++ )
                {
                    members[i] = new org.omg.CORBA.StructMember( type.member_name(i),
                                                                 type.member_type(i),
                                                                 null );
                }
            }
            catch( Exception e )
            {
                this.logger.error("Caught Exception", e);
            }

            if (this.logger.isDebugEnabled())
            {
                this.logger.debug("ExceptionDef: " + absolute_name);
            }
        }
        catch ( Exception e )
        {
            this.logger.error("Caught Exception", e);
            throw new INTF_REPOS( ErrorMsg.IR_Not_Implemented,
                                  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }
    }

    /**
     */

    void define()
    {
        for( int i = 0; i < members.length; i++ )
        {
            members[i].type_def = IDLType.create( members[i].type,
                                                  containing_repository,
                                                  this.logger,
                                                  this.poa);
        }
    }

    /**
     */

    public org.omg.CORBA.ExceptionDescription describe_exception()
    {
        return new org.omg.CORBA.ExceptionDescription(name(),
                                                      id(),
                                                      org.omg.CORBA.ContainedHelper.narrow( defined_in ).id(),
                                                      version(),
                                                      type());
    }

    public org.omg.CORBA.TypeCode type()
    {
        if (type == null)
        {
            throw new INTF_REPOS ("Exception TypeCode is null");
        }
        return type;
    }

    public org.omg.CORBA.Contained lookup(java.lang.String search_name)
    {
        return null;
    }


    public org.omg.CORBA.StructMember[] members()
    {
        return members;
    }


    // write interface not supported!

    public void members(org.omg.CORBA.StructMember[] a)
    {
    }


    public org.omg.CORBA.ModuleDef create_module( String id, String name, String version)
    {
        return null;
    }

    public org.omg.CORBA.ConstantDef create_constant(java.lang.String id, java.lang.String name, java.lang.String version, org.omg.CORBA.IDLType type, org.omg.CORBA.Any value)
    {
        return null;
    }

    public org.omg.CORBA.StructDef create_struct( String id, String name, String version, /*StructMemberSeq*/ org.omg.CORBA.StructMember[] members){
        return null;
    }

    public org.omg.CORBA.UnionDef create_union( String id, String name, String version, org.omg.CORBA.IDLType discriminator_type, /*UnionMemberSeq*/ org.omg.CORBA.UnionMember[] members){
        return null;
    }

    public org.omg.CORBA.EnumDef create_enum( String id, String name, String version,  String[] members){
        return null;
    }

    public org.omg.CORBA.AliasDef create_alias( String id, String name, String version, org.omg.CORBA.IDLType original_type){
        return null;
    }

    /**
     * not supported
     */

    public org.omg.CORBA.ExceptionDef create_exception(java.lang.String id, java.lang.String name , java.lang.String version, org.omg.CORBA.StructMember[] member )
    {
        return null;
    }

    /**
     * not supported
     */

    public org.omg.CORBA.InterfaceDef create_interface(
                    String id,
                    String name,
                    String version,
                    /*InterfaceDefSeq*/ org.omg.CORBA.InterfaceDef[] base_interfaces)
    {
        return null;
    }

    /**
     * not supported
     */

    public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String id,
                                                      java.lang.String name,
                                                      java.lang.String version,
                                                      org.omg.CORBA.IDLType type)
    {
        return null;
    }


    /**
     * not supported
     */

    public  org.omg.CORBA.ValueDef create_value(
                                     java.lang.String id,
                                     java.lang.String name,
                                     java.lang.String version,
                                     boolean is_custom,
                                     boolean is_abstract,
                                     org.omg.CORBA.ValueDef base_value,
                                     boolean is_truncatable,
                                     org.omg.CORBA.ValueDef[] abstract_base_values,
                                     org.omg.CORBA.InterfaceDef[] supported_interfaces,
                                     org.omg.CORBA.Initializer[] initializers)
    {
        return null;
    }


    /**
     * not supported
     */

    public org.omg.CORBA.NativeDef create_native(java.lang.String id,
                                                 java.lang.String name,
                                                 java.lang.String version)
    {
        return null;
    }



    // from IRObject

    public org.omg.CORBA.DefinitionKind def_kind()
    {
        return org.omg.CORBA.DefinitionKind.dk_Exception;
    }

    public void destroy(){
        throw new INTF_REPOS(ErrorMsg.IR_Not_Implemented,
                                           org.omg.CORBA.CompletionStatus.COMPLETED_NO);
    }


    public /*ContainedSeq*/ org.omg.CORBA.Contained[] lookup_name(/*Identifier*/ String search_name, int levels_to_search, org.omg.CORBA.DefinitionKind limit_type, boolean exclude_inherited)
    {
        return null;
    }

    public /*DescriptionSeq*/ org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind limit_type, boolean exclude_inherited, int max_returned_objs){
        return null;
    }


    public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind limit_type,
                                              boolean exclude_inherited)
    {
        Hashtable limited = new Hashtable();

        // analog constants, exceptions etc.

        for( Enumeration e = contained.elements(); e.hasMoreElements();  )
        {
            Contained c = (Contained)e.nextElement();
            if( limit_type.value() == org.omg.CORBA.DefinitionKind._dk_all ||
                limit_type.value() == c.def_kind.value() )
            {
                limited.put( c, "" );
            }
        }

        org.omg.CORBA.Contained[] c = new org.omg.CORBA.Contained[limited.size()];
        int i;
        Enumeration e;
        for( e = limited.keys(), i=0 ; e.hasMoreElements(); i++ )
            c[i] = (org.omg.CORBA.Contained)e.nextElement();
        return c;

    }


    // from Contained

    public org.omg.CORBA.ContainedPackage.Description describe()
    {
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.CORBA.ExceptionDescription ed = describe_exception();
        org.omg.CORBA.ExceptionDescriptionHelper.insert( a, ed );
        return new org.omg.CORBA.ContainedPackage.Description( org.omg.CORBA.DefinitionKind.dk_Exception, a);
    }

   public AbstractInterfaceDef create_abstract_interface (String id, String name, String version,
            AbstractInterfaceDef[] baseInterfaces)
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public ExtValueDef create_ext_value (String id, String name, String version, boolean isCustom,
            boolean isAbstract, ValueDef baseValue, boolean isTruncatable,
            ValueDef[] abstractBaseValues, InterfaceDef[] supportedInterfaces,
            ExtInitializer[] initializers)
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public LocalInterfaceDef create_local_interface (String id, String name, String version,
            InterfaceDef[] baseInterfaces)
   {
      throw new NO_IMPLEMENT ("NYI");
   }
}
