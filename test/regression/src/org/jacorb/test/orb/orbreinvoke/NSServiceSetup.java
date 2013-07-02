/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jacorb.test.orb.orbreinvoke;

import java.util.Properties;
import junit.framework.Test;
import org.jacorb.naming.NameServer;
import org.jacorb.test.common.ServerSetup;

/**
 *
 * @author nguyenq
 */
public class NSServiceSetup extends ServerSetup
{
    public NSServiceSetup (Test test, Properties props, int id)
    {
        super (
                test,
                NSServiceRunner.class.getName(),
                NameServer.class.getName(),
                props
              );

        errName = "NS-" + Integer.toString(id) + "-ERR";
        outName = "NS-" + Integer.toString(id) + "-OUT";
    }
}
