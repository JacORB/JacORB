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

    Any (org.omg.CORBA.ORB orb)
    {
        this.orb = orb;
        typeCode = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_null);
    }
        
    public TCKind kind ()
    {
        return typeCode.kind ();
    }

    public org.omg.CORBA.TypeCode type ()
    {
        return typeCode;
    }

    public org.omg.CORBA.TypeCode originalType ()
    {
        return ((org.jacorb.orb.TypeCode)typeCode).originalType ();
    }

    public void type (org.omg.CORBA.TypeCode t)
    {
        typeCode = t;
        value = null;
    }

    public java.lang.Object value ()
    {
        return value;
    }

    public int _get_TCKind () 
    {
        return org.omg.CORBA.TCKind._tk_any;
    }

    private void tc_error (String s)
    {
        throw new BAD_OPERATION (s);
    }

    private void checkExtract (int value, String s)
    {
       if (originalType().kind().value() != value)
       {
           throw new BAD_OPERATION (s);
       }
    }

    public boolean equal (org.omg.CORBA.Any a)
    {   
        if (a == null)
        {
           throw new BAD_PARAM ("Null passed to Any equal operation");
        }

        if (!typeCode.equal (a.type ()))
        {
            return false; 
        }

        int kind = originalType().kind().value();
        switch (kind)
        {
            case TCKind._tk_null: 
            case TCKind._tk_void:
                return true;
            case TCKind._tk_short:
                return extract_short() == a.extract_short();
            case TCKind._tk_long:
                return extract_long() == a.extract_long();
            case TCKind._tk_longlong:
                return extract_longlong() == a.extract_longlong();
            case TCKind._tk_ushort:
                return extract_ushort() == a.extract_ushort();
            case TCKind._tk_ulong:
                return extract_ulong() == a.extract_ulong();
            case TCKind._tk_ulonglong:
                return extract_ulonglong() == a.extract_ulonglong();
            case TCKind._tk_float:
                return extract_float() == a.extract_float();
            case TCKind._tk_double:
                return extract_double() == a.extract_double();
            case TCKind._tk_fixed:
                return extract_fixed().equals( a.extract_fixed() );
            case TCKind._tk_boolean:
                return extract_boolean() == a.extract_boolean();
            case TCKind._tk_char:
                return extract_char() == a.extract_char();
            case TCKind._tk_wchar:
                return extract_wchar() == a.extract_wchar();
            case TCKind._tk_octet:
                return extract_octet() == a.extract_octet();
            case TCKind._tk_any:
                return extract_any().equals( a.extract_any() );
            case TCKind._tk_TypeCode:
                return extract_TypeCode().equal( a.extract_TypeCode() );
            case TCKind._tk_Principal:
                throw new org.omg.CORBA.NO_IMPLEMENT ("Principal deprecated");
            case TCKind._tk_objref: 
                return extract_Object().equals( a.extract_Object() );
            case TCKind._tk_string: 
                return extract_string().equals( a.extract_string() );
            case TCKind._tk_wstring: 
                return extract_wstring().equals( a.extract_wstring() );
            case TCKind._tk_array: 
            case TCKind._tk_sequence: 
            case TCKind._tk_struct: 
            case TCKind._tk_except:
            case TCKind._tk_enum:
            case TCKind._tk_union:
            {
                CDROutputStream out1, out2;
                if( !( orb instanceof org.jacorb.orb.ORB ))
                {
                    out1 = new CDROutputStream();
                    out2 = new CDROutputStream();
                }
                else
                {
                    out1 = new CDROutputStream(orb);
                    out2 = new CDROutputStream(orb);
                }
                write_value( out1 );
                a.write_value( out2 );

                if( out1.size() != out2.size() )
                    return false;

                for( int i = 0; i < out1.size(); i++ )
                {
                    if( out1.getInternalBuffer()[ i ] !=
                        out2.getInternalBuffer()[ i ] )
                    {
                        return false;
                    }
                }

                return true;
            }
            default:
                throw new RuntimeException ("Cannot compare anys with type kind " + kind);
        } 
    }

    public boolean equals (java.lang.Object obj)
    {
        if( obj instanceof org.omg.CORBA.Any)
            return equal((org.omg.CORBA.Any)obj);
        else
            return false;
    }

    public int hashCode ()
    {
        return value.hashCode ();
    }

    public String toString()
    {
        if( value != null )
            return value.toString();
        else
            return "null";
    }

    // short

    public void insert_short (short s)
    {
        value = new Short (s);
        typeCode = orb.get_primitive_tc (TCKind.tk_short);
    }

    public short extract_short ()
        throws org.omg.CORBA.BAD_OPERATION
    {
        checkExtract (TCKind._tk_short, "Cannot extract short");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_short ();
        }
        return ((Short)value).shortValue ();
    }

    // ushort

    public void insert_ushort (short s)
    {
        value = new Short (s);
        typeCode = orb.get_primitive_tc (TCKind.tk_ushort);
    }

    public short extract_ushort ()
    {
        checkExtract (TCKind._tk_ushort, "Cannot extract ushort");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_ushort ();
        }
        return ((Short)value).shortValue ();
    }

    // long

    public void insert_long (int i)
    {
        value = new Integer (i);
        typeCode = orb.get_primitive_tc (TCKind.tk_long);
    }

    public int extract_long () 
    {
        checkExtract (TCKind._tk_long, "Cannot extract long");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_long ();
        }
        return ((Integer)value).intValue ();
    }

    // ulong

    public void insert_ulong (int i)
    {
        value = new Integer (i);
        typeCode = orb.get_primitive_tc( TCKind.tk_ulong );
    }

    public int extract_ulong () 
    {
        checkExtract (TCKind._tk_ulong, "Cannot extract ulong");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_ulong ();
        }
        return ((Integer)value).intValue ();
    }

    // longlong

    public void insert_longlong (long l)
    {
        value = new Long (l);
        typeCode = orb.get_primitive_tc (TCKind.tk_longlong);
    }

    public long extract_longlong () 
    {
        checkExtract (TCKind._tk_longlong, "Cannot extract longlong");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_longlong ();
        }
        return ((Long)value).longValue ();
    }

    // ulonglong

    public void insert_ulonglong (long l)
    {
        value = new Long (l);
        typeCode = orb.get_primitive_tc (TCKind.tk_ulonglong);
    }

    public long extract_ulonglong () 
    {
        checkExtract (TCKind._tk_ulonglong, "Cannot extract ulonglong");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_ulonglong ();
        }
        return ((Long)value).longValue ();
    }

    // float

    public void insert_float (float f)
    {
        value = new Float (f);
        typeCode = orb.get_primitive_tc (TCKind.tk_float);
    }

    public float extract_float () 
    {
        checkExtract (TCKind._tk_float, "Cannot extract float");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_float ();
        }
        return ((Float)value).floatValue ();
    }

    // double

    public void insert_double (double d)
    {
        value = new Double (d);
        typeCode = orb.get_primitive_tc (TCKind.tk_double);
    }

    public double extract_double () 
    {
        checkExtract (TCKind._tk_double, "Cannot extract double");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_double ();
        }
        return ((Double)value).doubleValue ();
    }

    // boolean

    public void insert_boolean (boolean b)
    {
        value = new Boolean (b);
        typeCode = orb.get_primitive_tc (TCKind.tk_boolean);
    }

    public boolean extract_boolean () 
    {
        checkExtract (TCKind._tk_boolean, "Cannot extract boolean");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_boolean ();
        }
        return ((Boolean)value).booleanValue ();
    }

    // char

    public void insert_char (char c)
    {
        value = new Character (c);
        typeCode = orb.get_primitive_tc (TCKind.tk_char);
    }

    public char extract_char () 
    {
        checkExtract (TCKind._tk_char, "Cannot extract char");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_char ();
        }
        return ((Character)value).charValue ();
    }

    public void insert_wchar (char c)
    {
        value = new Character (c);
        typeCode = orb.get_primitive_tc (TCKind.tk_wchar);
    }

    public char extract_wchar () 
    {
        checkExtract (TCKind._tk_wchar, "Cannot extract wchar");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_wchar ();
        }
        return ((Character)value).charValue ();
    }

    // octet

    public void insert_octet (byte b)
    {
        value = new Byte (b);
        typeCode = orb.get_primitive_tc (TCKind.tk_octet);
    }

    public byte extract_octet () 
    {
        checkExtract (TCKind._tk_octet, "Cannot extract octet");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_octet ();
        }
        return ((Byte)value).byteValue ();
    }

    // any

    public void insert_any (org.omg.CORBA.Any a)
    {
        value = a;
        typeCode = orb.get_primitive_tc (TCKind.tk_any);
    }

    public org.omg.CORBA.Any extract_any () 
    {
        checkExtract (TCKind._tk_any, "Cannot extract any");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_any ();
        }
        return (org.omg.CORBA.Any)value;
    }

    // TypeCode

    public void insert_TypeCode (org.omg.CORBA.TypeCode tc)
    {
        value = tc;
        typeCode = orb.get_primitive_tc (TCKind.tk_TypeCode);
    }

    public org.omg.CORBA.TypeCode extract_TypeCode () 
    {
        checkExtract (TCKind._tk_TypeCode, "Cannot extract TypeCode");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_TypeCode ();
        }
        return (TypeCode)value;
    }

    // string

    public void insert_string (String s)
    { 
        value = s;
        typeCode = orb.create_string_tc (0);
    }

    public String extract_string () 
    {
        checkExtract (TCKind._tk_string, "Cannot extract string");
        return value.toString ();
    }

    public void insert_wstring (String s)
    {
        value = s;
        typeCode = orb.create_wstring_tc (0);
    }

    public String extract_wstring () 
    {
        checkExtract (TCKind._tk_wstring, "Cannot extract wstring");
        return value.toString ();
    }

    // fixed

    public void insert_fixed (java.math.BigDecimal _value) 
    {
        value = _value;
        typeCode = (new org.omg.CORBA.FixedHolder(_value))._type();
    }

    public java.math.BigDecimal extract_fixed () 
    {
        checkExtract (TCKind._tk_fixed, "Cannot extract fixed");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_fixed ();
        }
        return (java.math.BigDecimal)value;
    }
        
    public void insert_fixed(java.math.BigDecimal _value, org.omg.CORBA.TypeCode type) 
    // ??       throws org.omg.CORBA.BAD_INV_ORDER 
    {
        value = _value;
        typeCode = type;
    }

    // obj refs

    public void insert_Object (org.omg.CORBA.Object o)
    { 
        value = o;

        org.omg.CORBA.ORB orb = null;
        String typeId = null;
        String name = "";

        if( value == null )
        {
           orb = org.omg.CORBA.ORB.init();
           typeId = "IDL:omg.org/CORBA/Object:1.0";
           name = "Object";
        }
        else
        {            
           orb = ((org.omg.CORBA.portable.ObjectImpl)o)._orb();
           typeId = ((org.omg.CORBA.portable.ObjectImpl)o)._ids()[0];

           // check if the repository Id is in IDL format
           if (typeId.startsWith ("IDL:"))
           {
              // parse the name from the repository Id string
              name = typeId.substring (4, typeId.lastIndexOf (':'));
              name = name.substring (name.lastIndexOf ('/') + 1);
           }
        }
        typeCode = orb.create_interface_tc( typeId , name );
    }

    public void insert_Object (org.omg.CORBA.Object o, 
                               org.omg.CORBA.TypeCode type)
    { 
        if( type.kind().value() != TCKind._tk_objref )
            tc_error("Illegal, non-object TypeCode!"); 

        if( value == null )
        {
            orb = org.omg.CORBA.ORB.init();
        }
        else
        {
            orb = ((org.omg.CORBA.portable.ObjectImpl)o)._orb();
        }
        value = o;
        typeCode = type;
    }

    public org.omg.CORBA.Object extract_Object ()
    {
        checkExtract (TCKind._tk_objref, "Cannot extract object");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_Object ();
        }
        return (org.omg.CORBA.Object)value;
    }

    // workaround: as long as local objects don't have stubs, we need to 
    // return *Java* objects

    public java.lang.Object extract_objref ()
    {
        checkExtract (TCKind._tk_objref, "Cannot extract object");
        return value;
    }

    // Principal (deprecated)

    public void insert_Principal (org.omg.CORBA.Principal p)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("Principal deprecated");
    }

    public org.omg.CORBA.Principal extract_Principal ()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("Principal deprecated");
    }

    public void insert_Streamable (org.omg.CORBA.portable.Streamable s)
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
        int kind = typeCode.kind().value();
        if (kind != TCKind._tk_value &&
            kind != TCKind._tk_null)
            tc_error ("Cannot extract value!");
        return (java.io.Serializable)value;
    }


    public void insert_Value(java.io.Serializable value)
    {
        if (value != null)
        {
            this.value    = value;
            this.typeCode = TypeCode.create_tc (value.getClass());
        }
        else
        {
            this.value    = null;
            this.typeCode = new TypeCode (TCKind._tk_null);
        }
    }

    public void insert_Value(java.io.Serializable value, org.omg.CORBA.TypeCode type) 
        throws org.omg.CORBA.MARSHAL
    {
        this.value    = value;
        this.typeCode = type;
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
            return new org.jacorb.orb.CDRInputStream( orb, ((CDROutputStream)value).getBufferCopy());
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

    public void read_value (org.omg.CORBA.portable.InputStream input, 
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
            throw new org.omg.CORBA.NO_IMPLEMENT ("Principal deprecated");
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
        case TCKind._tk_alias:
            if(! (orb instanceof org.jacorb.orb.ORB ))
                value = new CDROutputStream();
            else
                value = new CDROutputStream(orb);
            ((CDRInputStream)input).read_value(type, (CDROutputStream)value);

            break;
        case TCKind._tk_value:
            insert_Value 
                (((org.omg.CORBA_2_3.portable.InputStream)input).read_value());
            break;
        case TCKind._tk_abstract_interface:
	    java.lang.Object obj = 
		((org.omg.CORBA_2_3.portable.InputStream)input).read_abstract_interface();
	    if (obj instanceof org.omg.CORBA.Object)
		insert_Object((org.omg.CORBA.Object)obj);
	    else
		insert_Value((java.io.Serializable)obj);
            break;
        default:
            throw new RuntimeException("Cannot handle TypeCode with kind " + kind);
        }
        //        org.jacorb.util.Debug.output( 4, "Any.read_value: kind " + type().kind().value() );
    }

    public void write_value( org.omg.CORBA.portable.OutputStream output )
    {
        int kind = typeCode.kind().value();
        // org.jacorb.util.Debug.output(3, "Any.writeValue kind " + kind );
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
            throw new org.omg.CORBA.NO_IMPLEMENT ("Principal deprecated");
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
        case TCKind._tk_alias:
            try
            {
                if( value instanceof org.omg.CORBA.portable.Streamable )
                { 
                    org.omg.CORBA.portable.Streamable s = 
                        (org.omg.CORBA.portable.Streamable)value;
                    s._write(output);
                }
                else if ( value instanceof org.omg.CORBA.portable.OutputStream )
                { 
                    byte [] internal_buf = 
                        ((CDROutputStream)value).getInternalBuffer();
                    
                    CDRInputStream in = 
                        new CDRInputStream( orb, internal_buf );

                    ((CDROutputStream)output).write_value( typeCode, in );                    
                }
                break;
            } 
            catch( Exception e )
            {
                e.printStackTrace();
                throw new RuntimeException( e.getMessage());
            }
//          case TCKind._tk_alias:
//              try
//              {
//                  // save tc
//                  org.omg.CORBA.TypeCode _tc = typeCode;
//                  typeCode = typeCode.content_type();
//                  // it gets overwritten here
//                  write_value( output);
//                  // restore
//                  typeCode = _tc;
//              } 
//              catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
//              {
//                  throw new org.omg.CORBA.UNKNOWN("Bad TypeCode kind");
//              }
//              break;
        case TCKind._tk_value:
            ((org.omg.CORBA_2_3.portable.OutputStream)output)
                .write_value ((java.io.Serializable)value);
            break;
        case TCKind._tk_abstract_interface:
            ((org.omg.CORBA_2_3.portable.OutputStream)output)
                .write_abstract_interface (value);
            break;
        default:
            throw new RuntimeException("Cannot handle TypeCode with kind " + kind);
        }
    }

    // other, proprietary

    public void insert_void ()
    {
        value = null;
        typeCode = orb.get_primitive_tc (TCKind.tk_void);
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
