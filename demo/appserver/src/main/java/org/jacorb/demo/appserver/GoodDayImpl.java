package org.jacorb.demo.appserver;

public class GoodDayImpl
        extends GoodDayPOA
{
    private String input;

    public String get_string()
    {
        return input;
    }

    public void record_string(String msg)
    {
        input = msg;
    }
}
