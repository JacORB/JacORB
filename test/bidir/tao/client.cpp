#include "BiDirTest_i.h"
#include "BiDirTestC.h"
#include <ace/streams.h>
#include <tao/BiDir_GIOP/BiDirGIOP.h>

int
main( int argc, char *argv[] )
{
    try {
        // Initialize orb
        CORBA::ORB_var orb = CORBA::ORB_init( argc, argv );

        // Destringify ior
        CORBA::Object_var obj = orb->string_to_object( "file://IOR" );
        if( CORBA::is_nil( obj.in() ) ) {
            cerr << "Nil  reference" << endl;
            throw 0;
        }

        // Narrow
        CallbackServer_var cb_server = CallbackServer::_narrow( obj.in() );
        if( CORBA::is_nil( cb_server.in() ) ) {
            cerr << "Argument is not a CallbackServer reference" << endl;
            throw 0;
        }

        //Get reference to Root POA
        obj = orb->resolve_initial_references( "RootPOA" );
        PortableServer::POA_var poa = PortableServer::POA::_narrow( obj.in() );

        // Policies for the childPOA to be created.
        CORBA::PolicyList policies (4);
        policies.length (4);

        CORBA::Any pol;
        pol <<= BiDirPolicy::BOTH;
        policies[0] =
            orb->create_policy (BiDirPolicy::BIDIRECTIONAL_POLICY_TYPE,
                                pol);

        policies[1] = 
            poa->create_id_assignment_policy(PortableServer::SYSTEM_ID);

        policies[2] = 
            poa->create_implicit_activation_policy( PortableServer::IMPLICIT_ACTIVATION );

        policies[3] = 
            poa->create_lifespan_policy(PortableServer::TRANSIENT);
          
        PortableServer::POAManager_var mgr = poa->the_POAManager();

        // Create POA as child of RootPOA with the above policies.  This POA
        // will receive request in the same connection in which it sent
        // the request
        PortableServer::POA_var child_poa =
            poa->create_POA ("childPOA",
                             mgr.in(),
                             policies);

        // Creation of childPOA is over. Destroy the Policy objects.
        for (CORBA::ULong i = 0;
             i < policies.length ();
             ++i)
        {
            policies[i]->destroy ();
        }

        // Activate POA Manager
        mgr->activate();

        // Create an object
        ClientCallback_i servant;

        // Register the servant with the RootPOA, obtain its object
        // reference, stringify it, and write it to a file.
        obj = child_poa->servant_to_reference( &servant );
        //ClientCallback_var ccb = ClientCallback::_narrow( obj.in() );

        cb_server->callback_hello( ClientCallback::_narrow( obj.in() ),
                                   CORBA::string_dup( "Greetings earthling" ));
    }
    catch( const CORBA::Exception &ex ) {
        cerr << "Uncaught CORBA exception: " << ex << endl;
        return 1;
    }
}








