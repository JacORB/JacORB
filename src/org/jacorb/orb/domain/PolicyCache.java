package org.jacorb.orb.domain;

import java.util.Hashtable;
import org.jacorb.util.Environment;
/**
 * PolicyCache.java
 * provides a cache for domain policies. used by a domain client stub
 *
 * Created: Mon Aug  7 14:48:50 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class PolicyCache  
{
  /** policy type -> CacheEntry, see below */
  private Hashtable thePolicies;

  public PolicyCache() 
  {
    thePolicies= new Hashtable(10);
  }

  /** checks whether the cache has a valid entry for a given policy type. 
   *  @param policyType the policy type to lookup
   *  @return true if the cache contains a valid policy of type "policyType"
   */
  public boolean isValid(int policyType)
  {
    java.lang.Object hashResult= thePolicies.get(new Integer(policyType));
    if (hashResult == null)
      return false;
    
    CacheEntry entry= (CacheEntry) hashResult;
    if (System.currentTimeMillis() - entry.timestamp>  Environment.LifetimeOfCacheEntry() )
      return false;    // time passed since writing of timestamp greater than lifetime
    else return true;  // lifetime of cache entry is not passed
  } // isValid
  

  /** reads a cache entry. The wanted policy is identified by its policy type.
   *  @param policyType the type of policy to retrieve
   *  @return a policy of type "policy type" or null, if the cache has no *valid
   *           entry* of that type
   */
  public org.omg.CORBA.Policy read(int policyType)
  {
    java.lang.Object hashResult= thePolicies.get(new Integer(policyType));
    if (hashResult == null)
      return null;
    // else

    CacheEntry entry= (CacheEntry) hashResult;
    if (System.currentTimeMillis() -  entry.timestamp > Environment.LifetimeOfCacheEntry() )
      return null;    // time passed since writing of timestamp greater than lifetime
    else return entry.policy;  // lifetime of cache entry is not passed
  } // read

  /** writes into the cache. Any previous policy in the cache of the same type of
   * "policy" may be overwritten.
   * @param policy the policy to write
   */
  public void write(org.omg.CORBA.Policy policy)
  {
    thePolicies.put(new Integer( policy.policy_type() ), 
		    new CacheEntry(policy, System.currentTimeMillis() ));
  } // write

  /** removes a cache entry. */
  public void remove(int policyType)
  {
    thePolicies.remove( new Integer(policyType) );
  } // remove

  /** clears the policy cache. */
  public void clear()
  {
    thePolicies.clear();
  }
} // PolicyCache


// a tuple 
class CacheEntry
{
  org.omg.CORBA.Policy policy;

  /* the time of the last retrival from the server in milliseconds from 1.1.1970 as
   *  returned by System.currentTimeMillis() */ 
  long timestamp; 

  CacheEntry(org.omg.CORBA.Policy pol, long stamp)
  {
    policy   = pol;
    timestamp= stamp;
  }

}

