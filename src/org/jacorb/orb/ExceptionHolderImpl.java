package org.jacorb.orb;

import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.UserException;

public class ExceptionHolderImpl extends org.omg.Messaging.ExceptionHolder
{
    public ExceptionHolderImpl ()
    {
        super();        
    }

    public void raise_exception() throws UserException
    {
    }

    public void raise_exception_with_list(ExceptionList exc_list)
        throws UserException
    {
    }
}
