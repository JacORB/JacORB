package org.jacorb.orb.domain;

import org.omg.CORBA.TCKind;

/**
 * The implementation of the IDL-interface PolicyFactory.
 *
 * Created: Mon Mar 27 16:32:08 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class PolicyFactoryImpl 
    extends org.jacorb.orb.domain.PolicyFactoryPOA 
{
  
    public PolicyFactoryImpl() {}
  
    /** 
     * creates  a policy.   The type  of the policy  to be  created is
     * specified  by the parameter "type".   The parameter "initValue"
     * of type Any contains the initail state of the policy.
     * @param type the wanted policy type 
     * @param initValue a any containing potential initial value for 
     *        the policy to be created
     * @exception org.omg.CORBA.PolicyError if the parameter "type" 
     *        is invalid or the any "initValue" contains invalid inital 
     *        date 
     */
    public org.omg.CORBA.Policy create_policy(int type, 
                                              org.omg.CORBA.Any initValue)
        throws org.omg.CORBA.PolicyError
    {
        switch (type)
        {

        case CONFLICT_RESOLUTION_POLICY_ID.value:
            if ( initValue.type().kind().value() ==  TCKind._tk_short )
            {
                short choice= initValue.extract_short();
                return createConflictResolutionPolicy(choice);
            }
            else 
                throw new org.omg.CORBA.PolicyError(org.omg.CORBA.BAD_POLICY_TYPE.value);

      
        case  INITIAL_MAP_POLICY_ID.value:
            if ( initValue.type().kind().value() ==  TCKind._tk_short )
            {
                short choice= initValue.extract_short();
                return createInitialMapPolicy( choice );
            }
            else 
                throw new org.omg.CORBA.PolicyError(org.omg.CORBA.BAD_POLICY_TYPE.value);

        case  PROPERTY_POLICY_ID.value:
	    return createPropertyPolicy();

        case  META_PROPERTY_POLICY_ID.value:
	    return createMetaPropertyPolicy();
	
        default:
            throw new org.omg.CORBA.PolicyError(org.omg.CORBA.BAD_POLICY.value);
	
        }
    }

    /** 
     *  creates  an  initial  map policy.   The  parameter  "whichOne"
     * identifes the strategy/subtype of the policy to be created
     *  @param whichOne the subtype of the initial map policy. May 
     *         currently be one of
     *         <UL>
     *                <LI> InitialMapPolicy.DEFAULT_DOMAIN        </LI>
     *		      <LI> InitialMapPolicy.TYPE_DOMAINS          </LI>
     *		      <LI> InitialMapPolicy.POA_DOMAINS           </LI>
     *	       </UL>
     *  @exception org.omg.CORBA.PolicyError if the suptype "whichOne" 
     *              is unknown 
    */
    public org.jacorb.orb.domain.InitialMapPolicy createInitialMapPolicy(short whichOne) 
    {
        try 
        {
            switch (whichOne)
            {
            case InitialMapPolicy.DEFAULT_DOMAIN:
                return InitialMapPolicyHelper.narrow
                    (_poa().servant_to_reference(
                             new InitialMapToDefaultDomainPolicyPOATie
                        // (_poa().servant_to_reference( new InitialMapPolicyPOATie
                        ( new MapToDefaultDomainPolicy() ) ));


            case InitialMapPolicy.TYPE_DOMAINS:
                MapToTypeDomainsPolicy pol =  new MapToTypeDomainsPolicy();
                InitialMapPolicyPOATie tie = new InitialMapPolicyPOATie(pol);
                // make tie known to pol
                pol.setTie(tie);
                return InitialMapPolicyHelper.narrow( _poa().servant_to_reference(tie) );

                //   case InitialMapPolicy.POA_DOMAINS : 
                //  	    org.jacorb.util.Debug.output(1, " InitialMapPolicy.POA_DOMAINS not yet implemented");

                //  	    break;

	
            default: 
                throw new org.omg.CORBA.PolicyError(org.omg.CORBA.BAD_POLICY_VALUE.value);
            }
        }
        catch (org.omg.PortableServer.POAPackage.WrongPolicy wp) 
        {
            org.jacorb.util.Debug.output(1, "the poa of this domain(" + this 
                                     +") has the wrong policies for \"servant_to_reference\".");
        }
        catch (Exception e)
        {
            org.jacorb.util.Debug.output(1, e);
            return null;
        }
        // should never be called
        org.jacorb.util.Debug.assert(1, false, 
                                 "PolicyFactoryImpl.createInitialMapPolicy: impossible "
                                 +" fallthrough happened !!!");
        return null;
    } // createInitialMapPolicy


    /** 
     *    creates  a  conflict   resolution  policy.    The  parameter
     *  "whichOne" identifes the  strategy/subtype of the policy to be
     *  created
     *  @param whichOne the subtype of the conflict solution policy.
     *         May currently be one of
     *                <UL>
     *                <LI> ConflictResolutionPolicy.FIRST        </LI>
     *		      <LI> ConflictResolutionPolicy.PARENT_RULES  </LI>
     *		      <LI> ConflictResolutionPolicy.CHILD_RULES   </LI>
     *		      </UL>
     *  @exception org.omg.CORBA.PolicyError if the suptype "whichOne" 
     * is unknown 
     */

    public org.jacorb.orb.domain.ConflictResolutionPolicy createConflictResolutionPolicy(short whichOne)
    {
        try 
        {
            switch (whichOne)
            {
            case ConflictResolutionPolicy.FIRST: 
                return ConflictResolutionPolicyHelper.narrow
                    (_poa().servant_to_reference( new ConflictResolutionPolicyPOATie
                        ( new FirstConflictResolutionPolicy() ) ));

            case ConflictResolutionPolicy.PARENT_RULES: 
                ConflictResolutionPolicy pol=
                    ConflictResolutionPolicyHelper.narrow
                    (_poa().servant_to_reference( new ConflictResolutionPolicyPOATie
                        ( new ParentRulesPolicy() ) ));
                org.jacorb.util.Debug.assert(1, pol != null, "pol is null");
                return pol;

            case ConflictResolutionPolicy.CHILD_RULES: 

                return ConflictResolutionPolicyHelper.narrow
                    (_poa().servant_to_reference( new ConflictResolutionPolicyPOATie
                        ( new ChildRulesPolicy() ) ));

	
            default: throw new org.omg.CORBA.PolicyError(org.omg.CORBA.BAD_POLICY_VALUE.value);
            }
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
    
        // never be called
        return null;
    } // createConflictResolutionPolicy

    /**
     * returns a property policy. The property policy has no 
     * properties defined. 
     */

    public PropertyPolicy createPropertyPolicy()
    {
        try
        {
            return PropertyPolicyHelper.narrow
                (_poa().servant_to_reference( new PropertyPolicyPOATie
                    ( new PropertyPolicyImpl() ) ));
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

        return null;

    } // createPropertyPolicy


    public org.jacorb.orb.domain.MetaPropertyPolicy createMetaPropertyPolicy()
    {
        try
        {
            return MetaPropertyPolicyHelper.narrow
                (_poa().servant_to_reference( new MetaPropertyPolicyPOATie
                    ( new MetaPropertyPolicyImpl() ) ));
        }
        catch (org.omg.PortableServer.POAPackage.WrongPolicy wp) 
        {
            org.jacorb.util.Debug.output(1, 
                                     "the poa of this domain(" + 
                                     this  + 
                                     ") has the wrong policies for \"servant_to_reference\".");
        }
        catch (Exception e)
        {
            org.jacorb.util.Debug.output(1, e);
        }

        return null;
    }

} // PolicyFactoryImpl






