package org.jacorb.orb.domain;

import java.util.Hashtable;
import java.util.Enumeration;
import org.jacorb.util.Debug;

/**
 * The implementation of the IDL interface ORBDomain.
 * 
 * Created: Sun Aug 13 11:10:27 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class ORBDomainImpl
    extends DomainImpl 
    implements ORBDomainOperations
{
    /** the tie servant for an instance of this class */
    private ORBDomainPOATie theTie= null;

    /** a cache for ODM entries. */
    private ODMCache theCache;
  
    /** the set of local domains */
    private Hashtable theLocalDomains;

    /** 
     *  creates an orb domain.
     *  The domain is initially empty, contains no policies,
     *  and its name is the empty  string.
     */

    public ORBDomainImpl()
    {
        this(null, null, "");
    }

    /** 
     *  creates an orb domain.   The domain gets the specified list as
     *  initial members,  contains no  policies and  its name  is the
     *  empty string.
     *  @param initialMembers a list of objects which shall become 
     *                        members of the domain 
     */

    public ORBDomainImpl( org.omg.CORBA.Object[] initialMembers )
    {
        this( initialMembers, null, "");
    }
  
  
    /** creates an orb domain.
     *  The newly created domain contains the list of initial members,
     *  has the policies from the list of initial policies set and the
     *  the parameter mainPolicyType specifies the default policy type.
     *  string.
     */
    public ORBDomainImpl(org.omg.CORBA.Object[] initialMembers, 
                         org.omg.CORBA.Policy[] initialPolicies)
    { 
        this(initialMembers, initialPolicies, "");
    }

    /** 
     * Creates  an orb  domain.  A set  of initial members,  a default
     * policy type, a set of  initial   minor policies and the name of
     * the domain are specified by the parameters.  
     */

    public ORBDomainImpl(org.omg.CORBA.Object[] initialMembers, 
                         org.omg.CORBA.Policy[] initialPolicies,
                         String                 name) 
    {
        super(initialMembers, initialPolicies, name);
        theLocalDomains = new Hashtable(30);
        theCache = new ODMCache();
    }


    /** 
     * registers a domain as a local domain to this orb domain.
     * Post: isLocalTo(aDomain)
     */
    public void insertLocalDomain(org.jacorb.orb.domain.Domain aDomain)
    {
        theLocalDomains.put(aDomain, aDomain);
    }

    /** 
     * checks whether a domain is registered as local at 
     * this orb domain. 
     */
    public boolean isLocalTo(org.jacorb.orb.domain.Domain aDomain)
    {
        return theLocalDomains.contains(aDomain);
    }
  
    /** 
     * deregisters a domain as a local domain from this orb domain. 
     * Post: NOT isLocalTo(aDomain)
     */
    public void removeLocalDomain(org.jacorb.orb.domain.Domain aDomain)
    {
        theLocalDomains.remove(aDomain);
    }

    /** 
     * sets the tie for the orb domain. Necessary to delegate 
     * the _this() call 
     */
    public void setTie(ORBDomainPOATie tie)
    {
        theTie= tie;
    }

    // tie stuff

    /** 
     *  overwrites org.jacorb.orb.domain.DomainPOA._this(). delegates the call
     *  to the tie which must have been set before.
     *  @see org.jacorb.orb.domain.ORBDomainImpl#setTie
     *  @see org.jacorb.orb.domain.ORBDomainPOATie
     */
    public org.jacorb.orb.domain.Domain _this()
    {
        Debug.output( Debug.DOMAIN | Debug.DEBUG1, "ORBDomainImpl._this()");
        Debug.assert( 1, theTie != null, 
                      "ORBDomainPOATie._this: the tie has not been set (is null)");
        return theTie._this();
    }
  
    /** 
     *  gets the poa via the tie servant. 
     *  @see org.jacorb.orb.domain.DomainImpl#_getPOA
     */

    public org.omg.PortableServer.POA _getPOA()
    {   
        Debug.assert(1, theTie != null, 
                     "ORBDomainPOATie._getPOA: the tie has not been set (is null)");
        return theTie._poa();
    }

    /** 
     * gets the orb. 
     */

    public org.omg.CORBA.ORB _getORB()
    {
        Debug.assert(1, theTie != null, 
                     "ORBDomainPOATie._getORB: the tie has not been set (is null)");
        return theTie._orb();
    }


    /** 
     *  @return the  domains the  object is  associated with.  The orb
     * domain has its own optimized algorithm for the search: Firstly,
     * all domains, wich are registered as local at the orb domain are
     * searched for  a valid odm entry. If none  of them provides one,
     *  the searches  starts  at  the root  domain  and traverses  the
     *   underlying  domain  graph   until  a   valid  odm   entry  is
     * found.  Normally all calls to  obtain the doamins  of an object
     * are  delegeted to  this function. For  example a  normal domain
     *  delegates  the getDomains  call  to  its  orb domain  and  the
     * ServantDelegate also delegates to its orb domain.
     *  @see org.jacorb.orb.domain.DomainImpl#traverseDownwards
     *  @see org.jacorb.orb.domain.DomainImpl#getDomains
     *  @see org.jacorb.orb.ServantDelegate#_domainService
     *  @see org.jacorb.orb.ServantDelegate#_get_domain_managers 
     */

    public Domain[] getDomains(org.omg.CORBA.Object obj)
    { 
        // synchronized (_memberLock)
        {    
            Domain domain;
            Domain result[];
            // check cache
            result= theCache.read(obj);
            if (result != null)
            {
                Debug.output(Debug.DOMAIN | Debug.DEBUG1,
                             " ORBDomainImpl<" + name() 
                             + ">.getDomains: cache hit");
                return result;
            }

            // else
            Debug.output(Debug.DOMAIN | Debug.DEBUG1,
                         "ORBDomainImpl<" + name() 
                         + ">.getDomains: cache miss");
            // firstly, try all local domains
            Enumeration resultEnum =  theLocalDomains.keys();
            while ( resultEnum.hasMoreElements() ) 
            {
                domain= (Domain) resultEnum.nextElement() ;
                if ( domain.hasMember(obj) ) 
                {
                    Debug.output(Debug.DOMAIN | Debug.DEBUG1,
                                 " ORBDomainImpl<" + name() 
                                 + ">.getDomains: found mapping for an obj in " + 
                                 domain.name() );
                    result= domain.getMapping(obj); 
                    theCache.write(obj, result);
                    return result;
                }
            }

            // mapping for obj not found
            // secondly, traverse from root domain
            Debug.output(Debug.DOMAIN | Debug.DEBUG1,
                         " ORBDomainImpl<" + name() 
			 + ">.getDomains: mapping not found in local domains, searching root");

            DomainListHolder holder = new DomainListHolder();
            if ( super.traverseDownwards(obj, getRootDomain(), 
                                         holder, new Hashtable() ) )
            { 
                // found
                theCache.write(obj, holder.value);
                return holder.value; 
            }
            else 
            {
                Debug.output(Debug.DOMAIN | Debug.DEBUG1,
                             " ORBDomainImpl<" + name() 
                             + ">.getDomains: nothing found, returning empty list");
                return new Domain[0];     // not found, return empty list
            }
        }

        // if not found in some local domains, delegate call to the root domain
        //    if ( isRoot() )
        //        { // ops, we are not mounted so some domain
        //  	return new Domain[0];
        //        }
        //      else return getRootDomain().getDomains(obj);

        //      // finally, return the empty list
        //      //     return new Domain[0];
        //      try
        //        {
        //  	domain= DomainHelper.narrow(_getORB().resolve_initial_references
        //  				    ("DomainService"));
        //  	if ( domain == null )
        //  	  {
        //  	    Debug.output(Debug.DOMAIN | Debug.IMPORTANT, "ORBDomainImpl.getDoamins: "
        //  +" could not get reference to domain server, returning empty domain list");
        //  	    return new Domain[0];
        //  	  }
        //  	if ( domain._non_existent() ) 
        //  	  return new Domain[0];
        //  	else return domain.getDomains(obj);
        //        }
        //      catch (org.omg.CORBA.ORBPackage.InvalidName invalid)
        //        {
        //  	Debug.output(Debug.DOMAIN | Debug.IMPORTANT, invalid);
        //  	return new Domain[0];
        //        }
        //      catch (org.omg.CORBA.COMM_FAILURE failure)
        //        {
        //  	Debug.output(Debug.DOMAIN | Debug.IMPORTANT,failure );
        //  	return new Domain[0];
        //        }
    } // getDomains


    public void updateODMCache(org.omg.CORBA.Object obj, 
                               org.jacorb.orb.domain.Domain[] group)
    {
        theCache.write(obj, group);
    }

    public void invalidateODMCache(org.omg.CORBA.Object obj)
    {
        theCache.remove(obj);
    }

    /** adds the domain aDomain to the list of domains in the cache. */
    public void addToODMCache(org.omg.CORBA.Object obj, 
                              org.jacorb.orb.domain.Domain aDomain)
    {
        theCache.writeAddDomain(obj, aDomain);
    }
  
    /** removes the domain aDomain from  the list of domains of obj in
     *    the cache. 
    */
    public void removeFromODMCache(org.omg.CORBA.Object obj, 
                                   org.jacorb.orb.domain.Domain aDomain)
    {
        theCache.writeRemoveDomain(obj, aDomain);
    }

} // ORBDomainImpl






