package org.jacorb.test.miop;

public class GreetingImpl extends GreetingServicePOA
{
    private String greeting;

    public GreetingImpl()
    {
        System.out.println("Hello created!");
    }

    public void greeting_oneway(String s)
    {
        greeting = s;
    }

    public String greeting_check()
    {
        return greeting;
    }
}
