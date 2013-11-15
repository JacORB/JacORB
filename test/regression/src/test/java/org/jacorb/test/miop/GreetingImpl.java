package org.jacorb.test.miop;

public class GreetingImpl extends GreetingServicePOA
{
    private String greeting = "";

    public void greeting_oneway(String s)
    {
        greeting = s;
    }

    public String greeting_check()
    {
        return greeting;
    }
}
