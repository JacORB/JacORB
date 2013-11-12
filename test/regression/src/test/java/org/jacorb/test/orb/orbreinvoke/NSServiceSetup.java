/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jacorb.test.orb.orbreinvoke;

import java.io.IOException;
import java.util.Properties;
import org.jacorb.naming.NameServer;
import org.jacorb.test.common.ServerSetup;

/**
 *
 * @author nguyenq
 */
public class NSServiceSetup extends ServerSetup
{
    public NSServiceSetup (Properties props, int id) throws IOException
    {
        super (
                NSServiceRunner.class.getName(),
                NameServer.class.getName(),
                props
              );

        errName = "NS-" + Integer.toString(id) + "-ERR";
        outName = "NS-" + Integer.toString(id) + "-OUT";
    }
}
