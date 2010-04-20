/***** Copyright (c) 1999-2000 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

/***** This class is specifed by the mapping as abstract.
       A "dummy" implementation is provided so that the "official" org.omg.*
       packages may be compiled.

       ORB-vendors shall provide a complete implementation of the class
       by extending it with a vendor-specific class which
       provides "real" implementations for all the methods. E.g.

       package com.acme_orb_vendor.CORBA;
       public class ORB extends org.omg.CORBA { ... }

       In order to be conformant the class shall support the signatures
       specified here, but will have an orb-specific implementation.

       Implementations of the static init methods in this class are also required.

       The class may support additional vendor specific functionality.
*****/

package org.omg.CORBA;

abstract public class ORB {
   public String id()
   {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   abstract public org.omg.CORBA.Object string_to_object(String str);

   abstract public String object_to_string(org.omg.CORBA.Object obj);

   // Dynamic Invocation related operations

   abstract public NVList create_list(int count);

   /**
    * @deprecated by CORBA 2.3
    */
   public NVList create_operation_list(org.omg.CORBA.Object oper) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   abstract public NamedValue create_named_value(String s, Any any, int flags);

   abstract public ExceptionList create_exception_list();

   abstract public ContextList create_context_list();

   abstract public Context get_default_context();

   abstract public Environment create_environment();

   abstract public void send_multiple_requests_oneway(Request[] req);

   abstract public void send_multiple_requests_deferred(Request[] req);

   abstract public boolean poll_next_response();

   abstract public Request get_next_response() throws WrongTransaction;

   public boolean get_service_information(short service_type,
                                          ServiceInformationHolder service_info) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   abstract public String[] list_initial_services();

   public void register_initial_reference(
      String object_name,
      org.omg.CORBA.Object object
                                         ) throws org.omg.CORBA.ORBPackage.InvalidName
   {
      throw new org.omg.CORBA.NO_IMPLEMENT() ;
   }

   // Initial reference operation

   abstract public org.omg.CORBA.Object resolve_initial_references(
      String object_name)
      throws org.omg.CORBA.ORBPackage.InvalidName;

   // typecode creation

   abstract public TypeCode create_struct_tc(String id, String name,
                                             StructMember[] members);

   abstract public TypeCode create_union_tc(String id, String name,
                                            TypeCode discriminator_type,
                                            UnionMember[] members);

   abstract public TypeCode create_enum_tc(String id, String name,
                                           String[] members);

   abstract public TypeCode create_alias_tc(String id, String name,
                                            TypeCode original_type);

   abstract public TypeCode create_exception_tc(String id, String name,
                                                StructMember[] members);

   abstract public TypeCode create_interface_tc(String id, String name);

   abstract public TypeCode create_string_tc(int bound);

   abstract public TypeCode create_wstring_tc(int bound);

   public org.omg.CORBA.TypeCode create_fixed_tc(short digits, short scale) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   abstract public TypeCode create_sequence_tc(int bound,
                                               TypeCode element_type);

   /**
    * @deprecated by CORBA 2.3
    */
   abstract public TypeCode create_recursive_sequence_tc(int bound, int offset);

   abstract public TypeCode create_array_tc(int length, TypeCode element_type);

   public org.omg.CORBA.TypeCode create_value_tc(String id,
                                                 String name,
                                                 short type_modifier,
                                                 TypeCode concrete_base,
                                                 ValueMember[] members) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public org.omg.CORBA.TypeCode create_value_box_tc(String id,
                                                     String name,
                                                     TypeCode boxed_type) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public org.omg.CORBA.TypeCode create_native_tc(String id,
                                                  String name) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public org.omg.CORBA.TypeCode create_recursive_tc(String id) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public org.omg.CORBA.TypeCode create_abstract_interface_tc(
      String id,
      String name) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }


   public org.omg.CORBA.TypeCode create_local_interface_tc(
      String id,
      String name) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   /**
    * @deprecated Deprecated by CORBA 2.3
    */
   public org.omg.CORBA.Current get_current() {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   /**
    * @deprecated Deprecated by Portable Object Adapter.
    * see OMG document orbos/98-01-06 for details.
    */
   public void connect(org.omg.CORBA.Object obj) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   /**
    * @deprecated Deprecated by Portable Object Adapter.
    * see OMG document orbos/98-01-06 for details.
    */
   public void disconnect(org.omg.CORBA.Object obj) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   // Thread related operations

   public boolean work_pending() {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public void perform_work() {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public void run() {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public void shutdown(boolean wait_for_completion) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   // policy related operations

   public org.omg.CORBA.Policy create_policy(int type, org.omg.CORBA.Any val)
      throws org.omg.CORBA.PolicyError {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   // Additional methods for IDL/Java mapping

   abstract public TypeCode get_primitive_tc(TCKind tcKind);

   abstract public Any create_any();

   abstract public org.omg.CORBA.portable.OutputStream create_output_stream();

   // Additional static methods for ORB initialization

   public static ORB init(String[] args, java.util.Properties props) {
      /* VENDOR MUST SUPPLY IMPLEMENTATION */
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public static ORB init(java.applet.Applet app, java.util.Properties props) {
      /* VENDOR MUST SUPPLY IMPLEMENTATION */
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public static ORB init() {
      /* VENDOR MUST SUPPLY IMPLEMENTATION */
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   abstract protected void set_parameters(String[] args,
                                          java.util.Properties props);

   abstract protected void set_parameters(java.applet.Applet app,
                                          java.util.Properties props);

   // always return a ValueDef or throw BAD_PARAM if not repid of a value
   public org.omg.CORBA.Object get_value_def(String repid) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public org.omg.CORBA.portable.ValueFactory register_value_factory(
      String id, org.omg.CORBA.portable.ValueFactory factory) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public void unregister_value_factory(String id) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public org.omg.CORBA.portable.ValueFactory lookup_value_factory(String id){
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public void set_delegate(java.lang.Object wrapper) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public String exception_to_string( SystemException exc ) {
      throw new org.omg.CORBA.NO_IMPLEMENT();
   }
}
