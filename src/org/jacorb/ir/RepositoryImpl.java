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
 *   Software Foundation, Inc., 675 Mass Ave, Cambrigde, MA 02139, USA.
 */

import org.omg.PortableServer.*;

import java.util.*;
import java.io.*;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

/**
 * The Interface Repository.
 * <p>
 * This class represents the repository itself as
 * well as the executable server.
 * <p>
 * Methods from the "write" interface to the IR
 * are not supported.
 * <p>
 *
 * @author Gerald Brose
 * @version $Id$
 */

public class RepositoryImpl
    extends IRObject
    implements org.omg.CORBA.RepositoryOperations, Configurable

{
    private Container[]         containers ;
    private Container           delegate ;
    private POA poa;

    /** the configuration object for this IR instance */
    private org.jacorb.config.Configuration configuration = null;

    /** the IR logger instance */
    private Logger logger = null;

    /**
     *  constructor to launch a repository with the contents of <tt>classpath</tt>
     *
     *  @param classpath a classpath string made up of directories separated by ":"
     */

    public RepositoryImpl( String classpath,
                           String outfile,
                           //#ifjdk 1.2
                              java.net.URLClassLoader loader )
                           //#else
                           //# ClassLoader loader )
                           //#endif
        throws Exception
    {
        def_kind = org.omg.CORBA.DefinitionKind.dk_Repository;
        name = "Repository";

        // parse classpath and create a top-level container for
        // each directory in the path

        StringTokenizer strtok =
            new StringTokenizer( classpath , java.io.File.pathSeparator );
        //            new StringTokenizer( classpath , ";" );

        String [] paths =
            new String [ strtok.countTokens() ];

        containers =
            new Container[ paths.length ];

        org.omg.CORBA.ORB orb =
            org.omg.CORBA.ORB.init( new String[0], null );

        this.configure(((org.jacorb.orb.ORB) orb).getConfiguration());

        poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        org.omg.CORBA.Repository myRef =
            org.omg.CORBA.RepositoryHelper.narrow(
                poa.servant_to_reference( new org.omg.CORBA.RepositoryPOATie( this ) ) );


        for( int i = 0; strtok.hasMoreTokens(); i++ )
        {
            paths[i] =  strtok.nextToken();

            if (this.logger.isDebugEnabled())
            {
                logger.debug("found path: " + paths[i]);
            }

            containers[i] = new Container( this, paths[i], null,
                                           loader, poa, logger );
        }

        // dummy
        delegate = containers[0];

        PrintWriter out = new PrintWriter( new FileOutputStream( outfile ), true);
        out.println( orb.object_to_string( myRef ) );
        setReference( myRef );
        out.close();
        poa.the_POAManager().activate();

        if (this.logger.isInfoEnabled())
        {
            //#ifjdk 1.2
            java.net.URL urls[] = loader.getURLs();
            //#else
            //# java.net.URL urls[] = new java.net.URL[0];
            //#endif
            StringBuffer sb =
                new StringBuffer("IR configured for class path: ");
            for( int i = 0; i < urls.length; i++ )
            {
                sb.append( urls[i].toString() + "\n");
            }

            logger.info(sb.toString());
        }
    }

    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)myConfiguration;
        this.logger = configuration.getNamedLogger("jacorb.ir");
    }

    // Repository

    /**
     * convert a repository ID to a scoped name
     * @param id a string in Repository ID format, e.g. "IDL:myModule/MyInterface:1.0"
     * @return a scoped name, e.g. "::myModule::MyInterface", or null
     * if the id argument does not begin with "IDL:"
     */

    private String idToScopedName( String id )
    {
        String scoped = "";

        if( !id.startsWith("IDL:") ||
            !id.endsWith( ":1.0"))
            return null;

        // strip "IDL:" and ":1.0")

        String base = id.substring( id.indexOf(':')+1,
                                    id.lastIndexOf(':')).replace( fileSeparator, '/' );

        if( base.startsWith( "omg.org") )
            base = "org/omg" + base.substring( 7 );

        StringTokenizer strtok = new StringTokenizer( base, "/" );

        for( int i = 0; strtok.hasMoreTokens(); i++ )
            scoped = scoped + "::" + strtok.nextToken();

        return scoped;

    }

    /**
     * lookup a repository ID
     * @param search_id a string in Repository ID format,
     *         e.g. "IDL:myModule/MyInterface:1.0"
     * @return a reference to the object or null, if not found
     */
    public org.omg.CORBA.Contained lookup_id( String search_id )
    {
        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("IR lookup_id: " + search_id );
        }

        String name = idToScopedName( search_id );
        if( name == null )
        {
            return null;
        }
        return lookup( name );
    }

    public org.omg.CORBA.PrimitiveDef get_primitive(org.omg.CORBA.PrimitiveKind kind)
    {
        try
        {
            return org.omg.CORBA.PrimitiveDefHelper.narrow(
                poa.servant_to_reference(
                        new org.omg.CORBA.PrimitiveDefPOATie(
                            new org.jacorb.ir.PrimitiveDef( kind.value() ))));
        }
        catch( Exception e )
        {
            logger.error("unexpected exception", e);
            return null;
        }
    }

    /**
     * not supported
     */

    public org.omg.CORBA.StringDef create_string(int bound)
    {
        return null;
    }

    /**
     * not supported
     */

    public org.omg.CORBA.WstringDef create_wstring(int bound)
    {
        return null;
    }

    /**
     * not supported
     */

    public org.omg.CORBA.FixedDef create_fixed(short digits, short scale)
    {
        return null;
    }

    /**
     * not supported
     */

    public org.omg.CORBA.SequenceDef create_sequence(int bound,
                                                     org.omg.CORBA.IDLType element_type)
    {
        return null;
    }

    /**
     * not supported
     */

    public org.omg.CORBA.ArrayDef create_array(int length,
                                               org.omg.CORBA.IDLType element_type)
    {
        return null;
    }



    public org.omg.CORBA.TypeCode get_canonical_typecode(org.omg.CORBA.TypeCode tc)
    {
        return null;
    }


    // container

    /**
     * lookup a scoped name in the repository
     *
     * @param name	the name to look for
     * @return a reference to the item with the specified name
     * or null, if not found
     */


    public org.omg.CORBA.Contained lookup( String name )
    {
        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("IR lookup : " + name );
        }

        org.omg.CORBA.Contained result = null;
        for( int i = 0; i < containers.length; i++ )
        {
            result = containers[i].lookup( name );
            if( result != null )
                break;
        }
        return result;
    }

    /**
     * lookup a simple name in the repository
     * (neither scoped nor ID formatted)
     *
     * @param search_name the name to look for
     * @param levels_to_search  if 1, search only this object, if -1, search
     *         all containers contained in this repository, else search
     *         until the specified depth is reached
     * @param limit_type  limit the description to objects of this type
     * @param exclude_inherited	exclude inherited items from the description
     * @return an array of items with the specified name
     */

    public org.omg.CORBA.Contained[] lookup_name(
                String search_name,
                int levels_to_search,
                org.omg.CORBA.DefinitionKind limit_type,
                boolean exclude_inherited )
    {
        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("IR lookup_name: " + search_name);
        }

        org.omg.CORBA.Contained[] result = null;
        Vector intermediate = new Vector();

        for( int i = 0; i < containers.length; i++ )
        {
            intermediate.addElement( containers[i].lookup_name( search_name,
                                                                levels_to_search,
                                                                limit_type,
                                                                exclude_inherited ));
        }

        int size = 0;
        for( int i = 0; i < intermediate.size(); i++ )
        {
            size += ((Object[])intermediate.elementAt(i)).length;
        }
        result = new  org.omg.CORBA.Contained[size];
        int start = 0;

        for( int i = 0; i < intermediate.size(); i++ )
        {
            org.omg.CORBA.Contained[] src =
                (org.omg.CORBA.Contained[])intermediate.elementAt(i);

            System.arraycopy( src, 0, result, start, src.length );
            start += src.length;
        }

    return result;
    }


    /**
     * list the contents of the repository
     * @param limit_type  limit the description to objects of this type
     * @param exclude_inherited	exclude inherited items from the description
     * @return an array of items contained in this repository
     */

    public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind limit_type,
                                              boolean exclude_inherited)
    {
        org.omg.CORBA.Contained[] result = null;
        Vector intermediate = new Vector();
        for( int i = 0; i < containers.length; i++ )
        {
            intermediate.addElement( containers[i].contents( limit_type,
                                                             exclude_inherited ));
        }

        int size = 0;
        for( int i = 0; i < intermediate.size(); i++ )
        {
            size += ((Object[])intermediate.elementAt(i)).length;
        }

        result = new  org.omg.CORBA.Contained[size];
        int start = 0;

        // assemble result array
        for( int i = 0; i < intermediate.size(); i++ )
        {
            org.omg.CORBA.Contained[] src =
                (org.omg.CORBA.Contained[])intermediate.elementAt(i);

            System.arraycopy( src, 0, result, start, src.length );
            start += src.length;
        }

    return result;
    }


    /**
     * describe the contents of the repository
     * @param limit_type  limit the description to objects of this type
     * @param exclude_inherited	exclude inherited items from the description
     * @param max_returned_objs   return only so many items
     * @return an array of descriptions
     */

    public org.omg.CORBA.ContainerPackage.Description[] describe_contents(
                           org.omg.CORBA.DefinitionKind limit_type,
                           boolean exclude_inherited,
                           int max_returned_objs)
    {
        org.omg.CORBA.Contained[] c = contents( limit_type, exclude_inherited );
        int size;
        if( max_returned_objs > c.length )
            size = max_returned_objs;
        else
            size = c.length;
        org.omg.CORBA.ContainerPackage.Description[] result =
            new org.omg.CORBA.ContainerPackage.Description[size];
        for( int i = 0; i < size; i++ )
        {
            result[i] = new org.omg.CORBA.ContainerPackage.Description();
            org.omg.CORBA.ContainedPackage.Description cd_descr = c[i].describe();
            result[i].contained_object = c[i];
            result[i].kind = cd_descr.kind;
            result[i].value = cd_descr.value;
        }
        return result;
    }


    void define()
    {
        // do nothing
    }

    public void loadContents()
    {
        if (this.logger.isInfoEnabled())
        {
            this.logger.info("Repository loads contents...");
        }

        for( int i = 0; i < containers.length; i++ )
        {
            containers[i].loadContents();
        }
        for( int i = 0; i < containers.length; i++ )
        {
            containers[i].define();
        }

        if (this.logger.isInfoEnabled())
        {
            this.logger.info("Repository contents loaded");
        }
    }

    public org.omg.CORBA.ModuleDef create_module( String id,
                                                  String name,
                                                  String version)
    {
        return delegate.create_module( id,  name,  version);
    }

    public org.omg.CORBA.ConstantDef create_constant( String id, String name,
                                                      String version,
                                                      org.omg.CORBA.IDLType type,
                                                      org.omg.CORBA.Any value)
    {
        return delegate.create_constant(  id,  name, version, type, value);
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
                                                org.omg.CORBA.IDLType
                                                discriminator_type,
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

    public org.omg.CORBA.InterfaceDef create_interface(String id,
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

    public org.omg.CORBA.ValueBoxDef create_value_box(String id,
                                                      String name,
                                                      String version,
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
        return delegate.create_value(id, name, version,is_custom, is_abstract, base_value, is_truncatable,
                                     abstract_base_values,  supported_interfaces,  initializers);
    }


    /**
     * not supported
     */

    public org.omg.CORBA.NativeDef create_native(String id,
                                                 String name,
                                                 String version)
    {
        return delegate.create_native( id,  name,  version);
    }


    public void destroy()
    {
        delegate.destroy();
    }


}










