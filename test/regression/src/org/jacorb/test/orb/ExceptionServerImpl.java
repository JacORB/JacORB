package org.jacorb.test.orb;

import org.jacorb.test.ExceptionServerPOA;
import org.jacorb.test.MyUserException;
import org.jacorb.test.NonEmptyException;

public class ExceptionServerImpl extends ExceptionServerPOA
{
    public void throwRuntimeException(String message)
    {
        throw new RuntimeException(message);
    }

    public void throwUserExceptionWithMessage1(int f1, String f2) throws NonEmptyException
    {
        throw new NonEmptyException (f1, f2);
    }

    public void throwUserException() throws MyUserException
    {
        throw new MyUserException();
    }

    public void throwUserExceptionWithMessage2(String reason, String message) throws MyUserException
    {
        throw new MyUserException(reason, message);
    }
}
