package org.jacorb.test.orb;

import org.jacorb.Tests.CallbackServerPOA;
import org.omg.CORBA.CharHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;

public class CallbackServerImpl extends CallbackServerPOA
{

    public void ping()
    {
        return;
    }

    public void delayed_ping(int delay)
    {
        delay( delay );
        return;
    }

    public void pass_in_char(char x, int delay)
    {
        delay( delay );
    }

    public char return_char(short unicode_number, int delay)
    {
        delay( delay );
        return ( char ) unicode_number;
    }

    public int operation(CharHolder p1, IntHolder p2, boolean p3, int delay)
    {
        delay( delay );
        p1.value = Character.toUpperCase(p1.value);
        if ( p3 )
            p2.value = 1234;
        else
            p2.value = 4321;
        return p2.value;
    }

    private void delay( long time )
    {
        try
        {
            wait( time );
        }
        catch( InterruptedException e )
        {
            throw new RuntimeException( "delay interrupted" );
        }
    }        

}
