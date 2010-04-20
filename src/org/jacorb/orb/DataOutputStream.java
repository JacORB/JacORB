package org.jacorb.orb;

import org.omg.CORBA.Any;
import org.omg.CORBA.BadFixedValue;
import org.omg.CORBA.NO_IMPLEMENT;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004  Gerald Brose.
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

/**
 * This class is the implementation of DataOutputStream used for
 * custom marshalling of value type.
 *
 * It simply delegates to OutputStream all functions.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class DataOutputStream
    implements org.omg.CORBA.DataOutputStream
{
    /**
     * Reference to the InputStream
     */
    private final org.omg.CORBA.portable.OutputStream delegate;

    /**
     * Constructor
     */
    public DataOutputStream( org.omg.CORBA.portable.OutputStream ostream )
    {
        delegate = ostream;
    }

    /**
     * List of truncatable _ids
     */
    public String [] _truncatable_ids()
    {
        return null;
    }

    /**
     * Operation write_any
     */
    public void write_any( org.omg.CORBA.Any value )
    {
        delegate.write_any( value );
    }

    /**
     * Operation write_boolean
     */
    public void write_boolean( boolean value )
    {
        delegate.write_boolean( value );
    }

    /**
     * Operation write_char
     */
    public void write_char( char value )
    {
        delegate.write_char( value );
    }

    /**
     * Operation write_wchar
     */
    public void write_wchar( char value )
    {
        delegate.write_wchar( value );
    }

    /**
     * Operation write_octet
     */
    public void write_octet( byte value )
    {
        delegate.write_octet( value );
    }

    /**
     * Operation write_short
     */
    public void write_short( short value )
    {
        delegate.write_short( value );
    }

    /**
     * Operation write_ushort
     */
    public void write_ushort( short value )
    {
        delegate.write_ushort( value );
    }

    /**
     * Operation write_long
     */
    public void write_long( int value )
    {
        delegate.write_long( value );
    }

    /**
     * Operation write_ulong
     */
    public void write_ulong( int value )
    {
        delegate.write_ulong( value );
    }

    /**
     * Operation write_longlong
     */
    public void write_longlong( long value )
    {
        delegate.write_longlong( value );
    }

    /**
     * Operation write_ulonglong
     */
    public void write_ulonglong( long value )
    {
        delegate.write_ulonglong( value );
    }

    /**
     * Operation write_float
     */
    public void write_float( float value )
    {
        delegate.write_float( value );
    }

    /**
     * Operation write_double
     */
    public void write_double( double value )
    {
        delegate.write_double( value );
    }

    /**
     * Operation write_longdouble. This is not implemented.
     */
    public void write_longdouble( double value )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation write_string
     */
    public void write_string( java.lang.String value )
    {
        delegate.write_string( value );
    }

    /**
     * Operation write_wstring
     */
    public void write_wstring( java.lang.String value )
    {
        delegate.write_wstring( value );
    }

    /**
     * Operation write_Object
     */
    public void write_Object( org.omg.CORBA.Object value )
    {
        delegate.write_Object( value );
    }

    /**
     * Operation write_Abstract
     */
    public void write_Abstract( java.lang.Object value )
    {
        ( ( org.omg.CORBA_2_3.portable.OutputStream ) delegate ).write_abstract_interface( value );
    }

    /**
     * Operation write_value
     */
    public void write_Value( java.io.Serializable value )
    {
        ( ( org.omg.CORBA_2_3.portable.OutputStream ) delegate ).write_value( value );
    }

    /**
     * Operation write_TypeCode
     */
    public void write_TypeCode( org.omg.CORBA.TypeCode value )
    {
        delegate.write_TypeCode( value );
    }

    /**
     * Operation write_any_array
     */
    public void write_any_array( org.omg.CORBA.Any[] seq, int offset, int length )
    {
        for ( int i = offset ; i < offset + length ; i++ )
        {
            delegate.write_any( seq[ i ] );
        }
    }

    /**
     * Operation write_boolean_array
     */
    public void write_boolean_array( boolean[] seq, int offset, int length )
    {
        delegate.write_boolean_array( seq, offset, length );
    }

    /**
     * Operation write_char_array
     */
    public void write_char_array( char[] seq, int offset, int length )
    {
        delegate.write_char_array( seq, offset, length );
    }

    /**
     * Operation write_wchar_array
     */
    public void write_wchar_array( char[] seq, int offset, int length )
    {
        delegate.write_wchar_array( seq, offset, length );
    }

    /**
     * Operation write_octet_array
     */
    public void write_octet_array( byte[] seq, int offset, int length )
    {
        delegate.write_octet_array( seq, offset, length );
    }

    /**
     * Operation write_short_array
     */
    public void write_short_array( short[] seq, int offset, int length )
    {
        delegate.write_short_array( seq, offset, length );
    }

    /**
     * Operation write_ushort_array
     */
    public void write_ushort_array( short[] seq, int offset, int length )
    {
        delegate.write_ushort_array( seq, offset, length );
    }

    /**
     * Operation write_long_array
     */
    public void write_long_array( int[] seq, int offset, int length )
    {
        delegate.write_long_array( seq, offset, length );
    }

    /**
     * Operation write_ulong_array
     */
    public void write_ulong_array( int[] seq, int offset, int length )
    {
        delegate.write_ulong_array( seq, offset, length );
    }

    /**
     * Operation write_longlong_array
     */
    public void write_longlong_array( long[] seq, int offset, int length )
    {
        delegate.write_longlong_array( seq, offset, length );
    }

    /**
     * Operation write_ulonglong_array
     */
    public void write_ulonglong_array( long[] seq, int offset, int length )
    {
        delegate.write_ulonglong_array( seq, offset, length );
    }

    /**
     * Operation write_float_array
     */
    public void write_float_array( float[] seq, int offset, int length )
    {
        delegate.write_float_array( seq, offset, length );
    }

    /**
     * Operation write_double_array
     */
    public void write_double_array( double[] seq, int offset, int length )
    {
        delegate.write_double_array( seq, offset, length );
    }

   public void write_fixed (Any fixedValue) throws BadFixedValue
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public void write_fixed_array (Any[] seq, int offset, int length) throws BadFixedValue
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public void write_long_double_array (double[] seq, int offset, int length)
   {
      throw new NO_IMPLEMENT ("NYI");
   }
}
