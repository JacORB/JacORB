package org.jacorb.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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
import org.omg.CORBA.TypeCode;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class ORBSingleton
    extends org.omg.CORBA_2_3.ORB
{
    /* factory methods: */

    public org.omg.CORBA.Any create_any()
    {
        return new org.jacorb.orb.Any(this);
    } 

    /* TypeCode factory section */

    public TypeCode create_alias_tc( String id, 
                                     String name, 
                                     TypeCode original_type)
    {
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_alias, 
                                            id, name, original_type);
    }

    public TypeCode create_array_tc( int length, TypeCode element_type)
    {
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_array,
                                        length,
                                        element_type);
    }


    public TypeCode create_enum_tc( String id, 
                                    String name, 
                                    String[] members)
    {
        return new org.jacorb.orb.TypeCode( id, name, members);
    }

    public TypeCode create_exception_tc( String id, 
                                         String name, 
                                         org.omg.CORBA.StructMember[] members)
    {
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_except,
                                        id,
                                        name,
                                        members);
    }

    public TypeCode create_interface_tc( String id, String name)
    {
        return new org.jacorb.orb.TypeCode( id, name);
    }

    public org.omg.CORBA.TypeCode create_fixed_tc( short digits, 
                                                   short scale)
    {
        return new org.jacorb.orb.TypeCode(digits, scale);
    }

    public org.omg.CORBA.TypeCode create_recursive_tc( String id ) 
    {
        return new org.jacorb.orb.TypeCode( id );
    }  
 
    /**
     * @deprecated Deprecated by CORBA 2.3 
     */

    public TypeCode create_recursive_sequence_tc( int bound, int offset)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public TypeCode create_sequence_tc( int bound, TypeCode element_type)
    {
        org.jacorb.orb.TypeCode tc = 
            new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_sequence,
                                     bound, 
                                     element_type);
        return tc;
    }

    public TypeCode create_string_tc(int bound)
    {
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_string, bound );
    }

    public TypeCode create_wstring_tc(int bound)
    {
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_wstring, bound);
    }

    public TypeCode create_struct_tc(String id, 
                                     String name,
                                     org.omg.CORBA.StructMember[] members)
    {
        org.jacorb.orb.TypeCode tc = 
            new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_struct, 
                                     id, 
                                     name, 
                                     members);
        //        tc.resolve_recursion();
        return tc;
    }

    public TypeCode create_union_tc( String id, 
                                     String name, 
                                     TypeCode discriminator_type, 
                                     org.omg.CORBA.UnionMember[] members)
    {
        org.jacorb.orb.TypeCode tc = 
            new org.jacorb.orb.TypeCode( id, 
                                     name,
                                     discriminator_type, 
                                     members);
        // tc.resolve_recursion();
        return tc;
    }

    public TypeCode get_primitive_tc(org.omg.CORBA.TCKind tcKind)
    {
        return org.jacorb.orb.TypeCode.get_primitive_tc( tcKind.value() );
    }

    public org.omg.CORBA.TypeCode create_value_tc(String id,
                                    String name,
                                    short type_modifier,
                                    TypeCode concrete_base,
                                    org.omg.CORBA.ValueMember[] members) {
        return new org.jacorb.orb.TypeCode (id, name, type_modifier,
                                            concrete_base, members);
    }

    public org.omg.CORBA.TypeCode create_value_box_tc(String id,
                                    String name,
                                    TypeCode boxed_type) {
        return new org.jacorb.orb.TypeCode (org.omg.CORBA.TCKind._tk_value_box,
                                            id, name, boxed_type);
    }

    /* DII helper methods */

    public org.omg.CORBA.ExceptionList create_exception_list()
    {
        return new org.jacorb.orb.dii.ExceptionList();
    }

    public org.omg.CORBA.NVList create_list(int count)
    {
        return new org.jacorb.orb.NVList(this);
    }

    public org.omg.CORBA.NamedValue create_named_value( String name,
                                                        org.omg.CORBA.Any value, 
                                                        int flags )
    {
        return new org.jacorb.orb.NamedValue( name, value, flags );
    }

    public org.omg.CORBA.NVList create_operation_list(org.omg.CORBA.OperationDef oper)
    {
        return null;
    }


    /* not allowed on the singleton: */

    public org.omg.CORBA.Object string_to_object(String str) 
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public  org.omg.CORBA.Environment create_environment()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }
 
    public  org.omg.CORBA.ContextList create_context_list()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public org.omg.CORBA.Current get_current()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public  org.omg.CORBA.Context get_default_context()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }
    public org.omg.CORBA.Request get_next_response()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public String[] list_initial_services()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public String object_to_string( org.omg.CORBA.Object obj)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public  boolean poll_next_response()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public org.omg.CORBA.Object resolve_initial_references(String identifier) 
        throws org.omg.CORBA.ORBPackage.InvalidName 
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public  void send_multiple_requests_deferred(org.omg.CORBA.Request[] req)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public  void send_multiple_requests_oneway(org.omg.CORBA.Request[] req)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    protected void set_parameters(String[] args, java.util.Properties props)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    protected void set_parameters(java.applet.Applet app, java.util.Properties  props)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public void run() 
    {   
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public void shutdown(boolean wait_for_completion) 
    {    
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }      

    public boolean work_pending() 
    {     
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }

    public void perform_work() 
    {     
        throw new org.omg.CORBA.NO_IMPLEMENT("The Singleton ORB only permits factory methods");
    }


}






