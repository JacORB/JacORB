package org.jacorb.orb.domain;

import java.util.Hashtable;

/**
 * An  instance   of   this  class   implements  the   IDL-interace
 * InitialMapPolicy. The OnReference-Creation operation of this class
 * maps a  newly created object reference to its  type domain.  A type
 * domain contains all object references of a determined idl type. New
 * object references are  therefore classified  by their  type.  <br>
 * Before using an instance of this class, its tie must be set via the
 *  "setTie"  operation.  This is  due  both  to  the poa  tie  object
 * implementation approach  of this class and to  the need of creating
 * object references (domains). If you  don't do this, you will get an
 * "org.omg.CORBA.BAD_INV_ORDER:  The Servant has  not been associated
 * with an ORBinstance" exception.
 *
 * Created: Sat Apr 22 14:10:24 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$ */

public class MapToTypeDomainsPolicy 
    extends ManagementPolicyImpl
    implements InitialMapPolicyOperations 
{    
  
    /** type id -> domain */
    private Hashtable _typeDomains;

    /** factory to create new type domains */
    private DomainFactory _factory;

    /** the associated tie of an instance of this class */
    private InitialMapPolicyPOATie _tie;

    /** 
     *   used to indicate that OnReferenceCreation itself is creating a
     *   new object and there calls itself recursively 
     */
    boolean callMyself= false;
  
    public MapToTypeDomainsPolicy() 
    {
        super("initial map");
        String desc= 
            "This object implements the domain initial map policy. The initial map policy"
            +" is used to map a newly created object reference to one or more domains. The strategy "
            +"of this object is to map a new object reference to its type domain. The type domain of"
            +" an object \"obj\" is the domain containing all objects which have the same most "
            +"specific type as \"obj\".";
        long_description(desc);

        // create hashtable
        _typeDomains= new Hashtable();
   
    }
  
    /** 
     * maps a newly created object reference to its type domain. 
     */
    public Domain[] OnReferenceCreation( org.omg.CORBA.Object newReference, 
                                        Domain rootDomain)
    {
        String type = Util.toID( newReference.toString() );

        //   try { getTie()._poa(); } catch (Exception e) { 
        //        org.jacorb.util.Debug.output(2, " call of _poa failed"); 
        //        org.jacorb.util.Debug.output(2, e);
        //      }
  
        Domain domain = (Domain) _typeDomains.get(type);
        if (domain == null) 
        { // new type, create domain for it

            // return empty list to avoid recursive hangup on second call
            if (callMyself) 
                return new Domain[0]; 

            callMyself= true; // indicate object creation which is not mapped to any domain
            domain= _domainFactory().createDomain(null, null, type);
            callMyself= false;
	
            _typeDomains.put(type, domain);

            try 
            { // insert new domain as child domain into root domain
                rootDomain.insertChild(domain);
            }
            catch (org.jacorb.orb.domain.GraphNodePackage.ClosesCycle cc)
            {
                org.jacorb.util.Debug.output(3, 
                                             "MapToTypeDomainsPolicy.OnReferenceCreation: cannot "
                                             + "insert "+Util.downcast(domain) + 
                                             " as child domain to "
                                             + "root domain " + Util.downcast(rootDomain));
                org.jacorb.util.Debug.output( 1, cc);
            }
            catch (org.jacorb.orb.domain.NameAlreadyDefined already)
            {
                org.jacorb.util.Debug.output(3, "MapToTypeDomainsPolicy.OnReferenceCreation: cannot "
                                             +"insert " + Util.downcast(domain) + 
                                             " as child domain to "
                                             + "root domain " +Util.downcast(rootDomain));
                org.jacorb.util.Debug.output(1, already);
            }
        }
        // domain != null
        Domain result[]= new Domain[1];
        result[0]= domain;
        return result;

    } // OnReferenceCreation
  

    /** 
     * returns a domain factory. 
     */

    private DomainFactory _domainFactory()
    {
        if (_factory == null)
        { // create domain factory
            try 
            {
                DomainFactoryImpl factoryImpl = new DomainFactoryImpl();

                callMyself= true;
                _factory = 
                    DomainFactoryHelper.narrow(
                         getTie()._poa().servant_to_reference(factoryImpl));
                callMyself= false;
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
        // factory != null
        return _factory;

    } // _domainFactory

    // tie functions

    /** 
     * sets  the tie of this  object. Because this class  uses the poa
     * tie approach to implement CORBA objects and this class needs to
     * access  the poa of its tie  to create new objects,  it needs to
     * know its tie. This method sets the tie.  It must be called from
     * outside after creating the object.
     * @param tie the servant which is used as tie to connect this 
     *        class to a poa 
     */

    public void setTie(InitialMapPolicyPOATie tie) { _tie= tie; }

    /** 
     * gets  the associated tie  of this object.  The tie must  be set
     * prior to this call.
     * @see org.jacorb.orb.domain.MapToTypeDomainsPolicy#setTie 
     */
    public InitialMapPolicyPOATie getTie() 
    { 
        org.jacorb.util.Debug.assert(1, 
                                 _tie != null,
                                 "MapToTypeDomainsPolicy.getTie: tie is not set !");
        return _tie; 
    }

    // inherited member functions

    public short strategy()
    { 
        return InitialMapPolicy.TYPE_DOMAINS; 
    } 

    public int policy_type()
    { 
        return INITIAL_MAP_POLICY_ID.value; 
    }

    public org.omg.CORBA.Policy copy() 
    { 
        return getTie()._this();
    }

} // MapToTypeDomainsPolicy



