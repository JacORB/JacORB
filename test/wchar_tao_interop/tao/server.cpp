#include "Echo_i.h"
#include <ace/streams.h>

int
main( int argc, char *argv[] )
{
    try {
        // Initialize orb
        CORBA::ORB_var orb = CORBA::ORB_init( argc, argv );

        //Get reference to Root POA
        CORBA::Object_var obj = orb->resolve_initial_references( "RootPOA" );
        PortableServer::POA_var poa = PortableServer::POA::_narrow( obj.in() );
          
        PortableServer::POAManager_var mgr = poa->the_POAManager();

        // Activate POA Manager
        mgr->activate();

        // Create an object
        GoodDay_i servant;

        // Register the servant with the RootPOA, obtain its object
        // reference, stringify it, and write it to a file.
        obj = poa->servant_to_reference( &servant );

        CORBA::String_var str = orb->object_to_string( obj.in() );
        ofstream iorFile( "IOR" );
        iorFile << str.in() << endl;
        iorFile.close();

        cout << "IOR written to file IOR" << endl;   

        // Accept requests
        orb->run();
        orb->destroy();
    }

    catch( const CORBA::Exception &ex ) {
        cerr << "Uncaught CORBA exception: " << ex <<endl;
        return 1;
    }

    return 0;
}
