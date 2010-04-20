package org.jacorb.orb;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnySeqHolder;
import org.omg.CORBA.BadFixedValue;
import org.omg.CORBA.DoubleSeqHolder;
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
 * This class is the implementation of DataInputStream used for custom marshalling
 * of value type.
 *
 * It simply delegates to InputStream all functions.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class DataInputStream
    implements org.omg.CORBA.DataInputStream
{
    private final org.omg.CORBA.portable.InputStream delegate;

    public DataInputStream( org.omg.CORBA.portable.InputStream istream )
    {
        delegate = istream;
    }

    /**
     * List of truncatable _ids
     */
    public String [] _truncatable_ids()
    {
        return null;
    }

    /**
     * Operation read_any
     */
    public org.omg.CORBA.Any read_any()
    {
        return delegate.read_any();
    }

    /**
     * Operation read_boolean
     */
    public boolean read_boolean()
    {
        return delegate.read_boolean();
    }

    /**
     * Operation read_char
     */
    public char read_char()
    {
        return delegate.read_char();
    }

    /**
     * Operation read_wchar
     */
    public char read_wchar()
    {
        return delegate.read_wchar();
    }

    /**
     * Operation read_octet
     */
    public byte read_octet()
    {
        return delegate.read_octet();
    }

    /**
     * Operation read_short
     */
    public short read_short()
    {
        return delegate.read_short();
    }

    /**
     * Operation read_ushort
     */
    public short read_ushort()
    {
        return delegate.read_ushort();
    }

    /**
     * Operation read_long
     */
    public int read_long()
    {
        return delegate.read_long();
    }

    /**
     * Operation read_ulong
     */
    public int read_ulong()
    {
        return delegate.read_ulong();
    }

    /**
     * Operation read_longlong
     */
    public long read_longlong()
    {
        return delegate.read_longlong();
    }

    /**
     * Operation read_ulonglong
     */
    public long read_ulonglong()
    {
        return delegate.read_ulonglong();
    }

    /**
     * Operation read_float
     */
    public float read_float()
    {
        return delegate.read_float();
    }

    /**
     * Operation read_double
     */
    public double read_double()
    {
        return delegate.read_double();
    }

    /**
     * Operation read_longdouble. This is not implemented.
     */
    public double read_longdouble()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation read_string
     */
    public java.lang.String read_string()
    {
        return delegate.read_string();
    }

    /**
     * Operation read_wstring
     */
    public java.lang.String read_wstring()
    {
        return delegate.read_wstring();
    }

    /**
     * Operation read_Object
     */
    public org.omg.CORBA.Object read_Object()
    {
        return delegate.read_Object();
    }

    /**
     * Operation read_Abstract
     */
    public java.lang.Object read_Abstract()
    {
        return ( ( org.omg.CORBA_2_3.portable.InputStream ) delegate ).read_abstract_interface();
    }

    /**
     * Operation read_value
     */
    public java.io.Serializable read_Value()
    {
        return ( ( org.omg.CORBA_2_3.portable.InputStream ) delegate ).read_value();
    }

    /**
     * Operation read_TypeCode
     */
    public org.omg.CORBA.TypeCode read_TypeCode()
    {
        return delegate.read_TypeCode();
    }

    /**
     * Operation read_any_array
     */
    public void read_any_array( org.omg.CORBA.AnySeqHolder seq, int offset, int length )
    {
        for ( int i = offset ; i < offset + length ; i++ )
        {
            seq.value[ i ] = delegate.read_any();
        }
    }

    /**
     * Operation read_boolean_array
     */
    public void read_boolean_array( org.omg.CORBA.BooleanSeqHolder seq, int offset, int length )
    {
        delegate.read_boolean_array( seq.value, offset, length );
    }

    /**
     * Operation read_char_array
     */
    public void read_char_array( org.omg.CORBA.CharSeqHolder seq, int offset, int length )
    {
        delegate.read_char_array( seq.value, offset, length );
    }

    /**
     * Operation read_wchar_array
     */
    public void read_wchar_array( org.omg.CORBA.WCharSeqHolder seq, int offset, int length )
    {
        delegate.read_wchar_array( seq.value, offset, length );
    }

    /**
     * Operation read_octet_array
     */
    public void read_octet_array( org.omg.CORBA.OctetSeqHolder seq, int offset, int length )
    {
        delegate.read_octet_array( seq.value, offset, length );
    }

    /**
     * Operation read_short_array
     */
    public void read_short_array( org.omg.CORBA.ShortSeqHolder seq, int offset, int length )
    {
        delegate.read_short_array( seq.value, offset, length );
    }

    /**
     * Operation read_ushort_array
     */
    public void read_ushort_array( org.omg.CORBA.UShortSeqHolder seq, int offset, int length )
    {
        delegate.read_ushort_array( seq.value, offset, length );
    }

    /**
     * Operation read_long_array
     */
    public void read_long_array( org.omg.CORBA.LongSeqHolder seq, int offset, int length )
    {
        delegate.read_long_array( seq.value, offset, length );
    }

    /**
     * Operation read_ulong_array
     */
    public void read_ulong_array( org.omg.CORBA.ULongSeqHolder seq, int offset, int length )
    {
        delegate.read_ulong_array( seq.value, offset, length );
    }

    /**
     * Operation read_longlong_array
     */
    public void read_longlong_array( org.omg.CORBA.LongLongSeqHolder seq, int offset, int length )
    {
        delegate.read_longlong_array( seq.value, offset, length );
    }

    /**
     * Operation read_ulonglong_array
     */
    public void read_ulonglong_array( org.omg.CORBA.ULongLongSeqHolder seq, int offset, int length )
    {
        delegate.read_ulonglong_array( seq.value, offset, length );
    }

    /**
     * Operation read_float_array
     */
    public void read_float_array( org.omg.CORBA.FloatSeqHolder seq, int offset, int length )
    {
        delegate.read_float_array( seq.value, offset, length );
    }

    /**
     * Operation read_double_array
     */
    public void read_double_array( org.omg.CORBA.DoubleSeqHolder seq, int offset, int length )
    {
        delegate.read_double_array( seq.value, offset, length );
    }

   public Any read_fixed (short digits, short scale) throws BadFixedValue
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public void read_fixed_array (AnySeqHolder seq, int offset, int length, short digits, short scale)
            throws BadFixedValue
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public void read_long_double_array (DoubleSeqHolder seq, int offset, int length)
   {
      throw new NO_IMPLEMENT ("NYI");
   }
}
