/***** Copyright (c) 1999 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

package org.omg.PortableServer;

public interface POAOperations {

    public org.omg.PortableServer.POA create_POA(java.lang.String adapter_name,
                                org.omg.PortableServer.POAManager a_POAManager,
                                org.omg.CORBA.Policy[] policies) throws
                                    org.omg.PortableServer.POAPackage.AdapterAlreadyExists,
                                    org.omg.PortableServer.POAPackage.InvalidPolicy;

    public org.omg.PortableServer.POA find_POA(java.lang.String adapter_name,
                                boolean activate_it) throws
                                    org.omg.PortableServer.POAPackage.AdapterNonExistent;

    void destroy(boolean etherealize_objects, boolean wait_for_completion);

    public org.omg.PortableServer.ThreadPolicy create_thread_policy(
                                org.omg.PortableServer.ThreadPolicyValue value);

    public org.omg.PortableServer.LifespanPolicy create_lifespan_policy(
                                org.omg.PortableServer.LifespanPolicyValue value);

    public org.omg.PortableServer.IdUniquenessPolicy create_id_uniqueness_policy(
                                org.omg.PortableServer.IdUniquenessPolicyValue value);

    public org.omg.PortableServer.IdAssignmentPolicy create_id_assignment_policy(
                                org.omg.PortableServer.IdAssignmentPolicyValue value);

    public org.omg.PortableServer.ImplicitActivationPolicy 
                                create_implicit_activation_policy(
                                org.omg.PortableServer.ImplicitActivationPolicyValue value);

    public org.omg.PortableServer.ServantRetentionPolicy create_servant_retention_policy(
                                org.omg.PortableServer.ServantRetentionPolicyValue value);

    public org.omg.PortableServer.RequestProcessingPolicy create_request_processing_policy(
                                org.omg.PortableServer.RequestProcessingPolicyValue value);

    public java.lang.String the_name();
    public org.omg.PortableServer.POA the_parent();
    public org.omg.PortableServer.POA[] the_children();
    public org.omg.PortableServer.POAManager the_POAManager();
    public org.omg.PortableServer.AdapterActivator the_activator();
    public void the_activator(org.omg.PortableServer.AdapterActivator the_activator);

    public org.omg.PortableServer.ServantManager get_servant_manager() throws
                                    org.omg.PortableServer.POAPackage.WrongPolicy;
    public void set_servant_manager(org.omg.PortableServer.ServantManager imgr) throws
                                    org.omg.PortableServer.POAPackage.WrongPolicy;

    public org.omg.PortableServer.Servant get_servant() throws
                                    org.omg.PortableServer.POAPackage.NoServant,
                                    org.omg.PortableServer.POAPackage.WrongPolicy;
    public void set_servant(org.omg.PortableServer.Servant p_servant) throws
                                    org.omg.PortableServer.POAPackage.WrongPolicy;

    public byte[] activate_object(org.omg.PortableServer.Servant p_servant) throws
                                    org.omg.PortableServer.POAPackage.ServantAlreadyActive,
                                    org.omg.PortableServer.POAPackage.WrongPolicy;
    public void activate_object_with_id(byte[] id,
                                org.omg.PortableServer.Servant p_servant) throws
                                    org.omg.PortableServer.POAPackage.ServantAlreadyActive,
                                    org.omg.PortableServer.POAPackage.ObjectAlreadyActive,
                                    org.omg.PortableServer.POAPackage.WrongPolicy;
    public void deactivate_object(byte[] oid) throws
                                    org.omg.PortableServer.POAPackage.ObjectNotActive,
                                    org.omg.PortableServer.POAPackage.WrongPolicy;

    public org.omg.CORBA.Object create_reference(java.lang.String intf) throws
                                    org.omg.PortableServer.POAPackage.WrongPolicy;
    public org.omg.CORBA.Object create_reference_with_id(byte[] oid,
                                java.lang.String intf) throws
                                    org.omg.PortableServer.POAPackage.WrongPolicy;

    public byte[] servant_to_id(org.omg.PortableServer.Servant p_servant) throws
                                    org.omg.PortableServer.POAPackage.ServantNotActive,
                                    org.omg.PortableServer.POAPackage.WrongPolicy;
    public org.omg.CORBA.Object servant_to_reference(
                                org.omg.PortableServer.Servant p_servant) throws
                                    org.omg.PortableServer.POAPackage.ServantNotActive,
                                    org.omg.PortableServer.POAPackage.WrongPolicy;
    public org.omg.PortableServer.Servant reference_to_servant(
                                org.omg.CORBA.Object reference) throws
                                    org.omg.PortableServer.POAPackage.ObjectNotActive,
                                    org.omg.PortableServer.POAPackage.WrongPolicy;
    public byte[] reference_to_id(org.omg.CORBA.Object reference) throws
                                    org.omg.PortableServer.POAPackage.WrongAdapter,
                                    org.omg.PortableServer.POAPackage.WrongPolicy;
    public org.omg.PortableServer.Servant id_to_servant(byte[] oid) throws
                                    org.omg.PortableServer.POAPackage.ObjectNotActive,
                                    org.omg.PortableServer.POAPackage.WrongPolicy;
    public org.omg.CORBA.Object id_to_reference(byte[] oid) throws
                                    org.omg.PortableServer.POAPackage.ObjectNotActive,
                                    org.omg.PortableServer.POAPackage.WrongPolicy;
}
