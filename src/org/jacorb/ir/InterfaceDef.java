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

import org.jacorb.util.Debug;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

import org.omg.CORBA.IDLType;
import org.omg.CORBA.INTF_REPOS;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription;
import org.omg.CORBA.ContainerPackage.*;
import org.omg.CORBA.OperationDescription;
import org.omg.CORBA.AttributeDescription;
import org.omg.CORBA.ConstantDescription;

/**
 * JacORB implementation of org.omg.CORBA.InterfaceDef
 *
 * @author Gerald Brose
 * @version $Id$
 */

public class InterfaceDef
    extends org.jacorb.ir.Contained
    implements org.omg.CORBA.InterfaceDefOperations, ContainerType
{
    protected static char 	    fileSeparator =
        System.getProperty("file.separator").charAt(0);

    Class 				         theClass;
    private Class                                helperClass;
    private Class                                signatureClass;

    private org.omg.CORBA.TypeCode               typeCode;
    private OperationDef[]                       op_defs;
    private org.omg.CORBA.OperationDescription[] operations;

    // to be done !!
    private boolean is_abstract = false;

    private AttributeDef[]                       att_defs;
    private org.omg.CORBA.AttributeDescription[] attributes;

    private ConstantDef[]                        constant_defs;
    private org.omg.CORBA.ConstantDescription[]  constants;

    private org.omg.CORBA.InterfaceDef[] 	 base_interfaces;
    private String [] 		                 base_names;
    private FullInterfaceDescription             fullDescription;
    /** local references to contained objects */
    private Hashtable		                 containedLocals = new Hashtable();

    /** CORBA references to contained objects */
    private Hashtable	                         contained = new Hashtable();

    /* reference to my container as a contained object */
    private org.omg.CORBA.Contained              myContainer;
    private org.omg.CORBA.InterfaceDef           myReference;

    private File 		                 my_dir;
    private Hashtable                            op = new Hashtable();
    private Hashtable                            att = new Hashtable();
    private Hashtable                            my_const = new Hashtable();
    private org.omg.CORBA.Contained []           classes;
    private String                               path;
    private String []                            class_names;
    private int                                  size = 0;
    private boolean                              defined = false;
    private boolean                              loaded = false;

    private Class                                containedClass = null;
    private Class                                containerClass = null;

    /**
     * Class constructor
     */

    InterfaceDef( Class c,
                  Class helperClass,
                  String path,
                  org.omg.CORBA.Container def_in,
                  org.omg.CORBA.Repository ir )
        throws INTF_REPOS
    {
        if (ir == null)
        {
            throw new INTF_REPOS ("IR null!");
        }
        if (def_in == null)
        {
            throw new INTF_REPOS ("Defined_in null!");
        }

        def_kind = org.omg.CORBA.DefinitionKind.dk_Interface;
        containing_repository = ir;
        defined_in = def_in;
        if( def_in.equals(ir) )
            myContainer = null;
        else
            myContainer = org.omg.CORBA.ContainedHelper.narrow( defined_in );

        this.path = path;

        theClass = c;
        String classId = c.getName();
        this.helperClass = helperClass;

        Hashtable irInfo= null;
        Class irHelperClass = null;
        try
        {
            irHelperClass =
                RepositoryImpl.loader.loadClass( theClass.getName() + "IRHelper");
            irInfo = (Hashtable)irHelperClass.getDeclaredField("irInfo").get(null);
        }
        catch( ClassNotFoundException e )
        {
            org.jacorb.util.Debug.output(1, "!! No IR helper class for interface " +
                                     theClass.getName());
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        if (irInfo == null)
        {
            throw new INTF_REPOS ("IR Info null!");
        }

        try
        {
            containedClass = RepositoryImpl.loader.loadClass("org.omg.CORBA.Contained");
            signatureClass = RepositoryImpl.loader.loadClass(classId + "Operations");

            id( (String)helperClass.getDeclaredMethod("id", null).invoke( null, null ) );
            version( id().substring( id().lastIndexOf(':')));
            typeCode = TypeCodeUtil.getTypeCode( c, null );

            full_name = classId.replace('.', '/');
            if( classId.indexOf('.') > 0 )
            {
                name = classId.substring( classId.lastIndexOf('.')+1);

                if (defined_in == null)
                {
                    throw new INTF_REPOS ("InterfaceDef " + name + " path " + path +
                                          " has no defined_in repository");
                }

                if( containedClass.isAssignableFrom( defined_in.getClass() ))
                    absolute_name =
                        ( myContainer != null ? myContainer.absolute_name() : "Global" )
                        + "::" + name;
                else
                    absolute_name = "::" + name;
            }
            else
            {
                name = classId;
                defined_in = containing_repository;
                absolute_name = "::" + name;
            }

            org.jacorb.util.Debug.output(2, "InterfaceDef: " + absolute_name +
                                         " path: " + path);


            /* get directory for nested definitions' classes */
            File f =
                new File( path + fileSeparator +
                          classId.replace('.', fileSeparator) + "Package" );

            if( f.exists() && f.isDirectory() )
                my_dir = f;


        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new INTF_REPOS( ErrorMsg.IR_Not_Implemented,
                                                org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }
    }



    public void loadContents()
    {
        org.jacorb.util.Debug.output(2, "Interface " +name+ " loading... ");
        // read from the interface class (operations and atributes)
        if (getReference() == null)
        {
            throw new INTF_REPOS ("getReference returns null");
        }

        myReference =
            org.omg.CORBA.InterfaceDefHelper.narrow( getReference());

        if (myReference == null)
        {
            throw new INTF_REPOS ("narrow failed for " + getReference ());
        }

        /* load nested definitions from interfacePackage directory */

        String[] classes = null;

        if( my_dir != null )
        {
            classes = my_dir.list( new IRFilenameFilter(".class") );

            // load class files in this interface's Package directory
            if( classes != null)
            {
                for( int j = 0; j< classes.length; j++ )
                {
                    try
                    {
                        org.jacorb.util.Debug.output(2, "Interface " +name+ " tries " +
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
                                   ( full_name.replace('.', fileSeparator) +
                                     "Package" + fileSeparator +
                                     classes[j].substring( 0, classes[j].indexOf(".class"))
                                     ).replace( fileSeparator, '/') );

                        Contained containedObject =
                            Contained.createContained( cl,
                                                       path,
                                                       myReference,
                                                       containing_repository );
                        if( containedObject == null )
                            continue;

                        org.omg.CORBA.Contained containedRef =
                            Contained.createContainedReference(containedObject);


                        containedRef.move( myReference,
                                           containedRef.name(),
                                           containedRef.version() );

                        org.jacorb.util.Debug.output(2, "Interface " + full_name +
                                                 " loads "+ containedRef.name() );

                        contained.put( containedRef.name() , containedRef );
                        containedLocals.put( containedRef.name(), containedObject );

                        if( containedObject instanceof ContainerType )
                            ((ContainerType)containedObject).loadContents();

                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        loaded = true;
        org.jacorb.util.Debug.output(2, "Interface " + name +  " loaded ]");
    }

    void define()
    {
        if ( ! loaded)
        {
            throw new INTF_REPOS ("Interface " + name +  " not loaded!");
        }
        org.jacorb.util.Debug.output(2, "Interface " + name +  " defining... ]");
        org.jacorb.util.Debug.output(2, "Interface " +name+ " loads attributes/ops");

        Vector ops = new Vector();
        Vector atts = new Vector();
        Hashtable irInfo= null;

        Class irHelperClass = null;
        try
        {
            irHelperClass =
                RepositoryImpl.loader.loadClass( theClass.getName() + "IRHelper");
            irInfo = (Hashtable)irHelperClass.getDeclaredField("irInfo").get(null);
        }
        catch( ClassNotFoundException e )
        {
            org.jacorb.util.Debug.output(1, "!! No IR helper class for interface " +
                                     theClass.getName());
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        Method methods[] = signatureClass.getDeclaredMethods();

        for( int i = 0; i < methods.length; i++ )
        {
            Object value = irInfo.get(methods[i].getName());
            if( value == null || !((String)value).startsWith("attribute"))
            {
                ops.addElement(
                     new OperationDef( methods[i], theClass, irHelperClass, myReference ));
            }
            else
            {
                if( ((String)value).startsWith("attribute") )
                {
                    String attrDescr = (String)value;

                    if( methods[i].getReturnType() == Void.class )
                        continue;

                    int idx = attrDescr.indexOf('-');
                    String attrTypeName =
                        attrDescr.substring( attrDescr.indexOf(";")+1);

                    atts.addElement(
                        new AttributeDef( methods[i],
                                          attrTypeName,
                                          ( idx > 0 ?
                                            org.omg.CORBA.AttributeMode.ATTR_NORMAL :
                                            org.omg.CORBA.AttributeMode.ATTR_READONLY  ),
                                          myReference,
                                          containing_repository ));
                }
            }
        }
        org.jacorb.util.Debug.output(2, "Interface " +name+ " defines ops");

        op_defs = new OperationDef[ ops.size() ];
        ops.copyInto( op_defs );
        for( int i = 0; i < op_defs.length; i++ )
        {
            op_defs[i].move( myReference , op_defs[i].name(), version );
            containedLocals.put( op_defs[i].name(), op_defs[i] );

            try
            {
                org.omg.CORBA.OperationDef operationRef =
                    org.omg.CORBA.OperationDefHelper.narrow(
                         RepositoryImpl.poa.servant_to_reference(
                                 new  org.omg.CORBA.OperationDefPOATie( op_defs[i] )));
                contained.put( op_defs[i].name(), operationRef ) ;
                op_defs[i].setReference(operationRef);
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
        org.jacorb.util.Debug.output(2, "Interface " +name+ " defines attributes");

        att_defs = new AttributeDef[ atts.size() ];
        atts.copyInto( att_defs );

        for( int i = 0; i < att_defs.length; i++ )
        {
            att_defs[i].move( myReference , att_defs[i].name(), version );
            containedLocals.put( att_defs[i].name(), att_defs[i] );
            try
            {
                org.omg.CORBA.AttributeDef attribute =
                    org.omg.CORBA.AttributeDefHelper.narrow(
                          RepositoryImpl.poa.servant_to_reference(
                               new  org.omg.CORBA.AttributeDefPOATie( att_defs[i] )));
                contained.put( att_defs[i].name(), attribute );
                att_defs[i].setReference( attribute );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }

        /* constants */
        org.jacorb.util.Debug.output(2, "Interface " + name + " defines constants");

        Field[] fields = theClass.getDeclaredFields();
        constant_defs = new ConstantDef[ fields.length ];
        for( int i = 0; i < constant_defs.length; i++ )
        {
            constant_defs[i] = new ConstantDef( fields[i],
                                                myReference,
                                                containing_repository );
            constant_defs[i].move( myReference , constant_defs[i].name(), version );
            containedLocals.put( constant_defs[i].name(), constant_defs[i] );
            try
            {
                org.omg.CORBA.ConstantDef constRef =
                    org.omg.CORBA.ConstantDefHelper.narrow(
                         RepositoryImpl.poa.servant_to_reference(
                             new  org.omg.CORBA.ConstantDefPOATie( constant_defs[i] )));

                contained.put( constant_defs[i].name(), constRef ) ;
                constant_defs[i].setReference(constRef);
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }

        for( Enumeration e = containedLocals.elements();
             e.hasMoreElements();
             ((IRObject)e.nextElement()).define())
            ;


        /* get base interfaces */
        Class class_interfaces [] = theClass.getInterfaces();
        Hashtable si = new Hashtable();

        Class objectClass = null;
        try
        {
            objectClass = RepositoryImpl.loader.loadClass( "org.omg.CORBA.Object");
        }
        catch( ClassNotFoundException cnfe )
        {}

        for( int i = 0; i < class_interfaces.length; i++ )
        {
            if( objectClass.isAssignableFrom( class_interfaces[i] ) &&
                !class_interfaces[i].getName().equals("org.omg.CORBA.Object") )
            {
                si.put( class_interfaces[i], "");
            }
        }

        Enumeration e = si.keys();
        base_names = new String[ si.size() ];
        int i = 0;
        Vector v = new Vector();
        while( e.hasMoreElements() )
        {
            try
            {
                Class baseClass = (Class)e.nextElement();
                base_names[i] = baseClass.getName();
                Class helperClass =
                    RepositoryImpl.loader.loadClass( base_names[i] + "Helper");
                String baseId =
                    (String)helperClass.getDeclaredMethod( "id", null).invoke(null,null);
                org.omg.CORBA.InterfaceDef base_interface =
                    org.omg.CORBA.InterfaceDefHelper.narrow(
                        containing_repository.lookup_id( baseId ));
                if( base_interface == null )
                {
                    org.jacorb.util.Debug.output( 1,
                                                  "Base interface def " +
                                                  baseId + " is null!!!");
                }
                else
                {
                    v.addElement( base_interface );
                }

                i++;
            }
            catch( Exception exc )
            {
                exc.printStackTrace();
            }
        }
        base_interfaces = new org.omg.CORBA.InterfaceDef[ v.size() ];
        v.copyInto( base_interfaces );

        defined = true;
        org.jacorb.util.Debug.output(2, "Interface " + name +  " defined ]");
    }


    public boolean is_abstract()
    {
        return false;
    }

    public void is_abstract(boolean arg)
    {
    }

    /**
     * @returns  an array containing interface definitions of the superclass and
     * the interfaces extended by this class. Has length 0 if this class
     * is Object.
     */

    public org.omg.CORBA.InterfaceDef[] base_interfaces()
    {
        return base_interfaces;
    }

    public FullInterfaceDescription describe_interface()
    {
        if ( ! defined)
        {
            throw new INTF_REPOS ("InterfaceDef " + name + " not defined.");
        }

        if( fullDescription == null )
        {
            String def_in = "IDL:Global:1.0";
            if( defined_in instanceof org.omg.CORBA.Contained )
                def_in = ((org.omg.CORBA.Contained)defined_in).id();

            /* before  assembling descriptions, get hold  of all super
               types' FullInterfaceDescriptions */

            FullInterfaceDescription [] baseDescriptions =
                new FullInterfaceDescription[ base_interfaces().length ];

            for( int b = 0; b < base_interfaces.length; b++ )
            {
                baseDescriptions[b] = base_interfaces[b].describe_interface();
            }

            /* build operation descriptions */

            Hashtable ops = new Hashtable();

            for( int c = 0; c < op_defs.length; c++ )
            {
                OperationDescription operation = op_defs[c].describe_operation();
                ops.put( operation.name, operation );
            }

            /* get operation descriptions from super types, potentially duplicate
               descriptions due to diamond inheritance are removed by hashing
             */

            for( int baseOps = 0; baseOps < baseDescriptions.length; baseOps++ )
            {
                for( int bbaseOps = 0;
                     bbaseOps < baseDescriptions[baseOps].operations.length;
                     bbaseOps++ )
                {
                    OperationDescription base_op =
                        baseDescriptions[baseOps].operations[bbaseOps];

                    if( !ops.containsKey( base_op.name ))
                        ops.put( base_op.name, base_op );
                }
            }

            operations = new OperationDescription[ ops.size() ];

            int opsCount = 0;
            for( Enumeration e = ops.elements(); e.hasMoreElements(); opsCount++ )
            {
                operations[ opsCount ] = (OperationDescription)e.nextElement();
            }
            ops.clear();

            /* build attribute descriptions */
            Hashtable atts = new Hashtable();

            for( int a = 0; a < att_defs.length; a++ )
            {
                AttributeDescription att = att_defs[a].describe_attribute();
                atts.put( att.name, att );
            }

            /* get attribute descriptions from super types */

            for( int baseAtts = 0; baseAtts < baseDescriptions.length; baseAtts++ )
            {
                for( int bbaseAtts = 0;
                     bbaseAtts < baseDescriptions[ baseAtts ].attributes.length;
                     bbaseAtts++ )
                {
                    AttributeDescription base_att =
                        baseDescriptions[ baseAtts ].attributes[ bbaseAtts ];

                    if( !atts.containsKey( base_att.name ))
                        atts.put( base_att.name, base_att );
                }
            }

            attributes = new AttributeDescription[ atts.size() ];

            int attsCount = 0;
            for( Enumeration e = atts.elements(); e.hasMoreElements(); attsCount++ )
            {
                attributes[ attsCount ] = (AttributeDescription)e.nextElement();
            }
            atts.clear();

            /* build constant descriptions */

            constants = new org.omg.CORBA.ConstantDescription[ constant_defs.length ];
            for( int b = 0; b < constant_defs.length; b++ )
            {
                constants[b] = constant_defs[b].describe_constant();
            }

            if (operations == null)
            {
                throw new INTF_REPOS ("operations null!");
            }
            if (attributes == null)
            {
                throw new INTF_REPOS ("attributes null!");
            }

            fullDescription =
                new FullInterfaceDescription( name,
                                              id,
                                              def_in,
                                              version,
                                              operations,
                                              attributes,
                                              base_names,
                                              typeCode,
                                              is_abstract );
        }
        return fullDescription;
    }


    public boolean is_a( String interface_id )
    {
        Debug.output( 2, "Is interface " + id() + "  a " + interface_id + "?" );
        if( id().equals( interface_id ))
            return true;

        org.omg.CORBA.InterfaceDef[] bases = base_interfaces();
        for( int i = 0; i < bases.length; i++ )
        {
            if( bases[i].is_a( interface_id ))
                return true;
            if( bases[i].id().equals("IDL:omg.org/CORBA/Object:1.0"))
                continue;
        }
        Debug.output( 2, "Interface " + id() + " is not a " + interface_id );
        return false;
    }

    // write methods on an InterfaceDef,
    // these are not supported at the moment !!

    public void base_interfaces( org.omg.CORBA.InterfaceDef[] a )
    {
        throw new INTF_REPOS(ErrorMsg.IR_Not_Implemented,
                                           org.omg.CORBA.CompletionStatus.COMPLETED_NO);
    }

    public org.omg.CORBA.AttributeDef create_attribute( String id,
                                                        String name,
                                                        String version,
                                                        IDLType type,
                                                        org.omg.CORBA.AttributeMode mode
                                                        )
    {
        throw new INTF_REPOS( ErrorMsg.IR_Not_Implemented,
                                            org.omg.CORBA.CompletionStatus.COMPLETED_NO);
    }

    public org.omg.CORBA.OperationDef create_operation( String id,
                                                       String name,
                                                       String version,
                                                       org.omg.CORBA.IDLType result,
                                                       org.omg.CORBA.OperationMode mode,
                                                       org.omg.CORBA.ParameterDescription[] params,
                                                       org.omg.CORBA.ExceptionDef[] exceptions,
                                                       String[] contexts
                                                       )
    {
        throw new INTF_REPOS( ErrorMsg.IR_Not_Implemented,
                                            org.omg.CORBA.CompletionStatus.COMPLETED_NO);
    }


    // from org.omg.CORBA.Container

    public org.omg.CORBA.Contained lookup( String scopedname )
    {
        org.jacorb.util.Debug.output(2,"Interface " + this.name +
                                     " lookup " + scopedname );

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
                if(  top instanceof org.omg.CORBA.Container)
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

    public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind limit_type,
                                              boolean exclude_inherited)
    {
        if ( ! defined)
        {
            throw new INTF_REPOS ("InterfaceDef " + name + " not defined.");
        }

        Hashtable limited = new Hashtable();

        // analog constants, exceptions etc.

        for( Enumeration e = contained.elements(); e.hasMoreElements();  )
        {
            org.omg.CORBA.Contained c = (org.omg.CORBA.Contained)e.nextElement();
            if( limit_type.value() == org.omg.CORBA.DefinitionKind._dk_all ||
                limit_type.value() == c.def_kind().value() )
            {
                limited.put( c, "" );
            }
        }

        org.omg.CORBA.Contained[] c =
            new org.omg.CORBA.Contained[limited.size()];

        int i;
        Enumeration e;
        for( e = limited.keys(), i=0 ; e.hasMoreElements(); i++ )
            c[i] = (org.omg.CORBA.Contained)e.nextElement();
        return c;

    }

    public org.omg.CORBA.Contained[] lookup_name(String search_name,
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
            // search up to a specific depth or indefinitely
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


    public Description[] describe_contents(org.omg.CORBA.DefinitionKind limit_type,
                                           boolean exclude_inherited,
                                           int max_returned_objs)
    {
        return null;
    }


    // write interface not supported!

    public org.omg.CORBA.ModuleDef create_module( String id,
                                                  String name,
                                                  String version)
    {
        return null;
    }

    public org.omg.CORBA.ConstantDef create_constant( /*RepositoryId*/ String id,
                                                      /*Identifier*/ String name,
                                                      /*VersionSpec*/ String version,
                                                      IDLType type, org.omg.CORBA.Any value)
    {
        return null;
    }

    public org.omg.CORBA.StructDef create_struct(/*RepositoryId*/ String id,
                                                 /*Identifier*/ String name,
                                                 /*VersionSpec*/ String version,
                                                 /*StructMemberSeq*/ org.omg.CORBA.StructMember[] members)
    {
        return null;
    }

    public org.omg.CORBA.UnionDef create_union( /*RepositoryId*/ String id,
                                                /*Identifier*/ String name,
                                                /*VersionSpec*/ String version,
                                                org.omg.CORBA.IDLType discriminator_type,
                                                /*UnionMemberSeq*/ org.omg.CORBA.UnionMember[] members)
    {
        return null;
    }

    public org.omg.CORBA.EnumDef create_enum(/*RepositoryId*/ String id,
                                             /*Identifier*/ String name,
                                             /*VersionSpec*/ String version,
                                             /*EnumMemberSeq*/ /*Identifier*/ String[] members)
    {
        return null;
    }

    public org.omg.CORBA.AliasDef create_alias(/*RepositoryId*/ String id,
                                               /*Identifier*/ String name,
                                               /*VersionSpec*/ String version,
                                               org.omg.CORBA.IDLType original_type)
    {
        return null;
    }


    /**
     * not supported
     */

    public org.omg.CORBA.ExceptionDef create_exception(String id,
                                                       String name ,
                                                       String version,
                                                       org.omg.CORBA.StructMember[] member )
    {
        return null;
    }

    /**
     * not supported
     */

    public org.omg.CORBA.InterfaceDef create_interface(/*RepositoryId*/ String id,
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

    public org.omg.CORBA.ValueBoxDef create_value_box(String id,
                                                      String name,
                                                      String version,
                                                      org.omg.CORBA.IDLType type)
    {
        return null;
    }


    /**
     * not supported
     */

    public  org.omg.CORBA.ValueDef create_value(String id,
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
        return null;
    }


    /**
     * not supported
     */

    public org.omg.CORBA.NativeDef create_native(String id,
                                                 String name,
                                                 String version)
    {
        return null;
    }

    // from Contained

    public org.omg.CORBA.ContainedPackage.Description describe()
    {
        if ( ! defined)
        {
            throw new INTF_REPOS ("InterfaceDef " + name + " not defined.");
        }

        org.omg.CORBA.Any a = orb.create_any();

        String def_in = null;

        if( myContainer != null )
            def_in = "Global";
        else
            def_in = myContainer.id();

        org.omg.CORBA.InterfaceDescriptionHelper.insert( a,
                                new org.omg.CORBA.InterfaceDescription( name,
                                                                        id,
                                                                        def_in,
                                                                        version,
                                                                        base_names,
                                                                        false )
                );
        return new org.omg.CORBA.ContainedPackage.Description(
                         org.omg.CORBA.DefinitionKind.dk_Interface, a);
    }

    // from IRObject

    public void destroy()
    {
        containedLocals.clear();
        contained.clear();
    }

    // from IDLType

    public org.omg.CORBA.TypeCode type()
    {
        return typeCode;
    }
}
