package org.jacorb.orb.standardInterceptors;

import org.omg.PortableInterceptor.*;
import org.jacorb.orb.*;

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
            if( org.jacorb.util.Environment.supportSSL() )
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
