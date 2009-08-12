package org.jacorb.test.orb;

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

import org.jacorb.test.BasicServerPOA;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ByteHolder;
import org.omg.CORBA.DoubleHolder;
import org.omg.CORBA.FloatHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.LongHolder;
import org.omg.CORBA.ShortHolder;

public class BasicServerImpl extends BasicServerPOA
{
    public void ping()
    {
        return;
    }

    public long bounce_long_long(long x)
    {
        return x;
    }

    public int bounce_long(int x)
    {
        return x;
    }

    public short bounce_short(short x)
    {
        return x;
    }

    public long bounce_unsigned_long_long(long x)
    {
        return x;
    }

    public int bounce_unsigned_long(int x)
    {
        return x;
    }

    public short bounce_unsigned_short(short x)
    {
        return x;
    }

    public void pass_in_long_long(long x)
    {
        // nothing
    }

    public void pass_in_long(int x)
    {
        // nothing
    }

    public void pass_in_short(short x)
    {
        // nothing
    }

    public void pass_in_unsigned_long_long(long x)
    {
        // nothing
    }

    public void pass_in_unsigned_long(int x)
    {
        // nothing
    }

    public void pass_in_unsigned_short(short x)
    {
        // nothing
    }

    public void pass_inout_long_long(LongHolder x)
    {
        x.value = x.value + 1;
    }

    public void pass_inout_long(IntHolder x)
    {
        x.value = x.value + 1;
    }

    public void pass_inout_short(ShortHolder x)
    {
        x.value = ( short ) ( x.value + 1 );
    }

    public void pass_inout_unsigned_long_long(LongHolder x)
    {
        x.value = x.value + 1;
    }

    public void pass_inout_unsigned_long(IntHolder x)
    {
        x.value = x.value + 1;
    }

    public void pass_inout_unsigned_short(ShortHolder x)
    {
        x.value = ( short ) ( x.value + 1 );
    }

    public void pass_out_long_long(LongHolder x)
    {
        x.value = 84;
    }

    public void pass_out_long(IntHolder x)
    {
        x.value = 83;
    }

    public void pass_out_short(ShortHolder x)
    {
        x.value = 82;
    }

    public void pass_out_unsigned_long_long(LongHolder x)
    {
        x.value = 81;
    }

    public void pass_out_unsigned_long(IntHolder x)
    {
        x.value = 80;
    }

    public void pass_out_unsigned_short(ShortHolder x)
    {
        x.value = 79;
    }

    public long return_long_long()
    {
        return 0xffeeddccbbaa0099L;
    }

    public int return_long()
    {
        return -17;
    }

    public short return_short()
    {
        return -4;
    }

    public long return_unsigned_long_long()
    {
        return 0xffeeddccbbaa0088L;
    }

    public int return_unsigned_long()
    {
        return 43;
    }

    public short return_unsigned_short()
    {
        return 87;
    }

    public boolean bounce_boolean(boolean x)
    {
        return x;
    }

    public void pass_in_boolean(boolean x)
    {
        // nothing
    }

    public void pass_inout_boolean(BooleanHolder x)
    {
        x.value = !x.value;
    }

    public void pass_out_boolean(BooleanHolder x)
    {
        x.value = true;
    }

    public boolean return_boolean()
    {
        return true;
    }

    public byte bounce_octet(byte x)
    {
        return x;
    }

    public void pass_in_octet(byte x)
    {
        // nothing
    }

    public void pass_inout_octet(ByteHolder x)
    {
        x.value = (byte) (x.value + 1);
    }

    public void pass_out_octet(ByteHolder x)
    {
        x.value = 23;
    }

    public byte return_octet()
    {
        return (byte)0xf0;
    }


    public double bounce_double(double x)
    {
        return x;
    }

    public float bounce_float(float x)
    {
        return x;
    }

    public void pass_in_double(double x)
    {
        // nothing
    }

    public void pass_in_float(float x)
    {
        // nothing
    }

    public void pass_inout_double(DoubleHolder x)
    {
        x.value = x.value + 1.0;
    }

    public void pass_inout_float(FloatHolder x)
    {
        x.value = x.value + 1.0F;
    }

    public void pass_out_double(DoubleHolder x)
    {
        x.value = 1234E12;
    }

    public void pass_out_float(FloatHolder x)
    {
        x.value = 0.005F;
    }

    public double return_double()
    {
        return 1E-100;
    }

    public float return_float()
    {
        return 1.5E-1F;
    }

    public String bounce_string(String value)
    {
        return value;
    }

    public String bounce_wstring(String value)
    {
        return value;
    }
}
