package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.*;
import org.omg.CORBA.UserException;

import org.jacorb.util.Debug;
/**
 * IORInterceptorIterator.java
 *
 *
 * Created: Mon Apr 17 09:53:33 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */

public class IORInterceptorIterator 
    extends AbstractInterceptorIterator 
{

    private IORInfoImpl info = null;

    public IORInterceptorIterator(Interceptor[] interceptors) {
        super(interceptors);
    }
  
    public void iterate(IORInfoImpl info)
        throws UserException{

        this.info = info;

        iterate();
    }

    protected void invoke(Interceptor interceptor)
        throws UserException{

        try
        {
            Debug.output( Debug.DEBUG1 | Debug.INTERCEPTOR, 
                          "Invoking IORInterceptor " + 
                          interceptor.name());

            ((IORInterceptor) interceptor).establish_components(info);
        }
        catch(Exception e)
        {
            Debug.output(Debug.INFORMATION | Debug.INTERCEPTOR, e);
        }
    }
} // IORInterceptorIterator
