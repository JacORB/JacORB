package org.jacorb.test.common;

/**
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public interface JacORBTest
{
    public boolean isApplicableTo (String clientVersion, String serverVersion);
}
