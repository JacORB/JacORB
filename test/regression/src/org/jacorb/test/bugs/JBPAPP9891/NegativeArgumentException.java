package org.jacorb.test.bugs.JBPAPP9891;

public class NegativeArgumentException extends Exception {

    int i = 0;
    public static int j = 134;

    public NegativeArgumentException(int i2)
    {
       super("Negative argument: " + i2);
       this.i = i2;
    }

    public int getNegativeArgument()
    {
       return i;
    }

}
