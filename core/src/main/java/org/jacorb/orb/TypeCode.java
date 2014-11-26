package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jacorb.ir.RepositoryID;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.ValueMember;


/**
 * JacORB implementation of CORBA TypeCodes
 *
 * @author Gerald Brose, FU Berlin
 */

public class TypeCode
    extends org.omg.CORBA.TypeCode
{
    private final int         kind;
    private TCKind tcKind;

    private final String      id;
    private final String      name;

    private String []   member_name = null;
    private org.omg.CORBA.TypeCode [] member_type = null;
    private short []    member_visibility = null;
    private Any []      member_label = null;
    private short       value_modifier = 0;

    private org.omg.CORBA.TypeCode    discriminator_type = null;
    private int         default_index = -1;
    private int         length = -1;
    private org.omg.CORBA.TypeCode    content_type = null;

    /** for fixed point types */
    private short       scale;
    private short       digits;

    /** if this TC is recursive */
    private final boolean     recursive;
    private TypeCode    actualTypecode = null;
    private boolean     secondIteration = false;

    private final static org.omg.CORBA.TypeCode[]  primitive_tcs = new TypeCode[34];

    /**
     * Maps the java.lang.Class objects for primitive types to
     * their corresponding TypeCode objects.
     */
    private final static Map primitive_tcs_map = new HashMap();

    static
    {
        /** statically create primitive TypeCodes for fast lookup */
        for( int i = 0; i <= 13; i++ )
        {
            primitive_tcs[i] = new TypeCode(i);
        }
        for( int i = 23; i <= 26; i++ )
        {
            primitive_tcs[i] = new TypeCode(i);
        }
        primitive_tcs [TCKind._tk_string]
            = new TypeCode( TCKind._tk_string, 0 );
        primitive_tcs [TCKind._tk_wstring]
            = new TypeCode( TCKind._tk_wstring, 0 );
        primitive_tcs [TCKind._tk_fixed]
            = new TypeCode( (short)1, (short)0 );

        // Sun's ValueHandler in JDK 1.3 and 1.4 calls
        // ORB.get_primitive_tc() for TCKind._tk_objref and TCKind._tk_value.
        // These don't exactly look "primitive" to us, but as a courtesy to
        // Sun we provide the following bogus TypeCode objects so that
        // we can return something in these cases.
        primitive_tcs [TCKind._tk_objref]
            = new TypeCode( TCKind._tk_objref,
                            "IDL:omg.org/CORBA/Object:1.0",
                            "Object" );
        primitive_tcs [TCKind._tk_value]
            = new TypeCode( "IDL:omg.org/CORBA/portable/ValueBase:1.0",
                            "ValueBase", org.omg.CORBA.VM_NONE.value,
                            null,
                            new org.omg.CORBA.ValueMember[0] );

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
     * @param _kind kind identifying a primitive TypeCode
     * @return TypeCode with integer kind _kind
     */

    static org.omg.CORBA.TypeCode get_primitive_tc( int _kind )
    {
       if ( primitive_tcs[_kind] == null )
       {
           throw new org.omg.CORBA.BAD_PARAM ("No primitive TypeCode for kind " + _kind);
       }

        return primitive_tcs[_kind];
    }

    /**
     * @return True if this TypeCode represents a primitive type,
     * false otherwise
     */

    public boolean is_primitive()
    {
        return ( ! is_recursive() && primitive_tcs[kind] != null );
    }

    private TypeCode(int kind, String id, String name, boolean recursive)
    {
        super();

        this.kind = kind;
        this.recursive = recursive;
        this.id = id;

        if( name != null )
        {
            this.name = name.replace('.','_'); // for orbixWeb Interop
        }
        else
        {
            this.name = "";
        }
    }

    /**
     * Constructor for primitive types, called only from
     * static initializer and org.jacorb.ir.TypeCodeUtil
     */

    public TypeCode( int kind )
    {
        this(kind, null, null, false);
    }

     // TypeCode constructors for every conceivable type follow.
     // These are called exclusively by the ORBSingleton, which is
     // the only legal TypeCode factory

    /**
     * Constructor for recursive types
     */
    public TypeCode( String id )
    {
        this(-1, id, null, true);
    }

    /**
     * Constructor for tk_struct and tk_except
     */

    public TypeCode ( int kind,
            String id,
            String name,
            org.omg.CORBA.StructMember[] members)
    {
        this(kind, id, name, false);

        member_name = new String[members.length];
        member_type = new org.omg.CORBA.TypeCode[members.length];

        for( int i = 0; i < members.length; i++ )
        {
            member_name[i] = members[i].name;
            member_type[i] = members[i].type;
        }

        if (kind == TCKind._tk_struct)
        {
           resolveRecursion(this);
        }
    }

    /**
     * Constructor for  tk_union
     */

    public TypeCode ( String id,
            String name,
            org.omg.CORBA.TypeCode _discriminator_type,
            org.omg.CORBA.UnionMember[] members )
    {
        this(TCKind._tk_union, id, name, false);

        discriminator_type = _discriminator_type;

        member_name  = new String[members.length];
        member_label = new Any[members.length];
        member_type  = new org.omg.CORBA.TypeCode[members.length];

        for( int i = 0; i < members.length; i++ )
        {
            member_name[i] = members[i].name;
            member_label[i] = (Any)members[i].label;
            if( member_label[i].kind().equals( TCKind.tk_octet ) &&
                ((Byte)member_label[i].value()).byteValue() == (byte)0 )
            {
                default_index = i;
            }
            member_type[i] = members[i].type;
        }

        resolveRecursion(this);
    }

    /**
     * Constructor for tk_enum
     */

    public TypeCode (java.lang.String id,
            java.lang.String name,
            java.lang.String[] members)
    {
        this(TCKind._tk_enum, id, name, false);

        member_name = new String[members.length];

        System.arraycopy(members, 0, member_name, 0, members.length);
    }

    /**
     * Constructor for tk_alias, tk_value_box
     */

    public TypeCode (int kind,
            String id,
            String name,
            org.omg.CORBA.TypeCode original_type)
    {
        this(kind, id, name, false);

        content_type = original_type;
    }

    /**
     * Constructor for tk_objref, tk_abstract_interface, tk_native,
     * tk_local_interface
     */

    public TypeCode (int kind,
            java.lang.String id,
            java.lang.String name)
    {
        this(kind, id, name, false);
    }

    /**
     * Constructor for tk_string, tk_wstring
     */

    public TypeCode ( int kind, int bound )
    {
        this(kind);

        length = bound;
    }

    /**
     * Constructor for tk_sequence, tk_array
     */

    public TypeCode (int kind,
            int bound,
            org.omg.CORBA.TypeCode element_type)
    {
        this(kind);

        length = bound;
        content_type = element_type;
        if (content_type == null)
        {
            throw new org.omg.CORBA.BAD_PARAM ("TypeCode.ctor, content_type null");
        }
    }

    /**
     * Constructor for tk_fixed
     */

    public TypeCode (short _digits, short _scale)
    {
        this(TCKind._tk_fixed);

        digits = _digits;
        scale = _scale;
    }

    /**
     * Constructor for tk_value
     */

    public TypeCode (String id,
            String name,
            short type_modifier,
            org.omg.CORBA.TypeCode concrete_base,
            org.omg.CORBA.ValueMember[] members)
    {
        this(TCKind._tk_value, id, name, false);

        value_modifier = type_modifier;
        content_type = concrete_base;
        setValueMembers(members);
        resolveRecursion(this);
    }

    /**
     * Auxiliary method that sets the members of a tk_value.
     */

    private void setValueMembers(org.omg.CORBA.ValueMember[] members)
    {
        int member_count = (members != null) ? members.length : 0;
        member_name = new String[member_count];
        member_type = new org.omg.CORBA.TypeCode[member_count];
        member_visibility = new short[member_count];
        for( int i = 0; i < member_count; i++ )
        {
            member_name[i] = members[i].name;
            member_type[i] = members[i].type;
            member_visibility[i] = members[i].access;
        }
    }

    /**
     * check TypeCodes for equality
     */

    public boolean equal( org.omg.CORBA.TypeCode tc )
    {
        try
        {
            if( is_recursive() )
            {
                checkActualTC();
                if( tc instanceof org.jacorb.orb.TypeCode &&
                    ((org.jacorb.orb.TypeCode)tc).is_recursive() )
                {
                    org.jacorb.orb.TypeCode jtc = (org.jacorb.orb.TypeCode)tc;

                    jtc.checkActualTC();
                    if ( secondIteration )
                    {
                        return true;
                    }

                    secondIteration = true;
                    boolean result = actualTypecode.equal( jtc.actualTypecode );
                    secondIteration = false;
                    return result;
                }
                return tc.equal( actualTypecode );
            }
            else if( tc instanceof org.jacorb.orb.TypeCode &&
                     ((org.jacorb.orb.TypeCode)tc).is_recursive() )
            {
                org.jacorb.orb.TypeCode jtc = (org.jacorb.orb.TypeCode)tc;

                jtc.checkActualTC();
                return equal( jtc.actualTypecode );
            }

            if ( kind().value() != tc.kind().value() )
            {
                return false;
            }

            if ( kind == TCKind._tk_objref || kind == TCKind._tk_struct ||
                 kind == TCKind._tk_union  || kind == TCKind._tk_enum ||
                 kind == TCKind._tk_alias  || kind == TCKind._tk_except ||
                 kind == TCKind._tk_value  || kind == TCKind._tk_value_box ||
                 kind == TCKind._tk_native || kind == TCKind._tk_abstract_interface ||
                 kind == TCKind._tk_local_interface )
            {
                if ( ! id().equals( tc.id() ) || ! name().equals( tc.name() ) )
                {
                    return false;
                }
            }

            if ( kind == TCKind._tk_struct || kind == TCKind._tk_union ||
                 kind == TCKind._tk_enum   || kind == TCKind._tk_value ||
                 kind == TCKind._tk_except )
            {
                if ( member_count() != tc.member_count() )
                {
                    return false;
                }

                for (int i = 0; i < member_count(); i++)
                {
                    if ( ! member_name(i).equals( tc.member_name(i) ) )
                    {
                        return false;
                    }

                    if ( kind != TCKind._tk_enum &&
                         ! member_type(i).equal( tc.member_type(i) ) )
                    {
                        return false;
                    }

                    if ( kind == TCKind._tk_union &&
                         ! member_label(i).equal( tc.member_label(i) ) )
                    {
                        return false;
                    }

                    if ( kind == TCKind._tk_value &&
                         member_visibility(i) != tc.member_visibility(i) )
                    {
                        return false;
                    }
                }
            }

            if ( kind == TCKind._tk_union )
            {
                if ( ! discriminator_type().equal( tc.discriminator_type() ) ||
                     default_index() != tc.default_index() )
                {
                    return false;
                }
            }

            if ( kind == TCKind._tk_string || kind == TCKind._tk_wstring ||
                 kind == TCKind._tk_array  || kind == TCKind._tk_sequence)
            {
                if ( length() != tc.length() )
                {
                    return false;
                }
            }

            if ( kind == TCKind._tk_array || kind == TCKind._tk_sequence ||
                 kind == TCKind._tk_alias || kind == TCKind._tk_value_box)
            {
               if ( ! content_type().equal( tc.content_type() ) )
                {
                    return false;
                }
            }

            if (kind == TCKind._tk_fixed)
            {
                if ( fixed_digits() != tc.fixed_digits() ||
                     fixed_scale() != tc.fixed_scale() )
                {
                    return false;
                }
            }

            if (kind == TCKind._tk_value)
            {
                if ( type_modifier() != tc.type_modifier())
                {
                    return false;
                }
                if (concrete_base_type() != null || tc.concrete_base_type() != null)
                {
                    if (concrete_base_type() == null || tc.concrete_base_type() == null)
                    {
                        return false;
                    }
                    if( ! concrete_base_type().equal(tc.concrete_base_type()))
                    {
                        return false;
                    }
                }
            }
        }
        // Equal does not raise Bounds or BadKind so just return false.
        catch( org.omg.CORBA.TypeCodePackage.Bounds b )
        {
            return false;
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
        {
            return false;
        }

        return true;
    }


    public org.omg.CORBA.TCKind kind()
    {
        if (tcKind == null)
        {
            if (is_recursive())
            {
                checkActualTC();
                tcKind = actualTypecode.kind();
            }
            else
            {
                tcKind = TCKind.from_int(kind);
            }
        }

        return tcKind;
    }


    public java.lang.String id()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if( is_recursive() )
        {
            return id;
        }

        switch (kind)
        {
            case TCKind._tk_objref:   //14
            case TCKind._tk_struct:   //15
            case TCKind._tk_union:    //16
            case TCKind._tk_enum:     //17
            {
                return id;
            }
            case TCKind._tk_string:   //18
            case TCKind._tk_sequence: //19
            case TCKind._tk_array:    //20
            {
                //dummy cases for optimized switch
                throw new org.omg.CORBA.TypeCodePackage.BadKind();
            }
            case TCKind._tk_alias:    //21
            case TCKind._tk_except:   //22
            {
                return id;
            }
            case TCKind._tk_longlong:  // 23
            case TCKind._tk_ulonglong: // 24
            case TCKind._tk_longdouble:// 25
            case TCKind._tk_wchar:     // 26
            case TCKind._tk_wstring:   // 27
            case TCKind._tk_fixed:     // 28
            {
                //dummy cases for optimized switch
                throw new org.omg.CORBA.TypeCodePackage.BadKind();
            }
            case TCKind._tk_value:     // 29
            case TCKind._tk_value_box: // 30
            {
                return id;
            }
            case TCKind._tk_native:    // 31
            {
                //dummy cases for optimized switch
                throw new org.omg.CORBA.TypeCodePackage.BadKind();
            }
            case TCKind._tk_abstract_interface: //32
            case TCKind._tk_local_interface:    //33
            {
                return id;
            }
            default:
            {
                throw new org.omg.CORBA.TypeCodePackage.BadKind();
            }
        }
    }

    public java.lang.String name()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if (is_recursive ())
        {
           checkActualTC ();
           return actualTypecode.name ();
        }

        switch( kind )
        {
            case   TCKind._tk_objref:
            case   TCKind._tk_struct:
            case   TCKind._tk_union:
            case   TCKind._tk_enum:
            case   TCKind._tk_alias:
            case   TCKind._tk_value:
            case   TCKind._tk_value_box:
            case   TCKind._tk_native:
            case   TCKind._tk_abstract_interface:
            case   TCKind._tk_local_interface:
            case   TCKind._tk_except:
                return name;
            default:
                throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
    }

    public int member_count()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if (is_recursive ())
        {
           checkActualTC ();
           return actualTypecode.member_count ();
        }

        switch( kind )
        {
            case   TCKind._tk_struct:
            case   TCKind._tk_except:
            case   TCKind._tk_union:
            case   TCKind._tk_value:
            case   TCKind._tk_enum:
                return member_name.length;
            default:
                throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
    }

    public java.lang.String member_name(int index)
        throws org.omg.CORBA.TypeCodePackage.BadKind,
               org.omg.CORBA.TypeCodePackage.Bounds
    {
        if (is_recursive ())
        {
           checkActualTC ();
           return actualTypecode.member_name (index);
        }

        switch( kind )
        {
        case TCKind._tk_struct:
        case TCKind._tk_except:
        case TCKind._tk_union:
        case TCKind._tk_value:
        case TCKind._tk_enum:
        {
            if( index >= 0 && index < member_name.length )
            {
                return member_name[index];
            }
            throw new  org.omg.CORBA.TypeCodePackage.Bounds();
        }
        default:
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
    }

    public org.omg.CORBA.TypeCode member_type(int index)
        throws org.omg.CORBA.TypeCodePackage.BadKind,
               org.omg.CORBA.TypeCodePackage.Bounds
    {
        if (is_recursive ())
        {
           checkActualTC ();
           return actualTypecode.member_type (index);
        }

        switch( kind )
        {
            case TCKind._tk_struct:
            case TCKind._tk_except:
            case TCKind._tk_union:
            case TCKind._tk_value:
            {
                if( index >= 0 && index < member_name.length )
                {
                    return member_type[index];
                }

                throw new  org.omg.CORBA.TypeCodePackage.Bounds();
            }
            default:  throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
    }

    public org.omg.CORBA.Any member_label( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind,
               org.omg.CORBA.TypeCodePackage.Bounds
    {
        if (is_recursive ())
        {
            checkActualTC ();
            return actualTypecode.member_label (index);
        }

        if( kind != TCKind._tk_union )
        {
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
        if( index < 0 || index >= member_name.length )
        {
            throw new  org.omg.CORBA.TypeCodePackage.Bounds();
        }
        return member_label[index];
    }

    public org.omg.CORBA.TypeCode discriminator_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if (is_recursive ())
        {
           checkActualTC ();
           return actualTypecode.discriminator_type ();
        }

        if( kind != TCKind._tk_union )
        {
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
        return discriminator_type;
    }


    public int default_index()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if (is_recursive ())
        {
            checkActualTC ();
            return actualTypecode.default_index ();
        }

        if( kind != TCKind._tk_union )
        {
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }

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
            case   TCKind._tk_array:
                return length;
            default:
                throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
    }

    public org.omg.CORBA.TypeCode content_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        switch( kind )
        {
            case   TCKind._tk_array:
            case   TCKind._tk_sequence:
            case   TCKind._tk_alias:
            case   TCKind._tk_value_box:
                return content_type;
            default:
                throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
    }

    public  short fixed_digits()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if( kind != TCKind._tk_fixed )
        {
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
        return digits;
    }

    public  short fixed_scale()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if( kind != TCKind._tk_fixed )
        {
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
        return scale;
    }

    /**
     * <code>get_compact_typecode</code> returns a new TypeCode with all
     * type and member information removed. RepositoryID and alias are
     * preserved.
     * This method effectively clones the original typecode - simpler than
     * trying to work out what type so what to duplicate (and compact).
     *
     * @return an <code>org.omg.CORBA.TypeCode</code> value
     */
    public org.omg.CORBA.TypeCode get_compact_typecode()
    {
        // New typecode with same kind, id and a blank name.
        final TypeCode result = new TypeCode (kind, id, "", recursive);

        // Duplicate the original typecode.

        // Member names are optional, so compact them down for transmission.
        if ( member_name != null)
        {
            result.member_name = new String [member_name.length];
            for (int i = 0; i < result.member_name.length; i++)
            {
                result.member_name[i] = "";
            }
        }

        // Compact the member types down as well.
        if (member_type != null)
        {
            result.member_type = new TypeCode [member_type.length];
            for (int i = 0; i < result.member_type.length; i++)
            {
                result.member_type[i] = member_type[i].get_compact_typecode ();
            }
        }

        result.member_visibility = member_visibility;
        result.member_label = member_label;
        result.value_modifier = value_modifier;

        result.discriminator_type = discriminator_type;
        result.default_index = default_index;
        result.length = length;

        if (content_type != null)
        {
            result.content_type = content_type.get_compact_typecode();
        }

        result.scale = scale;
        result.digits = digits;

        result.secondIteration = secondIteration;

        result.resolveRecursion(result);
        return result;
    }

    public short member_visibility(int index)
        throws org.omg.CORBA.TypeCodePackage.BadKind,
               org.omg.CORBA.TypeCodePackage.Bounds
    {
        if (kind != TCKind._tk_value)
        {
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
        if (index < 0 || index >= member_name.length)
        {
            throw new org.omg.CORBA.TypeCodePackage.Bounds();
        }

        return member_visibility[index];
    }

    public short type_modifier()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if (kind != TCKind._tk_value)
        {
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }

        return value_modifier;
    }

    public org.omg.CORBA.TypeCode concrete_base_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if (kind != TCKind._tk_value)
        {
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }

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
                checkActualTC();
                if( tc instanceof org.jacorb.orb.TypeCode &&
                    ((org.jacorb.orb.TypeCode)tc).is_recursive() )
                {
                    org.jacorb.orb.TypeCode jtc = (org.jacorb.orb.TypeCode)tc;

                    jtc.checkActualTC();
                    if ( secondIteration )
                    {
                        return true;
                    }

                    secondIteration = true;
                    boolean result = actualTypecode.equivalent( jtc.actualTypecode );
                    secondIteration = false;
                    return result;
                }
                return tc.equivalent( actualTypecode );
            }
            else if( tc instanceof org.jacorb.orb.TypeCode &&
                     ((org.jacorb.orb.TypeCode)tc).is_recursive() )
            {
                org.jacorb.orb.TypeCode jtc = (org.jacorb.orb.TypeCode)tc;

                jtc.checkActualTC();
                return equivalent( jtc.actualTypecode );
            }

            /* unalias any typedef'd types */

            if( kind().value() == TCKind._tk_alias )
            {
                return content_type().equivalent( tc );
            }

            if( tc.kind().value() == TCKind._tk_alias )
            {
                return equivalent( tc.content_type() );
            }

            if( kind().value() != tc.kind().value() )
            {
                return false;
            }

            if( kind == TCKind._tk_objref || kind == TCKind._tk_struct ||
                kind == TCKind._tk_union  || kind == TCKind._tk_enum ||
                kind == TCKind._tk_alias  || kind == TCKind._tk_except ||
                kind == TCKind._tk_value  || kind == TCKind._tk_value_box ||
                kind == TCKind._tk_native || kind == TCKind._tk_abstract_interface ||
                kind == TCKind._tk_local_interface )
            {
                if( id().length() > 0 && tc.id().length() > 0 )
                {
                    if ( id().equals( tc.id() ) )
                    {
                        return true;
                    }
                    return false;
                }
            }

            if ( kind == TCKind._tk_struct || kind == TCKind._tk_union ||
                 kind == TCKind._tk_enum   || kind == TCKind._tk_value ||
                 kind == TCKind._tk_except )
            {
                if ( member_count() != tc.member_count() )
                {
                    return false;
                }

                for (int i = 0; i < member_count(); i++)
                {
                    if ( kind != TCKind._tk_enum &&
                         ! member_type(i).equivalent( tc.member_type(i) ) )
                    {
                        return false;
                    }

                    if ( kind == TCKind._tk_union &&
                         ! member_label(i).equal( tc.member_label(i) ) )
                    {
                        return false;
                    }

                    if ( kind == TCKind._tk_value &&
                         member_visibility(i) != tc.member_visibility(i) )
                    {
                        return false;
                    }
                }
            }

            if ( kind == TCKind._tk_union )
            {
                if ( ! discriminator_type().equivalent( tc.discriminator_type() ) ||
                    default_index() != tc.default_index() )
                {
                    return false;
                }
            }

            if ( kind == TCKind._tk_string || kind == TCKind._tk_wstring ||
                 kind == TCKind._tk_array  || kind == TCKind._tk_sequence)
            {
                if ( length() != tc.length() )
                {
                    return false;
                }
            }

            if ( kind == TCKind._tk_array || kind == TCKind._tk_sequence ||
                 kind == TCKind._tk_alias || kind == TCKind._tk_value_box)
            {
                if ( ! content_type().equivalent( tc.content_type() ) )
                {
                    return false;
                }
            }

            if (kind == TCKind._tk_fixed)
            {
                if ( fixed_digits() != tc.fixed_digits() ||
                     fixed_scale() != tc.fixed_scale() )
                {
                    return false;
                }
            }

            if (kind == TCKind._tk_value)
            {
                if ( type_modifier() != tc.type_modifier() ||
                     ! concrete_base_type().equivalent( tc.concrete_base_type() ) )
                {
                    return false;
                }
            }
        }
        // Equivalent does not raise Bounds or BadKind so just return false.
        catch( org.omg.CORBA.TypeCodePackage.Bounds b )
        {
            return false;
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
        {
            return false;
        }

        return true;
    }

    // useful additional functionality

    public String toString()
    {
        return "{TypeCode: Kind=" + kind + " (" + kindToString(kind) + "), ID=" + id + ", recursive=" + recursive + "}";
    }

    /**
     * @return TRUE is this TypeCode is recursive. Both the initial
     * place holder TypeCode and the real TypeCode which replaces
     * the place holder return TRUE.
     */

    private boolean is_recursive()
    {
        return recursive;
    }

    /**
     * @return TRUE if the argument is a JacORB typecode and is recursive.
     */
    public static boolean isRecursive(org.omg.CORBA.TypeCode typeCode)
    {
        return (typeCode instanceof TypeCode) ? ((TypeCode)typeCode).is_recursive()
                                        : false;
    }

    private String kindToString(int kind)
    {
        switch (kind) {
            case -1 : return "recursive";
            case TCKind._tk_null: return "tk_null";
            case TCKind._tk_void: return "tk_void";
            case TCKind._tk_short: return "tk_short";
            case TCKind._tk_long: return "tk_long";
            case TCKind._tk_ushort: return "tk_ushort";
            case TCKind._tk_ulong: return "tk_ulong";
            case TCKind._tk_float: return "tk_float";
            case TCKind._tk_double: return "tk_double";
            case TCKind._tk_boolean: return "tk_boolean";
            case TCKind._tk_char: return "tk_char";
            case TCKind._tk_octet: return "tk_octet";
            case TCKind._tk_any: return "tk_any";
            case TCKind._tk_TypeCode: return "tk_TypeCode";
            case TCKind._tk_Principal: return "tk_Principal";
            case TCKind._tk_objref: return "tk_objref";
            case TCKind._tk_struct: return "tk_struct";
            case TCKind._tk_union: return "tk_union";
            case TCKind._tk_enum: return "tk_enum";
            case TCKind._tk_string: return "tk_string";
            case TCKind._tk_sequence: return "tk_sequence";
            case TCKind._tk_array: return "tk_array";
            case TCKind._tk_alias: return "tk_alias";
            case TCKind._tk_except: return "tk_except";
            case TCKind._tk_longlong: return "tk_longlong";
            case TCKind._tk_ulonglong: return "tk_ulonglong";
            case TCKind._tk_longdouble: return "tk_longdouble";
            case TCKind._tk_wchar: return "tk_wchar";
            case TCKind._tk_wstring: return "tk_wstring";
            case TCKind._tk_fixed: return "tk_fixed";
            case TCKind._tk_value: return "tk_value";
            case TCKind._tk_value_box: return "tk_value_box";
            case TCKind._tk_native: return "tk_native";
            case TCKind._tk_abstract_interface: return "tk_abstract_interface";
            case TCKind._tk_local_interface: return "tk_local_interface";
            default: throw new org.omg.CORBA.BAD_PARAM();
        }
    }

    /**
     * convenience method
     */
    public static String idlTypeName(org.omg.CORBA.TypeCode typeCode)
    {
        return (typeCode instanceof org.jacorb.orb.TypeCode)
                                  ? ((org.jacorb.orb.TypeCode)typeCode).idlTypeName()
                                  : "(foreign typecode)";
    }

    /** convenience method */

    public String idlTypeName()
    {
       if (is_recursive ())
       {
          checkActualTC ();
          return actualTypecode.idlTypeName ();
       }

       switch( kind().value() )
       {
           case   TCKind._tk_objref:
           case   TCKind._tk_struct:
           case   TCKind._tk_union:
           case   TCKind._tk_enum:
           case   TCKind._tk_alias:
           case   TCKind._tk_except:
           case   TCKind._tk_native:
           case   TCKind._tk_abstract_interface:
           case   TCKind._tk_local_interface:
           {
               try
               {
                   return  idToIDL(id());
               }
               catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
               {
                   throw new INTERNAL("should never happen");
               }
           }
           case   TCKind._tk_void: return "void";
           case   TCKind._tk_string: return "string";
           case   TCKind._tk_wstring: return "wstring";
           case   TCKind._tk_array:
           {
               try
               {
                   return idlTypeName(content_type()) + "[]";
               }
               catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
               {
                   throw new INTERNAL("should never happen");
               }
           }
           case   TCKind._tk_long: return "long";
           case   TCKind._tk_ulong: return "ulong";
           case   TCKind._tk_longlong: return "long long";
           case   TCKind._tk_ulonglong: return "ulong long";
           case   TCKind._tk_ushort: return "ushort";
           case   TCKind._tk_short: return "short";
           case   TCKind._tk_float: return "float";
           case   TCKind._tk_double: return "double";
           case   TCKind._tk_fixed:
           {
               try
               {
                   return "fixed <" + fixed_digits() + "," + fixed_scale()  + ">";
               }
               catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
               {
                   throw new INTERNAL("should never happen");
               }
           }
           case   TCKind._tk_boolean: return "boolean";
           case   TCKind._tk_octet: return "octet";
           case   TCKind._tk_char: return "char";
           case   TCKind._tk_wchar:
           {
               return "wchar";
           }
           case   TCKind._tk_any:
           {
               return "any";
           }
           case   TCKind._tk_sequence:
           {
               try
               {
                   return "sequence <" + idlTypeName(content_type()) + ">";
               }
               catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
               {
                   throw new INTERNAL("should never happen");
               }
           }
           default:
           {
               return "* no typeName for TK " + kind().value() + " *";
           }
       }
    }

    private static String idToIDL( String id )
    {
        if (id.length () > 4)
        {
            if (id.startsWith ("IDL:"))
            {
                id = id.substring (4, id.lastIndexOf (":"));
            }
            else
            {
                id = id.replace ('.','/') + ":1.0";
            }
        }

        StringBuffer sb = new StringBuffer( id );
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
     * @return the content type if the argument is an alias, or the argument
     *         itself otherwise
     */
    public static final org.omg.CORBA.TypeCode originalType(org.omg.CORBA.TypeCode typeCode)
    {
        if (isRecursive(typeCode))
        {
            // Recursive typecodes must be structs or unions so there is no
            // unwinding of aliases to be done. By returning here we avoid
            // calling kind() on a recursive typecode that might not have been
            // resolved yet. (If you remove the return statement below, you
            // will get org.omg.CORBA.BAD_INV_ORDER exceptions within kind()
            // calls on non-resolved recursive typecodes!)
            return typeCode;
        }

        try
        {
            while (typeCode.kind() == org.omg.CORBA.TCKind.tk_alias
                || typeCode.kind() == org.omg.CORBA.TCKind.tk_value_box)
            {
                typeCode = typeCode.content_type();
            }
        }
        catch (org.omg.CORBA.TypeCodePackage.BadKind bk)
        {
            throw new INTERNAL("should never happen");
        }
        return typeCode;
    }

    /**
     * Creates a TypeCode for an arbitrary Java class.
     * Right now, this only covers RMI classes, not those derived from IDL.
     */
    public static TypeCode create_tc (Class clazz)
    {
        return create_tc(clazz, new HashMap());
    }

    /**
     * Creates a TypeCode for class `clazz'.  `knownTypes' is a map
     * containing classes as keys and their corresponding type codes
     * as values.  If there is an entry for `clz' in `knownTypes',
     * then a recursive type code is returned for it.  If there is no
     * entry for `clz' in `knownTypes', and a value type code is
     * created for it, then an entry for `clz' is also inserted into
     * `knownTypes'.
     */
    private static TypeCode create_tc(Class clazz, Map knownTypes)
    {
        if (clazz.isPrimitive())
        {
            return (TypeCode)primitive_tcs_map.get(clazz);
        }
        else if (knownTypes.containsKey(clazz))
        {
            // recursive type code
            TypeCode newTypeCode = new TypeCode(RepositoryID.repId(clazz));
            newTypeCode.setActualTC((TypeCode)knownTypes.get(clazz));
            return newTypeCode;
        }
        else if (clazz.isArray())
        {
            // a Java array is mapped to a valuebox containing an IDL sequence
            TypeCode newTypeCode =
                new TypeCode(TCKind._tk_value_box,
                             RepositoryID.repId(clazz),
                             "Java_array",
                             new TypeCode(TCKind._tk_sequence,
                                          0,
                                          create_tc(clazz.getComponentType(),
                                                    knownTypes)));
            return newTypeCode;
        }
        else if (java.rmi.Remote.class.isAssignableFrom(clazz))
        {
            return new TypeCode(TCKind._tk_objref, RepositoryID.repId(clazz),
                                clazz.getName());
        }
        else if (org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(clazz))
        {
            // an IDL entity has a helper class with a static method type()
            String helperClassName = clazz.getName() + "Helper";
            try
            {
                final ClassLoader classLoader = clazz.getClassLoader();
                final Class helperClass;

                if (classLoader == null)
                {
                    helperClass = ObjectUtil.classForName(helperClassName);
                }
                else
                {
                    helperClass = classLoader.loadClass(helperClassName);
                }

                Method typeMethod = helperClass.getMethod("type", (Class[]) null);
                TypeCode newTypeCode =
                    (TypeCode)typeMethod.invoke(null, (Object[]) null);

                return newTypeCode;
            }
            catch (ClassNotFoundException e)
            {
                throw new IllegalArgumentException(
                                    "Cannot create TypeCode for class " + clazz
                                    + "\nReason: Error loading helper class "
                                    + helperClassName
                                    + "\n" + e);
            }
            catch (NoSuchMethodException e)
            {
                throw new IllegalArgumentException(
                            "Cannot create TypeCode for class: " + clazz
                            + "\nReason: no type() method in helper class "
                            + helperClassName + "\n" + e);
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalArgumentException(
                                    "Cannot create TypeCode for class: " + clazz
                                    + "\n" + e);
            }
            catch (java.lang.reflect.InvocationTargetException e)
            {
                throw new IllegalArgumentException(
                                    "Cannot create TypeCode for class: " + clazz
                                    + "\nReason: exception in type() method\n "
                                    + e.getTargetException());
            }
        }
        else if (clazz == java.io.Serializable.class ||
                 clazz == java.io.Externalizable.class ||
                 clazz == java.lang.Object.class)
        {
            // Each such Java type is mapped to an IDL typedef for an IDL any
            return (TypeCode)get_primitive_tc(TCKind._tk_any);
        }
        else if (isMappedToAnAbstractInterface(clazz))
        {
            TypeCode newTypeCode = new TypeCode(TCKind._tk_abstract_interface,
                                                RepositoryID.repId(clazz),
                                                clazz.getName());
            return newTypeCode;
        }
        else // clz is mapped to a valuetype
        {
            Class    superClass    = clazz.getSuperclass();
            TypeCode superTypeCode = null;
            if (superClass != null && superClass != java.lang.Object.class)
            {
                superTypeCode = create_tc(superClass, knownTypes);
            }
            TypeCode newTypeCode =
                new TypeCode(RepositoryID.repId(clazz),
                             clazz.getName(),
                             org.omg.CORBA.VM_NONE.value,
                             superTypeCode,
                             new ValueMember[0]);
            knownTypes.put(clazz, newTypeCode);
            newTypeCode.setValueMembers(getValueMembers(clazz, knownTypes));
            knownTypes.remove(clazz);
            return newTypeCode;
        }
    }

    /*
     * Java interfaces whose method definitions (including inherited method
     * definitions) all throw java.rmi.RemoteException or a superclass of
     * java.rmi.RemoteException are mapped to IDL abstract interfaces.
     */
    private static boolean isMappedToAnAbstractInterface(Class clazz)
    {
        if (!clazz.isInterface())
        {
            return false;
        }
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++)
        {
            Class[] exceps = methods[i].getExceptionTypes();
            int j = 0;
            while (j < exceps.length)
            {
                if (exceps[j].isAssignableFrom(
                        java.rmi.RemoteException.class))
                {
                    break;
                }
                j++;
            }
            if (j == exceps.length)
            {
                // method[i] does not throw java.rmi.RemoteException
                // or a superclass of java.rmi.RemoteException
                return false;
            }
        }
        // every method throws java.rmi.RemoteException
        // or a superclass of java.rmi.RemoteException
        return true;
    }

    /**
     * Returns the array of ValueMembers of class `clz'.  `knownTypes'
     * is a map of classes and corresponding type codes for which
     * recursive type codes must be created; this is passed through
     * from `create_tc (Class, Map)' above.
     */
    private static ValueMember[] getValueMembers (Class clazz, Map knownTypes)
    {
        final List    result = new ArrayList();
        final Field[] fields = clazz.getDeclaredFields();
        for (int i=0; i < fields.length; i++)
        {
            if ((fields[i].getModifiers()
                 & (Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT)) == 0)
            {
                result.add (createValueMember (fields[i], knownTypes));
            }
        }
        return (ValueMember[])result.toArray(new ValueMember[result.size()]);
    }

    /**
     * Creates a ValueMember for field `f'.  `knownTypes' is a map of
     * classes and their corresponding type codes for which recursive
     * type codes must be created; this is passed through from
     * `create_tc (Class, Map)' above.
     */
    private static ValueMember createValueMember (Field field, Map knownTypes)
    {
        final Class    type   = field.getType();
        final String   id     = RepositoryID.repId (type);
        final TypeCode tc     = create_tc (type, knownTypes);
        final short    access = ((field.getModifiers() & Modifier.PUBLIC) != 0)
                              ? org.omg.CORBA.PUBLIC_MEMBER.value
                              : org.omg.CORBA.PRIVATE_MEMBER.value;
        return new ValueMember (field.getName(), id, "", "1.0", tc, null, access);
    }

   /*
    * Resolve any recursive TypeCodes contained within this TypeCode.
    * @param actual The actual (non-recursive) TypeCode that replaces any
    * recursive TypeCodes contained in the actual TypeCode that have the same
    * RepositoryId
    */
   private void resolveRecursion (TypeCode actual)
   {
      if (member_type == null)
      {
         return;
      }

      org.omg.CORBA.TypeCode typeCode;
      TypeCode tc;
      for (int i = 0; i < member_type.length; i++)
      {
         typeCode = TypeCode.originalType (member_type[i]);
         if (typeCode instanceof TypeCode)
         {
             tc = (TypeCode)typeCode;

             switch (tc.kind)
             {
                 case TCKind._tk_struct:
                 case TCKind._tk_union:
                 case TCKind._tk_value:
                 {
                     tc.resolveRecursion (actual);
                     break;
                 }
                 case TCKind._tk_sequence:
                 {
                     typeCode = originalType (tc.content_type);
                     if (typeCode instanceof TypeCode)
                     {
                         tc = (TypeCode)typeCode;

                         if (tc.is_recursive () && tc.id.equals (actual.id))
                         {
                             tc.setActualTC (actual);
                         }
                         else
                         {
                             tc.resolveRecursion (actual);
                         }
                     }
                     break;
                 }
                 case -1: // create_recursive_tc sets kind to -1
                 {
                     if (tc.id.equals (actual.id))
                     {
                         tc.setActualTC (actual);
                     }
                     break;
                 }
             }
         }
      }
   }
   /*
    * Set the actual TypeCode if this TypeCode is recursive.
    * @param tc The actual TypeCode
    */
   private void setActualTC(TypeCode typeCode)
   {
      if (is_recursive ())
      {
         actualTypecode = typeCode;
      }
   }

   /*
    * Check that the actual TypeCode is set if this TypeCode is recursive.
    * This method ensures that operations aren't called on a recursive TypeCode
    * until the enclosing TypeCode has been fully resolved.
    * @exception BAD_INV_ORDER if this TypeCode is recursive and an operation
    * is called on it before the enclosing TypeCode has been fully resolved
    */
   private void checkActualTC()
   {
      if (is_recursive () && actualTypecode == null)
      {
         throw new org.omg.CORBA.BAD_INV_ORDER ();
      }
   }

   public boolean equals(Object obj)
   {
       if (this == obj)
       {
           return true;
       }

       if (! (obj instanceof org.omg.CORBA.TypeCode))
       {
           return false;
       }

       return equal((org.omg.CORBA.TypeCode)obj);
   }


   public int hashCode()
   {
       final int result;

       if (id == null)
       {
           // for primitive typecodes
           result = super.hashCode();
       }
       else
       {
           result = id.hashCode();
       }

       return result;
   }
}
