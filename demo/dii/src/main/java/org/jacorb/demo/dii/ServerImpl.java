package org.jacorb.demo.dii;

import org.jacorb.demo.dii.ServerPackage.*;

public class ServerImpl
    extends ServerPOA
{
    int long_number = 47;
    boolean shutdown;

    public int long_number()
    {
	System.out.println("Returning " + long_number );
	return long_number;
    }

    public void long_number( int l)
    {
	System.out.println("Setting to " + l );
	long_number = l;
    }

    public void add(int i, int j, org.omg.CORBA.IntHolder r)
    {
	System.out.println("add: " + (i+j) );
	r.value = i + j;
    }

    public void _notify( String msg )
    {
	System.out.println("Notify: " + msg );
    }

    public String writeNumber( int i )
    {
	System.out.println("Number: " + i );
	return "Number written";
    }

    public String writeNumberWithEx( int i )
	throws e
    {
	System.out.println("Throwing Exception " );
	if( true )
	    throw new e("TestException");
	return "Number written";
    }

    public void shutdown ()
    {
        shutdown = true;
    }

    public boolean getShutdown ()
    {
        return shutdown;
    }
}
