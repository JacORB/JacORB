package org.jacorb.orb.domain;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * implementation of the IDL-interface org.jacorb.orb.domain.ObjectDomainMapper 
 * <p> 
 * An ODM manages the domains
 * of objects. An  ODM is  normally used  for replication.  For every
 * object listed  in the ODM table, it  holds the corresonding domains
 * for that  object.  Note that the information provided  by an ODM is
 * redundant. It may be retrieved also by a complete traversial of the
 * doamin graph. An ODM holds the information the getDomains operation
 *  of  the  domain  interface  is searching  for.  It  provides  this
 * information for speeding up  the call of "getDomains".  Normally an
 * ODM is  asscociated with a domain which delegates  calls to the ODM
 * interface to its associated  ODM. The invariant of this association
 * is  as follows: For every  member of the domain  the associated ODM
 * holds the  complete list of domains of that  object.  Note that the
 * invariant holds only for domain members, not for all objects around
 * in the world.  The domains  of an object form an (virtual) group in
 *  the terms of  group communication.   Each domain  of such  a group
 *  holds  in its  ODM  the same  information  about  the other  group
 * members. Therefore the information about this group is replicated.
 *
 * Created: Tue Aug  8 16:35:48 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$ */

public class ODMImpl 
    implements  ObjectDomainMapperOperations
{
    // object -> domain hashtable
    private Hashtable _object2domains;
  
    public ODMImpl() 
    {
        _object2domains= new Hashtable();
    }

    /** 
     *  inserts a complete new mapping. 
     *  Post: hasMapping(obj) AND  dms == getMapping(obj) 
     *        AND for all dm e dms: areMapped(obj, dm)
     */

    public void insertMapping(org.omg.CORBA.Object obj,
                              org.jacorb.orb.domain.Domain[] dms)
    {
        synchronized(obj)
        {
            Hashtable domains = new Hashtable(10); // set of domains
            for (int i= 0; i < dms.length; i++)
                domains.put(dms[i], dms[i]);
            _object2domains.put(obj, domains);
        }
    } // insertMapping

    /** 
     *  deletes a mapping completely. 
     *  Post: NOT hasMapping(object)
     */
    public void deleteMapping(org.omg.CORBA.Object obj)
    {
        synchronized(obj)
        {
            _object2domains.remove(obj);
        }
    } // deleteMapping

    /** 
     *   returns a mapping. 
     *   Pre : hasMapping(obj)
     *   @return the domain manager list associated with object obj
     */
    public org.jacorb.orb.domain.Domain[] getMapping(org.omg.CORBA.Object obj)
    {
        synchronized(obj)
        {
            java.lang.Object hashResult= _object2domains.get(obj);
            if (hashResult == null)
                return new Domain[0]; // not found, return empty list
    
            Hashtable domains= (Hashtable) hashResult;
            Domain result[]= new Domain[ domains.size() ];
            // Enumeration objectEnum= _child_domains.keys();
            Enumeration domainEnum= domains.keys();
            // convert enumeration to array
            int i= 0;
            while ( domainEnum.hasMoreElements() ) 
            {
                result[i]= 
                    DomainHelper.narrow((org.omg.CORBA.Object)domainEnum.nextElement()) ;
                i++;
            }
            return result;
        }
    } // getMapping

    /** 
     * checks whether there is a mapping defined for obj ? (could also
     * be the empty list).  
     */
    public boolean hasMapping(org.omg.CORBA.Object obj)
    {
        synchronized(obj)
        {
            return _object2domains.contains(obj);
        }
    } // hasMapping

    /** is obj mapped to dm ? */
    public boolean areMapped(org.omg.CORBA.Object obj, 
                             org.jacorb.orb.domain.Domain dm)
    {
        synchronized(obj)
        {
            java.lang.Object hashResult= _object2domains.get(obj);
            if (hashResult == null)
                return false;
	
            return ( (Hashtable) hashResult ).contains(dm);
        }
    } // areMapped


    /** add an domain to the mapping of an object. 
     *  Post: hasMapping(obj) AND areMapped(obj, dm)
     */
    public void addToMapping(org.omg.CORBA.Object obj, 
                             org.jacorb.orb.domain.Domain dm)
    {
        synchronized(obj)
        {
            java.lang.Object hashResult= _object2domains.get(obj);
            if (hashResult == null)
            { // create new list and put dm into it
                Hashtable domains= new Hashtable(10);
                domains.put(dm, dm);
                _object2domains.put(obj, domains);
            }
            else 
                ( (Hashtable) hashResult ).put(dm, dm); // add dm to list
        }
    } // addToMapping
    


    /** removes a domain from the mapping of object obj. 
     *  Post: NOT areMapped(obj, dm)
     */
    public void removeFromMapping(org.omg.CORBA.Object obj, 
                                  org.jacorb.orb.domain.Domain dm)
    {
        synchronized(obj)
        {
            java.lang.Object hashResult= _object2domains.get(obj);
            if (hashResult == null)
                return; // nothing to do
            else 
                ( (Hashtable) hashResult ).remove(dm); // delete dm 
        }
    } // removeFromMapping
 
} // ODMImpl







