package test.interceptor.client_flow;

import org.jacorb.orb.portableInterceptor.*;
import org.omg.PortableInterceptor.*;
import java.io.*;
/**
 * ClientInitializer.java
 *
 *
 * Created: Fri Oct 26 10:58:29 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ClientInitializer 
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements ORBInitializer  
{
    public ClientInitializer()
    {        
    }

    // implementation of org.omg.PortableInterceptor.ORBInitializerOperations interface

    /**
     *
     * @param param1 <description>
     */
    public void pre_init(ORBInitInfo info) 
    {
    }

    /**
     *
     * @param param1 <description>
     */
    public void post_init(ORBInitInfo info) 
    {
        try
        {
            info.add_client_request_interceptor( new ClientInterceptorA() );

            BufferedReader br =
                new BufferedReader( new FileReader( info.arguments()[0] ));

            // get object reference from command-line argument file
            org.omg.CORBA.Object obj = 
               ((ORBInitInfoImpl) info).getORB().string_to_object( br.readLine() );

            br.close();

            info.add_client_request_interceptor( new ClientInterceptorB( obj ) );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}// ClientInitializer

