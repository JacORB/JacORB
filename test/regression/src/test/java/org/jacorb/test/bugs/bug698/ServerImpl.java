package org.jacorb.test.bugs.bug698;

public class ServerImpl extends ServerPOA
{
    public Top sendTop (Top arg)
    {
        return arg;
    }

    public TestStruct sendTestStruct (TestStruct ts)
    {
        return ts;
    }
}
