package org.jacorb.orb.domain;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import org.omg.CORBA.*;
import org.jacorb.util.Debug;
import org.jacorb.poa.POA;

/**
 * Implementation of IDL-interface org.jacorb.orb.domain.Domain.
 * 
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class DomainImpl 
    extends DomainPOA
    implements org.jacorb.poa.POAListener
{
    /** the domain factory this domain  uses to create other domains */
    private DomainFactory _factory; 

    /** cached copy of the orb domain reference provided
        by orb.resolve_initial_refs*/
    private ORBDomain theORBDomain= null;

    /** the policy factory this domain uses to create some
        needed policies */
    private PolicyFactory _policyFactory;
   
    /** the ObjectDomainMapper for delegating calls to the 
        interface ObjectDomainMapping */
    private ODMImpl _odm;

    /** the name of the domain */
    String _name;  

    /** the prefix used for autonaming */
    String _autonamingPrefix;

    /** the unique number for autonaming, simply increases for 
        every auto named member */
    long   _NameCount;

    /** the separator used to seperate domain names in domain pathnames */
    String _sep= "/";
    // Hashtables with the identity function are used to implement sets

    /** set of members, maps: object -> name */
    private Hashtable _members     = new Hashtable(); 

    /** map of names, maps: name -> object */
    private Hashtable _memberNames = new Hashtable(); 

    /** lock for member access */
    protected java.lang.Object _memberLock= new java.lang.Object();
    /** the list of domain policies */
    private Hashtable _policies    = new Hashtable();

    /** the list of meta policies */
    private Hashtable _metaPolicies    = new Hashtable();


    /** the child domain  registered at this domain  */
    private Hashtable _child_domains= new Hashtable();

    /** the names of the child domains */
    private Hashtable _child_domain_names= new Hashtable();

    /** the list of parent domains of this domain */
    protected Hashtable _parents   = new Hashtable();
  
    public final static int UNDEF = -1;

    // ***** constructors *****


    /** creates a domain.
     *  The domain is initially empty, contains no policies and
     *  its name is the empty string.
     */
    public DomainImpl()
    {
        this(null, null, "");
    }
  
    /** 
     *  creates a domain.
     *  The domain gets the specified list as initial members, 
     *  contains no policies and its name is the empty 
     *  string.
     *  @param initialMembers a list of objects which shall become 
     *                        members of the domain
     */
    public DomainImpl(org.omg.CORBA.Object[] initialMembers)
    {
        this(initialMembers, null,"");
    }
 
 
    public DomainImpl(org.omg.CORBA.Object[] initialMembers, 
                      int   mainPolicyType)

    { 
        this(initialMembers, null,  "");
    }
  
    /** creates a domain.
     *  The newly created domain contains the list of initial members,
     *  has the policies from the list of initial policies set
     */
    public DomainImpl(org.omg.CORBA.Object[] initialMembers, 
                      org.omg.CORBA.Policy[] initialPolicies)
    { 
        this(initialMembers, initialPolicies, "");
    }

    /** 
     * Creates a domain object. A set of initial members, 
     *   a set of initial minor policies and an informal 
     *   name are specified by the parameters.
     */
    public DomainImpl(org.omg.CORBA.Object[] initialMembers, 
                      org.omg.CORBA.Policy[] initialPolicies,
                      String                 name) 
    {
        _name= name;
        _autonamingPrefix= "member #";

        // set members
        if (initialMembers != null)
        { // insert member asynchronously
            Thread thread= new MemberListInserter(this, initialMembers);
            thread.start();
        }
        //   for (int i=0; i<initialMembers.length; i++)  
        //	insertMember(initialMembers[i]);
        else 
            Debug.output(3, "DMImpl.init: list of initial members is empty");

        // set policies
        // assuming Integer.hashCode() == Integer.intValue() !
        if (initialPolicies != null)
        {
            for (int i = 0; i<initialPolicies.length; i++) 
            { // set policies
                _policies.put(new Integer ( initialPolicies[i].policy_type() )
                    , initialPolicies[i] );
            }
        } 
        else Debug.output(3,"DMImpl.init: list of initial policies is empty");
    
    } // DomainImpl


    // ********** domain factory operations ***********************

   
    /** 
     * returns a domain factory. The reference is created 
     * on demand.
     */
    private DomainFactory domainFactory()
    {
        // use factory 
        if (_factory == null) 
        {
            DomainFactoryImpl factoryImpl = new DomainFactoryImpl();
            // use the poa of this domain to register servant
            try 
            {
                _factory= DomainFactoryHelper.narrow
                    ( _getPOA().servant_to_reference(factoryImpl) );
            } 
            catch (org.omg.PortableServer.POAPackage.WrongPolicy wp) 
            {
                Debug.output(1, "the poa of this domain(" + 
                             this +
                             ") has the wrong policies for \"servant_to_reference\".");
            }
            catch( Exception e )
            {
                org.jacorb.util.Debug.output(1, e);
            } 
        }

        return _factory;
    } // domainFactory

    /** 
     * creates a new domain.
     * @return - an object reference to a newly created domain 
     */

    public Domain createDomain (org.omg.CORBA.Object[] initialMembers,
                                org.omg.CORBA.Policy[] initialPolicies,
                                String                 name)
    {
        // use factory     
        return domainFactory().createDomain(initialMembers, initialPolicies, 
                                            name);
    }

    /** 
     * creates a new empty domain.
     * @return - an object reference to a newly created empty domain  
     */

    public Domain createEmptyDomain()
    {
        return domainFactory().createEmptyDomain();
    }
  


    public void clear(org.jacorb.orb.domain.Domain aDomain)
    {
        Debug.assert(1, aDomain.getChildCount() == 0, 
                     "DomainFactory.clear: "
                     +"cannot clear a domain with child domains, remove child domains first.");

        Domain self= _this();

        if ( aDomain._is_equivalent(self) )
        { // ok, clear self, this is a shortcut
            Domain parent[] = getParents();
            for (int i= 0; i < parent.length; i++) deleteParent( parent[i] );
            _members.clear(); 
            _memberNames.clear();
            _policies.clear();
            _metaPolicies.clear();
            _child_domain_names.clear();
            _child_domains.clear();
            _parents.clear();
            _NameCount= 0;
        }
    } // clear

    public void destroy(org.jacorb.orb.domain.Domain aDomain)
    {
        domainFactory().destroy(aDomain);
    } // destroy
 

    // ************ policy factory operations ********************

    /** 
     * returns a policy factory. 
     */
    private PolicyFactory policyFactory()
    {
        if (_policyFactory == null) 
        {
            PolicyFactoryImpl factoryImpl= new PolicyFactoryImpl();
            // use the poa of this domain to register servant
            try 
            {
                _policyFactory = 
                    PolicyFactoryHelper.narrow(_getPOA().servant_to_reference(factoryImpl));
            } 
            catch (org.omg.PortableServer.POAPackage.WrongPolicy wp) 
            {
                org.jacorb.util.Debug.output(1, "the poa of this domain(" + this 
                                         +") has the wrong policies for \"servant_to_reference\".");
            }
            catch (Exception e)
            {
                org.jacorb.util.Debug.output(1, e);
            } 
        }
     
        return _policyFactory;
     
    }// policyFactory

    /** 
     * creates a policy.
     * The type of the policy to be created is specified 
     * by the parameter "type". 
     * The parameter "initValue" of type Any contains
     * the initial state of the policy.
     *
     * @param type the wanted policy type 
     * @param initValue a any containing potential initial 
     *         value for the policy to be created
     * @exception org.omg.CORBA.PolicyError if the parameter 
     *         "type" is invalid or the any "initValue" contains invalid inital date
     */

    public org.omg.CORBA.Policy create_policy(int type, 
                                              org.omg.CORBA.Any initValue) 
        throws org.omg.CORBA.PolicyError
    { 
        // delegate to policy factory
        return policyFactory().create_policy(type, initValue); 
    }

    /** 
     * creates an initial map policy.
     * The parameter "whichOne" identifes the strategy/subtype 
     * of the policy to be created 
     *  @param whichOne the subtype of the initial map policy.
     *          May currently be one of
     *                  <UL>
     *                  <LI> InitialMapPolicy.DEFAULT_DOMAIN        </LI>
     *		      <LI> InitialMapPolicy.TYPE_DOMAINS          </LI>
     *		      <LI> InitialMapPolicy.POA_DOMAINS           </LI>
     *		      </UL>
     *  @exception org.omg.CORBA.PolicyError if the suptype 
     *             "whichOne" is unknown
     */ 

    public org.jacorb.orb.domain.InitialMapPolicy createInitialMapPolicy(short whichOne)
    {
        // delegate to policy factory
        return policyFactory().createInitialMapPolicy(whichOne); 
    }

    /** 
     * creates a conflict resolution policy.
     *  The parameter "whichOne" identifes the strategy/subtype 
     *  of the policy to be created 
     *  @param whichOne the subtype of the conflict solution 
     *          policy. May currently be one of
     *                  <UL>
     *                  <LI> ConflictResolutionPolicy.FIRST        </LI>
     *		      <LI> ConflictResolutionPolicy.PARENT_RULES  </LI>
     *		      <LI> ConflictResolutionPolicy.CHILD_RULES   </LI>
     *		      </UL>
     *  @exception org.omg.CORBA.PolicyError if the suptype "whichOne" 
     *              is unknown
     */
    public org.jacorb.orb.domain.ConflictResolutionPolicy createConflictResolutionPolicy(short whichOne)
    { 
        // delegate to policy factory
        return policyFactory().createConflictResolutionPolicy(whichOne);
    }

    /** 
     * returns a property policy. The property policy 
     * has no properties defined.
     */

    public org.jacorb.orb.domain.PropertyPolicy createPropertyPolicy()
    {
        // delegate to policy factory
        return policyFactory().createPropertyPolicy(); 
    }

    /** 
     * returns a meta property policy. 
     */

    public org.jacorb.orb.domain.MetaPropertyPolicy createMetaPropertyPolicy()
    { 
        // delegate to policy factory
        return policyFactory().createMetaPropertyPolicy(); 
    }

    // *********              MetaPolicyManager interface operations

    /** sets a meta policy.
     *  @param pol the meta policy to be set in this domain
     *  @exception PolicyTypeAlreadyDefined if there is 
     *             already a meta policy of type 
     *             pol.policy_type() valid in this domain
     */
    public void setMetaPolicy(org.jacorb.orb.domain.MetaPolicy pol) 
        throws org.jacorb.orb.domain.PolicyTypeAlreadyDefined
    {
        _metaPolicies.put( new Integer( pol.policy_type() ), pol);
        // this.set_domain_policy(pol);
    }

    /**
     * returns a meta policy. 
     *  @param the type of the wanted policy
     *  @exception INV_POLICY if this domain has no 
     *           meta policy for policies of type "type".
     */

    public org.jacorb.orb.domain.MetaPolicy getMetaPolicy(int metaType)
    {
        java.lang.Object hashResult =
            _metaPolicies.get( new Integer(metaType) );
        // java.lang.Object hashResult= _policies.get(new Integer (metaType) );

        if ( hashResult != null ) 
        {           
            return MetaPolicyHelper.narrow( (org.omg.CORBA.Object) hashResult);
        }
        else
        {
            // not found
            throw new INV_POLICY("no meta policy of type " + 
                                 metaType + " available");
        }
    } 

    /** 
     * removes a meta policy from this domain.
     */

    public void deleteMetaPolicy(org.jacorb.orb.domain.MetaPolicy pol)
    {
        _metaPolicies.remove( new Integer( pol.policy_type() ) );
        // _policies.remove    ( new Integer( pol.policy_type() ) );
    }

    // **************** domain interface operations ****************
    /** 
     * sets the name of the domain.
     *  @param name the new name of the domain
     */

    public void name(String newName) 
    { 
        _name= newName;
    }

    /** 
     * gets the name.
     *  @return an informal name of the usage of the managed domain 
     */
    public String name() 
    { 
        return _name; 
    }

    // policy operations


    /** retrieves a domain policy. The retrived policy applies to this
     *  domain.
     *  The type of policy is indicated by the "policy_type" parameter.
     *  @param policy_type the type of policy to be retrieved
     *  @exception INV_POLICY if this domain currently doesn't have 
     *             a policy of the type "policy type"
     *  @return policy the policy of type "policy_type" which 
     *             applies to this domain
     */

    public org.omg.CORBA.Policy get_domain_policy(int policy_type) 
    {

        Policy result= null;

        java.lang.Object hashResult = 
            _policies.get(new Integer (policy_type) );
        if (hashResult != null)  
            result= (org.omg.CORBA.Policy) hashResult; // main case
        else
            // policy not found in this domain
            throw (new INV_POLICY("DM.get_domain_policy: this domain " + this
                                  +"doesn't have a policy of type " + policy_type));	
    
        return result;
    } // get_domain_policy

  
    /**
     * retrieves  a domain policy.  The retrived policy may  depend on
     * policies of  parent domains. If a policy of  the wanted type is
     * not  available in  this domain the  parent domains, if  any, of
     *  this domain  are  asked.  If  there  is more  than one  policy
     * provided  by different parent domains,  the conflict resolution
     *  policy decides  which  one to  take.   The type  of policy  is
     * indicated by the "policy_type" parameter.
     *
     *  @param policy_type the type of policy to be retrieved
     *  @exception INV_POLICY if this domain and none of its 
     *       parent domains have  a policy of the type "policy type"
     *  @see org.jacorb.orb.domain.DomainImpl#get_domain_policy */

    public org.omg.CORBA.Policy getEffectiveDomainPolicy(int policy_type) 
    {
        Policy result= null;
        java.lang.Object hashResult = 
            _policies.get(new Integer (policy_type) );
        if (hashResult != null)  
            result= (org.omg.CORBA.Policy) hashResult; // main case
        else if ( !isRoot() )
        {  
            // check parents if this domain does not have the wanted policy
            result= getParentPolicy(policy_type);
        }
        else
            // policy not found in this domain
            throw (new INV_POLICY("DM.getNonInheritedPolicy: this domain " + 
                                  this +"doesn't have a policy of type " + 
                                  policy_type));
	
    
        return result;
    }
   
    /** 
     * checks whether this domain contains a policy of a certain type.
     *  @param type the type of policy to check 
     */
    public boolean hasPolicyOfType(int type)
    {
        return _policies.containsKey(new Integer (type) );
    }
   

    /** 
     * lists the direct object members of this domain. 
     */
    public org.omg.CORBA.Policy[] getPolicies()
    { 
        Enumeration objectEnum= _policies.elements();
        // convert Enumeration to array
        org.omg.CORBA.Policy[] result= new org.omg.CORBA.Policy[_policies.size()];
        int i= 0;
        while ( objectEnum.hasMoreElements() ) 
        {
            result[i]= (org.omg.CORBA.Policy) objectEnum.nextElement();
            i++;
        }
        return result;
    } // getPoliecies


    /** returns the number of policies. */
    public int getPolicyCount() 
    {
        return _policies.size(); 
    }

    /** 
     *  uses (one of) the parents to get the policy of the specified type.
     *  Pre: !isRoot() 
     */

    protected org.omg.CORBA.Policy getParentPolicy(int policy_type)
    {
        org.jacorb.util.Debug.assert(2, 
                                 !isRoot(), 
                                 "DMImpl.getParentPolicy: precondition violated");

        Domain ds;
        Enumeration objectEnum= _parents.keys();

        if (_parents.size() == 1 )
        { 
            // fine, only one parent, ask this one
            ds = DomainHelper.narrow
                ( (org.omg.CORBA.Object) objectEnum.nextElement() );
            return ds.get_domain_policy(policy_type);
        }
        else
        { 
            // potentially more than one parent incorporated, ask all
            Vector dsList= new Vector();
            while ( objectEnum.hasMoreElements() ) 
            {
                ds = DomainHelper.narrow
                    ( (org.omg.CORBA.Object)objectEnum.nextElement() );
                try 
                { 
                    ds.get_domain_policy(policy_type); 
                }
                // don't consider ds which does not have the right policy type
                catch (INV_POLICY invalid) 
                {
                    continue; 
                }    
                dsList.addElement(ds);
            }
            // now we have a list of ds with all overlap 
            // in the wanted policy type
            // first check if list is nonempty
            int n= dsList.size();
            if ( n < 1 )
                throw (new INV_POLICY("DM.get_domain_policy: this domain " + 
                                      this +"doesn't have a policy of type " +
                                      policy_type));
            else if (n == 1) // only one element, use this one
                return ((Domain) dsList.firstElement()).get_domain_policy(policy_type);
            else 
            { 
                // use conflict resolution policy to choose one
                ConflictResolutionPolicy resolver;
                if (policy_type == CONFLICT_RESOLUTION_POLICY_ID.value)
                { 
                    // this is bad: we have more than one domain with a 
                    // conflicting resolution policy; to resolve this 
                    // conflict, we need a resolution policy ...
                    // if we do it the usual way, we will end up 
                    // in a non-ending recursion ...
                    // the hardcoded ! solution is to create a 
                    // conflict resolution policy and 
                    // use this newly created conflict solution policy
                    org.jacorb.util.Debug.output(1,"DomainImpl.getParentPolicy: conflict resolution "
                                             +" policy itself conflicts, using (hardcoded)"
                                             +" ParentRulesPolicy to resolve this conflict.");

                    resolver = policyFactory().createConflictResolutionPolicy
                        (ConflictResolutionPolicy.PARENT_RULES);

                    Domain list[]= new Domain[dsList.size()];
                    dsList.copyInto(list);
                    return resolver.resolveConflict(list, policy_type);
                    // return dm.get_domain_policy(policy_type);
                }

                // normal conflict resolution
                try 
                { 
                    resolver = ConflictResolutionPolicyHelper.narrow 
                        ( get_domain_policy(CONFLICT_RESOLUTION_POLICY_ID.value));
                } 
                catch (INV_POLICY invalid) 
		{ 
                    // no conflict resolution policy defined, but we need one, 
                    // so define a default one and insert it into policy list	    
                    resolver = policyFactory().createConflictResolutionPolicy
                        (ConflictResolutionPolicy.PARENT_RULES);
		  
                    this.overwrite_domain_policy(resolver);
		}
                Domain overlappingDS[]= new Domain[dsList.size()];
                dsList.copyInto(overlappingDS);
                return resolver.resolveConflict(overlappingDS, policy_type);
                // return dm.get_domain_policy(policy_type);
            }
        }
    
    } // getParentPolicy

    /** 
     *  sets the specified policy.
     *  invariant: at most one policy of every type may exist .
     *  an already existing policy of the same type of pol may be overwritten
     *
     *  @param pol the policy to be set in this domain
     */

    public void set_domain_policy(org.omg.CORBA.Policy pol) 
        throws org.jacorb.orb.domain.PolicyTypeAlreadyDefined 
    {    
        int key= pol.policy_type();
        if ( _policies.containsKey( new Integer (key) ) ) 
            // Debug.output(1,"Domain.set_domain_policy:overwriting policy of type "+key);
            throw new PolicyTypeAlreadyDefined(key);
      
        _policies.put( new Integer(key), pol );
        Debug.output(3,"Domain.set_domain_policy:setting policy of type "+key);

    }

    /** 
     * sets a policy in a domain. Potentially
     * overwrites already existing policy. 
     * User with care ! 
     */
 
    public void overwrite_domain_policy(org.omg.CORBA.Policy policy)
    {
        _policies.put( new Integer(policy.policy_type()), policy );
    }
  
    /** removes a policy of of the specified type from the domain. */ 
    public void deletePolicyOfType(int type)
    {
        _policies.remove( new Integer(type) );
    }


    // ****** service operations *********

  
    /** returns the domains  the object is associated with */
    public Domain[] getDomains(org.omg.CORBA.Object obj)
    {
        // synchronized (_memberLock)
        {
    
            // delegate to orb domain
            return getORBDomain().getDomains(obj);
        }
    } // getDomains


    /**
     *  returns the policy of the specified type for an object.
     *
     *	@return Policy the policy of type "type" the object "obj" has
     *  @exception INV_POLICY if the object does not have a policy of that type
     */

    public org.omg.CORBA.Policy getPolicy(org.omg.CORBA.Object obj,int type) 
    {
	org.omg.CORBA.Policy pol= null;
	Vector targetDomains= new Vector();

	// first main step
	Domain domains[]= getDomains(obj);
	org.jacorb.util.Debug.assert(2, domains != null," Domain.getPolicy: "
				 +"result of getDomains is null");

	for (int i= 0; i < domains.length; i++) 
        {
	    try 
            { 
		// try to get policy from domain: second main step
		org.jacorb.util.Debug.assert(2, 
                                         domains[i] != null,
                                         "Domain.getPolicy:"
					 +" array entry is null");
		// pol= domains[i].get_domain_policy(type);
		pol= domains[i].getEffectiveDomainPolicy(type);

		// if successful add to found list
		targetDomains.addElement(domains[i]);     
            }
	    // if no success skip and try next one
	    catch (INV_POLICY invalid) { continue; }    
        }

	int n= targetDomains.size(); 
	if (n == 1) // fine, one domain has had the needed policy type,
            // so pol is set to the policy of this domain 
        {
	    Debug.assert(1, pol != null, "DomainImpl.getPolicy: pol is null");
	    return pol;
        }
	else if (n > 1)
        {
	    org.jacorb.util.Debug.output(2, 
                                     "Domain.getPolicy: there is more than one domain (n= " + n
                                     +") with the wanted policy, using conflict resolution for overlapping domains...");
	    pol = doConflictResolution(targetDomains, type, policyFactory() );
	    Debug.assert(1, pol != null, 
                         "DomainImpl.getPolicy: pol is null (conflict case)");
	    return pol;
        }
	// else ( n < 1) <=> not found
	else throw (new INV_POLICY("obj " + obj + 
                                   " does not have a policy of type " + type));

    } // getPolicy



    // ***** object member operations *****

    /** inserts a member object into this domain. */
    public void insertMember(org.omg.CORBA.Object obj)
    { 
        synchronized (_memberLock)
        {
            // getORBDomain();
            if ( this.hasMember(obj) ) 
            {
                // if already a member don't insert twice
                return;
            }

            Domain group[]= getDomains(obj); // retrieve old domain group

            // create auto name
            _NameCount++;
            String autoname= _autonamingPrefix + Long.toString(_NameCount);
	
            _members.put    (obj, autoname);
            _memberNames.put(autoname, obj);
    
            Domain self= _this();
    
            // set own odm
            this.insertMapping(obj, group);
            this.addToMapping(obj, self);
	
            // update odms of group: unreliable multicast
            for ( int i= 0; i < group.length; i++)
                try
                {
                    group[i].addToMapping(obj, self);
                }
                catch (org.omg.CORBA.COMM_FAILURE failure) { continue; }
	
            // inform all orb domain caches of changed membership
            invalidateORBDomainCaches(obj, getRootDomain(), new Hashtable());
        }
    } // insertMember


    /** 
     * deletes a member object from this domain. 
     */

    public void deleteMember(org.omg.CORBA.Object obj)
    { 
        synchronized (_memberLock)
        {
            if ( ! this.hasMember(obj) ) // nothing to do if not a member
                return;

            Domain group[]= getDomains(obj); // retrieve old domain group
	
            String name= getNameOf(obj);
            _members.remove(obj); 
            if ( name != null && ! name.equals("") ) 
                _memberNames.remove(name);
            // else do nothing because obj was not a member of this domain prior to operation 
            // call
	
            Domain self= _this();

            // update own odm
            this.deleteMapping(obj);

            // update odms of group: unreliable multicast
            for ( int i= 0; i < group.length; i++)
                try
                {
                    group[i].removeFromMapping(obj, self);
                }
                catch (org.omg.CORBA.COMM_FAILURE failure) { continue; }

            // inform all orb domain caches of changed membership
            invalidateORBDomainCaches(obj, getRootDomain(), new Hashtable());
        }
    
    } // deleteMember

    /** 
     * checks whether this domain has the specified 
     * object as a direct member. 
     */

    public boolean hasMember(org.omg.CORBA.Object obj)
    { 
        //synchronized (_memberLock)
        {
            return _members.containsKey(obj); 
        }
    }


    /**
     * checks whether this domain has the specified object 
     * as a indirect member
     * This funciton uses the private function "hasIndirectMember".
     *  @see org.jacorb.orb.domain.DomainImpl#hasIndirectMember
     */

    public boolean hasIndirectMember(org.omg.CORBA.Object obj)
    { 
        return hasIndirectMember(_this(), obj, new Hashtable() );
    }


    /** 
     * lists the direct object members of this domain. 
     */

    public org.omg.CORBA.Object[] getMembers()
    { 
        synchronized (_memberLock)
        {
            Enumeration objectEnum= _members.keys();
            // convert Enumeration to array
            org.omg.CORBA.Object obj;
            int oldSize= _members.size();
            org.omg.CORBA.Object[] result = new org.omg.CORBA.Object[oldSize];
            int counter= 0;
    
            while ( objectEnum.hasMoreElements() ) 
            {
                // only return valid elements
                // result[counter]= (org.omg.CORBA.Object) objectEnum.nextElement();
                obj= (org.omg.CORBA.Object) objectEnum.nextElement();

                if (obj._non_existent()) // check if object is still alive
                { // remove 
                    Debug.output(Debug.DOMAIN | Debug.INFORMATION, 
                                 "DomainImpl.get"
                                 +"Members: removing a invalid object reference from domain members");
                    this.deleteMember(obj);
                }
                else 
                {
                    result[counter]= obj;
                    counter++;
                }
            }
            if (counter == oldSize) // any failures ?
                return result;        // no, so return normal array
            else                    // yes, resize return array
            {
                // copy valid entries form result to result2
                Debug.output(Debug.DOMAIN | Debug.DEBUG1, "oldSize: "+ 
                             oldSize + "counter: "+ counter);
                org.omg.CORBA.Object[] result2= 
                    new org.omg.CORBA.Object[counter];    // counter == #valid refs
                for (int i= 0; i < counter; i++)
                    result2[i]= result[i];
                return result2;
            }
        }
    } // getMembers

    /**
     * returns the number of members. 
     */

    public int getMemberCount() 
    {
        // synchronized (_memberLock)
        {
            return _members.size(); 
        }
    }

    /** 
     * lists the indirect members of this domain. 
     *  This function uses the private static operation getIndirectMembers.
     *  @see org.jacorb.orb.domain.DomainImpl#getIndirectMembers
     */

    public org.omg.CORBA.Object[] getIndirectMembers()
    {
        Hashtable result= new Hashtable();
        getIndirectMembers(_this(), new Hashtable(), result);
        Enumeration objectEnum= result.keys();
        // convert Enumeration to array
        int n;
        org.omg.CORBA.Object[] toReturn = 
            new org.omg.CORBA.Object[n= result.size()];
        org.jacorb.util.Debug.output(4, "found " + n +" indirect members");
        int i= 0;
        while ( objectEnum.hasMoreElements() ) 
        {
            toReturn[i]= (org.omg.CORBA.Object) objectEnum.nextElement();
            i++;
        }
        return toReturn;
    } 


    // ************* object naming operations *******************

    /** 
     * gets the auto naming prefix. The auto naming prefix 
     * is the prefix of the name used by autonaming. Autonaming
     * names all objects which are inserted by the "insertMember" 
     * operation with the autonaming prefix and an unique number. 
     */

    public String NameAutoPrefix()
    {
        return _autonamingPrefix;
    }

    /**
     * sets the auto naming prefix. 
     * @see DomainImpl#NameAutoPrefix 
     */

    public void NameAutoPrefix(String arg)
    {
        _autonamingPrefix= arg;
    }

    /**
     * inserts an object as member in this domain. Associates 
     * an object name with the object. If the name is in use 
     * in this domain the exception NameAlready in
     *  use is raised.
     *  @see DomainImpl#insertMember
     */

    public void insertMemberWithName(String objName, 
                                     org.omg.CORBA.Object obj) 
        throws org.jacorb.orb.domain.NameAlreadyDefined
    {
        synchronized (_memberLock)
        {
            if ( _memberNames.containsKey(objName) )
                throw new NameAlreadyDefined(objName);
   
            if ( this.hasMember(obj) )
            { // if already a member don't insert twice, but change name
                String oldName= getNameOf(obj);
                try 
                {
                    if ( oldName.equals(objName) ) return;
                    else renameMember(oldName, objName);
                }
                catch (InvalidName inv) {} // impossible because this.hasMember(obj)
                return;
            }
    
            Domain group[]= getDomains(obj); // retrieve old domain group
  
            _memberNames.put(objName, obj);
            _members.put    (obj, objName); // may overwrite object name

            Domain self= _this();
     
            // set own odm
            this.insertMapping(obj, group);
            this.addToMapping(obj, self);
    
            // update odms of group: unreliable multicast
            for ( int i= 0; i < group.length; i++)
                try
                {
                    group[i].addToMapping(obj, self);
                }
                catch (org.omg.CORBA.COMM_FAILURE failure) { continue; }

            // inform all orb domain caches of changed membership
            invalidateORBDomainCaches(obj, getRootDomain(), new Hashtable());
        }
    } // insertMemberwithName

    /** 
     * renames a member. The old name is used to retrieve 
     * the object to rename. If the
     * old name is not valid in this domain the exception 
     * InvalidName is raised. If the new name is already in 
     * use in this domain the exception NameAlreadyDefined is
     * raised. If one of the two exception is raised, this
     * operation has no effect.
     */

    public void renameMember( String oldName, 
                              String newName) 
        throws org.jacorb.orb.domain.InvalidName, 
               org.jacorb.orb.domain.NameAlreadyDefined
    {
        // firstly, check for exceptions
        org.omg.CORBA.Object obj= resolveName(oldName); 
        if (obj == null) throw new InvalidName(oldName); 
        if ( _memberNames.containsKey(newName) )
            throw new NameAlreadyDefined(newName);

        // ok, no exceptions, do the work

        // remove (oldName, obj) 
        _memberNames.remove(oldName);
        _members.remove(obj);

        // associate newName with obj
        _memberNames.put(newName, obj);
        _members.put    (obj, newName); 
    } // renameMember
	
    /**
     * gets the local name of an object. Local means in 
     * the direct scope of this domain.
     *  @return name the name of the object "obj" 
     *  @return ""if the object "obj" is not a member of this domain 
     */

    public String getNameOf(org.omg.CORBA.Object obj)
    {
        java.lang.Object result= _members.get(obj);
        if ( result != null )
            return (String) result;
        else 
            return "";
        // else return null;
    }


    /** 
     * resolves a name. The name must be valid within this 
     * domain. Otherwise the exception InvalidNames is raised.
     *  @return obj an object identified by objName
     */

    public org.omg.CORBA.Object resolveName( String objName) 
    // throws org.jacorb.orb.domain.InvalidName
    {
        return (org.omg.CORBA.Object) _memberNames.get(objName);
        //  java.lang.Object obj= _memberNames.get(objName);
        //      if ( obj != null )
        //        return (org.omg.CORBA.Object) obj;
        //      else throw new InvalidName(objName);
    }
   
    // ************* domain naming operations *******************

    /** 
     * returns the separator. The separator is used to 
     * seperate names in a domain path name from each other. 
     * For example in the domain path name "states/france/paris" 
     * the string "/" serves as separator.
     */

    public String separator() 
    {
        return _sep; 
    }

    /** 
     * sets the separator.  The separator is used to seperate 
     * names in a domain path name  from each other. For 
     * example in the domain path name "states/france/paris"
     * the string  "/" serves as separator.
     */

    public void separator(String arg) 
    { 
        _sep= arg;
    }

    /** 
     * finds a child identified by a name. The child 
     * domain is identied by childName and must
     *  be a child domain of this domain.
     *  @param childName the name of the child domain to be found
     *  @return the child domain identified by childNam, 
     * null if there is no such child
     */

    public org.jacorb.orb.domain.Domain findChild( String childName) 
    //     throws org.jacorb.orb.domain.InvalidName
    {
        java.lang.Object hashResult = _child_domain_names.get(childName);
        // if (hashResult == null)
        //  throw new InvalidName(childName);

        return DomainHelper.narrow( (org.omg.CORBA.Object) hashResult);
    }
   
    /** 
     * renames a child domain. The old name is used to retrieve 
     * the child to rename. If the old name is not valid in 
     * this domain the exception InvalidName is raised. If the
     * new name is already in use in this domain the exception 
     * NameAlreadyDefined is raised. This operation also sets 
     * the name of the child domain.
     *  If one of the two exception is raised, this operation 
     *  has no effect.
     */

    public void renameChildDomain( String oldName, 
                                   String newName) 
        throws org.jacorb.orb.domain.InvalidName, 
               org.jacorb.orb.domain.NameAlreadyDefined
    {
        // firstly, check for exceptions
        if ( _child_domain_names.containsKey(newName) )
            throw new NameAlreadyDefined(newName);
        Domain child = findChild(oldName);
        if (child == null) 
            throw new InvalidName(oldName);

        child.name(newName);
        //      if ( ! newName.equals( child.name() ) ) 
        //        {
        //  	Debug.output(Debug.DOMAIN | Debug.IMPORTANT, "child name " + child.name() + " != " + newName); 
        //  	throw new InvalidName(newName);
        //        }

        // ok, no exceptions, do the work

        // remove (oldName, child) 
        _child_domain_names.remove(oldName);

        // associate newName with child
        _child_domain_names.put(newName, child);

    } // renameChildDomain


    /** 
     * resolves a domain path name. The path name is local to this 
     * domain. A domain path name consists of domain names
     * separated by the separator. Which separator is used can be set
     * by the separator attribute. An pathname example is "a/c/d/c" 
     * using "/" as separator. Firstly there must be child domain 
     * named a of this domain. That must have a child domain c, and so on.
     * Effectivly this function is the recursive version of findChild.
     *
     *  @param pathname a domain pathname
     *  @return domain a domain identified by the domain path name. 
     *      The name of the returned domain  matches the last portion
     *      of the domain path name.
     *  @exception InvalidName if a domain identified by 
     *      pathname is not found in this domain
     */

    public org.jacorb.orb.domain.Domain resolveDomainPathName(String pathname) 
        throws org.jacorb.orb.domain.InvalidName
    { 
        if (pathname == null) 
            throw new InvalidName("***pathname is null***");

        if( pathname.startsWith(_sep) )  //  pathname looks like "/...."
        {
            if ( pathname.equals(_sep) )    //  shortcut for root domain
                return getRootDomain();

            // else remove "/"
            if ( isRoot() )
                return this.resolveDomainPathName( deleteSeparatorAtFront( pathname ) );
            else 
                return getRootDomain().resolveDomainPathName( deleteSeparatorAtFront( pathname ) );
        }
     
        // now pathname does not start with "/"

        String childName, restOfPathname; // pathname -> (childName, restOfPathname)
        Domain child, current= _this();
        int end;

        while( true ) // non-recursive version
        {
            end  = pathname.indexOf(_sep);     
            // cut pathname into childname and rest
            if (end < 0) 
            { // "a" -> (a, "")
                // Debug.output(Debug.DOMAIN | 2, "recursion end: " + pathname);
                childName= pathname;
                restOfPathname= "";
            }
            else   // "a/b..." -> (a, b...)
            {
                childName= pathname.substring(0, end );
                restOfPathname= pathname.substring(end + _sep.length(), 
                                                   pathname.length() );
                //  Debug.output(Debug.DOMAIN | 2, "recursion continues with child " + childName
                //  			  +" and rest of pathname= " + restOfPathname);

            }

            child = current.findChild(childName);
            // throw exception if not found
            if (child == null) 
                throw new InvalidName(childName);

            if (restOfPathname.length() < 1)      // recursion end
                return child;

            // there is more to do ...
            pathname= restOfPathname;
            current= child;
        } // while
    } // resolveDomainPathName

 
    /** 
     * deletes the separator string from the pathname. 
     *  Pre: pathname.startsWith(separator())
     */
    private String deleteSeparatorAtFront(String pathname)
    {
        String result;
        result= pathname.substring(_sep.length(), pathname.length() );
        return result;
    }

    /** 
     * returns a list of valid pathnames for this domain.
     */
    public String[] getPathNames()
    {
        // TODO
        return null;
    }

    // parent operations

    /** 
     * checks whether this domain is a root domain in a potientallly
     * domain hierarchy.
     * @return True iff this domain has no parents 
     */

    public boolean isRoot()
    { 
        return _parents.size() == 0; 
    }
    
    /** 
     * returns the root domain of this domain.
     * It delegates this call to the optimized private 
     * function getRootDomain(Domain, Hashtable)
     *  @see org.jacorb.orb.domain.DomainImpl#getRootDomain(Domain, Hashtable)
     */

    public org.jacorb.orb.domain.Domain getRootDomain() 
    {
        return getRootDomain(_this(), new Hashtable());
    }

    //    public org.jacorb.orb.domain.Domain getRootDomain() 
    //    { 
    //      if ( isRoot() ) return _this();

    //      // else search the graph upwards

    //       Enumeration domainList= _parents.keys();
    //      // convert Enumeration to array
    //      Domain ds, oldfoundRoot= null, foundRoot;    
    //      while ( domainList.hasMoreElements() ) 
    //        {
    //  	ds= (Domain) domainList.nextElement();
    //  	org.jacorb.util.Debug.output(2, "Domain(" + _name +  ").getRootDomain: calling getRootDomain"
    //  				 +", parent is " + ds.name());
    //  	foundRoot= ds.getRootDomain();
    //  	org.jacorb.util.Debug.assert(1, foundRoot != null,"DMImpl.getRootDomain:"
    //  				 + " found root is null");
    //  	if (oldfoundRoot == null) oldfoundRoot= foundRoot;       // first step
    //  	else 
    //  	  { // check equality
    //  	    if (oldfoundRoot._is_equivalent(foundRoot)) ; // that's ok
    //  	    else throw new org.jacorb.util.AssertionViolation
    //  		   ("DMImpl.getRootDomain: invariant of "
    //  		    +" unique root violated");
    //  	  }

    //        }
    //      return oldfoundRoot;
    //    }


    /************************ child operations ***********************/


    /**
     * inserts a child domain into this domain.
     * Pre: NOT child.isReachable(this)  (to prevent cycles)
     *       the precondition of Domain.addParent also needs to be satisfied
     * Post: this.hasChild(child) AND child.hasParent(this)
     *
     * @param child the domain to be inserted as child domain
     * @exception org.jacorb.orb.domain.GraphNodePackage.ClosesCycle 
     *            if the insertion of the domain "child" would have
     *            closed a cycle in the domain graph. This is not allowed.
     * @exception org.jacorb.orb.domain.NameAlreadyDefined 
     *            if a domain with the name child.name already
     *            exists as a child domain in this domain
     */

    public void insertChild( Domain child ) 
        throws org.jacorb.orb.domain.GraphNodePackage.ClosesCycle,
               org.jacorb.orb.domain.NameAlreadyDefined
    {
        Domain self = _this();

        // check precondition
        org.jacorb.util.Debug.assert(2, child != null, "child is null");
        org.jacorb.util.Debug.assert(2, self != null, "self is null");

        if ( child.isReachable(self) ) 
            throw new org.jacorb.orb.domain.GraphNodePackage.ClosesCycle();

	// ("this operation would close a cycle in the domain graph");

        String name = child.name();
        if ( _child_domain_names.containsKey( name ) )
            throw new org.jacorb.orb.domain.NameAlreadyDefined(name);
 
        _child_domains.put( child, child );       // insert into child list
        _child_domain_names.put( name, child );    
        // insert also into member set so that getDomains can find it
        //  try 
        //        {
        //  	insertMemberWithName(child.name(), child);             
        //        }
        //      catch (NameAlreadyDefined already)
        //        {
        //  	Debug.output(2, "DomainImpl.insertChild: name " + already.name + " of child "
        //  		     +"already in use, inserting with autoname ... ");
        //  	insertMember(child);
        //  	Debug.output(2, "DomainImpl.insertChild: insert with autoname succeded.");
        //        }
                     
        // only insert, if not already there
        if (! child.hasParent( self ) )
        {
            child.insertParent( self ); 
        }

        // check postcondition
        org.jacorb.util.Debug.assert( 2, self.hasChild( child ) && child.hasParent( self ), 
                                 "post condition of Domain.insertChild violated");
	    
    } // insertChild
    
    /** 
     * removes a child domain from the domain.
     */

    public void deleteChild(Domain dm) 
    {
        Domain self= _this();

        _child_domains.remove(dm);
        // _members.remove(dm);
        try 
        {
            _child_domain_names.remove( dm.name() );

            if ( dm.hasParent( self ) )
                dm.deleteParent( self );

            org.jacorb.util.Debug.assert(2, ! dm.hasParent(self) && ! self.hasChild(dm),
                                     "DSImpl.deleteChild: post condition violated");
        }
        catch (org.omg.CORBA.COMM_FAILURE fail)
        {
            Debug.output(Debug.DOMAIN | 2, 
                         " DomainImpl.deleteChild: child domain doesn't answer, OK");
        }

    } // deleteChild

    /** 
     * checks whether this domain has the specified domain as
     *  child domain.
     *  @return true iff domain_manager is a direct child of this domain 
     */

    public boolean hasChild( Domain aDomain )
    {
        //  org.jacorb.util.Debug.assert(2, aDomain != null,
        //  " Domain.hasChild: parameter \"aDomain\" is null.");
        //      java.lang.Object hashResult= _child_domains.get( aDomain.name() );
        //      if ( hashResult == null) return false;
        //      // cannot simply return true, another domain could have the same name, but not be a child
        //      Domain found= DomainHelper.narrow( (org.omg.CORBA.Object) hashResult);
        //      return found._is_equivalent(aDomain);
        return _child_domains.containsKey( aDomain );
    } 

    /** 
     *  gets the child domains of this domain service.
     *  @return an array of all child domains of this domain. There is no order in the
     *          returned list.
     */

    public Domain[] getChilds()
    {
        Domain result[]= new Domain[_child_domains.size()];
        // Enumeration objectEnum= _child_domains.keys();
        Enumeration objectEnum= _child_domains.keys();
        // convert enumeration to array
        int i= 0;
        while ( objectEnum.hasMoreElements() ) {
            result[i]= (Domain) objectEnum.nextElement() ;
            i++;
        }
        return result;
    }

    /** 
     * returns the number of child domains. 
     */	

    public int getChildCount() 
    {
        return _child_domains.size(); 
    }


    /** 
     *  checks whether this domain has a domain as a child somewhere down a path.
     *  This is equivalent to check whether there exists a directed path from 
     *  this domain to the specified child domain. This method
     *  is mainly used for detecting and preventing cycles in a domain graph.
     *  @return true iff there exists a directed path from this domain 
     *          downwards to the node "aDomain".
     */

    public boolean isReachable(Domain aDomain)
    { 
        // firstly, check self
        if ( _this()._is_equivalent(aDomain) ) return true;
 
        // secondly, check direct childs
        if ( this.hasChild(aDomain) ) return true;
 
        // thirdly, check all indirect childs by depth-first search

        Domain child;
        Enumeration objectEnum= _child_domains.keys();
        // convert enumeration to array
        while ( objectEnum.hasMoreElements() ) 
        {
            child= (Domain) objectEnum.nextElement() ;
            if ( child.isReachable(aDomain) ) 
                return true;
        }
        // no matching child found
        return false;
    } // isReachable


    /** 
     * adds a new parent domain to this domain. <br>
     * Pre :         this.isRoot()
     *       OR (NOT this.isRoot()  
     *           AND getRootDomain(parentDS) == getRootDomain(this))
     *       the precondition of Domain.addChild must also be satisfied <br>
     * Post: this.hasParent(parentDS) AND parentDS.hasChild(this)
     *       AND NOT this.isRoot() 
     *       AND getRootDomain(parentDS) == getRootDomain(this)
     *       (this is now a child domain of the parent domain parentDS and
     *        they have the same root domain)
     * @param parentDS the domain to be added as parent domain
     * @see org.jacorb.orb.domain.DomainImpl#insertChild
     * @see org.jacorb.orb.domain.DomainImpl#getRootDomain
     */

    public void insertParent( org.jacorb.orb.domain.Domain parentDS )
        throws org.jacorb.orb.domain.GraphNodePackage.ClosesCycle,
               org.jacorb.orb.domain.NameAlreadyDefined
    { 
        Hashtable domain2root= new Hashtable();
        Domain self          = _this();
        Domain rootOfSelf    = getRootDomain(self, domain2root);
        Domain rootOfParent  = getRootDomain(parentDS, domain2root);

        // check precondition
        org.jacorb.util.Debug.assert(Debug.DOMAIN | Debug.IMPORTANT, 
                                 self.isRoot() || 
                                 ( !self.isRoot() && 
                                   rootOfParent._is_equivalent(rootOfSelf) ),
                                 "DMImpl.insertParent: precondition violated");

        org.jacorb.util.Debug.output(Debug.DOMAIN | Debug.DEBUG1,
                                 "Domain("+_name+").insertParent: "
                                 +" adding " + parentDS.name()+ " as parent domain.");

        org.jacorb.util.Debug.assert( !hasParent( parentDS ), "Parent already there");

        if ( self.isReachable(parentDS) ) 
            throw new org.jacorb.orb.domain.GraphNodePackage.ClosesCycle();
	// ("this operation would close a cycle in the domain graph");

        // update ODM`s in merging graphs
        exchangeODMInformation(parentDS,    
                               self, 
                               rootOfParent,
                               rootOfSelf );

        _parents.put( parentDS, parentDS ); // put into parent table   

        // set at parent
        if ( ! parentDS.hasChild(self) )
        {
            // org.jacorb.util.Debug.output(Debug.DOMAIN | Debug.DEBUG1, "DomainImpl(" + _name 
            //			  +")insertParent.: BEFORE insertChild");
            try 
            {
                parentDS.insertChild( self );
            } 
            catch (org.jacorb.orb.domain.NameAlreadyDefined already)
            { 
                // undo insertion and rethrow exception
                // don't undo exchange of odm information
                _parents.remove( parentDS );
                throw already;
            }
        }
        // check post condition
        domain2root.clear();
        rootOfSelf    = getRootDomain(self, domain2root);
        rootOfParent  = getRootDomain(parentDS, domain2root);
        org.jacorb.util.Debug.assert(1, self.hasParent(parentDS) && 
                                 parentDS.hasChild(self),
                                 "DMImpl.insertParent: child <-> parent postcond. violated");
        org.jacorb.util.Debug.assert(1, ! self.isRoot(), 
                                 "DMImpl.insertParent: root postcondition violated");

        if ( ! rootOfSelf._is_equivalent(rootOfParent) ) 
        {
            org.jacorb.util.Debug.output(1, self.name() + ": parent " + parentDS.name() 
                                     +"has root " + rootOfParent.name()
                                     +"I have "+ rootOfSelf.name() + " as root.");
        }
        org.jacorb.util.Debug.assert(1, rootOfParent._is_equivalent(rootOfSelf) ,
                                 "DMImpl.insertParent: unique root postcondition violated");
    } // insertParent

    /** 
     *  removes a parent domain from the list of parent domains.
     *  Post: NOT this.hasParent(parentDS) AND NOT parentDS.hasChild(this)
     */

    public void deleteParent(org.jacorb.orb.domain.Domain parentDS)
    {
        Domain self;
        // TODO: check precondition

        _parents.remove(parentDS);
        if (parentDS.hasChild( self= _this() ) )
            parentDS.deleteChild( self );
    
        // check post condition
        org.jacorb.util.Debug.assert(2, !parentDS.hasChild(self) && !self.hasParent(parentDS),
                                 "DMImpl.insertParent: child <-> parent postcondition violated");
    }
  
    /** 
     * checks whether this domain has the specified parent domain
     * @return true iff parentDS is a parent domain of this  domain
     */

    public boolean hasParent(org.jacorb.orb.domain.Domain parentDS)
    { 
        return _parents.containsKey(parentDS);
    }
	
    /** 
     * lists all parent domains. 
     */

    public org.jacorb.orb.domain.Domain[] getParents()
    {
        Enumeration objectEnum= _parents.keys();
        // convert Enumeration to array
        Domain[] result= new Domain[_parents.size()];
        int i= 0;
        while ( objectEnum.hasMoreElements() ) 
        {
            result[i] = DomainHelper.narrow
                ( (org.omg.CORBA.Object) objectEnum.nextElement() );
            i++;
        }
        return result;
    }

    /** 
     * returns the number of parent domains. 
     */

    public int getParentCount() 
    { 
        return _parents.size(); 
    }

    /// ObjectDomainMapping operations
    // all calls are delegated

    /** 
     * returns an ODM. 
     */

    private final ODMImpl getODM()
    {
        if (_odm == null) 
            _odm= new ODMImpl();
        return _odm;
    }

    /**
     *  inserts a complete new mapping. 
     *  Post: hasMapping(obj) AND  dms == getMapping(obj) 
     *        AND for all dm e dms: areMapped(obj, dm)
     */

    public final void insertMapping(org.omg.CORBA.Object obj, 
                                    org.jacorb.orb.domain.Domain[] dms)
    {
        getODM().insertMapping(obj, dms);
        getORBDomain().updateODMCache(obj, dms);
    }

    /** 
     *  deletes a mapping completely. 
     *  Post: NOT hasMapping(object)
     */

    public final void deleteMapping(org.omg.CORBA.Object obj)
    {
        getODM().deleteMapping(obj);
        getORBDomain().invalidateODMCache(obj);
    }

    /** 
     *  returns a mapping. 
     *   Pre : hasMapping(obj)
     *   @return the domain manager list associated with object obj
     */
    public final org.jacorb.orb.domain.Domain[] getMapping(org.omg.CORBA.Object obj)
    {
        return getODM().getMapping(obj);
    }

    /** 
     * checks whether there is a mapping defined for obj ? (could also
     * be the empty list).
     */

    public final boolean hasMapping(org.omg.CORBA.Object obj)
    {
        return getODM().hasMapping(obj);
    }

    /** 
     *  is obj mapped to dm ? 
     */

    public final boolean areMapped(org.omg.CORBA.Object obj, 
                                   org.jacorb.orb.domain.Domain dm)
    {
        return getODM().areMapped(obj, dm);
    }

    /** 
     *  add an domain to the mapping of an object. 
     *  Post: hasMapping(obj) AND areMapped(obj, dm)
     */

    public final void addToMapping(org.omg.CORBA.Object obj, 
                                   org.jacorb.orb.domain.Domain dm)
    {
        getODM().addToMapping(obj, dm); // change odm
        getORBDomain().addToODMCache(obj, dm ); // update cache
    }


    /** 
     *  removes a domain from the mapping of object obj. 
     *  Post: NOT areMapped(obj, dm)
     */

    public final void removeFromMapping(org.omg.CORBA.Object obj, 
                                        org.jacorb.orb.domain.Domain dm)
    {
        getODM().removeFromMapping(obj, dm);
        getORBDomain().addToODMCache(obj, dm );
    }
  

    // ************ private helper functions  **********

    /** 
     * gets the poa. 
     */

    public org.omg.PortableServer.POA _getPOA()
    {
        return _poa();
    }

    /**
     * gets the orb. 
     */

    public org.omg.CORBA.ORB _getORB()
    {
        return _orb();
    }

    /**
     * gets the orb domain. 
     */

    private final org.jacorb.orb.domain.ORBDomain getORBDomain()
    {
        if (theORBDomain == null)
        {
            try
            {
                return (org.jacorb.orb.domain.ORBDomain) _getORB().resolve_initial_references("LocalDomainService");
            }
            catch (org.omg.CORBA.ORBPackage.InvalidName inv) 
            { 
                Debug.output(Debug.DOMAIN | Debug.IMPORTANT, inv);
            }
        } // if

        return theORBDomain;
    } // getORBDomain

    /** 
     * called from insertChild to exchange the ODM infomration of the 
     *  two domain graphs which get merged by insertChild.
     * 
     *  @param parent the parents side of the parent-child-relationship
     *  @param child  the child side of the parent-child-relationship
     *  @param rootOfParent the root domain of the parent domain, 
     *         used as hint to speedup 
     *         calculation, if null, the hint is not used
     *  @param rootOfChild the root domain of the child domain, 
     *         used as hint to speedup 
     *         calculation, if null, the hint is not used
     */

    private final static void exchangeODMInformation(Domain parent,
                                                     Domain child,
                                                     Domain rootOfParentHint, 
                                                     Domain rootOfChildHint)
    {
        //  Debug.output(Debug.DOMAIN | Debug.INFORMATION, "DomainImpl.exchangeODMInformation: "
        //  		 +" starting ...");
        if (isEmptyDomain(parent)  || isEmptyDomain(child) )
            return; // intersection empty

        //   Debug.output(Debug.DOMAIN | Debug.INFORMATION, "DomainImpl.exchangeODMInformation: "
        //  		 +"between isEmpty and is_equivalent");

        Domain parentRoot;
        Domain childRoot;
        Hashtable domain2root= new Hashtable();

        if (rootOfParentHint == null) parentRoot= getRootDomain(parent, domain2root);
        else                          parentRoot= rootOfParentHint;

        if (rootOfChildHint == null) childRoot  = getRootDomain(child, domain2root);
        else                         childRoot  = rootOfChildHint;

        if (parentRoot._is_equivalent(childRoot))
            return; // just an edge

        //    Debug.output(Debug.DOMAIN | Debug.INFORMATION, "DomainImpl.exchangeODMInformation: "
        //  		 +" do real work ...");
        // ok, two graphs have been merged, now the update work starts
        Hashtable parentMembers= new Hashtable();
        Hashtable childMembers = new Hashtable();

        domain2root.clear(); // reuse
        getAllMembers(parentRoot, domain2root, parentMembers);
        domain2root.clear(); // reuse
        getAllMembers(childRoot,  domain2root, childMembers);
        //   Debug.output(Debug.DOMAIN | Debug.INFORMATION, "DomainImpl.exchangeODMInformation: "
        //  		 +parentMembers.size() + " members in " +  parentRoot.name());
        //      Debug.output(Debug.DOMAIN | Debug.INFORMATION, "DomainImpl.exchangeODMInformation: "
        //  		 +childMembers.size() + " members in " + childRoot.name() );

        Hashtable intersection= calculateIntersection(parentMembers, childMembers);
        Debug.output(Debug.DOMAIN | Debug.DEBUG1, "DomainImpl.exchangeODMInformation: "
                     +intersection.size() + " members in intersection.");
        Domain parentGroup[], childGroup[], mergedGroup[];
        org.omg.CORBA.Object obj;
        int i, j;
        Enumeration objectEnum= intersection.keys();

	while ( objectEnum.hasMoreElements() ) 
        {
	    obj=  (org.omg.CORBA.Object) objectEnum.nextElement();

	    parentGroup= parentRoot.getDomains(obj);
	    childGroup = childRoot.getDomains(obj);

	    // eleminate duplicates (*is* necessary)
	    mergedGroup= union(parentGroup, childGroup);
	  
	    for (i =0 ; i < parentGroup.length; i++) 
                parentGroup[i].insertMapping(obj, mergedGroup);

	    for (i =0 ; i <  childGroup.length; i++) 
                childGroup[i].insertMapping(obj, mergedGroup);

        }    
    } // exchangeODMInformation

    /** 
     *  creates the union of two arrays. The resulting array contains all members from
     *  array a and b. There are no duplicates in the result array. a and b are not
     *  modified.
     */

    private static final Domain[] union(Domain a[], Domain b[])
    {
        int i;
        Hashtable set= new Hashtable(a.length + b.length);

        for (i= 0; i < a.length; i++) set.put( a[i], a[i] );
        for (i= 0; i < b.length; i++) set.put( b[i], b[i] );

        Domain result[]= new Domain [ set.size() ];
        Enumeration domainEnum= set.elements();

        // convert Enumeration to array
        i= 0;
        while ( domainEnum.hasMoreElements() ) 
        {
            result[i]= (Domain) domainEnum.nextElement();
            i++;
        }
        //   int diff= a.length + b.length - set.size();
        //      if (diff > 0)
        //        {
        //  	Debug.output(Debug.DOMAIN | Debug.INFORMATION, "DomainImpl.union: eleminated "
        //  		     + diff + " duplicates.");
        //        }
        return result;
    }
    private final static boolean isEmptyDomain(Domain aDomain)
    {
        return aDomain.getMemberCount() == 0
            && aDomain.getChildCount()  == 0
            && aDomain.getParentCount() == 0;
    }

    /** 
     * calculates the intersection between hashtable a and b.
     * 
     *  @param (a and b) the two hashtables, NOTE: both hashtables are subject to change, so
     *         don't use them after operation call
     *  @return a hashtable containing the intersection of the hashtables a and b. Note: To speed
     *          up the calculation one of the two hashtables (the smaller one) is reused 
     *          and returned
     */

    private final static Hashtable calculateIntersection(Hashtable a, Hashtable b)
    {
        if (a == null || b == null)
            return null;

        java.lang.Object obj;
        if ( a.size() < b.size() ) // use the smallest one
        {
            Enumeration objectEnum= a.keys();
            while ( objectEnum.hasMoreElements() ) 
            {
                obj=  objectEnum.nextElement();
                if ( b.contains(obj) ) ; // ok, element is in intersection
                else a.remove(obj);      // not in intersection, remove from result set
            }
            return a;
        }
        else
        {
            Enumeration objectEnum= b.keys();
            while ( objectEnum.hasMoreElements() ) 
            {
                obj=  objectEnum.nextElement();
                if ( a.contains(obj) ) ; // ok, element is in intersection
                else b.remove(obj);      // not in intersection, remove from result set
            }
            return b;
        }
    } // calculateIntersection

    /** 
     * does the conflict resolution in the case of overlapping domains. 
     */

    private org.omg.CORBA.Policy doConflictResolution(Vector targetDomains, 
                                                      int overlapType,
                                                      PolicyFactory factory)
    {
        ConflictResolutionPolicy resolver;
        try 
        { 
            resolver = ConflictResolutionPolicyHelper.narrow 
                ( get_domain_policy(CONFLICT_RESOLUTION_POLICY_ID.value));
            org.jacorb.util.Debug.output(2, "DSImpl.doConflictResolution: using policy "
                                     + resolver.short_description()+
                                     " which states: \"" + 
                                     resolver.long_description() +"\"");
        } 
        catch (INV_POLICY invalid) 
        {
            // no conflict resolution policy defined, but we need one, 
            // so define a default one and insert it into policy list

            resolver = 
                factory.createConflictResolutionPolicy(ConflictResolutionPolicy.FIRST);
            org.jacorb.util.Debug.output(2, 
                                     "DSImpl.doConflictResolution: no conflict policy found. "
                                     + "Creating a FIRST conflict resolution policy and inserting it into this domain."); 
				 
            this.overwrite_domain_policy(resolver);
        }
        Domain overlappingDM[] = new Domain[targetDomains.size()];
        targetDomains.copyInto(overlappingDM);

        return resolver.resolveConflict(overlappingDM, overlapType);
        // Domain dm= resolver.resolveConflict(overlappingDM, overlapType);
        // return dm.get_domain_policy(overlapType);
    } // doConflictResolution


    /**  
     *  does a depth-frist search downwards the domain structure to 
     *  obtain all domains which 
     *  contain the member "obj" as direct member. Every domain is traversed once.
     *  @param obj the object to search for
     *  @param ds the domain where to start the search 
     *  @param result a hashtable which is used to store the resulting domains 
     *  @param visitedDomain a hashtable which is used to store all 
     *  already visited domains
     */

    private final static void traverseDownwards(org.omg.CORBA.Object obj,
                                                Domain aDomain, 
                                                Hashtable result,
                                                Hashtable visitedDomains)
    {    
        org.jacorb.util.Debug.output(4,
                                 "D.traverseDownwards: visiting domain " + 
                                 aDomain.name());

        // firstly, check domain node "aDomain"

        // skip domain if already traversed
        if ( visitedDomains.containsKey(aDomain) ) 
        { 
            org.jacorb.util.Debug.output(4,"D.traverseDownwards: domain " + 
                                     aDomain.name()
                                     +" already (down)traversed, skipping it");
            return;
        }
        // else
        visitedDomains.put(aDomain , aDomain);   // mark as visited

        if ( aDomain.hasMember(obj) )      
        {
	    // put domain "aDomain" and all its parent domains into result table
	    // traverseUpwards(aDomain, result);
	    // TODO
	    // put domain "aDomain" into result table
	    result.put(aDomain, aDomain);
        }

        // secondly, check child domains

        Domain child[]= aDomain.getChilds();
        for (int i= 0; i < child.length; i++)
        {
            traverseDownwards(obj, child[i], result, visitedDomains);  // recursion
        }
    } // traverseDownwards

    /**  
     * does a depth-frist search downwards the domain structure. 
     * The search stops when a domain is
     * found which has the object "obj" as a member.
     *  Every domain is traversed at most once.
     *  @param obj the object to search for
     *  @param ds the domain where to start the search 
     *  @param result a reference holding a domain list which is
     *         the result of a successful search. The list in the holder 
     *         is the list of domains for the object "obj". This list 
     *         is taken from the ODM associated with the domain where 
     *          the search stops.
     *  @param visitedDomain a hashtable which is used to store 
     *          all already visited domains
     *  @return true, if the search was successful. The Holder
     *          then contains a valid domain list of object "obj"
     *  @return false, if the search was not successful, the 
     *          holder does *not* contain a valid list of domains
     *          whith the domain where the search stops.
     */

    protected final static boolean traverseDownwards(org.omg.CORBA.Object obj,
                                                     Domain aDomain, 
                                                     DomainListHolder result, // out parameter
                                                     Hashtable visitedDomains)
    {    
        // Debug.output(Debug.DOMAIN | Debug.INFORMATION, "D.traverseDownwards: "
        // + "visiting domain " + aDomain.name() );

        // firstly, check domain node "aDomain"

        // skip domain if already traversed
        if ( visitedDomains.containsKey(aDomain) ) 
        { 
            // org.jacorb.util.Debug.output(Debug.DOMAIN | Debug.INFORMATION,"D.traverseDownwards: "
            //    +"domain " + aDomain.name() + " already (down)traversed, skipping it");
            return false;
        }
        // else
        visitedDomains.put(aDomain , aDomain);   // mark as visited

        if ( aDomain.hasMember(obj) )      
        {
	    result.value=  aDomain.getMapping(obj);
	    Debug.assert(1, result.value != null, 
                         "D<" + aDomain.name()+">: has member, but"
			 +" mapping is null");
	    Debug.output(Debug.DOMAIN | Debug.INFORMATION, 
                         "DomainImpl.traverseDownwards"
                         +" found domain: " + aDomain.name()); 
	    return true; 
        }

        // secondly, check child domains

        Domain child[] = aDomain.getChilds();
        for (int i= 0; i < child.length; i++)
        {
            if ( traverseDownwards(obj, child[i], result, visitedDomains) )  // recursion
                return true;
            // else try next child
        }
        // Debug.output(Debug.DOMAIN | Debug.INFORMATION, "DomainImpl.traverseDownwards"
        //			 +" nothing found in domain " + aDomain.name());
        // result.value= new Domain[0];
        return false;
    } // traverseDownwards

    /**  
     * does a depth-first search upwards the domain structure to obtain
     * all parent domains  of the domain "aDomain" inlcuding the domain
     * aDomain itself.
     *  @param aDomain the domain where to start the search 
     *  @param result a hashtable which is used to store the result 
     */

    private final static void traverseUpwards(Domain aDomain, 
                                              Hashtable result)

    {
        org.jacorb.util.Debug.output(4,"D.traverseUpwards: visiting domain " + 
                                 aDomain.name());

        // if the current domain is in the result table, 
        // all its parents are, too
        if ( result.containsKey(aDomain) )
        {
            org.jacorb.util.Debug.output(4,"D.traverseUpwards: domain " + 
                                     aDomain.name()
                                     +" already (up)traversed, skipping it");
            return;
        }
        // else   
        result.put(aDomain, aDomain);
        Domain parent[]= aDomain.getParents();
        for (int i= 0; i < parent.length; i++)
        {
            traverseUpwards(parent[i], result);
        }
    } // traverseUpwards


    /**  
     * does a graph traversal downwards the domain 
     * structure starting at a given domain. At every visited
     * orb domain invalidates its ODM cache of the objcet obj.
     * Every domain is traversed at most once.
     *
     *  @param obj the object to invalidate at each orb domain
     *  @param ds the domain where to start the traversal
     *  @param visitedDomain a hashtable which is used to store all already visited domains
     */

    protected final static void invalidateORBDomainCaches(org.omg.CORBA.Object obj,
                                                          Domain aDomain, 
                                                          Hashtable visitedDomains)
    {    
        // firstly, check domain node "aDomain"

        // skip domain if already traversed
        if ( visitedDomains.containsKey(aDomain) ) 
            return;

        // else
        visitedDomains.put(aDomain , aDomain);   // mark as visited
        // do the work
        try
        {
            ORBDomain orbDomain = ORBDomainHelper.narrow(aDomain);
            Debug.output(Debug.DOMAIN | Debug.DEBUG1, 
                         "DomainImpl.invalidateORBDomainCaches: "
                         + "invalidating odm cache of orb domain \"" + 
                         orbDomain.name() 
                         + "\" on a  \"" + Util.toID(obj.toString()) );
            orbDomain.invalidateODMCache(obj);
        }
        catch( org.omg.CORBA.BAD_PARAM bp )
        {
        }
        // else continue with childs
        // secondly, check child domains
						
        try 
        {
            Domain child[]= aDomain.getChilds();
            for (int i= 0; i < child.length; i++)
            {
                invalidateORBDomainCaches(obj, child[i], visitedDomains);   // recursion
            }
        }
        catch (org.omg.CORBA.COMM_FAILURE fail)
        {
            Debug.output(Debug.DOMAIN | Debug.INFORMATION, 
                         "DomainImpl.invalidateORBDomainCaches: "
                         +"comm failer occured, continue.");
        }
    } // invalidateORBDomainCaches

    /** 
     * gets the root domain of a specified domain.
     *   @return the root domain of the domain "aDomain" 
     */

    private final static org.jacorb.orb.domain.Domain getRootDomain(Domain aDomain)
    {
        org.jacorb.util.Debug.assert(2, aDomain != null, 
                                 "Domain.getRootDomain: Parameter aDomain is null");

        if ( aDomain.isRoot() ) return aDomain;

        // else search the graph upwards

        Domain domainList[]= aDomain.getParents();
        Domain ds, oldfoundRoot= null, foundRoot;    
        for (int i= 0; i < domainList.length; i++ ) 
        {
            ds= domainList[i];
            org.jacorb.util.Debug.output(Debug.DOMAIN | Debug.DEBUG1, 
                                     "Domain.getRootDomain: calling getRootDomain on "
                                     + aDomain.name() + ", parent is " + 
                                     ds.name());
            foundRoot= getRootDomain(ds);
            org.jacorb.util.Debug.assert(1, foundRoot != null,
                                     "DMImpl.getRootDomain:"
                                     + " found root is null");

            if (oldfoundRoot == null) 
                oldfoundRoot= foundRoot;       // first step
            else 
            { // check equality
                if (oldfoundRoot._is_equivalent(foundRoot)) ; 
                // if a domain has more than one parents all this parents must have the same root
                else throw new org.jacorb.util.AssertionViolation
                    ("DMImpl.getRootDomain: invariant of "
                     +" unique root violated");
            }

        }
        return oldfoundRoot;
    
    } // getRootDomain 


    /**
     * gets the root domain of the domain "aDomain".

     * This  version visits each  domain once. The search  upwards the
     * doamin  graph is  done by the  concept of  dynamic programming.
     * The  hashtable "domain2root" holds the  results achieved during
     * the run of the algorithm.
     *
     *  @param domain2root a hashtable which maps from domains to their root domain
     *  @return the root domain of the domain "aDomain" 
     */

    final static org.jacorb.orb.domain.Domain getRootDomain(Domain aDomain, 
                                                        Hashtable domain2root)
    {
        org.jacorb.util.Debug.assert(2, aDomain != null, 
                                 "Domain.getRootDomain: Parameter aDomain is null");

        if ( aDomain.isRoot() ) 
        { 
            domain2root.put(aDomain, aDomain);         
            // dynamic programming: fill up table
            return aDomain;
        }

        // else search the graph upwards

        Domain domainList[]= aDomain.getParents();
        Domain d, ds, oldfoundRoot= null, foundRoot;    
        for (int i= 0; i < domainList.length; i++ ) 
        {
            ds= domainList[i];

            // dynamic programming: reuse result if already calculated
            if ((d= (Domain) domain2root.get(ds)) != null) 
                return d;

            org.jacorb.util.Debug.output(3, 
                                     "Domain.getRootDomain(Dynamic): calling getRootDomain on "
                                     + aDomain.name() + ", parent is " + 
                                     ds.name());
            foundRoot= getRootDomain(ds, domain2root);

            org.jacorb.util.Debug.assert(1, foundRoot != null,
                                     "DMImpl.getRootDomain:"
                                     + " found root is null");

            if (oldfoundRoot == null) 
                oldfoundRoot= foundRoot;       // first step
            else 
            { 
                // check equality
                if (oldfoundRoot._is_equivalent(foundRoot)) ; 
                // if a domain has more than one parents all this parents must have the same root
                else throw new org.jacorb.util.AssertionViolation
                    ("DMImpl.getRootDomain: invariant of "
                     +" unique root violated");
            }

        }

        domain2root.put(aDomain, oldfoundRoot);       
        // dynamic programming: fill up table

        return oldfoundRoot;
    
    } // getRootDomain (dynamic programming)
  

    /* *************** private (indirect) member functions ******************
     *
     * The hasMember and getMember function operate on 
     * the direct members of a domain.
     * The next two functions, hasIndirectMember and 
     * getIndirectMembers, search the subgraph
     * of a domain for indirect members.
     */
  
    /** 
     *  checks whether  the domain  "aDomain" has  an  indirect member
     * "obj".   The domain "aDomain"  has an indirect  member "obj"if,
     * and only if, it has a subdomain which has the object "obj" as a
     * direct member.
     *
     *  @param aDomain the domain which might contain a indirect member
     *  @param obj the object to check
     *  @return True iff this domain has obj as indirect member */

    private static boolean hasIndirectMember(Domain aDomain,
                                             org.omg.CORBA.Object obj, 
                                             Hashtable visitedDomains)
    {    
        if ( aDomain.hasMember(obj) ) 
            return true;             // end of recursion
    
        // otherwise check all child domains of aDomain
        Domain child[]= aDomain.getChilds();
        for (int i= 0; i < child.length; i++)
        {
            // skip domains already traversed
            if (visitedDomains.containsKey( child[i]) )
                continue;
            // else
	
            visitedDomains.put(child[i], child[i]);
            if ( hasIndirectMember(child[i], obj, visitedDomains) ) 
                return true; // recursion
        }
    
        // obj not found in this subgraph
        return false;
    
    } // hasIndirectMember


    private final static void getIndirectMembers(Domain aDomain, 
                                                 Hashtable visitedDomains,
                                                 Hashtable result)
    {
        Domain child[]= aDomain.getChilds();
        for (int i= 0; i < child.length; i++)
        {
            // skip domains already traversed
            if (visitedDomains.containsKey( child[i]) ) continue;
            // else
            visitedDomains.put(child[i], child[i]);

            // add direct members of child to result
            org.omg.CORBA.Object member[]= child[i].getMembers();
            for (int j=0; j < member.length; j++) 
                result.put(member[j], member[j]);

            // recursion
            getIndirectMembers(child[i], visitedDomains, result);
        }
    }

    /** 
     * gets all members (direct and indirect members ) of a domain. 
     */

    private final static void getAllMembers(Domain aDomain, 
                                            Hashtable visitedDomains,
                                            Hashtable result)
    {
        org.omg.CORBA.Object directMembers[]= aDomain.getMembers();
        for (int i= 0; i < directMembers.length; i++)
            result.put(directMembers[i], directMembers[i]);

        Domain child[]= aDomain.getChilds();
        for (int i= 0; i < child.length; i++)
        {
            // skip domains already traversed
            if (visitedDomains.containsKey( child[i]) ) continue;
            // else
            visitedDomains.put(child[i], child[i]);

            // add direct members of child to result
            org.omg.CORBA.Object member[]= child[i].getMembers();
            for (int j=0; j < member.length; j++) 
                result.put(member[j], member[j]);

            // recursion
            getIndirectMembers(child[i], visitedDomains, result);
        }
    } // getAllMembers

    /** 
     * calculates the  distances from a given start  domain to all its
     *  direct  and indirect  child  domains.   This  function uses  a
     * breatdh-first-search to walk down the domain graph.
     *
     *  @param startnode the domain from where the breatdh-search-starts
     *  @return hashtable containing the result. The hashtable maps from domains to their
     *              distance to the startnode */

    static Hashtable calculateDistances(Domain startnode)
    {
        Hashtable result= new Hashtable(); // domain -> distance to start node

        result.put(startnode, new Integer(0) ); // d(startnode) =0
        DomainQueue q= new DomainQueueImpl();
        q.enqueue(startnode);

        Domain domain;
        Domain list[];
        int i, distance;
        while ( !q.isEmpty() )
        {
            domain= q.dequeue();
            list= domain.getChilds();
            for (i= 0; i < list.length; i++)
            {
                if (! result.containsKey(list[i]) )
                {
                    distance= ((Integer) result.get(domain)).intValue();
                    result.put(list[i], new Integer(distance + 1) );
                    q.enqueue(list[i]);
                }
            }
        }

        return result;

    } // calculateDistances

    /* implementation of the interface org.jacorb.poa.POAListener */

    public void poaCreated(POA poa) {}
    public void poaStateChanged(POA poa, int new_state){}

    public void referenceCreated(org.omg.CORBA.Object object)
    {
        org.jacorb.util.Debug.output(2,
                                 " DomainImpl.referenceCreated: POA created new object reference"
                                 + object);
    }

} // DomainImpl






















