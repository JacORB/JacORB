package org.jacorb.test.orb;

import org.jacorb.test.SamplePOA;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class SampleImpl extends SamplePOA
{
    public int ping (int data)
    {
        return data+1;
    }
}
