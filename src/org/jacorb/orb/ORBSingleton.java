package org.jacorb.orb;

/*
 *        JacORB  - a free Java ORB
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

import java.util.*;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_TYPECODE;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.CompletionStatus;

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
   
    /** 
     * Determine if a character is ok to start an id. 
     * (Note that '_' is allowed here - it might have
     * been inserted by the IDL compiler to avoid clashes
     * with reserved Java identifiers )
     * @param ch the character in question.
     */

    final protected static boolean legalStartChar(int ch)
    {
        return
           ( ch >= 'a' &&  ch <= 'z') || (ch == '_') || 
           ( ch >= 'A' && ch <= 'Z');
    }


    /**
     * Determine if a character is ok for the middle of an id.
     * @param ch the character in question. 
     */
    final protected static boolean legalNameChar(int ch)
    {
        return
           legalStartChar(ch) ||  
           (ch == '_') 
           || (ch >= '0' && ch <= '9');
    }


    /**
     * check that a name is a legal IDL name
     * (cf. CORBA 2.4 chapter 10, section 7.3
     * @throw org.omg.CORBA.BAD_PARAM
     */

    private void checkTCName (String name)
        throws BAD_PARAM
    {
        if (name != null)
        {
            // note that legal names can be empty
            if( name.length() > 0 )
            {
                // check that name begins with an ASCII char
                if( !legalStartChar( name.charAt(0)) )
                {
                    throw new BAD_PARAM("Illegal IDL name: " + name, 15, 
                                        CompletionStatus.COMPLETED_NO );    
                }
                for( int i = 0; i < name.length(); i++ )
                {
                    if( ! legalNameChar( name.charAt(i) ))
                        throw new BAD_PARAM("Illegal IDL name: " + name, 15, 
                                            CompletionStatus.COMPLETED_NO );  
                }
            }       
        }
        else
        {
            throw new BAD_PARAM("Illegal null IDL name", 15, 
                                CompletionStatus.COMPLETED_NO );    
        }
    }

    /**
     * check that a repository ID is legal
     * (cf. CORBA 2.4 chapter 10, section 7.3
     * @throw org.omg.CORBA.BAD_PARAM
     */

    private void checkTCRepositoryId( String repId )
        throws BAD_PARAM
    {
        if( repId == null || repId.indexOf( ':' ) < 0 )
        {
            throw new BAD_PARAM("Illegal Repository ID: " + repId, 
                                16, CompletionStatus.COMPLETED_NO );    
        }
    }

    /**
     * check that a type is a legal member type
     * (cf. CORBA 2.4 chapter 10, section 7.3
     * @throw org.omg.CORBA.BAD_PARAM
     */

    private void checkTCMemberType( TypeCode tc )
        throws BAD_TYPECODE
    {
        if( !(((org.jacorb.orb.TypeCode) tc).is_recursive ()) &&
            (tc == null || 
             tc.kind().value() == TCKind._tk_null ||
             tc.kind().value() == TCKind._tk_void ||
             tc.kind().value() == TCKind._tk_except
             )
            )
        {
            throw new BAD_TYPECODE("Illegal member tc", 2, 
                                   CompletionStatus.COMPLETED_NO );    
        }
    }



    /* TypeCode factory section */


    public TypeCode create_alias_tc( String id, 
                                     String name, 
                                     TypeCode original_type)
    {
        checkTCRepositoryId( id );
        checkTCName( name );
        checkTCMemberType( original_type );
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_alias, 
                                            id, name, original_type);
    }

    public TypeCode create_array_tc( int length, TypeCode element_type)
    {
        checkTCMemberType( element_type );
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_array,
                                            length,
                                            element_type);
    }

    /**
     * create an enum TypeCode
     */

    public TypeCode create_enum_tc( String id, 
                                    String name, 
                                    String[] members)
    {
        checkTCRepositoryId( id );
        checkTCName( name );

        // check that member names are legal and unique
        Hashtable names = new Hashtable() ;
        for( int i = 0; i < members.length; i++ )
        {
            boolean fault = false;
            try
            {
                checkTCName( members[i] );
            }
            catch( BAD_PARAM bp )
            {
                fault = true;
            }
            if( names.containsKey( members[i] ) || fault )
            {
                throw new BAD_PARAM("Illegal enum member name: " + members[i], 
                                    17, CompletionStatus.COMPLETED_NO );    
            }
            names.put( members[i], "" );
        }
        names.clear();

        return new org.jacorb.orb.TypeCode( id, name, members);
    }

    /**
     * create an exception TypeCode
     */

    public TypeCode create_exception_tc( String id, 
                                         String name, 
                                         org.omg.CORBA.StructMember[] members)
    {
        checkTCRepositoryId( id );
        checkTCName( name );

        // check that member names are legal and unique
        Hashtable names = new Hashtable() ;
        for( int i = 0; i < members.length; i++ )
        {
            checkTCMemberType( members[i].type );
            boolean fault = false;
            try
            {
                checkTCName( members[i].name );
            }
            catch( BAD_PARAM bp )
            {
                fault = true;
            }
            if( names.containsKey( members[i].name ) || fault )
            {
                throw new BAD_PARAM("Illegal exception member name: " + 
                                    members[i].name, 
                                    17, CompletionStatus.COMPLETED_NO );    
            }
            names.put( members[i].name, "" );
        }
        names.clear();


        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_except,
                                            id,
                                            name,
                                            members);
    }

    public TypeCode create_interface_tc( String id, String name)
    {
        checkTCRepositoryId( id );
        checkTCName( name );
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_objref, 
					   id, name);
    }

    public org.omg.CORBA.TypeCode create_fixed_tc( short digits, 
                                                   short scale)
    {
        if (digits <= 0 || scale < 0 || scale > digits)
        {
            throw new org.omg.CORBA.BAD_PARAM
               ("Invalid combination of digits and scale factor");
        }
        return new org.jacorb.orb.TypeCode(digits, scale);
    }

    public org.omg.CORBA.TypeCode create_recursive_tc( String id ) 
    {
        checkTCRepositoryId( id );
        return new org.jacorb.orb.TypeCode( id );
    }  
 
    public TypeCode create_sequence_tc( int bound, TypeCode element_type)
    {
        checkTCMemberType( element_type );
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

    /** 
     * create a struct TypeCode
     */

    public TypeCode create_struct_tc(String id, 
                                     String name,
                                     org.omg.CORBA.StructMember[] members)
    {
        checkTCRepositoryId( id );
        checkTCName( name );

        // check that member names are legal and unique
        Hashtable names = new Hashtable() ;
        for( int i = 0; i < members.length; i++ )
        {
            checkTCMemberType( members[i].type );
            boolean fault = false;
            try
            {
                checkTCName( members[i].name );
            }
            catch( BAD_PARAM bp )
            {
                fault = true;
            }
            if( names.containsKey( members[i].name ) || fault )
            {
                throw new BAD_PARAM("Illegal struct member name: " + 
                                    members[i].name + (fault? " (Bad PARAM) ": "" ),
                                    17, CompletionStatus.COMPLETED_NO );    
            }
            names.put( members[i].name, "" );
        }
        names.clear();

        org.jacorb.orb.TypeCode tc = 
            new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_struct, 
                                         id, 
                                         name, 
                                         members);

        // resolve any recursive references to this TypeCode in its members
        tc.resolveRecursion();
        return tc;
    }

    /**
     * create a union TypeCode
     */ 

    public TypeCode create_union_tc( String id, 
                                     String name, 
                                     TypeCode discriminator_type, 
                                     org.omg.CORBA.UnionMember[] members)
    {
        checkTCRepositoryId( id );
        checkTCName( name );

        // check discriminator type

        org.jacorb.orb.TypeCode disc_tc =
          ((org.jacorb.orb.TypeCode) discriminator_type).originalType ();

        if (disc_tc == null ||
            !(disc_tc.kind().value() == TCKind._tk_short ||
              disc_tc.kind().value() == TCKind._tk_long ||
              disc_tc.kind().value() == TCKind._tk_longlong ||
              disc_tc.kind().value() == TCKind._tk_ushort ||
              disc_tc.kind().value() == TCKind._tk_ulong  ||
              disc_tc.kind().value() == TCKind._tk_ulonglong ||
              disc_tc.kind().value() == TCKind._tk_char ||
              disc_tc.kind().value() == TCKind._tk_boolean ||
              disc_tc.kind().value() == TCKind._tk_enum
              )
            )
        {
            throw new BAD_PARAM("Illegal union discriminator type",
                                20, CompletionStatus.COMPLETED_NO );
        }

        // check that member names are legal (they do not need to be unique)
        
        for( int i = 0; i < members.length; i++ )
        {
            checkTCMemberType( members[i].type );
            try
            {
                checkTCName( members[i].name );
            }
            catch( BAD_PARAM bp )
            {
                throw new BAD_PARAM("Illegal union member name: " + 
                                    members[i].name, 
                                    17, CompletionStatus.COMPLETED_NO );    
            }

            // check that member type matches discriminator type or is default

            org.omg.CORBA.Any label = members[i].label;
            if (! discriminator_type.equivalent( label.type () ) &&
                ! ( label.type().kind().value() == TCKind._tk_octet &&
                    label.extract_octet() == (byte)0
                    )
                )
            {
                throw new BAD_PARAM("Label type does not match discriminator type",
                                    19, 
                                    CompletionStatus.COMPLETED_NO );
            }

            // check that member labels are unique
            
            for( int j = 0; j < i; j++ )
            {
                if( label.equal( members[j].label ))
                {
                    throw new BAD_PARAM("Duplicate union case label", 
                                        18, 
                                        CompletionStatus.COMPLETED_NO );   
                }
            }
        }
        
        org.jacorb.orb.TypeCode tc = 
           new org.jacorb.orb.TypeCode( id, 
                                        name,
                                        discriminator_type, 
                                        members);

        // resolve any recursive references to this TypeCode in its members
        tc.resolveRecursion();
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
                                                  org.omg.CORBA.ValueMember[] members) 
    {
        checkTCRepositoryId( id );
        checkTCName( name );
        return new org.jacorb.orb.TypeCode (id, 
                                            name, 
                                            type_modifier,
                                            concrete_base, 
                                            members);
    }

    public org.omg.CORBA.TypeCode create_value_box_tc(String id,
                                                      String name,
                                                      TypeCode boxed_type) 
    {
        checkTCRepositoryId( id );
        checkTCName( name );
        return new org.jacorb.orb.TypeCode (org.omg.CORBA.TCKind._tk_value_box,
                                            id,
                                            name,
                                            boxed_type);
    }

    public org.omg.CORBA.TypeCode create_abstract_interface_tc(String id,
                                                               String name) 
    {
       checkTCRepositoryId( id );
       checkTCName( name );
       return new org.jacorb.orb.TypeCode (org.omg.CORBA.TCKind._tk_abstract_interface,
                                           id, 
                                           name);
    }

    public org.omg.CORBA.TypeCode create_local_interface_tc(String id,
                                                            String name) 
    {
       checkTCRepositoryId( id );
       checkTCName( name );
       return new org.jacorb.orb.TypeCode (org.omg.CORBA.TCKind._tk_local_interface,
                                           id, 
                                           name);
    }
   
    public org.omg.CORBA.TypeCode create_native_tc(String id,
                                                   String name) 
    {
       checkTCRepositoryId( id );
       checkTCName( name );
       return new org.jacorb.orb.TypeCode (org.omg.CORBA.TCKind._tk_native,
                                           id, 
                                           name);
    }

   /* not allowed on the singleton: */

    public org.omg.CORBA.ExceptionList create_exception_list()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public org.omg.CORBA.NVList create_list (int count)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public org.omg.CORBA.NamedValue create_named_value
        (String name, org.omg.CORBA.Any value, int flags)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public org.omg.CORBA.NVList create_operation_list
        (org.omg.CORBA.OperationDef oper)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public org.omg.CORBA.NVList create_operation_list
        (org.omg.CORBA.Object obj)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public org.omg.CORBA.Object string_to_object(String str) 
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public  org.omg.CORBA.Environment create_environment()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }
 
    public  org.omg.CORBA.ContextList create_context_list()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public org.omg.CORBA.Current get_current()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public  org.omg.CORBA.Context get_default_context()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public org.omg.CORBA.Request get_next_response()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public String[] list_initial_services()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public String object_to_string( org.omg.CORBA.Object obj)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public  boolean poll_next_response()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public org.omg.CORBA.Object resolve_initial_references(String identifier) 
        throws org.omg.CORBA.ORBPackage.InvalidName 
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public  void send_multiple_requests_deferred(org.omg.CORBA.Request[] req)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public  void send_multiple_requests_oneway(org.omg.CORBA.Request[] req)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    protected void set_parameters(String[] args, java.util.Properties props)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    protected void set_parameters(java.applet.Applet app, java.util.Properties  props)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public void run() 
    {   
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public void shutdown(boolean wait_for_completion) 
    {    
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }      

    public boolean work_pending() 
    {     
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }

    public void perform_work() 
    {     
        throw new org.omg.CORBA.NO_IMPLEMENT ("The Singleton ORB only permits factory methods");
    }
}
