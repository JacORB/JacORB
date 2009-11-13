package org.jacorb.test.common;

import org.jacorb.imr.ImplementationRepositoryImpl;

/**
 * starts the ImplementationRepository and prints out its IOR
 * in a format understandable to ClientServerSetup.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */
public class ImplementationRepositoryRunner
{
    public static void main(String[] args)
    {
        ImplementationRepositoryImpl.main(new String[] {"-printIOR"});
    }
}
