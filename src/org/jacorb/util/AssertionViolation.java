package org.jacorb.util;
/**
 * AssertionViolation.java
 *
 *
 * Created: Fri Mar  3 16:58:41 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class AssertionViolation 
    extends RuntimeException 
{

    public AssertionViolation() { this(""); }
  
    public AssertionViolation(String msg) 
    {
        super(msg);    
    }
  
} // AssertionViolation
