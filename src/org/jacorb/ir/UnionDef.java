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

import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import org.slf4j.Logger;
import org.omg.CORBA.INTF_REPOS;
import org.omg.PortableServer.POA;

public class UnionDef
    extends TypedefDef
    implements org.omg.CORBA.UnionDefOperations, ContainerType
{
    private org.omg.CORBA.UnionMember [] members;
    private org.omg.CORBA.TypeCode discriminator_type;
    private org.omg.CORBA.IDLType discriminator_type_def;

    private Method memberMethods[];
    private int member_size;

    /* reference to my container as a contained object */
    private org.omg.CORBA.Contained       myContainer;
    /** local references to contained objects */
    private Hashtable  containedLocals = new Hashtable();
    /** CORBA references to contained objects */
    private Hashtable  contained = new Hashtable();

    private File 		                 my_dir;
    private String                       path;
    private Logger logger;
    private ClassLoader loader;
    private POA poa;

    public UnionDef( Class c,
                      String path,
                     org.omg.CORBA.Container _defined_in,
                     org.omg.CORBA.Repository ir,
                     ClassLoader loader,
                     Logger logger,
                     POA poa )
    {
        this.loader = loader;
        this.logger = logger;
        this.poa = poa;

        def_kind = org.omg.CORBA.DefinitionKind.dk_Union;
        containing_repository = ir;
        defined_in = _defined_in;
        version = "1.0";
        String classId = c.getName();
        myContainer = org.omg.CORBA.ContainedHelper.narrow( defined_in );

        if( classId.indexOf('.') > 0 )
        {
            name( classId.substring( classId.lastIndexOf('.')+1));
            absolute_name = myContainer.absolute_name() + "::" + name;
        }
        else
        {
            name( classId );
            defined_in = containing_repository;
            absolute_name = "::" + name;
        }

        Class helperClass;
        try
        {
            helperClass = this.loader.loadClass(classId + "Helper");
            id( (String)helperClass.getDeclaredMethod("id", (Class[]) null).invoke( null, (Object[]) null ));
            type = TypeCodeUtil.getTypeCode( c, this.loader, null, classId, this.logger );
            members = new org.omg.CORBA.UnionMember[ type.member_count() ];
            for( int i = 0; i < members.length; i++ )
            {
                members[i] = new org.omg.CORBA.UnionMember( type.member_name(i),
                                                            type.member_label(i),
                                                            type.member_type(i),
                                                            null );
            }
            discriminator_type =  type.discriminator_type();
        }
        catch( Exception e )
        {
            this.logger.error("Caught Exception", e);
        }
    }

    public void loadContents()
    {
        // read from the  class (operations and atributes)
        if (getReference() == null)
        {
            throw new INTF_REPOS ("getReference returns null");
        }

        org.omg.CORBA.UnionDef myReference =
            org.omg.CORBA.UnionDefHelper.narrow( getReference());

        if (myReference == null)
        {
            throw new INTF_REPOS ("narrow failed for " + getReference() );
        }

        /* load nested definitions from interfacePackage directory */

        String[] classes = null;
        if( my_dir != null )
        {
            classes = my_dir.list( new IRFilenameFilter(".class") );

            // load class files in this interface's Package directory
            if( classes != null)
            {
                for( int j = 0; j < classes.length; j++ )
                {
                    try
                    {
                        if (this.logger.isDebugEnabled())
                        {
                            this.logger.debug("Union " +name+ " tries " +
                                              full_name +
                                              "Package." +
                                              classes[j].substring( 0, classes[j].indexOf(".class")));
                        }

                        ClassLoader loader = getClass().getClassLoader();
                        if( loader == null )
                        {
                            loader = this.loader;
                        }

                        Class cl =
                            loader.loadClass(
                                   ( full_name + "Package." +
                                     classes[j].substring( 0, classes[j].indexOf(".class"))
                                     ));


                        Contained containedObject =
                            Contained.createContained( cl,
                                                       path,
                                                       myReference,
                                                       containing_repository,
                                                       this.logger,
                                                       this.loader,
                                                       this.poa);
                        if( containedObject == null )
                            continue;

                        org.omg.CORBA.Contained containedRef =
                            Contained.createContainedReference(containedObject,
                                                               this.logger,
                                                               this.poa);

                        if( containedObject instanceof ContainerType )
                            ((ContainerType)containedObject).loadContents();

                        containedRef.move( myReference, containedRef.name(), containedRef.version() );

                        if (this.logger.isDebugEnabled())
                        {
                            this.logger.debug("Union " + full_name +
                                              " loads "+ containedRef.name());
                        }

                        contained.put( containedRef.name() , containedRef );
                        containedLocals.put( containedRef.name(), containedObject );
                    }
                    catch ( Exception e )
                    {
                        this.logger.error("Caught Exception", e);
                    }
                }
            }
        }
    }


    public void define()
    {
        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("Union " + name +  " defining...");
        }

        discriminator_type_def =
            IDLType.create( discriminator_type, containing_repository,
                            this.logger, this.poa);

        for( Enumeration e = containedLocals.elements();
             e.hasMoreElements();
             ((IRObject)e.nextElement()).define())
            ;

        try
        {
            for( int i = 0; i < members.length; i++ )
            {
                members[i].type_def =
                    IDLType.create( members[i].type, containing_repository,
                                    this.logger, this.poa );
            }
        }
        catch ( Exception e )
        {
            this.logger.error("Caught Exception", e);
        }

        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("UnionDef " + name + " defined");
        }
    }

    public org.omg.CORBA.UnionMember[] members()
    {
        return members;
    }

    public void members(org.omg.CORBA.UnionMember[] m)
    {
        members = m;
    }

    public org.omg.CORBA.TypeCode discriminator_type()
    {
        return discriminator_type;
    }

    public org.omg.CORBA.IDLType discriminator_type_def()
    {
        return discriminator_type_def;
    }

    public void discriminator_type_def(org.omg.CORBA.IDLType dt)
    {
        discriminator_type_def = dt;
    }


    // from Contained

    public org.omg.CORBA.ContainedPackage.Description describe()
    {
        org.omg.CORBA.Any a = orb.create_any();
        String def_in_name;

        if( myContainer != null )
            def_in_name = myContainer.id();
        else
            def_in_name = "IDL:/:1.0";

        org.omg.CORBA.TypeDescriptionHelper.insert( a,
                                new org.omg.CORBA.TypeDescription( name(),
                                                                   id(),
                                                                   def_in_name,
                                                                   version(),
                                                                   type()
                                                                   ) );
        return new org.omg.CORBA.ContainedPackage.Description( org.omg.CORBA.DefinitionKind.dk_Union, a);
    }

    // from IRObject

    public void destroy(){}


    public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind limit_type,
                                              boolean exclude_inherited)
    {
        Hashtable filtered = new Hashtable();

        if( limit_type == org.omg.CORBA.DefinitionKind.dk_all )
        {
            filtered = contained;
        }
        else
        {
            Enumeration f = contained.keys();
            while( f.hasMoreElements() )
            {
                Object k = f.nextElement();
                org.omg.CORBA.Contained c = (org.omg.CORBA.Contained)contained.get( k );
                if( c.def_kind() == limit_type )
                    filtered.put( k, c );
            }
        }

        Enumeration e = filtered.elements();
        org.omg.CORBA.Contained[] result = new org.omg.CORBA.Contained[ filtered.size() ];

        for( int i = 0; i < filtered.size(); i++ )
            result[i] = (org.omg.CORBA.Contained)e.nextElement();

        return result;
    }

    /**
     * retrieves a contained object given a scoped name
     */

    public  org.omg.CORBA.Contained lookup( String scopedname )
    {
        String top_level_name;
        String rest_of_name;
        String name;

        if( scopedname.startsWith("::") )
        {
            name = scopedname.substring(2);
        }
        else
            name = scopedname;

        if( name.indexOf("::") > 0 )
        {
            top_level_name = name.substring( 0, name.indexOf("::") );
            rest_of_name = name.substring( name.indexOf("::") + 2);
        }
        else
        {
            top_level_name = name;
            rest_of_name = null;
        }

        org.omg.CORBA.Contained top = (org.omg.CORBA.Contained)contained.get( top_level_name );
        if( top == null )
        {
            if (this.logger.isDebugEnabled())
            {
                this.logger.debug("Container " + this.name + " top " +
                                  top_level_name + " not found ");
            }

            return null;
        }

        if( rest_of_name == null )
        {
            return top;
        }
        else
        {
            org.omg.CORBA.Container topContainer = org.omg.CORBA.ContainerHelper.narrow( top );
            if( topContainer != null )
            {
                return topContainer.lookup( rest_of_name );
            }
            else
            {
                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Container " + this.name +" " +
                                      scopedname + " not found, top " +
                                      top.getClass().getName());
                }
                return null;
            }
        }
    }

    public org.omg.CORBA.Contained[] lookup_name( String search_name, /*Identifier*/
                                                  int levels_to_search,
                                                  org.omg.CORBA.DefinitionKind limit_type,
                                                  boolean exclude_inherited)
    {
        if( levels_to_search == 0 )
            return null;

        org.omg.CORBA.Contained[] c = contents( limit_type, exclude_inherited );
        Hashtable found = new Hashtable();

        for( int i = 0; i < c.length; i++)
            if( c[i].name().equals( search_name ) )
                found.put( c[i], "" );

        if( levels_to_search > 1 || levels_to_search < 0 )
        {
            // search up to a specific depth or undefinitely
            for( int i = 0; i < c.length; i++)
            {
                if( c[i] instanceof org.omg.CORBA.Container )
                {
                    org.omg.CORBA.Contained[] tmp_seq =
                        ((org.omg.CORBA.Container)c[i]).lookup_name(
                                                                    search_name, levels_to_search-1, limit_type, exclude_inherited);
                    if( tmp_seq != null )
                        for( int j = 0; j < tmp_seq.length; j++)
                            found.put( tmp_seq[j], "" );
                }
            }
        }


        org.omg.CORBA.Contained[] result = new org.omg.CORBA.Contained[ found.size() ];
        int idx = 0;

        for( Enumeration e = found.keys(); e.hasMoreElements(); )
            result[ idx++] = (org.omg.CORBA.Contained)e.nextElement();

        return result;
    }

    public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind limit_type, boolean exclude_inherited, int max_returned_objs)
    {
        return null;
    }

    public org.omg.CORBA.ModuleDef create_module(/*RepositoryId*/ String id, /*Identifier*/ String name, /*VersionSpec*/ String version){
        return null;
    }

    public org.omg.CORBA.ConstantDef create_constant(/*RepositoryId*/ String id, /*Identifier*/ String name, /*VersionSpec*/ String version, org.omg.CORBA.IDLType type, org.omg.CORBA.Any value){
        return null;
    }

    public org.omg.CORBA.StructDef create_struct(/*RepositoryId*/ String id, /*Identifier*/ String name, /*VersionSpec*/ String version, /*StructMemberSeq*/ org.omg.CORBA.StructMember[] members){
        return null;
    }

    public org.omg.CORBA.UnionDef create_union(/*RepositoryId*/ String id, /*Identifier*/ String name, /*VersionSpec*/ String version, org.omg.CORBA.IDLType discriminator_type, /*UnionMemberSeq*/ org.omg.CORBA.UnionMember[] members){
        return null;
    }

    public org.omg.CORBA.EnumDef create_enum(/*RepositoryId*/ String id, /*Identifier*/ String name, /*VersionSpec*/ String version, /*EnumMemberSeq*/ /*Identifier*/ String[] members){
        return null;
    }

    public org.omg.CORBA.AliasDef create_alias(/*RepositoryId*/ String id, /*Identifier*/ String name, /*VersionSpec*/ String version, org.omg.CORBA.IDLType original_type){
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
                    /*RepositoryId*/ String id,
                    /*Identifier*/ String name,
                    /*VersionSpec*/ String version,
                    /*InterfaceDefSeq*/ org.omg.CORBA.InterfaceDef[] base_interfaces,
                    boolean is_abstract )
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



}










