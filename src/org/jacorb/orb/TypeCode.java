package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
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

import org.omg.CORBA.TCKind;
import org.omg.CORBA.ValueMember;

import org.jacorb.ir.RepositoryID;

import java.util.*;
import java.lang.reflect.*;


/**
 * JacORB implementation of CORBA TypeCodes
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$    
 */
 
public class TypeCode 
    extends org.omg.CORBA.TypeCode
{
    private int kind = -1;

    private String id = null;
    private String name = null;

    private int         member_count = 0;
    private String []   member_name = null;
    private TypeCode [] member_type = null;
    private short []    member_visibility = null;
    private Any []      member_label = null;
    private short       value_modifier = 0;

    private TypeCode    discriminator_type = null;
    private int         default_index = -1;
    private int         length = -1;
    private TypeCode    content_type = null;

    /** for fixed point types */
    private short       scale;
    private short       digits;

    /** if this TC is recursive... */
    private boolean     recursive = false;

    private static boolean     class_init = false;
    private static TypeCode[]  primitive_tcs = new TypeCode[33];

    /**
     * Maps the java.lang.Class objects for primitive types to
     * their corresponding TypeCode objects.
     */
    private static Map primitive_tcs_map = new HashMap(); 

    static
    {
        /** statically create primitive TypeCodes for fast lookup */
        for( int i = 0; i < 14; i++ )
        {
            primitive_tcs[i] = new TypeCode(i);
        }   
        primitive_tcs[18] = new TypeCode( 18, 0 );

        for( int i = 23; i < 29; i++ )
        {
            primitive_tcs[i] = new TypeCode(i);
        }
        put_primitive_tcs (Boolean.TYPE,   TCKind._tk_boolean);
        put_primitive_tcs (Character.TYPE, TCKind._tk_wchar);
        put_primitive_tcs (Byte.TYPE,      TCKind._tk_octet);
        put_primitive_tcs (Short.TYPE,     TCKind._tk_short);
        put_primitive_tcs (Integer.TYPE,   TCKind._tk_long);
        put_primitive_tcs (Long.TYPE,      TCKind._tk_longlong);
        put_primitive_tcs (Float.TYPE,     TCKind._tk_float);
        put_primitive_tcs (Double.TYPE,    TCKind._tk_double);
    }

    /**
     * Internal method for populating `primitive_tcs_map'. 
     */
    private static void put_primitive_tcs (Class clz, int kind)
    {
        primitive_tcs_map.put (clz, primitive_tcs[kind]);
    }

    /**
     * Constructor for primitive types, called only from
     * static initializer and org.jacorb.ir.TypeCodeUtil
     */

    public TypeCode( int _kind )
    {
        kind = _kind;
    }

    /**
     * @returns TypeCode with integer kind "kind", 
     * null if not a primitive TypeCode
     */

    static TypeCode get_primitive_tc( int _kind )
    {
        org.jacorb.util.Debug.assert( primitive_tcs[_kind] != null, 
                                      "No primitive TypeCode for kind " + _kind);
        return primitive_tcs[_kind];
    }

    public boolean is_primitive()
    {
        return ( primitive_tcs[kind] != null );
    }


    /*
     * TypeCode constructors for every conceivable type follow.
     * These are called exclusively by the ORBSingleton, which is
     * the only legal TypeCode factory
     */


    /**
     * Constructor for recursive types
     */

    public TypeCode( String id )
    {
        this.id = id;      
        recursive = true;
        kind = -1;
    }

    /**
     * Constructor for tk_struct and tk_except
     */

    public TypeCode ( int _kind, 
                      String _id, 
                      String _name, 
                      org.omg.CORBA.StructMember[] _members)
    {
        kind = _kind;
        id =  _id;
        name = _name.replace('.','_'); // for orbixWeb Interop
        member_count = _members.length;
        member_name = new String[member_count];
        member_type = new TypeCode[member_count];
        for( int i = 0; i < member_count; i++ )
        {
            member_name[i] = _members[i].name;
            member_type[i] = (TypeCode)_members[i].type;
        }        
    }

    /**
     * Constructor for  tk_union
     */

    public TypeCode ( String _id, 
                      String _name, 
                      org.omg.CORBA.TypeCode _discriminator_type, 
                      org.omg.CORBA.UnionMember[] _members ) 
    {
        kind = TCKind._tk_union;
        id   =  _id ;
        name = _name.replace('.','_'); // for orbixWeb Interop
        discriminator_type = (TypeCode)_discriminator_type;

        member_count = _members.length;
        member_name  = new String[member_count];
        member_label = new Any[member_count];
        member_type  = new TypeCode[member_count];

        for( int i = 0; i < member_count; i++ )
        {
            member_name[i] = _members[i].name;
            member_label[i] = (Any)_members[i].label;
            if( member_label[i].kind().equals( TCKind.tk_octet ) &&
                ((Byte)member_label[i].value()).byteValue() == (byte)0 )
            {
                default_index = i;
            }
            member_type[i] = (TypeCode)_members[i].type;
        }
    }

    /**
     * Constructor for tk_enum
     */

    public TypeCode (java.lang.String _id, 
                     java.lang.String _name, 
                     java.lang.String[] _members) 
    {
        kind = TCKind._tk_enum;
        id = _id;
        name = _name.replace('.','_'); // for orbixWeb Interop
        member_count = _members.length;
        member_name = new String[member_count];

        for( int i = 0; i < member_count; i++ ) 
        {
            member_name[i] = _members[i];
        }
    }

    /**
     * Constructor for tk_alias, tk_value_box
     */  

    public TypeCode (int _kind, 
                     String _id, 
                     String _name, 
                     org.omg.CORBA.TypeCode _original_type)
    { 
        id = _id;
        kind = _kind;
        if( _name != null )
            name = _name.replace('.','_'); // for orbixWeb Interop
        else
            name = null;
        content_type = (TypeCode)_original_type;
    }

    /**
     * Constructor for tk_objref
     */  

    public TypeCode (java.lang.String _id, 
              java.lang.String _name)
    { 
        kind = TCKind._tk_objref;
        id   = _id;
        name = _name.replace('.','_'); // for orbixWeb Interop
    }

    /**
     * Constructor for tk_string, tk_wstring
     */  

    public TypeCode ( int _kind, int _bound) 
    {
        kind = _kind;
        length = _bound;
    }

    /**
     * Constructor for tk_sequence, tk_array
     */  

    public TypeCode (int _kind,
              int _bound, 
              org.omg.CORBA.TypeCode _element_type) 
    {
        kind = _kind;
        length = _bound;
        content_type = (TypeCode)_element_type;
        org.jacorb.util.Debug.assert( content_type != null, 
                                      "TypeCode.ctor, content_type null");

    }
    

    /**
     * Constructor for tk_fixed
     */  

    public TypeCode (short _digits, 
              short _scale) 
    {
        kind = TCKind._tk_fixed;
        digits = _digits;
        scale = _scale;
    }

    /**
     * Constructor for tk_value
     */
    public TypeCode(String id, String name, short type_modifier,
                    org.omg.CORBA.TypeCode concrete_base,
                    org.omg.CORBA.ValueMember[] members)
    {
        kind = TCKind._tk_value;
        this.id = id;
        this.name = name.replace('.','_'); // for orbixWeb Interop
        value_modifier = type_modifier;
        content_type = (TypeCode)concrete_base;

        member_count = members.length;
        member_name = new String[member_count];
        member_type = new TypeCode[member_count];
        member_visibility = new short[member_count];
        for( int i = 0; i < member_count; i++ )
        {
            member_name[i] = members[i].name;
            member_type[i] = (TypeCode)members[i].type;
            member_visibility[i] = members[i].access;
        }        
    }

    /**
     * check TypeCodes for structural equality
     */

    public boolean equal( org.omg.CORBA.TypeCode tc)
    {
        org.jacorb.util.Debug.output( 4, "Comparing this " + kind().value() + 
                                  " with tc " + tc.kind().value());

        if( kind().value() != tc.kind().value())
            return false;

        /* for primitive type codes, only kinds need be equal */

        if( kind().value() < 14 ||
            ( kind().value() > 22 && kind().value() < 29 ))
        {
            return true;
        }

        /* compare repository ids */

        try 
        {
            if( kind ==  TCKind._tk_array )
            {
                return ( length() == tc.length() && 
                         content_type().equal( tc.content_type()));
            }

            if( kind == TCKind._tk_sequence )
            {
                TypeCode this_tc = (org.jacorb.orb.TypeCode)content_type();
                TypeCode other_tc = (org.jacorb.orb.TypeCode)tc.content_type();

                return ( length() == tc.length() && 
                         this_tc.equal( other_tc ));
            }

            if( kind == TCKind._tk_objref || kind == TCKind._tk_struct || 
                kind == TCKind._tk_union  || kind == TCKind._tk_enum || 
                kind == TCKind._tk_alias  || kind == TCKind._tk_except ||
                kind == TCKind._tk_value  || kind == TCKind._tk_value_box)
            {
                if( ! id().equals( tc.id()) )
                    return false;
            }
            
            if (kind == TCKind._tk_value || kind == TCKind._tk_value_box)
            {
                if (name() != tc.name() || 
                    !content_type().equal(tc.content_type()))
                    return false;
            }

            if (kind == TCKind._tk_value)
            {
                if (member_count() != tc.member_count())
                    return false;

                for (int i = 0; i < member_count(); i++) {
                    if (!member_name(i).equals(tc.member_name(i)) ||
                        !member_type(i).equal(tc.member_type(i))  ||
                        member_visibility(i) != tc.member_visibility(i))
                        return false;
                }
            }

            if( kind == TCKind._tk_union )
            {
                if( !discriminator_type().equal( tc.discriminator_type()))
                    return false;
        
                if( default_index() != tc.default_index())
                    return false;
                
                if( member_count() != tc.member_count())
                    return false;

                for( int i = 0; i < member_count(); i++ )
                {
                    if( ! member_type(i).equal(tc.member_type(i)))
                        return false;

                    if( ! member_label(i).equals( tc.member_label(i)))
                        return false;
                }
            }

        }
        catch( org.omg.CORBA.TypeCodePackage.Bounds bs)
        {
            bs.printStackTrace();
            return false;
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind bk)
        {
            bk.printStackTrace();
            return false;
        }
        return true;
    }


    public org.omg.CORBA.TCKind kind()
    {
        return org.omg.CORBA.TCKind.from_int(kind);
    }


    public int _kind()
    {
        return kind;
    }

    public java.lang.String id() 
        throws org.omg.CORBA.TypeCodePackage.BadKind 
    {
        if( is_recursive() )
        {
            return id;
        }
        else
            switch( kind )
            {
            case   TCKind._tk_objref:
            case   TCKind._tk_struct:
            case   TCKind._tk_union:
            case   TCKind._tk_enum:
            case   TCKind._tk_alias:
            case   TCKind._tk_value:
            case   TCKind._tk_value_box:
            case   TCKind._tk_except : return id;
            default:  throw new org.omg.CORBA.TypeCodePackage.BadKind();
            }
    }

    public java.lang.String name() 
        throws org.omg.CORBA.TypeCodePackage.BadKind 
    {
        switch( kind )
        {
        case   TCKind._tk_objref:
        case   TCKind._tk_struct:
        case   TCKind._tk_union:
        case   TCKind._tk_enum:
        case   TCKind._tk_alias:
        case   TCKind._tk_value:
        case   TCKind._tk_value_box:
        case   TCKind._tk_except : return name;
        default:  throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
    }

    public int member_count() 
        throws org.omg.CORBA.TypeCodePackage.BadKind 
    {
        switch( kind )
        {
        case   TCKind._tk_struct:
        case   TCKind._tk_union:
        case   TCKind._tk_value:
        case   TCKind._tk_enum : return member_count;
        default:  throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
    }

    public java.lang.String member_name(int index) 
        throws org.omg.CORBA.TypeCodePackage.BadKind,
               org.omg.CORBA.TypeCodePackage.Bounds 
    {
        switch( kind )
        {
        case TCKind._tk_struct:
        case TCKind._tk_union:
        case TCKind._tk_enum: 
        case TCKind._tk_value: 
            if( index <= member_count )
                return member_name[index];
            else
                throw new  org.omg.CORBA.TypeCodePackage.Bounds();
        default:  throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }

    }

    public org.omg.CORBA.TypeCode member_type(int index) 
        throws org.omg.CORBA.TypeCodePackage.BadKind,
               org.omg.CORBA.TypeCodePackage.Bounds
    {
        if( kind != TCKind._tk_struct && kind != TCKind._tk_union &&
            kind != TCKind._tk_value )
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        if( index > member_count )
            throw new  org.omg.CORBA.TypeCodePackage.Bounds();
        return member_type[index];
    }

    public org.omg.CORBA.Any member_label( int index ) 
        throws org.omg.CORBA.TypeCodePackage.BadKind,  
               org.omg.CORBA.TypeCodePackage.Bounds
    {
        if( kind != TCKind._tk_struct && kind != TCKind._tk_union )
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        if( index > member_count )
            throw new  org.omg.CORBA.TypeCodePackage.Bounds();
        return member_label[index];
    }

    public org.omg.CORBA.TypeCode discriminator_type() 
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if( kind != TCKind._tk_struct && kind != TCKind._tk_union )
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        return discriminator_type;
    }


    public int default_index() 
        throws org.omg.CORBA.TypeCodePackage.BadKind 
    {
        if( kind != TCKind._tk_struct && kind != TCKind._tk_union )
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        return default_index;
    }


    public int length() 
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        switch( kind )
        {
        case   TCKind._tk_string:
        case   TCKind._tk_wstring:
        case   TCKind._tk_sequence:
        case   TCKind._tk_array : return length;
        default: throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
    }

    public org.omg.CORBA.TypeCode content_type() 
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        switch( kind )
        {
        case   TCKind._tk_array :
        case   TCKind._tk_sequence :
        case   TCKind._tk_alias : 
        case   TCKind._tk_value_box :
            return content_type;
        default: throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
    }

    public  short fixed_digits() throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if( kind != TCKind._tk_fixed )
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        return digits;
    }

    public  short fixed_scale() throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if( kind != TCKind._tk_fixed )
            throw new org.omg.CORBA.TypeCodePackage.BadKind();    
        return scale;
    }

    public org.omg.CORBA.TypeCode get_compact_typecode()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public short member_visibility(int index)
        throws org.omg.CORBA.TypeCodePackage.BadKind,
               org.omg.CORBA.TypeCodePackage.Bounds
    {
        if (kind != TCKind._tk_value)
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        if (index < 0 || index > member_count)
            throw new org.omg.CORBA.TypeCodePackage.Bounds();

        return member_visibility[index];
    }

    public short type_modifier()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if (kind != TCKind._tk_value)
            throw new org.omg.CORBA.TypeCodePackage.BadKind();

        return value_modifier;
    }

    public org.omg.CORBA.TypeCode concrete_base_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if (kind != TCKind._tk_value)
            throw new org.omg.CORBA.TypeCodePackage.BadKind();

        return content_type;
    }

    /**
     * less strict equivalence check, unwinds aliases
     */
    public boolean equivalent( org.omg.CORBA.TypeCode tc )
    {
        try 
        {
            if( is_recursive() )
            {
                if( !((org.jacorb.orb.TypeCode)tc).is_recursive())
                    return false;
                else
                    return id().equals( tc.id());
            }
            else
                if( ((org.jacorb.orb.TypeCode)tc).is_recursive())
                    return false;

            /* unalias any typedef'd types */

            if( kind().value() == TCKind._tk_alias )
                return content_type().equivalent( tc );
            
            if( tc.kind().value() == TCKind._tk_alias )
                return equivalent( tc.content_type() );
            
            if( kind().value() != tc.kind().value())
                return false;

            /* for primitive type codes, only kinds need be equal */

            if( kind().value() < 14 ||
                ( kind().value() > 22 && kind().value() < 29 ))
            {
                return true;
            }

            /* compare repository ids if applicable */

            if( kind == TCKind._tk_objref  || kind == TCKind._tk_struct || 
                kind == TCKind._tk_union || kind == TCKind._tk_enum || 
                kind == TCKind._tk_alias  || kind ==  TCKind._tk_except )
            {
                if( id().length() > 0 && 
                    tc.id().length() > 0 && 
                    !id().equals( tc.id()) )
                    return false;
            }
            
            if( kind ==  TCKind._tk_array || 
                kind == TCKind._tk_sequence )
            {
                return ( length() == tc.length() && 
                         content_type().equivalent( tc.content_type()));
            }

            if( kind == TCKind._tk_fixed )
            {
                return ( fixed_digits() == tc.fixed_digits() && 
                         fixed_scale() == tc.fixed_scale() );
            }

            if( kind == TCKind._tk_union )
            {
                if( !discriminator_type().equivalent( tc.discriminator_type()))
                    return false;
        
                if( default_index() != tc.default_index())
                    return false;
                
                if( member_count() != tc.member_count())
                    return false;

                for( int i = 0; i < member_count(); i++ )
                {
                    if( ! member_type(i).equivalent( tc.member_type(i)) )
                        return false;

                    if( ! member_label(i).equals( tc.member_label(i)))
                        return false;
                }
            }

        }
        catch( org.omg.CORBA.TypeCodePackage.Bounds bs)
        {
            bs.printStackTrace();
            return false;
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind bk)
        {
            bk.printStackTrace();
            return false;
        }
        return true;
    }

    // useful additional functionality

    public void toSequence()
    {
        kind = TCKind._tk_sequence;
    }

    public String toString()
    {
        return idlTypeName();
    }

    /**
     * @returns TRUE is this TypeCode is recursive. Both the initial
     * place holder TypeCode and the real TypeCode which replaces
     * the place holder return TRUE.
     */

    public boolean is_recursive()
    {
        return recursive;
    }

    /** 
     * called after replacing the placeholder
     * to be able to break off recursion 
     */
    
    private void set_recursive()
    {
        recursive = true;
    }


