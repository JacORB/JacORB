package org.jacorb.test.orb;

import org.jacorb.test.ExceptionServerPOA;

public class ExceptionServerImpl extends ExceptionServerPOA
{

    public void throwRuntimeException(String message)
    {
        throw new RuntimeException(message);
    }

}
