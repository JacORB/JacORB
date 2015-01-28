#include "BiDirTest_i.h"
#include "BiDirTestC.h"
#include "tao/x11/bidir_giop/bidir_giop.h"
#include "tao/x11/anytypecode/any.h"
#include "tao/x11/portable_server/portableserver_functions.h"

int
main( int argc, char *argv[] )
{
  try {
    IDL::traits<CORBA::ORB>::ref_type orb =
      CORBA::ORB_init (argc, argv);

    // Destringify ior
    IDL::traits<CORBA::Object>::ref_type obj = orb->string_to_object( "file://IOR" );
    if(!obj)
    {
      std::cerr << "Nil  reference" << std::endl;
      return 1;
    }

    // Narrow
    IDL::traits<test::interop::bidir::CallbackServer>::ref_type cb_server =
      IDL::traits<test::interop::bidir::CallbackServer>::narrow(obj);
    if(!cb_server)
    {
      std::cerr << "Argument is not a CallbackServer reference" << std::endl;
      return 1;
    }

    //Get reference to Root POA
    IDL::traits<CORBA::Object>::ref_type poa_object =
      orb->resolve_initial_references ("RootPOA");

    IDL::traits<PortableServer::POA>::ref_type root_poa =
      IDL::traits<PortableServer::POA>::narrow (poa_object);

    // Policies for the childPOA to be created.
    CORBA::PolicyList policies (4);

    CORBA::Any pol;
    pol <<= BiDirPolicy::BOTH;
    policies[0] =
      orb->create_policy (BiDirPolicy::BIDIRECTIONAL_POLICY_TYPE, pol);

    policies[1] =
      root_poa->create_id_assignment_policy(PortableServer::IdAssignmentPolicyValue::SYSTEM_ID);

    policies[2] =
      root_poa->create_implicit_activation_policy(PortableServer::ImplicitActivationPolicyValue::IMPLICIT_ACTIVATION);

    policies[3] =
      root_poa->create_lifespan_policy(PortableServer::LifespanPolicyValue::TRANSIENT);

    IDL::traits<PortableServer::POAManager>::ref_type poa_manager =
      root_poa->the_POAManager ();

    // Create POA as child of RootPOA with the above policies.  This POA
    // will receive request in the same connection in which it sent
    // the request
    IDL::traits<PortableServer::POA>::ref_type child_poa =
      root_poa->create_POA ("childPOA", poa_manager, policies);

    // Creation of childPOA is over. Destroy the Policy objects.
    for (IDL::traits<CORBA::Policy>::ref_type _pol : policies)
    {
      _pol->destroy ();
    }

    // Activate POA Manager
    poa_manager->activate();

    // Create a servant
    CORBA::servant_traits<ClientCallback_i>::ref_type callback_impl =
      CORBA::make_reference<ClientCallback_i> ();

    // Register the servant with the child POA, obtain its object
    // reference, stringify it, and write it to a file.
    PortableServer::ObjectId id =
      PortableServer::string_to_ObjectId ("callback");

    child_poa->activate_object_with_id (id, callback_impl);

    obj = child_poa->id_to_reference (id);

    IDL::traits<test::interop::bidir::ClientCallback>::ref_type ccb =
      IDL::traits<test::interop::bidir::ClientCallback>::narrow(obj);

    cb_server->callback_hello(ccb, "Greetings earthling");
  }
  catch(const CORBA::Exception &ex ) {
    std::cerr << "Uncaught CORBA exception: " << ex << std::endl;
    return 1;
  }

  return 0;
}








