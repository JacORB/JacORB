package org.jacorb.test.dii;

import org.jacorb.test.dii.DIIServerPackage.DIIException;

public class ServerDelegate implements DIIServerOperations
{
    int long_number = 47;

    public int long_number()
    {
        return long_number;
    }

    public void long_number( int l)
    {
        long_number = l;
    }

    public void add(int i, int j, org.omg.CORBA.IntHolder r)
    {
        r.value = i + j;
    }

    public void _notify( String msg )
    {
    }

    public String writeNumber( int i )
    {
        return "Number written";
    }

    public void raiseException()
        throws DIIException
    {
        throw new DIIException("TestException");
    }
}
