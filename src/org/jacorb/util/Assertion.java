package org.jacorb.util;

/**
 * AssertionViolation.java
 *
 * @author 
 * @version $Revision$
 */

public class Assertion
{
    /**
     */
    
    public static void assert( boolean assertion, String msg )
    {
        if( !assertion )
            throw new AssertionViolation(msg);
    }  
}






