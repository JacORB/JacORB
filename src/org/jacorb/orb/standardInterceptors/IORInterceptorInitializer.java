package org.jacorb.orb.standardInterceptors;

import org.omg.PortableInterceptor.*;
import org.jacorb.orb.*;
import org.jacorb.util.Environment;
/**
 * This class initializes the default IOR interceptors 
 * used by JacORB.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class IORInterceptorInitializer 
    extends org.omg.CORBA.LocalObject 
    implements ORBInitializer
{
  
    public IORInterceptorInitializer() {
    
    }

    // implementation of org.omg.PortableInterceptor.ORBInitializerOperations interface

    /**
     * Adds the SSLComponentInterceptor and the CodeSetInfoInterceptor 
     * to the set of IORInterceptors.
     *
     * @param info the info object.
     */
    public void post_init(ORBInitInfo info) 
    {
        try
        {
            ORB orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB();
            if( Environment.isPropertyOn( "jacorb.security.support_ssl" ) &&
                Environment.hasProperty( "jacorb.security.ssl.server.supported_options" ) && 
                Environment.hasProperty( "jacorb.security.ssl.server.required_options" ))
            {
                info.add_ior_interceptor(new SSLComponentInterceptor(orb));
            }

            int giop_minor = 
                Integer.parseInt( 
                    Environment.getProperty( 
                        "jacorb.giop_minor_version",
                        "2" ));
                
            if( giop_minor > 0 )
            {
                info.add_ior_interceptor(new CodeSetInfoInterceptor(orb));
            }
        }
        catch (Exception e)
        {
            org.jacorb.util.Debug.output(1, e);
        }
    }

    /**
     *
     * @param info <description>
     */

    public void pre_init(ORBInitInfo info) 
    {
        // do nothing
    }

} // IORInterceptorInitializer


