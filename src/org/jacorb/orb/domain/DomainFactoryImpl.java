package org.jacorb.orb.domain;

import org.jacorb.util.Debug;
/**
 * The implemenation of the IDL-interface DomainFactory. 
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class DomainFactoryImpl 
    extends DomainFactoryPOA
{
    /** cached copy of the orb domain reference 
        provided by orb.resolve_initial_refs */
    private ORBDomain theORBDomain= null;

    public DomainFactoryImpl() 
    {}

    /** 
     *  creates a domain  object. 
     *  Post: result is a domain  AND result.isRoot() 
     */

    public Domain createDomain( org.omg.CORBA.Object[] initialMembers,
                                org.omg.CORBA.Policy[] initialPolicies,
                                String                 name)
    {
        DomainImpl domain = new DomainImpl(initialMembers,
                                          initialPolicies,
                                          name);

        Domain result= null;
        try 
        {
            result = DomainHelper.narrow(_poa().servant_to_reference(domain));
            getORBDomain().insertLocalDomain(result);
        } 
        catch (org.omg.PortableServer.POAPackage.WrongPolicy wp) 
	{
            org.jacorb.util.Debug.output(1, "the poa of this domain(" + this 
                                     +") has the wrong policies for \"servant_to_reference\".");
	}
        catch ( Exception e )
        {
            org.jacorb.util.Debug.output(1, e);
        } 
      
        org.jacorb.util.Debug.output(4, "Domain.createDomain: finished.");

        org.jacorb.util.Debug.assert(2, result != null, 
                                 "DomainFactoryImpl.createDomain: result is null");
        org.jacorb.util.Debug.assert(2, result.isRoot(),
                                 "DomainFactoryImpl.createDomain: result is"
                                 +"not a root domain");

        return result;
    } // createDomain

    public Domain createEmptyDomain()
    {
        DomainImpl impl= new DomainImpl();
     
        Domain result= null;
        try 
        {
            result = DomainHelper.narrow(_poa().servant_to_reference(impl));
            getORBDomain().insertLocalDomain(result);
        } 
        catch (org.omg.PortableServer.POAPackage.WrongPolicy wp) 
	{
            org.jacorb.util.Debug.output(1, 
                                     "DomainFactory.createEmptyDomain:the poa of this domain("
                                     + this 
                                     +") has the wrong policies for \"servant_to_reference\".");
	}
        catch (Exception e)
        {
            org.jacorb.util.Debug.output(1, e);
        } 

        return result;
      
    } // createEmptyDomain


    /** 
     * clears the contenst of a domain. Also removes from every parent domain.
     *  Pre: aDomain.getChildCount() == 0
     * Post: aDomain.getPolicyCount() == 0, 
     *       aDomain.getMemberCount() == 0, 
     *       aDomain.getParentCount() == 0
     */
    public void clear(jacorb.orb.domain.Domain aDomain)
    {
        Debug.assert(1, aDomain.getChildCount() == 0, 
                     "DomainFactory.clear: "
                     +"cannot clear a domain with child domains, remove child domains first.");

        Domain parent[]              = aDomain.getParents();
        org.omg.CORBA.Policy policy[]= aDomain.getPolicies();
        org.omg.CORBA.Object member[]= aDomain.getParents();

        for (int i= 0; i < parent.length; i++) 
            aDomain.deleteParent( parent[i] );

        for (int i= 0; i < member.length; i++) 
            aDomain.deleteMember( member[i] );

        for (int i= 0; i < policy.length; i++) 
            aDomain.deletePolicyOfType( policy[i].policy_type() );
    
    } // clear

    public void destroy(jacorb.orb.domain.Domain aDomain)
    {
        if (aDomain.getChildCount() > 0 || aDomain.getParentCount() > 0)
            throw new org.omg.CORBA.BAD_CONTEXT();
        clear(aDomain);
        getORBDomain().removeLocalDomain(aDomain);
    } // destroy
  
    /** 
     * gets the orb domain. 
     */

    private final org.jacorb.orb.domain.ORBDomain getORBDomain()
    {
        if (theORBDomain == null)
        {
            try
            {
                return (jacorb.orb.domain.ORBDomain) _orb().resolve_initial_references("LocalDomainService");
            }
            catch (org.omg.CORBA.ORBPackage.InvalidName inv) 
            { 
                Debug.output(Debug.DOMAIN | Debug.IMPORTANT, inv);
            }
        } // if

        return theORBDomain;
    } // getORBDomain


} // DomainFactoryImpl










