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
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.avalon.framework.logger.Logger;
import org.omg.CORBA.INTF_REPOS;
import org.omg.PortableServer.POA;

public class Container
    extends IRObject
    implements org.omg.CORBA.ContainerOperations
{
    protected IRObject                delegator;

    /** CORBA references to contained objects */
    protected Hashtable		    contained = new Hashtable();

    /** local references to contained objects */
    protected Hashtable		    containedLocals = new Hashtable();

    protected File 		    my_dir = null;
    protected String 		    path = null;
    protected String 		    full_name = null;

    /** CORBA reference to this container */
    protected org.omg.CORBA.Container this_container;

    /** outer container */
    protected org.omg.CORBA.Container defined_in;
    protected org.omg.CORBA.Repository containing_repository;

    protected boolean defined = false;

    private ClassLoader loader;
    private POA poa;
    private Logger logger;

    /**
     */

    public Container( IRObject delegator,
                      String path,
                      String full_name,
                      ClassLoader loader,
                      POA poa,
                      Logger logger )
    {
        this.loader = loader;
        this.poa = poa;
        this.logger = logger;
        this.delegator = delegator;
        this.path = path;
        this.full_name = full_name;

        my_dir = new File( path + fileSeparator +
                           ( full_name != null ? full_name : "" ).replace('.', fileSeparator) );

        if ( ! my_dir.isDirectory())
        {
            throw new INTF_REPOS ("no directory : " + path + fileSeparator + full_name);
        }

        this.name = delegator.getName();

        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("New Container full_name " +
                              full_name + " name : " + name +
                              " path: " +  path);
        }
        // else: get reference from delegator, but must be postponed until later
    }

    /**
     */

    void loadContents()
    {
        this_container =
            org.omg.CORBA.ContainerHelper.narrow( delegator.getReference());
        if (this_container == null)
        {
            throw new INTF_REPOS ("no container !");
        }

        if( delegator instanceof Contained )
        {
            containing_repository = ((Contained)delegator).containing_repository();
            defined_in = ((Contained)delegator).defined_in();
        }
        else
        {
            containing_repository = org.omg.CORBA.
                RepositoryHelper.narrow( delegator.getReference());
            defined_in = containing_repository;
        }

        if (containing_repository == null)
        {
            throw new INTF_REPOS ("no containing repository");
        }

        String[] classes;
        String[] dirs;

        // get all files in this directory which either end in ".class" or
        // do not contain a "." at all

        classes = my_dir.list( new IRFilenameFilter(".class") );
        dirs = my_dir.list( new IRFilenameFilter( null ) );

        // load class files in this module/package
        if( classes != null)
        {
            String prefix =
                ( full_name != null ? full_name + '.' : "");

            for( int j = 0; j< classes.length; j++ )
            {
                try
                {
                    if (this.logger.isDebugEnabled())
                    {
                        this.logger.debug("Container " +name+ " tries " +
                                          prefix +
                                          classes[j].substring( 0, classes[j].indexOf(".class")));
                    }

                    Class cl =
                        this.loader.loadClass(
                                         ( prefix +
                                           classes[j].substring( 0, classes[j].indexOf(".class"))
                                           ));

                    Contained containedObject =
                        Contained.createContained( cl,
                                                   path,
                                                   this_container,
                                                   containing_repository,
                                                   this.logger,
                                                   this.loader,
                                                   this.poa );
                    if( containedObject == null )
                    {
                        if (this.logger.isDebugEnabled())
                        {
                            this.logger.debug("Container: nothing created for "
                                              + cl.getClass().getName());
                        }
                        continue;
                    }

                    org.omg.CORBA.Contained containedRef =
                        Contained.createContainedReference(containedObject,
                                                           this.logger,
                                                           this.poa);

                    containedRef.move( this_container,
                                       containedRef.name(),
                                       containedRef.version() );

                    if (this.logger.isDebugEnabled())
                    {
                        this.logger.debug("Container " + prefix +
                                          " loads "+ containedRef.name());
                    }

                    contained.put( containedRef.name() , containedRef );
                    containedLocals.put( containedRef.name(), containedObject );
                    if( containedObject instanceof ContainerType )
                        ((ContainerType)containedObject).loadContents();

                }
                catch ( java.lang.Throwable e )
                {
                    this.logger.error("Caught exception", e);
                }
            }
        }

        if( dirs != null)
        {
            for( int k = 0; k < dirs.length; k++ )
            {
                if( !dirs[k].endsWith("Package"))
                {
                    File f = new File( my_dir.getAbsolutePath() +
                                       fileSeparator +
                                       dirs[k] );
                    try
                    {
                        String [] classList = f.list();
                        if( classList != null && classList.length > 0)
                        {
                            ModuleDef m =
                                new ModuleDef( path,
                                               (( full_name != null ?
                                                 full_name + fileSeparator :
                                                 ""
                                                ) + dirs[k]).replace('/', '.'),
                                               this_container,
                                               containing_repository,
                                               this.loader,
                                               this.poa,
                                               this.logger);

                            org.omg.CORBA.ModuleDef moduleRef =
                                org.omg.CORBA.ModuleDefHelper.narrow(
                                    this.poa.servant_to_reference(
                                        new org.omg.CORBA.ModuleDefPOATie( m ) ));

                            m.setReference( moduleRef );
                            m.loadContents();

                            if (this.logger.isDebugEnabled())
                            {
                                this.logger.debug("Container " +
                                                  full_name +
                                                  " puts module " + dirs[k]);
                            }

                            m.move( this_container, m.name(), m.version() );
                            contained.put( m.name() , moduleRef );
                            containedLocals.put( m.name(), m );
                        }
                    }
                    catch ( Exception e )
                    {
                        this.logger.error("Caught Exception", e);
                    }
                }
            }
        }
    }

    void define()
    {
        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("Container " + full_name + " defining...");
        }

        for( Enumeration e = containedLocals.elements();
             e.hasMoreElements();
             ((IRObject)e.nextElement()).define())
            ;

        defined = true;

        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("Container " + full_name + " defined");
        }
    }


    public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind limit_type,
                                              boolean exclude_inherited)
    {
        if ( ! defined)
        {
            throw new INTF_REPOS ("contents undefined");
        }

        Hashtable filtered = new Hashtable();

        if( limit_type.value() == org.omg.CORBA.DefinitionKind._dk_all )
        {
            filtered = contained;
        }
        else
        {
            Enumeration f = contained.keys();
            while( f.hasMoreElements() )
            {
                Object k = f.nextElement();
                org.omg.CORBA.Contained c =
                    (org.omg.CORBA.Contained)contained.get( k );
                if( c.def_kind().value() == limit_type.value() )
                    filtered.put( k, c );
            }
        }

        Enumeration e = filtered.elements();
        org.omg.CORBA.Contained[] result =
            new org.omg.CORBA.Contained[ filtered.size() ];

        for( int i = 0; i < filtered.size(); i++ )
            result[i] = (org.omg.CORBA.Contained)e.nextElement();

        return result;
    }

    /**
     * retrieves a contained object given a scoped name
     */

    public org.omg.CORBA.Contained lookup( String scopedname )
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

        org.omg.CORBA.Contained top =
            (org.omg.CORBA.Contained)contained.get( top_level_name );

        if( top == null )
        {
            if (this.logger.isDebugEnabled())
            {
                this.logger.debug("Container " + this.name +
                                  " top " + top_level_name + " not found ");
            }
            return null;
        }

        if( rest_of_name == null )
        {
            return top;
        }

        org.omg.CORBA.Container topContainer =
            org.omg.CORBA.ContainerHelper.narrow( top );
        if( topContainer != null )
        {
            return topContainer.lookup( rest_of_name );
        }

        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("Container " + this.name +" " +
                    scopedname + " not found, top " +
                    top.getClass().getName());
        }
        return null;
    }

    public org.omg.CORBA.Contained[] lookup_name( String search_name,
                                                  int levels_to_search,
                                                  org.omg.CORBA.DefinitionKind limit_type,
                                                  boolean exclude_inherited)
    {
        if( levels_to_search == 0 )
            return null;

        org.omg.CORBA.Contained[] c =
            contents( limit_type, exclude_inherited );

        Hashtable found = new Hashtable();

        for( int i = 0; i < c.length; i++)
            if( c[i].name().equals( search_name ) )
                found.put( c[i], "" );

        if( levels_to_search > 1 || levels_to_search == -1 )
        {
            // search up to a specific depth or undefinitely
            for( int i = 0; i < c.length; i++)
            {
                if( c[i] instanceof org.omg.CORBA.Container )
                {
                    org.omg.CORBA.Contained[] tmp_seq =
                        ((org.omg.CORBA.Container)c[i]).lookup_name( search_name,
                                                                     levels_to_search-1,
                                                                     limit_type,
                                                                     exclude_inherited);
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

    public void destroy(){}


}
