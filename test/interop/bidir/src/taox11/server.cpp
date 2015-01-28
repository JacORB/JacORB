#include "BiDirTest_i.h"
#include "tao/x11/bidir_giop/bidir_giop.h"
#include "tao/x11/anytypecode/any.h"
#include "tao/x11/portable_server/portableserver_functions.h"
#include <fstream>

int
main( int argc, char *argv[] )
{
  try {
    // Initialize orb
      IDL::traits<CORBA::ORB>::ref_type orb =
        CORBA::ORB_init (argc, argv);

    // Get reference to Root POA
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
        root_poa->create_implicit_activation_policy( PortableServer::ImplicitActivationPolicyValue::IMPLICIT_ACTIVATION );

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

    // Create an object
    CORBA::servant_reference<CallbackServer_i> server_impl =
      CORBA::make_reference<CallbackServer_i> ();

    // Register the servant with the child POA, obtain its object
    // reference, stringify it, and write it to a file.
    PortableServer::ObjectId id =
      PortableServer::string_to_ObjectId ("simple_server");

    child_poa->activate_object_with_id (id, server_impl);

    IDL::traits<CORBA::Object>::ref_type obj =
      child_poa->id_to_reference (id);

    std::string str = orb->object_to_string(obj);
    std::ofstream iorFile( "IOR" );
    iorFile << str << std::endl;
    iorFile.close();

    std::cout << "IOR written to file IOR" << std::endl;

    // Accept requests
    orb->run();
    orb->destroy();
  }
  catch( const CORBA::Exception &ex ) {
    std::cerr << "Uncaught CORBA exception: " << ex << std::endl;
    return 1;
  }

  return 0;
}
