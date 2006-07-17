package org.jacorb.orb;

/*
 *        JacORB - the free Java ORB
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

import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.*;
import org.omg.CORBA.portable.Streamable;
import org.omg.CORBA_2_3.portable.OutputStream;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * CORBA any
 *
 * @author Gerald Brose
 * $Id$
 */

public final class Any
    extends org.omg.CORBA.Any
{
    private org.omg.CORBA.TypeCode typeCode;
    private java.lang.Object value;
    private final org.omg.CORBA.ORB orb;

    Any(org.omg.CORBA.ORB orb)
    {
        super();

        this.orb = orb;
        typeCode = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_null);
    }

    public TCKind kind()
    {
        return typeCode.kind();
    }

    public org.omg.CORBA.TypeCode type()
    {
        return typeCode;
    }

    public org.omg.CORBA.TypeCode originalType()
    {
      return TypeCode.originalType(typeCode);
    }

    public void type(org.omg.CORBA.TypeCode type)
    {
        typeCode = type;
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

    private void tc_error(String cause)
    {
        throw new BAD_OPERATION (cause);
    }

    private void checkNull()
    {
        if (value == null)
        {
            throw new BAD_OPERATION("No value has previously been inserted");
        }
    }

    private void checkExtract(int value, String cause)
    {
       if (originalType().kind().value() != value)
       {
           throw new BAD_OPERATION(cause);
       }
    }

    public boolean equal(org.omg.CORBA.Any other)
    {
        if (other == null)
        {
           throw new BAD_PARAM ("Null passed to Any equal operation");
        }

        if (!typeCode.equivalent(other.type()))
        {
            return false;
        }

        // TODO
        // as this was changed from orgininalType().kind().value()
        // this is possibly an alias for a primitive
        // type. could prohably be optimized?
        int kind = kind().value();

        switch (kind)
        {
            case TCKind._tk_null:       // 0
                // fallthrough
            case TCKind._tk_void:       // 1
            {
                return true;
            }
            case TCKind._tk_short:      // 2
            {
                return extract_short() == other.extract_short();
            }
            case TCKind._tk_long:       // 3
            {
                return extract_long() == other.extract_long();
            }
            case TCKind._tk_ushort:     // 4
            {
                return extract_ushort() == other.extract_ushort();
            }
            case TCKind._tk_ulong:      // 5
            {
                return extract_ulong() == other.extract_ulong();
            }
            case TCKind._tk_float:      // 6
            {
                return extract_float() == other.extract_float();
            }
            case TCKind._tk_double:     // 7
            {
                return extract_double() == other.extract_double();
            }
            case TCKind._tk_boolean:    // 8
            {
                return extract_boolean() == other.extract_boolean();
            }
            case TCKind._tk_char:       // 9
            {
                return extract_char() == other.extract_char();
            }
            case TCKind._tk_octet:      // 10
            {
                return extract_octet() == other.extract_octet();
            }
            case TCKind._tk_any:        // 11
            {
                return extract_any().equal( other.extract_any() );
            }
            case TCKind._tk_TypeCode:   // 12
            {
                return extract_TypeCode().equal( other.extract_TypeCode() );
            }
            case TCKind._tk_Principal:  // 13
            {
                throw new org.omg.CORBA.NO_IMPLEMENT("Principal deprecated");
            }
            case TCKind._tk_objref:     // 14
            {
                java.lang.Object myValue = extract_Object();
                java.lang.Object otherValue = other.extract_Object();
                if (myValue == null && otherValue == null)
                {
                    return true;
                }
                else if (myValue != null)
                {
                    return myValue.equals(otherValue);
                }
                else //if (otherValue != null)
                {
                    // For this case otherValue must be null. Can there
                    // be a case where an actual object instance represents
                    // a null object reference? Ignore the FindBugs complaint
                    // here.
                    return otherValue.equals(myValue);
                }
            }
            case TCKind._tk_struct:     // 15
                // fallthrough
            case TCKind._tk_union:      // 16
                // falltrough
            case TCKind._tk_enum:       // 17
            {
                return this.compareComplexValue(other);
            }
            case TCKind._tk_string:     // 18
            {
                return extract_string().equals( other.extract_string() );
            }
            case TCKind._tk_sequence:   // 19
                // fallthrough
            case TCKind._tk_array:      // 20
                // fallthrough
            case TCKind._tk_alias:      // 21
                // fallthrough
            case TCKind._tk_except:     // 22
            {
                return this.compareComplexValue(other);
            }
            case TCKind._tk_longlong:   // 23
            {
                return extract_longlong() == other.extract_longlong();
            }
            case TCKind._tk_ulonglong:  // 24
            {
                return extract_ulonglong() == other.extract_ulonglong();
            }
            case TCKind._tk_longdouble: // 25
            {
                throw new org.omg.CORBA.BAD_TYPECODE(
                    "type longdouble not supported in java");
            }
            case TCKind._tk_wchar:      // 26
            {
                return extract_wchar() == other.extract_wchar();
            }
            case TCKind._tk_wstring:    // 27
            {
                return extract_wstring().equals( other.extract_wstring() );
            }
            case TCKind._tk_fixed:      // 28
            {
                return extract_fixed().equals( other.extract_fixed() );
            }
            case TCKind._tk_value:      // 29
                // fallthrough
            case TCKind._tk_value_box:  // 30
            {
                return compareComplexValue(other);
            }
            // These typecodes are not currently supported.
            //case TCKind._tk_native:               // 31
            //case TCKind._tk_abstract_interface:   // 32
            //case TCKind._tk_local_interface:      // 33
            default:
            {
                throw new BAD_TYPECODE("Cannot compare anys with TypeCode kind " + kind);
            }
        }
    }

    public boolean equals (java.lang.Object obj)
    {
        if( obj instanceof org.omg.CORBA.Any)
        {
            return equal((org.omg.CORBA.Any)obj);
        }
        return false;
    }

    public String toString()
    {
        if( value != null )
        {
            return value.toString();
        }
        return "null";
    }

    // short

    public void insert_short (short s)
    {
        value = new Short (s);
        typeCode = orb.get_primitive_tc (TCKind.tk_short);
    }

    public short extract_short()
        throws org.omg.CORBA.BAD_OPERATION
    {
        checkExtract (TCKind._tk_short, "Cannot extract short");

        checkNull();

        if (value instanceof Short)
        {
            return ((Short) value).shortValue();
        }
        else if (value instanceof ShortHolder)
        {
            return ((ShortHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_short();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // ushort

    public void insert_ushort (short s)
    {
        value = new Short (s);
        typeCode = orb.get_primitive_tc (TCKind.tk_ushort);
    }

    public short extract_ushort()
    {
        checkExtract (TCKind._tk_ushort, "Cannot extract ushort");

        checkNull();

        if (value instanceof Short)
        {
            return ((Short) value).shortValue();
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_ushort();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // long

    public void insert_long (int i)
    {
        value = ObjectUtil.newInteger(i);
        typeCode = orb.get_primitive_tc (TCKind.tk_long);
    }

    public int extract_long()
    {
        checkExtract (TCKind._tk_long, "Cannot extract long");

        checkNull();

        if (value instanceof Integer)
        {
            return ((Integer) value).intValue();
        }
        else if (value instanceof IntHolder)
        {
            return ((IntHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_long();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // ulong

    public void insert_ulong (int i)
    {
        value = ObjectUtil.newInteger(i);
        typeCode = orb.get_primitive_tc( TCKind.tk_ulong );
    }

    public int extract_ulong()
    {
        checkExtract (TCKind._tk_ulong, "Cannot extract ulong");

        checkNull();

        if (value instanceof Integer)
        {
            return ((Integer) value).intValue();
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_ulong();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // longlong

    public void insert_longlong (long l)
    {
        value = new Long (l);
        typeCode = orb.get_primitive_tc (TCKind.tk_longlong);
    }

    public long extract_longlong()
    {
        checkExtract (TCKind._tk_longlong, "Cannot extract longlong");

        checkNull();

        if (value instanceof Long)
        {
            return ((Long) value).longValue();
        }
        else if (value instanceof LongHolder)
        {
            return ((LongHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_longlong();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // ulonglong

    public void insert_ulonglong (long l)
    {
        value = new Long (l);
        typeCode = orb.get_primitive_tc (TCKind.tk_ulonglong);
    }

    public long extract_ulonglong()
    {
        checkExtract (TCKind._tk_ulonglong, "Cannot extract ulonglong");

        checkNull();

        if (value instanceof Long)
        {
            return ((Long) value).longValue();
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_ulonglong();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // float

    public void insert_float (float f)
    {
        value = new Float (f);
        typeCode = orb.get_primitive_tc (TCKind.tk_float);
    }

    public float extract_float()
    {
        checkExtract (TCKind._tk_float, "Cannot extract float");

        checkNull();

        if (value instanceof Float)
        {
            return ((Float) value).floatValue();
        }
        else if (value instanceof FloatHolder)
        {
            return ((FloatHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_float();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // double

    public void insert_double (double d)
    {
        value = new Double (d);
        typeCode = orb.get_primitive_tc (TCKind.tk_double);
    }

    public double extract_double()
    {
        checkExtract (TCKind._tk_double, "Cannot extract double");

        checkNull();

        if (value instanceof Double)
        {
            return ((Double) value).doubleValue();
        }
        else if (value instanceof DoubleHolder)
        {
            return ((DoubleHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_double();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }


    /**
     * <code>insert_boolean</code> inserts a Boolean into this Any.
     *
     * @param bool a <code>boolean</code> value
     */
    public void insert_boolean (boolean bool)
    {
        // Equivilant to the static valueOf factory which is only
        // available post 1.4.
        value = (bool ? Boolean.TRUE : Boolean.FALSE );
        typeCode = orb.get_primitive_tc (TCKind.tk_boolean);
    }

    public boolean extract_boolean()
    {
        checkExtract (TCKind._tk_boolean, "Cannot extract boolean");

        checkNull();

        if (value instanceof Boolean)
        {
            return ((Boolean) value).booleanValue();
        }
        else if (value instanceof BooleanHolder)
        {
            return ((BooleanHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_boolean();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // char

    public void insert_char (char c)
    {
        value = new Character (c);
        typeCode = orb.get_primitive_tc (TCKind.tk_char);
    }

    public char extract_char()
    {
        checkExtract (TCKind._tk_char, "Cannot extract char");

        checkNull();

        if (value instanceof Character)
        {
            return ((Character) value).charValue();
        }
        else if (value instanceof CharHolder)
        {
            return ((CharHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_char();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    public void insert_wchar (char c)
    {
        value = new Character (c);
        typeCode = orb.get_primitive_tc (TCKind.tk_wchar);
    }

    public char extract_wchar()
    {
        checkExtract (TCKind._tk_wchar, "Cannot extract wchar");

        checkNull();

        if (value instanceof Character)
        {
            return ((Character) value).charValue();
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_wchar();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // octet

    public void insert_octet (byte b)
    {
        value = new Byte (b);
        typeCode = orb.get_primitive_tc (TCKind.tk_octet);
    }

    public byte extract_octet()
    {
        checkExtract (TCKind._tk_octet, "Cannot extract octet");

        checkNull();

        if (value instanceof Byte)
        {
            return ((Byte) value).byteValue();
        }
        else if (value instanceof ByteHolder)
        {
            return ((ByteHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_octet();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // any

    public void insert_any (org.omg.CORBA.Any a)
    {
        value = a;
        typeCode = orb.get_primitive_tc (TCKind.tk_any);
    }

    public org.omg.CORBA.Any extract_any()
    {
        checkExtract(TCKind._tk_any, "Cannot extract any");

        checkNull();

        if (value instanceof Any)
        {
            return (Any) value;
        }
        else if (value instanceof AnyHolder)
        {
            return ((AnyHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_any();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // TypeCode

    public void insert_TypeCode (org.omg.CORBA.TypeCode tc)
    {
        value = tc;
        typeCode = orb.get_primitive_tc (TCKind.tk_TypeCode);
    }

    public org.omg.CORBA.TypeCode extract_TypeCode()
    {
        checkExtract (TCKind._tk_TypeCode, "Cannot extract TypeCode");

        checkNull();

        if (value instanceof TypeCode)
        {
            return (TypeCode) value;
        }
        else if (value instanceof TypeCodeHolder)
        {
            return ((TypeCodeHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_TypeCode();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // string

    public void insert_string (String s)
    {
        value = s;
        typeCode = orb.create_string_tc (0);
    }

    public String extract_string()
    {
        checkExtract (TCKind._tk_string, "Cannot extract string");

        checkNull();

        if (value instanceof String)
        {
            return (String) value;
        }
        else if (value instanceof StringHolder)
        {
            return ((StringHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_string();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    public void insert_wstring (String s)
    {
        value = s;
        typeCode = orb.create_wstring_tc (0);
    }

    public String extract_wstring()
    {
        checkExtract (TCKind._tk_wstring, "Cannot extract wstring");

        checkNull();

        if (value instanceof String)
        {
            return (String) value;
        }
        else if (value instanceof StringHolder)
        {
            return ((StringHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_wstring();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // fixed

    public void insert_fixed (BigDecimal fixed)
    {
        value = fixed;
        typeCode = (new org.omg.CORBA.FixedHolder(fixed))._type();
    }

   public void insert_fixed(BigDecimal fixed,
                            org.omg.CORBA.TypeCode type)
   {
       try
       {
          String val = fixed.toString();
          int extra = fixed.scale() - type.fixed_scale();
          if ( extra > 0 )
          {
             // truncate the value to fit the scale of the typecode
             val = val.substring( 0, val.length() - extra );
          }
          else if ( extra < 0 )
          {
             StringBuffer sb = new StringBuffer (val);

             // add the decimal point if necessary
             if ( val.indexOf('.') == -1 )
             {
                sb.append('.');
             }

             // pad the value with zeros to fit the scale of the typecode
             for ( int i = extra; i < 0; i++ )
             {
                sb.append('0');
             }
             val = sb.toString();
          }
          BigDecimal tmp = new BigDecimal( val );

          org.omg.CORBA.FixedHolder holder =
             new org.omg.CORBA.FixedHolder( tmp );
          org.omg.CORBA.TypeCode tc = holder._type();

          if ( tc.fixed_digits() > type.fixed_digits() )
          {
             throw new org.omg.CORBA.BAD_TYPECODE();
          }

          value = tmp;
          typeCode = type;
       }
       catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
       {
          throw new org.omg.CORBA.BAD_TYPECODE();
       }
   }

    public java.math.BigDecimal extract_fixed()
    {
        checkExtract (TCKind._tk_fixed, "Cannot extract fixed");

        checkNull();

        if (value instanceof BigDecimal)
        {
            return (BigDecimal) value;
        }
        else if (value instanceof FixedHolder)
        {
            return ((FixedHolder) value).value;
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_fixed();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // obj refs

    public void insert_Object(org.omg.CORBA.Object obj)
    {
        String typeId = null;
        String name = "";

        if (obj == null)
        {
           typeId = "IDL:omg.org/CORBA/Object:1.0";
           name = "Object";
        }
        else
        {
           typeId = ((org.omg.CORBA.portable.ObjectImpl)obj)._ids()[0];

           // check if the repository Id is in IDL format
           if (typeId.startsWith("IDL:"))
           {
              // parse the interface name from a repository Id string
              // like "IDL:some.prefix/Some/Module/TheInterfaceName"
              name = typeId.substring(4, typeId.lastIndexOf (':'));
              name = name.substring(name.lastIndexOf ('/') + 1);
           }
           else if (typeId.startsWith("RMI:"))
           {
              // parse the interface name from a repository Id string
              // like "RMI:some.java.package.TheInterfaceName"
              name = typeId.substring(4, typeId.lastIndexOf(':'));
              name = name.substring(name.lastIndexOf('.') + 1);
           }
           else
           {
              throw new org.omg.CORBA.BAD_PARAM("Unknown repository id format");
           }
        }
        typeCode = orb.create_interface_tc( typeId , name );
        value = obj;
    }

    public void insert_Object (org.omg.CORBA.Object obj,
                               org.omg.CORBA.TypeCode type)
    {
        if( type.kind().value() != TCKind._tk_objref )
        {
            tc_error("Illegal, non-object TypeCode!");
        }

        value = obj;
        typeCode = type;
    }

    public org.omg.CORBA.Object extract_Object()
    {
        checkExtract (TCKind._tk_objref, "Cannot extract object");

        if (value == null)
        {
            //return null directly, saves cast
            return null;
        }
        if (value instanceof org.omg.CORBA.Object)
        {
            return (org.omg.CORBA.Object) value;
        }
        else if (value instanceof Streamable)
        {
            Class valueClass = value.getClass();
            try
            {
                Field field = valueClass.getDeclaredField("value");
                return (org.omg.CORBA.Object) field.get(value);
            }
            catch(Exception e)
            {
                throw new INTERNAL(
                    "Failed to retrieve value from Holder via reflection: " +
                    e);
            }
        }
        else if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_Object();
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }

    // workaround: as long as local objects don't have stubs, we need to
    // return *Java* objects

    public java.lang.Object extract_objref()
    {
        checkExtract (TCKind._tk_objref, "Cannot extract object");
        return value;
    }

    // Principal (deprecated)

    public void insert_Principal (org.omg.CORBA.Principal p)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("Principal deprecated");
    }

    public org.omg.CORBA.Principal extract_Principal()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("Principal deprecated");
    }

    public void insert_Streamable (org.omg.CORBA.portable.Streamable s)
    {
        int kind = s._type().kind().value();
        if (kind == TCKind._tk_value ||
            kind == TCKind._tk_value_box ||
            kind == TCKind._tk_abstract_interface ||
            kind == TCKind._tk_null)
        {
            throw new NO_IMPLEMENT(
                "No support for valuetypes through streamable interface");
        }

        value = s;
        typeCode = s._type();
    }

    public org.omg.CORBA.portable.Streamable extract_Streamable()
        throws org.omg.CORBA.BAD_INV_ORDER
    {
        if (value instanceof org.omg.CORBA.portable.Streamable)
        {
            return (org.omg.CORBA.portable.Streamable) value;
        }
        else if (value == null)
        {
            throw new BAD_OPERATION("No value has previously been inserted");
        }
        else
        {
            throw new org.omg.CORBA.BAD_INV_ORDER(
                "Any value is not a Streamable, but a " + value.getClass());
        }
    }

    public java.io.Serializable extract_Value()
        throws org.omg.CORBA.BAD_OPERATION
    {
        int kind = typeCode.kind().value();
        if (kind != TCKind._tk_value &&
            kind != TCKind._tk_value_box &&
            kind != TCKind._tk_abstract_interface &&
            kind != TCKind._tk_null)
        {
            tc_error ("Cannot extract value!");
        }

        if (value == null)
        {
            //return null directly, saves cast
            return null;
        }
        else if (value instanceof Serializable)
        {
            return (Serializable) value;
        }
        else if (value instanceof Streamable)
        {
            Class valueClass = value.getClass();
            try
            {
                Field field = valueClass.getDeclaredField("value");
                return (Serializable) field.get(value);
            }
            catch(Exception e)
            {
                throw new INTERNAL(
                    "Failed to retrieve value from Holder via reflection: " +
                    e);
            }
        }
        else
        {
            throw new INTERNAL("Encountered unexpected type of value: " +
                value.getClass());
        }
    }


    public void insert_Value(Serializable value)
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

    public void insert_Value(Serializable value, org.omg.CORBA.TypeCode type)
        throws org.omg.CORBA.MARSHAL
    {
        this.value    = value;
        this.typeCode = type;
    }

    // portable

    public org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        if(orb instanceof org.jacorb.orb.ORB)
        {
            value = new CDROutputStream(orb);
        }
        else
        {
            value = new CDROutputStream();
        }

        return (CDROutputStream)value;
    }

    public org.omg.CORBA.portable.InputStream create_input_stream()
    {
        if(value instanceof org.jacorb.orb.CDROutputStream)
        {
            return new org.jacorb.orb.CDRInputStream( orb, ((CDROutputStream)value).getBufferCopy());
        }

        final org.jacorb.orb.CDROutputStream out;

        if(orb instanceof org.jacorb.orb.ORB)
        {
            out = new org.jacorb.orb.CDROutputStream(orb);
        }
        else
        {
            out = new org.jacorb.orb.CDROutputStream();
        }

        try
        {
            write_value(out);
            return new org.jacorb.orb.CDRInputStream(orb, out.getBufferCopy());
        }
        finally
        {
            out.close();
        }
    }

    public void read_value (org.omg.CORBA.portable.InputStream input,
                            org.omg.CORBA.TypeCode type)
        throws org.omg.CORBA.MARSHAL
    {
        if (type == null)
        {
           throw new org.omg.CORBA.BAD_PARAM("TypeCode is null");
        }
        typeCode = type;

        int kind = type.kind().value();
        switch (kind)
        {
            case TCKind._tk_null:       // 0
            {
                break;
            }
            case TCKind._tk_void:       // 1
            {
                break;
            }
            case TCKind._tk_short:      // 2
            {
                insert_short( input.read_short());
                break;
            }
            case TCKind._tk_long:       // 3
            {
                insert_long( input.read_long());
                break;
            }
            case TCKind._tk_ushort:     // 4
            {
                insert_ushort(input.read_ushort());
                break;
            }
            case TCKind._tk_ulong:      // 5
            {
                insert_ulong( input.read_ulong());
                break;
            }
            case TCKind._tk_float:      // 6
            {
                insert_float( input.read_float());
                break;
            }
            case TCKind._tk_double:     // 7
            {
                insert_double( input.read_double());
                break;
            }
            case TCKind._tk_boolean:    // 8
            {
                insert_boolean( input.read_boolean());
                break;
            }
            case TCKind._tk_char:       // 9
            {
                insert_char( input.read_char());
                break;
            }
            case TCKind._tk_octet:      // 10
            {
                insert_octet( input.read_octet());
                break;
            }
            case TCKind._tk_any:        // 11
            {
                insert_any( input.read_any());
                break;
            }
            case TCKind._tk_TypeCode:   // 12
            {
                insert_TypeCode( input.read_TypeCode());
                break;
            }
            case TCKind._tk_Principal:  // 13
            {
                throw new org.omg.CORBA.NO_IMPLEMENT ("Principal deprecated");
            }
            case TCKind._tk_objref:     // 14
            {
                insert_Object( input.read_Object());
                break;
            }
            case TCKind._tk_struct:     // 15
                // fallthrough
            case TCKind._tk_union:      // 16
                // fallthrough
            case TCKind._tk_enum:       // 17
            {
                CDROutputStream out = new CDROutputStream(orb);
                out.write_value(type, input);
                value = out;
                break;
            }
            case TCKind._tk_string:     // 18
            {
                insert_string( input.read_string());
                break;
            }
            case TCKind._tk_sequence:   // 19
                // fallthrough
            case TCKind._tk_array:      // 20
                // fallthrough
            case TCKind._tk_alias:      // 21
                // fallthrough
            case TCKind._tk_except:     // 22
            {
                CDROutputStream out = new CDROutputStream(orb);
                out.write_value(type, input);
                value = out;
                break;
            }
            case TCKind._tk_longlong:   // 23
            {
                insert_longlong( input.read_longlong());
                break;
            }
            case TCKind._tk_ulonglong:  // 24
            {
                insert_ulonglong( input.read_ulonglong());
                break;
            }
            case TCKind._tk_longdouble: // 25
            {
                throw new org.omg.CORBA.BAD_TYPECODE(
                "type longdouble not supported in java");
            }
            case TCKind._tk_wchar:      // 26
            {
                insert_wchar( input.read_wchar());
                break;
            }
            case TCKind._tk_wstring:    // 27
            {
                insert_wstring( input.read_wstring());
                break;
            }
            case TCKind._tk_fixed:      // 28
            {
                try
                {
                    // move the decimal based on the scale
                    java.math.BigDecimal fixed = input.read_fixed();
                    int scale = type.fixed_scale();
                    insert_fixed( fixed.movePointLeft( scale ), type );
                }
                catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
                {
                    throw new INTERNAL("should never happen");
                }
                break;
            }
            case TCKind._tk_value:      // 29
            case TCKind._tk_value_box:  // 30
            {
                insert_Value
                (((org.omg.CORBA_2_3.portable.InputStream)input).read_value(),
                        type);
                break;
            }
            case TCKind._tk_native:     //31
            {
                throw new BAD_TYPECODE(
                        "Cannot handle TypeCode with kind " + kind);
            }
            case TCKind._tk_abstract_interface: // 32
            {
                java.lang.Object obj =
                    ((org.omg.CORBA_2_3.portable.InputStream)input).read_abstract_interface();
                if (obj instanceof org.omg.CORBA.Object)
                {
                    insert_Object((org.omg.CORBA.Object)obj);
                }
                else
                {
                    insert_Value((java.io.Serializable)obj);
                }
                break;
            }
            default:
            {
                throw new BAD_TYPECODE("Cannot handle TypeCode with kind " + kind);
            }
        }
    }

    public void write_value (org.omg.CORBA.portable.OutputStream output)
    {
        final int kind = typeCode.kind().value();

        if (value instanceof Streamable &&
                kind != TCKind._tk_value &&
                kind != TCKind._tk_value_box &&
                kind != TCKind._tk_abstract_interface &&
                kind != TCKind._tk_null)
        {
            ((Streamable) value)._write(output);
        }
        else
        {
            switch (kind)
            {
                case TCKind._tk_null:       // 0
                case TCKind._tk_void:       // 1
                {
                    break;
                }
                case TCKind._tk_short:      // 2
                {
                    output.write_short(extract_short());
                    break;
                }
                case TCKind._tk_long:       // 3
                {
                    output.write_long(extract_long());
                    break;
                }
                case TCKind._tk_ushort:     // 4
                {
                    output.write_ushort(extract_ushort());
                    break;
                }
                case TCKind._tk_ulong:      // 5
                {
                    output.write_ulong(extract_ulong());
                    break;
                }
                case TCKind._tk_float:      // 6
                {
                    output.write_float(extract_float());
                    break;
                }
                case TCKind._tk_double:     // 7
                {
                    output.write_double(extract_double());
                    break;
                }
                case TCKind._tk_boolean:    // 8
                {
                    output.write_boolean(extract_boolean());
                    break;
                }
                case TCKind._tk_char:       // 9
                {
                    output.write_char(extract_char());
                    break;
                }
                case TCKind._tk_octet:      // 10
                {
                    output.write_octet(extract_octet());
                    break;
                }
                case TCKind._tk_any:        // 11
                {
                    output.write_any(extract_any());
                    break;
                }
                case TCKind._tk_TypeCode:   // 12
                {
                    output.write_TypeCode(extract_TypeCode());
                    break;
                }
                case TCKind._tk_Principal:  // 13
                {
                    throw new org.omg.CORBA.NO_IMPLEMENT(
                    "Principal deprecated");
                }
                case TCKind._tk_objref:     // 14
                {
                    output.write_Object(extract_Object());
                    break;
                }
                case TCKind._tk_struct:     // 15
                case TCKind._tk_union:      // 16
                case TCKind._tk_enum:       // 17
                {
                    this.writeComplexValue(output);
                    break;
                }
                case TCKind._tk_string:     // 18
                {
                    output.write_string(extract_string());
                    break;
                }
                case TCKind._tk_sequence:   // 19
                case TCKind._tk_array:      // 20
                case TCKind._tk_alias:      // 21
                case TCKind._tk_except:     // 22
                {
                    this.writeComplexValue(output);
                    break;
                }
                case TCKind._tk_longlong:   // 23
                {
                    output.write_longlong(extract_longlong());
                    break;
                }
                case TCKind._tk_ulonglong:  // 24
                {
                    output.write_ulonglong(extract_ulonglong());
                    break;
                }
                case TCKind._tk_longdouble: // 25
                {
                    throw new org.omg.CORBA.BAD_TYPECODE(
                    "type longdouble not supported in java");
                }
                case TCKind._tk_wchar:      // 26
                {
                    output.write_wchar(extract_wchar());
                    break;
                }
                case TCKind._tk_wstring:    // 27
                {
                    output.write_wstring(extract_wstring());
                    break;
                }
                case TCKind._tk_fixed:     // 28
                {
                    output.write_fixed(extract_fixed());
                    break;
                }
                case TCKind._tk_value:      // 29
                case TCKind._tk_value_box:  // 30
                {
                    final OutputStream outputStream = ((org.omg.CORBA_2_3.portable.OutputStream)output);
                    final Serializable serializable = (Serializable)value;
                    outputStream.write_value (serializable);
                    break;
                }
                case TCKind._tk_native:     //31
                {
                    throw new BAD_TYPECODE(
                            "Cannot handle TypeCode with kind " + kind);
                }
                case TCKind._tk_abstract_interface:  //32
                {
                    ((org.omg.CORBA_2_3.portable.OutputStream)output)
                    .write_abstract_interface(value);
                    break;
                }
                default:
                {
                    throw new BAD_TYPECODE("Cannot handle TypeCode with kind "
                            + kind);
                }
            }
        }
    }

    // other, proprietary

    public void insert_void()
    {
        typeCode = orb.get_primitive_tc(TCKind.tk_void);
        value = null;
    }

    /**
     * Convenience method for making a shallow copy of an Any.
     */
    public void insert_object(org.omg.CORBA.TypeCode typeCode,
                             java.lang.Object object)
    {
        this.typeCode = typeCode;
        this.value = object;
    }

    private void writeComplexValue (org.omg.CORBA.portable.OutputStream output)
    {
        if (value instanceof org.omg.CORBA.portable.Streamable)
        {
            org.omg.CORBA.portable.Streamable streamable =
                (org.omg.CORBA.portable.Streamable)value;
            streamable._write (output);
        }
        else if (value instanceof org.omg.CORBA.portable.OutputStream)
        {
            // Use ORB from CDROutputStream if Any has been created from
            // ORBSingleton.
            org.omg.CORBA.ORB toUse = orb;

            if ( ! (toUse instanceof org.jacorb.orb.ORB))
            {
                checkStreamClass (output);
                toUse = ((CDROutputStream) output).orb();
            }
            checkStreamClass ((org.omg.CORBA.portable.OutputStream)value);
            CDROutputStream out = (CDROutputStream) value;
            final CDRInputStream in = new CDRInputStream(toUse, out.getBufferCopy ());

            try
            {
                in.read_value (typeCode, output);
            }
            finally
            {
                in.close();
            }
        }
        else
        {
            throw new org.omg.CORBA.INTERNAL(
                "Encountered unexpected type for any value: " +
                value.getClass());
        }
    }

    /**
     * <code>checkStreamClass</code> is used to provide a sanity check and some
     * debugging on the type of stream class.
     *
     * @param stream an <code>org.omg.CORBA.portable.OutputStream</code> value
     */
    private void checkStreamClass (org.omg.CORBA.portable.OutputStream stream)
    {
        if ( ! (stream instanceof CDROutputStream))
        {
            throw new INTERNAL
            (
                "Output class not CDROutputStream " +
                stream.getClass().getName()
            );
        }
    }


    private boolean compareComplexValue(org.omg.CORBA.Any other)
    {
        final CDROutputStream thisStream;
        if (value instanceof CDROutputStream)
        {
            thisStream = (CDROutputStream)value;
        }
        else
        {
            thisStream = new CDROutputStream(orb);
            write_value(thisStream);
        }

        final CDROutputStream otherStream;
        if (other instanceof Any &&
            ((Any) other).value instanceof CDROutputStream)
        {
            otherStream = (CDROutputStream) ((Any) other).value;
        }
        else
        {
            otherStream = new CDROutputStream(orb);
            other.write_value( otherStream );
        }

        return Arrays.equals( thisStream.getBufferCopy(),
                              otherStream.getBufferCopy());
    }
}
