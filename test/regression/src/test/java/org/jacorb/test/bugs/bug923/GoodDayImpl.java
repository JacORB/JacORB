package org.jacorb.test.bugs.bug923;


public class GoodDayImpl
    extends GoodDayPOA
{
    public String hello_simple(String i)
    {
        return "Hello World, from " + i;
    }

    public void say() {
    }

    public String hello_wide(String wide_msg)
    {
        return "Hello World, from 1 2 3 0 *&^%$#@!@";
    }
}
