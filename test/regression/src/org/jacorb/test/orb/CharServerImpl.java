package org.jacorb.test.orb;

import org.jacorb.test.CharServerPOA;
import org.omg.CORBA.CharHolder;
import org.jacorb.test.CharServerPackage.wcharSeqHolder;
import org.jacorb.test.CharServerPackage.DataFlavour;
import org.jacorb.test.CharServerPackage.DataFlavourHelper;

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

    public char[] test_wchar_seq( char[] argin,
                                  wcharSeqHolder argout,
                                  wcharSeqHolder arginout)
    {
        test( argin );
        test( arginout.value );

        arginout.value = new char[]{ 'a', 'a' };
        argout.value = new char[]{ 'a', 'a' };

        return new char[]{ 'a', 'a' };
    }

    public org.omg.CORBA.Any return_dataflavour_inany(DataFlavour flavour)
    {
        if ( ! ( flavour.name.equals( "Test_Flavour" ) ) )
        {
            throw new RuntimeException( "Error - unexpected value for flavour" );
        }

        org.omg.CORBA.Any result = org.omg.CORBA.ORB.init().create_any();
        DataFlavourHelper.insert( result, flavour );
        return result;
    }

    private void test( char[] arg )
    {
        if ( arg[ 0 ] != 'a' && arg[ 1 ] != 'a'  )
        {
            throw new RuntimeException( "Error - arguments do not match expected value" );
       }
    }
}
