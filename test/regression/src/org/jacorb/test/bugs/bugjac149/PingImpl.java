package org.jacorb.test.bugs.bugjac149;


import java.io.Serializable;


/**
 * IPing impl.
 * Supplied by Cisco
 */
public class PingImpl
        implements IPing
{
    public Serializable ping(Serializable o)
    {
        return o;
    }
}
