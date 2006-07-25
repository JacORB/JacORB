package org.jacorb.test.orb.factory;

import org.jacorb.orb.factory.DefaultSocketFactory;
import org.jacorb.orb.factory.SocketFactory;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class DefaultSocketFactoryTest extends AbstractSocketFactoryTest
{
    protected SocketFactory newObjectUnderTest()
    {
        return new DefaultSocketFactory();
    }
}
