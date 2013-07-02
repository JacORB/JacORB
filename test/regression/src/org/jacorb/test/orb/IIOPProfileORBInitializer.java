
package org.jacorb.test.orb;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

/**
 * @author Andre Spiegel
 */
public class IIOPProfileORBInitializer
    extends LocalObject
    implements ORBInitializer
{
    public void pre_init (ORBInitInfo info)
    {
        try
        {
            info.add_ior_interceptor (new IIOPProfileInterceptor());
        }
        catch (DuplicateName ex)
        {
            throw new RuntimeException (ex.toString());
        }
    }

    public void post_init (ORBInitInfo info)
    {

    }

}
