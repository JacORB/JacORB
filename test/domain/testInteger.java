package org.jacorb.orb.domain.test;

import java.util.*;

/**
 *
 * A simple Integer test program.
 * Its to check if Integer.hashCode() == Integer.intValue() 
 * The answer is yes.
 * 
 *
 * @author Herbert Kiefer
 * @version 1.0
 */
public class testInteger  {
  
  public static void main(String args[]) {
    Hashtable test= new Hashtable();
    int min= -30, max= 60;
    System.out.println("testing begins...");
    for (int i= min; i < max; i++) test.put( new Integer(i), new Integer(i));
    System.out.println(test);

     for (int i= max-1; i > min; i--) {
      Integer value= (Integer) test.get(new Integer(i));
      System.out.println(value.intValue());
      }
  }

  
} // testInteger






