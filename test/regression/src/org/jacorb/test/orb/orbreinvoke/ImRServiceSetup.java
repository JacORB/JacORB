/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jacorb.test.orb.orbreinvoke;

import java.util.Properties;
import junit.framework.Test;
import org.jacorb.imr.ImplementationRepositoryImpl;
import org.jacorb.test.common.ServerSetup;

/**
 *
 * @author nguyenq
 */
public class ImRServiceSetup extends ServerSetup
{
    public ImRServiceSetup (Test test, Properties imrProps, int imrNum)
    {
        super (
                test,
                ImRServiceRunner.class.getName(),
                ImplementationRepositoryImpl.class.getName(),
                imrProps
              );

        errName = "IMR-" + Integer.toString(imrNum) + "-ERR";
        outName = "IMR-" + Integer.toString(imrNum) + "-OUT";
    }
}
