package org.jacorb.test.bugs.bug698;

public class TopImpl extends Top
{
    private static final long serialVersionUID = 1L;

    public TopImpl ()
    {
        byteArray = new byte[5000];
        as = new A[] { new AImpl(), new AImpl() };
    }
}
