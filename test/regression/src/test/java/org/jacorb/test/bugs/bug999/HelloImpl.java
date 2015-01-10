package org.jacorb.test.bugs.bug999;

public final class HelloImpl extends HelloPOA
{
    @Override
    public int inputString(String s1)
    {
        return (s1.length());
    }

    @Override
    public void inputData(Data[] ds1)
    {

    }

    @Override
    public void sayHello()
    {

    }

    @Override
    public void sayGoodbye()
    {

    }
}
