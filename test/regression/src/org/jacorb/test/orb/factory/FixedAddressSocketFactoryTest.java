package org.jacorb.test.orb.factory;

import org.jacorb.orb.factory.FixedAddressSocketFactory;
import org.jacorb.orb.factory.SocketFactory;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class FixedAddressSocketFactoryTest extends AbstractSocketFactoryTest
{
    protected SocketFactory newObjectUnderTest()
    {
        return new FixedAddressSocketFactory();
    }
}
