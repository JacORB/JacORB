#include "Echo_i.h"
#include "EchoC.h"
#include <ace/streams.h>

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
        GoodDay_var server = GoodDay::_narrow( obj.in() );
        if( CORBA::is_nil( server.in() ) ) {
            cerr << "Argument is not a GoodDay reference" << endl;
            throw 0;
        }
    }
    catch( const CORBA::Exception &ex ) {
        cerr << "Uncaught CORBA exception: " << ex << endl;
        return 1;
    }
}