//      private void resolve_recursion( Hashtable tcMap )
//      {
//          try
//          {
//              org.jacorb.util.Debug.output( 3, "resolve recursion looks at kind: " + _kind() );

//              switch ( _kind() )
//              {
//              case TCKind._tk_sequence:
//              case TCKind._tk_array:
//                  { 
//                      if( ((org.jacorb.orb.TypeCode)content_type()).is_recursive() && 
//                          ((org.jacorb.orb.TypeCode)content_type())._kind() == -1 )
//                      {
//                          TypeCode tc = (TypeCode)tcMap.get( content_type().id() );
//                          org.jacorb.util.Debug.assert( tc != null, 
//                                                    "Could not resolve recursive TypeCode for " + 
//                                                    content_type().id());
                        
//                          tc.set_recursive();
//                          content_type = tc;
//                      }  
//                      else
//                          ((org.jacorb.orb.TypeCode)content_type()).resolve_recursion(tcMap);
//                      return;
//                  }
//              case TCKind._tk_struct:
//              case TCKind._tk_except:
//              case TCKind._tk_union:
//                  {
//                      tcMap.put(this.id(), this);
//                      org.jacorb.util.Debug.output( 4, "resolve recursion.put : " + this.id() );
//                      for( int i = 0; i < member_count(); i++ )
//                      {
//                          if( ((org.jacorb.orb.TypeCode)member_type(i)).is_recursive()&& 
//                              ((org.jacorb.orb.TypeCode)member_type(i))._kind() == -1 )
//                          {    
//                              TypeCode tc = (TypeCode)tcMap.get( member_type(i).id() );
//                              org.jacorb.util.Debug.assert( tc != null, 
//                                                        "Could not resolve recursive TypeCode for " +
//                                                        member_type(i).id());
//                              tc.set_recursive();

