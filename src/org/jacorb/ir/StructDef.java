package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import org.jacorb.orb.TypeCode;
//import org.jacorb.orb.TypeCodeUtil;
import org.jacorb.util.Debug;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;


public class StructDef 
    extends TypedefDef
    implements org.omg.CORBA.StructDefOperations, ContainerType
{
    protected static char 	    fileSeparator = 
        System.getProperty("file.separator").charAt(0);

    private org.omg.CORBA.TypeCode       type;
    private Class                        myClass;
    private Class                        helperClass;
    private org.omg.CORBA.StructMember[] members;

    /** local references to contained objects */
    private Hashtable		             containedLocals = new Hashtable();
    /** CORBA references to contained objects */
    private Hashtable	                 contained = new Hashtable();

    private File 		         my_dir;
    private String                       path;

    private boolean defined = false;

    public StructDef(Class c, 
                     String path,
                     org.omg.CORBA.Container _defined_in,
                     org.omg.CORBA.Repository ir)
    {
        def_kind = org.omg.CORBA.DefinitionKind.dk_Struct;
        containing_repository = ir;
        defined_in = _defined_in;
        this.path = path;
        Debug.myAssert( defined_in != null, "defined_in = null");
        Debug.myAssert( containing_repository != null, "containing_repository = null");

        try
        { 
            String classId = c.getName();
            myClass = c;
            version( "1.0" );
            full_name = classId.replace('.', '/');

            if( classId.indexOf('.') > 0 ) 
            {
                name( classId.substring( classId.lastIndexOf('.')+1 ) );
                absolute_name = 
                    org.omg.CORBA.ContainedHelper.narrow( defined_in ).absolute_name() + 
                    "::" + name;
            }             
            else 
            {
                name( classId );
                absolute_name = "::" + name;
            }
	
            helperClass = RepositoryImpl.loader.loadClass( classId + "Helper") ;
            id( (String)helperClass.getDeclaredMethod( "id", null ).invoke( null, null ));

//              type = 
//                  TypeCodeUtil.getTypeCode( myClass, RepositoryImpl.loader, null, classId );

            type = (TypeCode)helperClass.getDeclaredMethod( "type", null ).invoke( null, null );
            
            members = new org.omg.CORBA.StructMember[ type.member_count() ];
            for( int i = 0; i < members.length; i++ )
            {
                org.omg.CORBA.TypeCode type_code = type.member_type(i);
                String member_name = type.member_name(i);

                org.jacorb.util.Debug.output(3, "StructDef " + absolute_name  + " member " + member_name);

                members[i] = new org.omg.CORBA.StructMember( member_name, 
                                                             type_code,
                                                             null );
            }
            /* get directory for nested definitions' classes */
            File f = new File( path + fileSeparator + 
                               classId.replace('.', fileSeparator) + "Package" );

            if( f.exists() && f.isDirectory() )
                my_dir = f;
            org.jacorb.util.Debug.output(2, "StructDef: " + absolute_name );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new org.omg.CORBA.INTF_REPOS( ErrorMsg.IR_Not_Implemented,
                                                org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }
    }


    public void loadContents()
    {
        // read from the  class (operations and atributes)
        Debug.myAssert( getReference() != null, "my own ref null");

        org.omg.CORBA.StructDef myReference = 
            org.omg.CORBA.StructDefHelper.narrow( getReference());

        Debug.myAssert( myReference != null, "narrow failed for " + getReference() );

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
                        org.jacorb.util.Debug.output(2, "Struct " +name+ " tries " + 
                                                 full_name.replace('.', fileSeparator) + 
                                                 "Package" + fileSeparator + 
                                                 classes[j].substring( 0, classes[j].indexOf(".class")) );

                        ClassLoader loader = getClass().getClassLoader();
                        if( loader == null )
                        {
                            loader = RepositoryImpl.loader;
                        }

                        Class cl = 
                            loader.loadClass( 
                                             ( full_name.replace('.', fileSeparator) + "Package" + fileSeparator + 
                                               classes[j].substring( 0, classes[j].indexOf(".class"))
                                               ).replace( fileSeparator, '/') );
                        

                        Contained containedObject = Contained.createContained( cl, 
                                                                               path,
                                                                               myReference, 
                                                                               containing_repository );
                        if( containedObject == null )
                            continue;
                        
                        org.omg.CORBA.Contained containedRef = 
                            Contained.createContainedReference(containedObject);
                        
                        if( containedObject instanceof ContainerType )
                            ((ContainerType)containedObject).loadContents();
                        
                        containedRef.move( myReference, containedRef.name(), containedRef.version() );
                        
                        org.jacorb.util.Debug.output(2, "Struct " + full_name + 
                                                 " loads "+ containedRef.name() );
                        contained.put( containedRef.name() , containedRef );
                        containedLocals.put( containedRef.name(), containedObject );                        
                    } 
                    catch ( Exception e ) 
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    /**
     */

    public void define()
    {
        org.jacorb.util.Debug.output(2, "Struct " + name +  " defining...");
        for( Enumeration e = containedLocals.elements();
             e.hasMoreElements();
             ((IRObject)e.nextElement()).define())
            ;

        for( int i = 0; i < members.length; i++ )
        {
            members[i].type_def = 
                IDLType.create( members[i].type, containing_repository);

            org.jacorb.util.Debug.myAssert( members[i].type_def != null,
                                          "No type_def for member " + members[i].name + 
                                          " in struct " +  full_name );
        }
        defined = true;
        org.jacorb.util.Debug.output(2, "Struct " + name +  " defined");

    }

    /**
     */

    org.omg.CORBA.TypeDescription describe_struct() 
    {
        org.jacorb.util.Debug.myAssert( defined,
                                  "Struct " + full_name + " not defined! ");
        return new org.omg.CORBA.TypeDescription(name(), 
                                                 id(), 
                                                 org.omg.CORBA.ContainedHelper.narrow( defined_in ).id(),
                                                 version(), 
                                                 type());
    }

    public org.omg.CORBA.TypeCode type() 
    {
        Debug.myAssert( type != null, "Struct TypeCode is null");
        return type;
    }

    public org.omg.CORBA.Contained lookup( String scopedname )
    {
        org.jacorb.util.Debug.output(2,"Struct " + this.name + " lookup " + scopedname );

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
		
        try
        {
            org.omg.CORBA.Contained top = 
                (org.omg.CORBA.Contained)contained.get( top_level_name );

            if( top == null )
            {
                org.jacorb.util.Debug.output(2,"Interface " + this.name + 
                                         " top " + top_level_name + " not found ");
                return null;
            }
	
            if( rest_of_name == null )
            {
                return top;
            }
            else 
            {
                if( top instanceof org.omg.CORBA.Container)
                {
                    return ((org.omg.CORBA.Container)top).lookup( rest_of_name );
                }
                else
                {
                    org.jacorb.util.Debug.output(2,"Interface " + this.name +
                                             " " + scopedname + " not found ");
                    return null;		
                }
            }
        } 
        catch( Exception e )
        {
            e.printStackTrace();
            return null;			
        }	
    }

    public org.omg.CORBA.StructMember[] members()
    {
        org.jacorb.util.Debug.myAssert( defined,
                                  "Struct " + full_name + " not defined! ");
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

    public org.omg.CORBA.ConstantDef create_constant(java.lang.String id, 
                                                     java.lang.String name, 
                                                     java.lang.String version, 
                                                     org.omg.CORBA.IDLType type, 
                                                     org.omg.CORBA.Any value)
    {
        return null;
    }

    public org.omg.CORBA.StructDef create_struct( String id, String name, 
                                                  String version,
                                                  org.omg.CORBA.StructMember[] members){
        return null;
    }

    public org.omg.CORBA.UnionDef create_union( String id, String name, 
                                                String version, 
                                                org.omg.CORBA.IDLType discriminator_type, 
                                                org.omg.CORBA.UnionMember[] members){
        return null;
    }

    public org.omg.CORBA.EnumDef create_enum( String id, String name, 
                                              String version,  String[] members){
        return null;
    }

    public org.omg.CORBA.AliasDef create_alias( String id, String name, 
                                                String version, 
                                                org.omg.CORBA.IDLType original_type){
        return null;
    }

    /**
     * not supported
     */

    public org.omg.CORBA.ExceptionDef create_exception(java.lang.String id, 
                                                       java.lang.String name , 
                                                       java.lang.String version, 
                                                       org.omg.CORBA.StructMember[] member ) 
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
                                                       org.omg.CORBA.InterfaceDef[] base_interfaces,
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
 



    public void destroy()
    {
        throw new org.omg.CORBA.INTF_REPOS(ErrorMsg.IR_Not_Implemented,
                                           org.omg.CORBA.CompletionStatus.COMPLETED_NO);
    }


    public org.omg.CORBA.Contained[] lookup_name( String search_name, 
                                                  int levels_to_search, 
                                                  org.omg.CORBA.DefinitionKind limit_type, 
                                                  boolean exclude_inherited )
    {
        if( levels_to_search == 0 )
            return null;

        org.omg.CORBA.Contained[] c = contents( limit_type, exclude_inherited );
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

    public org.omg.CORBA.ContainerPackage.Description[] describe_contents( 
                                                     org.omg.CORBA.DefinitionKind limit_type, 
                                                     boolean exclude_inherited, 
                                                     int max_returned_objs )
    {
        return null;
    }


    public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind limit_type, 
                                              boolean exclude_inherited)
    {
        Hashtable limited = new Hashtable();

        // analog constants, exceptions etc.

        for( Enumeration e = contained.elements(); e.hasMoreElements();  )
        {
            org.omg.CORBA.Contained c = (org.omg.CORBA.Contained)e.nextElement();
            if( limit_type == org.omg.CORBA.DefinitionKind.dk_all || 
                limit_type == c.def_kind() )
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
        org.jacorb.util.Debug.myAssert( defined,
                                  "Struct " + full_name + "not defined! ");

        org.omg.CORBA.Any a = orb.create_any();
        org.omg.CORBA.TypeDescription ed = describe_struct();
        org.omg.CORBA.TypeDescriptionHelper.insert( a, ed );
        return new org.omg.CORBA.ContainedPackage.Description( 
                                                              org.omg.CORBA.DefinitionKind.dk_Struct, a);
    }

}




