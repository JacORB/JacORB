package org.jacorb.test.orb;

import org.jacorb.Tests.CharServerPOA;
import org.omg.CORBA.CharHolder;

public class CharServerImpl extends CharServerPOA
{
    public char bounce_char(char x)
    {
        return x;
    }

    public char bounce_wchar(char x)
    {
        return x;
    }

    public short pass_in_char(char x)
    {
        return ( short ) x;
    }

    public short pass_in_wchar(char x)
    {
        return ( short ) x;
    }

    public void pass_inout_char(CharHolder x)
    {
        x.value = Character.toUpperCase( x.value );
    }

    public void pass_inout_wchar(CharHolder x)
    {
        x.value = Character.toUpperCase( x.value );
    }

    public void pass_out_char(short unicode_number, CharHolder x)
    {
        x.value = ( char ) unicode_number;
    }

    public void pass_out_wchar(short unicode_number, CharHolder x)
    {
        x.value = ( char ) unicode_number;
    }

    public char return_char(short unicode_number)
    {
        return ( char ) unicode_number;
    }

    public char return_wchar(short unicode_number)
    {
        return ( char ) unicode_number;
    }

}