//                              member_type[i] = tc;
//                          }
//                          else
//                              ((org.jacorb.orb.TypeCode)member_type(i)).resolve_recursion(tcMap);
//                      }
//                      return;
//                  }
//              default:
//                  return;
//              }
//          }
//          catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
//          {
//              bk.printStackTrace();
//          }
//          catch( org.omg.CORBA.TypeCodePackage.Bounds b )
//          {
//              b.printStackTrace();
//          }
//      }

    
    /** convenience method */

    public String idlTypeName() 
    {
        switch( kind().value() ) 
        {
        case   TCKind._tk_objref:
        case   TCKind._tk_struct:
        case   TCKind._tk_union:
        case   TCKind._tk_enum:
        case   TCKind._tk_alias:
        case   TCKind._tk_except : 
            try
            {
                return  idToIDL(id());   
            } catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
            {}
        case   TCKind._tk_void:   return "void";
        case   TCKind._tk_string: return "string";
        case   TCKind._tk_wstring: return "wstring";
        case   TCKind._tk_array: 
            try
            {
                return ((org.jacorb.orb.TypeCode)content_type()).idlTypeName() + "[]";
            } catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
            {}
        case   TCKind._tk_long: return "long";
        case   TCKind._tk_ulong: return "ulong";
        case   TCKind._tk_longlong: return "long long";
        case   TCKind._tk_ulonglong: return "ulong long";
        case   TCKind._tk_ushort: return "ushort";
        case   TCKind._tk_short: return "short";
        case   TCKind._tk_float: return "float";
        case   TCKind._tk_double: return "double";
        case   TCKind._tk_fixed: 
            try
            {
              return "fixed <" + fixed_digits() + "," + fixed_scale()  + ">";
            } 
            catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
            {}
        case   TCKind._tk_boolean: return "boolean";
        case   TCKind._tk_octet: return "octet";
        case   TCKind._tk_char: return "char";
        case   TCKind._tk_wchar: return "wchar";
        case   TCKind._tk_any: return "any";
        case   TCKind._tk_sequence: 
            try
            {
                return "sequence <" + 
                    ((org.jacorb.orb.TypeCode)content_type()).idlTypeName() 
                    + ">";
            } catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
            {}
        default: return "*no typeName for TK " + kind() + "*";
        }
    }

    private static String idToIDL( String s )
    {
        if( s.startsWith("IDL:"))
            s = s.substring( 4, s.lastIndexOf(":") );
        else 
            s = s.replace('.','/') + ":1.0";
        
        StringBuffer sb = new StringBuffer( s );
        int i = 0;
        while( i < sb.length() )
        {
            if( sb.charAt(i) == '/' )
            {
                sb.setCharAt(i,':');
                sb.insert(i,':');
            }
            i++;
        }
        return sb.toString();
    }

    /**
     * proprietary convenience method, not in CORBA API
     * @return the content type if this is an alias, this otherwise
     */

    public org.jacorb.orb.TypeCode originalType()
    {
        org.jacorb.orb.TypeCode tc = this;
        try
        {
            while( tc.kind() == org.omg.CORBA.TCKind.tk_alias)
                tc = (org.jacorb.orb.TypeCode)tc.content_type();
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
        {
            // does not happen
        }
        return tc;
    }

    /**
     * Creates a TypeCode for an arbitrary Java class.
     * Right now, this only covers RMI classes, not those derived from IDL.
     */
    public static TypeCode create_tc (Class clz)
    {
        return create_tc (clz, new HashSet());
    }

    /**
     * Creates a TypeCode for class `clz'.  If `clz' is a member of
     * `knownClasses', then a recursive type code is returned for it.
     * If `clz' is not a member of `knownClasses', and a value type
     * code is created for it, then `clz' is also inserted into
     * `knownClasses'.  
     */
    private static TypeCode create_tc (Class clz, Set knownClasses)
    {
        if (clz.isPrimitive())
            return (TypeCode)primitive_tcs_map.get (clz);
        else if (knownClasses.contains (clz))
            // recursive type code
            return new TypeCode (RepositoryID.repId (clz));
        else if (clz.isArray())
            return new TypeCode (TCKind._tk_sequence,
                                 0, create_tc (clz.getComponentType(),
                                               knownClasses));
        else if (java.rmi.Remote.class.isAssignableFrom (clz))
            return new TypeCode (RepositoryID.repId (clz),
                                 clz.getName());
        else if (java.io.Serializable.class.isAssignableFrom (clz))
        {
            Class    superClass    = clz.getSuperclass();
            TypeCode superTypeCode = null;
            if (superClass != null && superClass != java.lang.Object.class)
                superTypeCode = create_tc (superClass, knownClasses);

            knownClasses.add (clz);
            return new TypeCode (RepositoryID.repId (clz),
                                 clz.getName(),
                                 org.omg.CORBA.VM_NONE.value,
                                 superTypeCode,
                                 getValueMembers (clz, knownClasses));
        }
        else
            throw new RuntimeException 
                                ("cannot create TypeCode for class: " + clz);
    }

    /**
     * Returns the array of ValueMembers of class `clz'.
     * `knownClasses' is the set of classes for which recursive
     * typecodes must be created; this is passed through from
     * `create_tc (Class, Set)' above.  
     */
    private static ValueMember[] getValueMembers (Class clz, Set knownClasses)
    {
        List    result = new ArrayList();
        Field[] fields = clz.getDeclaredFields();
        for (int i=0; i < fields.length; i++)
        {
            if ((fields[i].getModifiers()
                 & (Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT))
                == 0)
                result.add (createValueMember (fields[i], knownClasses));
        }
        return (ValueMember[])result.toArray (new ValueMember[0]);
    }

    /**
     * Creates a ValueMember for field `f'.  `knownClasses' is the set
     * of classes for which recursive type codes must be created; this
     * is passed through from `create_tc (Class, Set)' above.  
     */
    private static ValueMember createValueMember (Field f, Set knownClasses)
    {
        Class    type   = f.getType();
        String   id     = RepositoryID.repId (type);
        TypeCode tc     = create_tc (type, knownClasses);
        short    access = ((f.getModifiers() & Modifier.PUBLIC) != 0)
                              ? org.omg.CORBA.PUBLIC_MEMBER.value
                              : org.omg.CORBA.PRIVATE_MEMBER.value;
        return new ValueMember (f.getName(), id, "", "1.0", tc, null, access);
    }

}
