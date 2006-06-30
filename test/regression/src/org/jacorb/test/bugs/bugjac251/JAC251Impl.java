package org.jacorb.test.bugs.bugjac251;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.DynamicAny.DynArray;
import org.omg.DynamicAny.DynArrayHelper;
import org.omg.DynamicAny.DynStruct;
import org.omg.DynamicAny.DynStructHelper;
import org.omg.DynamicAny.DynUnion;
import org.omg.DynamicAny.DynUnionHelper;

/**
 * <code>PT251Impl</code> is the server class to test dynamic any.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class JAC251Impl extends JAC251POA implements Configurable
{
    /**
     * <code>orb</code> reference from starting the server.
     */
    private ORB orb;


    /**
     * DynamicAny <code>factory</code>.
     */
    private DynAnyFactory factory;

    /**
     * <code>pass_any</code> tests that a MARSHAL exception is not thrown
     * internally (e.g. by using a Singleton ORB).
     *
     * @param type a <code>String</code> value denoting the type of test
     * @param value an <code>Any</code> value
     */
    public void pass_any(String type, Any value)
    {
        if ("struct".equals (type))
        {
            try
            {
                DynStruct dynStruct = DynStructHelper.narrow
                    (factory.create_dyn_any(value));

                dynStruct.to_any();

            }
            catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e)
            {
                throw new INTERNAL();
            }
        }
        else if ("array".equals (type))
        {
            try
            {
                DynArray dynArray = DynArrayHelper.narrow
                    (factory.create_dyn_any(value));

                dynArray.to_any();
            }
            catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e)
            {
                throw new INTERNAL();
            }
        }
        else if ("union".equals (type))
        {
            try
            {
                DynUnion dynUnion = DynUnionHelper.narrow
                    (factory.create_dyn_any(value));

                dynUnion.to_any();
            }
            catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e)
            {
                throw new INTERNAL();
            }
        }
        else
        {
            throw new INTERNAL ("Unknown type " + type);
        }
    }

    public void configure(Configuration arg0) throws ConfigurationException
    {
        this.orb = ((org.jacorb.config.Configuration)arg0).getORB();

        try
        {
            factory = DynAnyFactoryHelper.narrow
                (orb.resolve_initial_references("DynAnyFactory"));
        }
        catch (org.omg.CORBA.ORBPackage.InvalidName e)
        {
            throw new INTERNAL (e.toString());
        }
    }
}
