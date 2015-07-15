/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jacorb.test.bugs.bug1013;

import java.io.IOException;
import java.util.Properties;
import org.jacorb.imr.ImplementationRepositoryImpl;
import org.jacorb.test.harness.ImplementationRepositoryRunner;
import org.jacorb.test.harness.ServerSetup;

/**
 *
 * @author nguyenq
 */
public class ImRServiceSetup extends ServerSetup
{
    public ImRServiceSetup (Properties imrProps, int imrNum) throws IOException
    {
        super (
                ImplementationRepositoryRunner.class.getName(),
                ImplementationRepositoryImpl.class.getName(),
                imrProps
              );

        errName = "IMR-" + Integer.toString(imrNum) + "-ERR";
        outName = "IMR-" + Integer.toString(imrNum) + "-OUT";
    }
}
