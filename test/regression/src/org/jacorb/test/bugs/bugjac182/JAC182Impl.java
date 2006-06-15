package org.jacorb.test.bugs.bugjac182;

import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;

/**
 * <code>JAC182Impl</code> is a basic server implementation. It checks the
 * information stored with PICurrent by the SInterceptor in order to return
 * the correct value to the client.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class JAC182Impl extends JAC182POA
{
    private ORB orb;

    public JAC182Impl(ORB orb)
    {
        super();
        this.orb = orb;
    }

    /**
     * <code>test182Op</code> dummy impl.
     * @return an <code>boolean</code> value depending upon the result of the
     *         interceptors. true for local; false for not local.
     */
    public boolean test182Op()
    {
        boolean result;

        try
        {
            Current current = (Current)orb.resolve_initial_references( "PICurrent" );

            Any anyName = current.get_slot( SInitializer.slotID );

            result = anyName.extract_boolean();
        }
        catch (InvalidSlot e)
        {
            e.printStackTrace();
            throw new INTERNAL(e.toString());
        }
        catch (InvalidName e)
        {
            e.printStackTrace();
            throw new INTERNAL(e.toString());
        }
        return result;
    }
}
