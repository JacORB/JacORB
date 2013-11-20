package org.jacorb.test.bugs.bug960;

public final class HelloImpl extends HelloPOA
{
    public void sayHello()
    {
        String hello = "Hello, World!";
        System.out.println(hello);
    }
}
