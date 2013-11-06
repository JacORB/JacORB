package org.jacorb.test.bugs.bug923;


public class GoodDayImpl
    extends GoodDayPOA
{
    public String hello_simple(String i)
    {
        System.out.println("hello_simple, i=" + i);
        return "Hello World, from " + i;
    }

    public void say() {
       System.out.println("say, speak for yourself");
    }

    public String hello_wide(String wide_msg)
    {
        System.out.println("The message is: " + wide_msg );
        return "Hello World, from 1 2 3 0 *&^%$#@!@";
    }
}
