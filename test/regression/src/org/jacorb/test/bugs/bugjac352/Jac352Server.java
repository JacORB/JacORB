package org.jacorb.test.bugs.bugjac352;

import org.jacorb.test.bugs.bug352.JAC352POA;

public class Jac352Server extends JAC352POA
{
    public String bounce_wstringvalue(String value)
    {
        return value;
    }

    public String bounce_stringvalue(String value)
    {
        return value;
    }

    public String[] bounce_wstrings(String[] value)
    {
        return value;
    }
}
