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

import org.omg.CORBA.*;

/**
 * CORBA any
 *
 * Differences to the standardized any type:
 * - no support for "streamable"
 * - additional insert_void operation
 * 
 * @author (c) Gerald Brose, FU Berlin 1997/98
 * $Id$ 
 * 
 */


public final class Any 
    extends org.omg.CORBA.Any
{
    private org.omg.CORBA.TypeCode typeCode;
    private java.lang.Object value;
    private org.omg.CORBA.ORB orb;

    Any( org.omg.CORBA.ORB _orb )
    {
        orb = _orb;
        typeCode = orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_null );
    }
        
    public TCKind kind()
    {
        return typeCode.kind();
    }

    public org.omg.CORBA.TypeCode type()
    {
        return typeCode;
    }

    public void type(org.omg.CORBA.TypeCode t)
    {
        typeCode = t;
        value = null;
    }

    public java.lang.Object value()
    {
        return value;
    }

    public int _get_TCKind() 
    {
        return org.omg.CORBA.TCKind._tk_any;
    }

    private void tc_error()
    {
        throw new BAD_OPERATION();
    }

    private void tc_error(String s)
    {
        throw new BAD_OPERATION(s);
    }

    public boolean equal(org.omg.CORBA.Any a)
    {   
        if( !typeCode.equal(a.type()))
            return false;
        else
            return ((org.jacorb.orb.Any)a).value().equals(value()); // compare values
    }

    public boolean equals( java.lang.Object obj )
    {
        if( obj instanceof org.omg.CORBA.Any)
            return equal((org.omg.CORBA.Any)obj);
        else
            return false;
    }

    public int hashCode()
    {
        return value.hashCode();
    }

    public String toString()
    {
        if( value != null )
            return value.toString();
        else
            return "null";
    }

  
    // short

    public void insert_short(short s)
    {
        value = new Short( s );
        typeCode = orb.get_primitive_tc( TCKind.tk_short );
    }

    public short extract_short()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if( typeCode.kind().value() != TCKind._tk_short )
            tc_error("Cannot extract short!");

        return ((Short)value).shortValue();
    }

    // ushort

    public void insert_ushort ( short s )
    {
        value = new Short( s );
        typeCode = orb.get_primitive_tc( TCKind.tk_ushort );
    }

    public short extract_ushort ()
    {
        if( typeCode.kind().value() != TCKind._tk_ushort )
            tc_error("Cannot extract ushort!");

        return ((Short)value).shortValue();
    }

    // long

    public void insert_long( int i )
    {
        value = new Integer( i );
        typeCode = orb.get_primitive_tc( TCKind.tk_long );
    }

    public int extract_long() 
    {
        if( typeCode.kind().value() != TCKind._tk_long )
            tc_error("Cannot extract long!");

        return ((Integer)value).intValue();
    }

    // ulong

    public void insert_ulong (int i)
    {
        value = new Integer( i );
        typeCode = orb.get_primitive_tc( TCKind.tk_ulong );
    }

    public int extract_ulong() 
    {
        if( typeCode.kind().value() != TCKind._tk_ulong )
            tc_error("Cannot extract ulong!");

        return ((Integer)value).intValue();
    }


    // longlong

    public void insert_longlong (long l)
    {
        value = new Long( l );
        typeCode = orb.get_primitive_tc( TCKind.tk_longlong );
    }

    public long extract_longlong() 
    {
        if( typeCode.kind().value() != TCKind._tk_longlong )
            tc_error("Cannot extract longlong! (TC is " + typeCode.kind().value() + ")");
        return ((Long)value).longValue();
    }

    // ulonglong

    public void insert_ulonglong (long l)
    {
        value = new Long( l );
        typeCode = orb.get_primitive_tc( TCKind.tk_ulonglong );
    }

    public long extract_ulonglong() 
    {
        if( typeCode.kind().value() != TCKind._tk_ulonglong )
            tc_error("Cannot extract ulonglong!");
        return ((Long)value).longValue();
    }


    // float

    public float extract_float() 
    {
        if( typeCode.kind().value() != TCKind._tk_float )
            tc_error("Cannot extract float!");
        return ((Float)value).floatValue();
    }

    public void insert_float(float f)
    {
        value = new Float( f );
        typeCode = orb.get_primitive_tc( TCKind.tk_float );
    }


    // double

    public double extract_double() 
    {

        if( typeCode.kind().value() != TCKind._tk_double )
            tc_error("Cannot extract double!");
        return ((Double)value).doubleValue();
    }

    public void insert_double( double d)
    {
        value = new Double(d);
        typeCode = orb.get_primitive_tc( TCKind.tk_double );
    }

    // boolean

    public boolean extract_boolean() 
    {
        if( typeCode.kind().value() != TCKind._tk_boolean )
            tc_error("Cannot extract boolean!");
        return ((Boolean)value).booleanValue();
    }

    public void insert_boolean( boolean b)
    {
        value = new Boolean(b);
        typeCode = orb.get_primitive_tc( TCKind.tk_boolean );
    }

    // char

    public char extract_char() 
    {
        if( typeCode.kind().value() != TCKind._tk_char )
            tc_error("Cannot extract char!"); 
        return ((Character)value).charValue();
    }

    public void insert_char( char c)
    {
        value = new Character( c );
        typeCode = orb.get_primitive_tc( TCKind.tk_char );
    }

    public void insert_wchar( char c)
    {
        value = new Character( c );
        typeCode = orb.get_primitive_tc( TCKind.tk_wchar );
    }

    public char extract_wchar() 
    {
        if( typeCode.kind().value() != TCKind._tk_wchar )
            tc_error("Cannot extract char!"); 
        return ((Character)value).charValue();
    }

    // octets

    public byte extract_octet() 
    {
        if( typeCode.kind().value() != TCKind._tk_octet )
            tc_error("Cannot extract octet!"); 
        return ((Byte)value).byteValue();
    }

    public void insert_octet(byte b)
    {
        value = new Byte(b);
        typeCode = orb.get_primitive_tc( TCKind.tk_octet );
    }

    // anys

    public org.omg.CORBA.Any extract_any() 
    {
        if( typeCode.kind().value() != TCKind._tk_any )
            tc_error("Cannot extract any!"); 
        return (org.omg.CORBA.Any)value;

    }

    public void insert_any(org.omg.CORBA.Any a)
    {
        value = a;
        typeCode = orb.get_primitive_tc( TCKind.tk_any );
    }

    // TypeCode

    public org.omg.CORBA.TypeCode extract_TypeCode() 
    {
        if( typeCode.kind().value() != TCKind._tk_TypeCode )
            tc_error("Cannot extract TypeCode!"); 
        return (TypeCode)value;
    }

    public void insert_TypeCode(org.omg.CORBA.TypeCode tc)
    {
        value = tc;
        typeCode = orb.get_primitive_tc( TCKind.tk_TypeCode );
    }

    // string

    public String extract_string() 
    {
        if( typeCode.kind().value() != TCKind._tk_string )
            tc_error("Cannot extract string!"); 
        return value.toString();
    }

    public void insert_string(String s)
    { 
        value = s;
        // if( typeCode.kind().value() == org.omg.CORBA.TCKind._tk_null )
            typeCode = orb.create_string_tc( s.length() );
    }

    public void insert_wstring(String s)
    {
        value = s;
        // if( typeCode.kind().value() == org.omg.CORBA.TCKind._tk_null )
            typeCode = orb.create_wstring_tc( s.length() );
    }

    public String extract_wstring() 
    {
        if( typeCode.kind().value() != TCKind._tk_wstring )
            tc_error("Cannot extract string!"); 
        return value.toString();
    }

    public java.math.BigDecimal extract_fixed() 
    {
        if( typeCode.kind().value() != TCKind._tk_fixed )
            tc_error("Cannot extract fixed!"); 
        return (java.math.BigDecimal)value;
    }

    public void insert_fixed(java.math.BigDecimal _value) 
    {
        value = _value;
        typeCode = (new org.omg.CORBA.FixedHolder(_value))._type();
    }
        
    public void insert_fixed(java.math.BigDecimal _value, org.omg.CORBA.TypeCode type) 
    // ??       throws org.omg.CORBA.BAD_INV_ORDER 
    {
        value = _value;
        typeCode = type;
    }

    // obj refs

    public void insert_Object(org.omg.CORBA.Object o)
    { 
        orb = ((org.omg.CORBA.portable.ObjectImpl)o)._orb();
        value = o;
        typeCode = orb.create_interface_tc( ((org.omg.CORBA.portable.ObjectImpl)o)._ids()[0],
                                            "*** don\'t know yet ***" );
    }

    public void insert_Object(org.omg.CORBA.Object o, org.omg.CORBA.TypeCode type)
    { 
        orb = ((org.omg.CORBA.portable.ObjectImpl)o)._orb();
        value = o;
        typeCode = type;
    }

    public org.omg.CORBA.Object extract_Object()
    {
        if( typeCode.kind().value() != TCKind._tk_objref )
            tc_error("Cannot extract object!"); 
                
        return (org.omg.CORBA.Object)value;
    }

    // workaround: as long as local objects don't have stubs, we need to 
    // return *Java* objects

    public java.lang.Object extract_objref()
    {
        if( !typeCode.kind().equals( TCKind.tk_objref ))
            tc_error();
        return value;
    }


    public void insert_Principal(org.omg.CORBA.Principal p)
    {
        value = p;
        typeCode = orb.get_primitive_tc( TCKind.tk_Principal );
    }

    public org.omg.CORBA.Principal extract_Principal()
    {
        if( !typeCode.kind().equals( TCKind.tk_Principal ))
            tc_error();
        return (org.omg.CORBA.Principal)value;
    }

    public void insert_Streamable(org.omg.CORBA.portable.Streamable s)
    {
        value = s;
        typeCode = s._type();
    }
    
    public org.omg.CORBA.portable.Streamable extract_Streamable()
        throws org.omg.CORBA.BAD_INV_ORDER
    {
        try
        {
            return (org.omg.CORBA.portable.Streamable)value;
        } 
        catch ( ClassCastException cce )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER();
        }
    }


    public java.io.Serializable extract_Value() 
        throws org.omg.CORBA.BAD_OPERATION
    {
        return null;
    }


    public void insert_Value(java.io.Serializable value)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void insert_Value(java.io.Serializable value, org.omg.CORBA.TypeCode type) 
        throws org.omg.CORBA.MARSHAL
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    // portable

    public org.omg.CORBA.portable.OutputStream create_output_stream()
    { 
        if(!( orb instanceof org.jacorb.orb.ORB ))
            value = new CDROutputStream();
        else
            value = new CDROutputStream(orb);
        return (CDROutputStream)value;
    }

    public org.omg.CORBA.portable.InputStream create_input_stream()
    { 
        if( value instanceof org.jacorb.orb.CDROutputStream )
        {
            //System.out.println("Any.create_input_stream()");
            //Connection.dumpBA( ((CDROutputStream)value).getBuffer());
            return new org.jacorb.orb.CDRInputStream(orb, ((CDROutputStream)value).getBufferCopy());
        }
        else
        {
            org.jacorb.orb.CDROutputStream out;
            if( !( orb instanceof org.jacorb.orb.ORB ))
                out = new org.jacorb.orb.CDROutputStream();
            else
                out = new org.jacorb.orb.CDROutputStream(orb);
            write_value(out);
            return new org.jacorb.orb.CDRInputStream(orb, out.getBufferCopy());
        }
    }


    public void read_value(org.omg.CORBA.portable.InputStream input, 
                           org.omg.CORBA.TypeCode type)
        throws org.omg.CORBA.MARSHAL
    {
        typeCode = type;
        int kind = type.kind().value();
        switch (kind)
        {
        case TCKind._tk_null: 
            break;
        case TCKind._tk_void:
            break;
        case TCKind._tk_short:
            insert_short( input.read_short());
            break;
        case TCKind._tk_long:
            insert_long( input.read_long());
            break;
        case TCKind._tk_longlong:
            insert_longlong( input.read_longlong());
            break;
        case TCKind._tk_ushort:
            insert_ushort(input.read_ushort());
            break;
        case TCKind._tk_ulong:
            insert_ulong( input.read_ulong());
            break;
        case TCKind._tk_ulonglong:
            insert_ulonglong( input.read_ulonglong());
            break;
        case TCKind._tk_float:
            insert_float( input.read_float());
            break;
        case TCKind._tk_double:
            insert_double( input.read_double());
            break;
        case TCKind._tk_fixed:
            insert_fixed( input.read_fixed());
            break;
        case TCKind._tk_boolean:
            insert_boolean( input.read_boolean());
            break;
        case TCKind._tk_char:
            insert_char( input.read_char());
            break;
        case TCKind._tk_octet:
            insert_octet( input.read_octet());
            break;
        case TCKind._tk_any:
            insert_any( input.read_any());
            break;
        case TCKind._tk_TypeCode:
            insert_TypeCode( input.read_TypeCode());
            break;
        case TCKind._tk_Principal:
            insert_Principal( input.read_Principal());
            break;
        case TCKind._tk_objref: 
            insert_Object( input.read_Object());
            break;
        case TCKind._tk_string: 
            insert_string( input.read_string());
            break;
        case TCKind._tk_wstring: 
            insert_wstring( input.read_wstring());
            break;
        case TCKind._tk_array: 
        case TCKind._tk_sequence: 
        case TCKind._tk_struct: 
        case TCKind._tk_except:
        case TCKind._tk_enum:
        case TCKind._tk_union:
            if(! (orb instanceof org.jacorb.orb.ORB ))
                value = new CDROutputStream();
            else
                value = new CDROutputStream(orb);
            ((CDRInputStream)input).read_value(type, (CDROutputStream)value);
            break;
        case TCKind._tk_alias:
            try
            {
                // save alias type code...
                org.omg.CORBA.TypeCode _tc = typeCode;
                // because it gets overwritten here...
                read_value( input, type.content_type());
                // restore type code
                typeCode = _tc;
            } 
            catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
            {
                throw new org.omg.CORBA.UNKNOWN("Bad TypeCode kind");
            }
            break;
        default:
            throw new RuntimeException("Cannot handle TypeCode with kind " + kind);
        }
        org.jacorb.util.Debug.output( 4, "Any.read_value: kind " + type().kind() );
    }

    public void write_value(org.omg.CORBA.portable.OutputStream output)
    {
        int kind = typeCode.kind().value();
        org.jacorb.util.Debug.output(3, "Any.writeValue kind " + kind );
        switch (kind)
        {
        case TCKind._tk_null: 
            break;
        case TCKind._tk_void:
            break;
        case TCKind._tk_short:
            output.write_short(extract_short());
            break;
        case TCKind._tk_long:
            output.write_long(extract_long());
            break;
        case TCKind._tk_longlong:
            output.write_longlong(extract_longlong());
            break;
        case TCKind._tk_ushort:
            output.write_ushort(extract_ushort());
            break;
        case TCKind._tk_ulong:
            output.write_ulong(extract_ulong());
            break;
        case TCKind._tk_ulonglong:
            output.write_ulonglong(extract_ulonglong());
            break;
        case TCKind._tk_float:
            output.write_float(extract_float());
            break;
        case TCKind._tk_double:
            output.write_double(extract_double());
            break;
        case TCKind._tk_fixed:
            output.write_fixed(extract_fixed());
            break;
        case TCKind._tk_boolean:
            output.write_boolean(extract_boolean());
            break;
        case TCKind._tk_char:
            output.write_char(extract_char());
            break;
        case TCKind._tk_octet:
            output.write_octet(extract_octet());
            break;
        case TCKind._tk_any:
            output.write_any(extract_any());
            break;
        case TCKind._tk_TypeCode:
            output.write_TypeCode(extract_TypeCode());
            break;
        case TCKind._tk_Principal:
            output.write_Principal(extract_Principal());
            break;
        case TCKind._tk_objref: 
            output.write_Object(extract_Object());
            break;
        case TCKind._tk_string: 
            output.write_string(extract_string());
            break;
        case TCKind._tk_wstring: 
            output.write_wstring(extract_wstring());
            break;
        case TCKind._tk_struct: 
        case TCKind._tk_except:
        case TCKind._tk_enum:
        case TCKind._tk_union:
        case TCKind._tk_array: 
        case TCKind._tk_sequence: 
            try
            {
                if( value instanceof org.omg.CORBA.portable.Streamable )
                {
                    org.omg.CORBA.portable.Streamable s = (org.omg.CORBA.portable.Streamable)value;
                    s._write(output);
                }
                else if ( value instanceof org.omg.CORBA.portable.OutputStream )
                {
                    CDRInputStream in = 
                        new CDRInputStream(orb, ((CDROutputStream)value).getInternalBuffer());
                    ((CDROutputStream)output).write_value(typeCode, in );
                }
                break;
            } 
            catch( Exception e )
            {
                e.printStackTrace();
                throw new RuntimeException( e.getMessage());
            }
        case TCKind._tk_alias:
            try
            {
                // save tc
                org.omg.CORBA.TypeCode _tc = typeCode;
                typeCode = typeCode.content_type();
                // it gets overwritten here
                write_value( output);
                // restore
                typeCode = _tc;
            } 
            catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
            {
                throw new org.omg.CORBA.UNKNOWN("Bad TypeCode kind");
            }
            break;
        default:
            throw new RuntimeException("Cannot handle TypeCode with kind " + kind);
        }
    }

    // other, proprietary

    public void insert_void()
    {
        value = null;
        typeCode = orb.get_primitive_tc( TCKind.tk_void );
    }
   
    /**
     * Convenience method for making a shallow copy of an Any.
     */
    public void insert_object(org.omg.CORBA.TypeCode typeCode,
                              java.lang.Object value)
    {
        insert_object( typeCode, null, value );
    }

    /**
     * Convenience method for making a shallow copy of an Any.
     */
    public void insert_object(org.omg.CORBA.TypeCode typeCode,
                              org.omg.CORBA.ORB orb,
                              java.lang.Object value)
    {
        this.typeCode = typeCode;
        if( orb != null )
            this.orb = orb;
        this.value = value;
    }

}








