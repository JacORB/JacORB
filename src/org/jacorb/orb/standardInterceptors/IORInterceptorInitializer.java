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
    extends LocalityConstrainedObject 
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

            info.add_ior_interceptor(new CodeSetInfoInterceptor(orb));
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


