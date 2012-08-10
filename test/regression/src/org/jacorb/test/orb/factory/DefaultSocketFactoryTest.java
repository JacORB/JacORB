package org.jacorb.test.orb.factory;

import org.jacorb.orb.factory.DefaultSocketFactory;
import org.jacorb.orb.factory.SocketFactory;

/**
 * @author Alphonse Bendt
 */
public class DefaultSocketFactoryTest extends AbstractSocketFactoryTestCase
{
    protected SocketFactory newObjectUnderTest()
    {
        return new DefaultSocketFactory();
    }
}
