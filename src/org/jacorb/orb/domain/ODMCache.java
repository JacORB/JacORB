package org.jacorb.orb.domain;

import java.util.*;
import org.jacorb.util.Environment;
import org.jacorb.util.Debug;

/**
 * ODMCache.java
 * A cache for an ODM. Used by the orb domain.
 *
 * Created: Wed Aug 16 10:36:21 2000
 *
 * @author Herbert Kiefer
 * @version $Id$
 */

public class ODMCache  
{
    private Hashtable theDomainLists;
  
    public ODMCache() 
    {
        theDomainLists = new Hashtable();
    }
  
    /** 
     * returns the  domains of the object obj, if in  cache. If there is
     * no  cache entry  for  "obj"  or if  it  is  not  valid, null  is
     * returned. Currently  the  lifetime for  valid  cache entries  is
     * obtained from Environment.LifetimeOfCacheEntry().  
     */
    public Domain[] read(org.omg.CORBA.Object obj)
    {
        synchronized(obj)
        {
            java.lang.Object hashResult= theDomainLists.get(obj);
            if (hashResult == null)
                return null;
            // else
	
            ODMCacheEntry entry= (ODMCacheEntry) hashResult;
            if (System.currentTimeMillis() -  entry.timestamp > 
                Environment.LifetimeOfCacheEntry() )
                return null;    // time passed since writing of timestamp greater than lifetime
            else
            {
                if (entry.domainArray != null)
                    return entry.domainArray;
                else
                {
                    entry.domainArray= new Domain[ entry.group.size() ];
                    // Enumeration objectEnum= _child_domains.keys();
                    Enumeration domainEnum= entry.group.keys();
                    // convert enumeration to array
                    int i= 0;
                    while ( domainEnum.hasMoreElements() ) 
                    {
                        entry.domainArray[i]= DomainHelper.narrow
                            ((org.omg.CORBA.Object)domainEnum.nextElement());
                        Debug.assert(0, entry.domainArray[i] != null, 
                                     "OMDCache.read: result " + i + 
                                     " is null.");
                        i++;
                    }
                    // cache result for later use to avoid hashtable -> array convertion
                    return entry.domainArray;
                }
            }
        }
    } // read

    /** 
     *  writes a  cache entry.  The domains  of the  object  "obj" are
     *  written into  the  cache  as "group".  Also  the timestamp  is
     * updated to the current time.  
     */
    public void write(org.omg.CORBA.Object obj, Domain group[])
    {
        synchronized(obj)
        {
            java.lang.Object hashResult=  theDomainLists.get(obj);
            if (hashResult == null)
            {
                Hashtable domainSet= new Hashtable(group.length);
                for (int i= 0; i < group.length; i++)
                    domainSet.put(group[i], group[i]);

                theDomainLists.put(obj, 
                                   new ODMCacheEntry(domainSet, 
                                                     System.currentTimeMillis() ));
            }
            else
            {
                ODMCacheEntry entry= (ODMCacheEntry) hashResult;
                for (int i= 0; i < group.length; i++)
                    entry.group.put(group[i], group[i]);

                entry.domainArray= null; // invalidate internal cache
                entry.timestamp= System.currentTimeMillis(); // refresh
            }
        }
    } // write
  
    /** 
     * fine granular version of a  write. writes not the whole list of
     * domains  but adds to  it a single  domain. Used to  avoid write
     * after write inconsistencies.  
     */

    public void writeAddDomain(org.omg.CORBA.Object obj, Domain aDomain)
    {
        synchronized(obj)
        {
            java.lang.Object hashResult= theDomainLists.get(obj);
            if (hashResult == null)
            { 
                // insert new list and put aDomain into it
                Hashtable domainSet= new Hashtable(10);
                domainSet.put(aDomain, aDomain);
                theDomainLists.put(obj, 
                                   new ODMCacheEntry(domainSet, 
                                                     System.currentTimeMillis() ));
            }
            else // list is already there
            {
                ODMCacheEntry entry= (ODMCacheEntry) hashResult;
                entry.group.put(aDomain, aDomain);
                entry.domainArray= null; // invalidate internal cache
                entry.timestamp= System.currentTimeMillis(); // refresh
            }
        }
    } // writeAddDomain

    /** 
     * more fine granular version  of write. writes not the whole list
     * of domains but adds to  it a single domain. Used to avoid write
     * after write inconsistencies.  
     */
    public void writeRemoveDomain(org.omg.CORBA.Object obj, Domain aDomain)
    {
        synchronized(obj)
        {
            java.lang.Object hashResult= theDomainLists.get(obj);
            if (hashResult == null)
                ; // nothing do to
            else
            {
                ODMCacheEntry entry= (ODMCacheEntry) hashResult;
                entry.group.remove(aDomain);
                entry.domainArray= null; // invalidate internal cache
                entry.timestamp= System.currentTimeMillis(); // refresh
            }
        }
    } // writeRemoveDomain

    /** 
     * removes a cache entry. 
     */

    public void remove(org.omg.CORBA.Object obj)
    {
        synchronized(obj)
        {
            theDomainLists.remove(obj);
        }
    } // remove

    /** 
     *  clears the odm cache. 
     */

    public void clear()
    {
        theDomainLists.clear();
    }
  
} // ODMCache

// a tuple 
class ODMCacheEntry
{
    /** set of domains for an object */
    Hashtable group; 

    /** array  of domains  of object, caches  the group  hashtable for
     *   faster access  */
    Domain[] domainArray;

    /* the time  of the last retrival from  the server in milliseconds
     *  from 1.1.1970 as returned by System.currentTimeMillis() */
    long timestamp; 

    ODMCacheEntry(Hashtable list, long stamp)
    {
        group    = list;
        timestamp= stamp;
        domainArray= null;
    }

}







