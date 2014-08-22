package org.jacorb.test.harness;

import org.jacorb.imr.ImplementationRepositoryImpl;

/**
 * starts the ImplementationRepository and prints out its IOR
 * in a format understandable to ClientServerSetup.
 *
 * @author Alphonse Bendt
 */
public class ImplementationRepositoryRunner
{
    public static void main(String[] args) throws Exception
    {
        ImplementationRepositoryImpl.main(new String[] {"-printIOR"});
    }
}
