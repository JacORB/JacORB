package org.jacorb.test.bugs.bugjac149;


import java.io.Serializable;


/**
 * Simple I/F for sending/returning a serializable object
 * Supplied by Cisco
 */
public interface IPing
{

    /**
     * Method for sending/receiving a serializable object
     */
    Serializable ping(Serializable o);
}
