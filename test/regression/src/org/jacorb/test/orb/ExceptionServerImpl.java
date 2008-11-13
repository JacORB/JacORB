package org.jacorb.test.orb;

import org.jacorb.test.*;

public class ExceptionServerImpl extends ExceptionServerPOA
{

    public void throwRuntimeException(String message)
    {
        throw new RuntimeException(message);
    }

    public void throwUserException(int f1, String f2) throws NonEmptyException
    {
        throw new NonEmptyException (f1, f2);
    }

}
