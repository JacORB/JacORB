package org.jacorb.orb.domain.test;
/**
 * Prints out the org.jacorb domain properties.
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class EnvironmentTester  {
  
  public static void main( String[] args )
  {
   
   System.out.println("\t Property \t\t Value");
   System.out.println("\t ======== \t\t =====");


   System.out.println("domain service URL \t\t" 
   	      + org.jacorb.util.Environment.getProperty("ORBInitRef.DomainService") );

   System.out.println("name service URL \t\t" 
   	      + org.jacorb.util.Environment.getProperty("ORBInitRef.NameService") );
   System.out.print("use domain \t\t\t");

   if ( org.jacorb.util.Environment.useDomain() ) System.out.println("on");
   else System.out.println("off");

   System.out.print("ds knows about orb domains\t");
    if ( org.jacorb.util.Environment.mountORBDomain() ) 
      System.out.println("on");
   else System.out.println("off");
		   

  System.out.println("orb domain filename \t\t" 
		      + org.jacorb.util.Environment.ORBDomainFilename() );
  System.out.println("default domains \t\t" 
		      + org.jacorb.util.Environment.DefaultDomains() );

  System.out.println("cache entry lifetime \t\t" 
		      + org.jacorb.util.Environment.LifetimeOfCacheEntry() );

  }


  
} // EnvironmentTester






