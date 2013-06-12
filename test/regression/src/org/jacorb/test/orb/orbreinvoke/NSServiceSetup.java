/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jacorb.test.orb.orbreinvoke;

import java.io.File;
import java.lang.String;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import junit.extensions.TestSetup;
import junit.framework.Test;
import org.jacorb.naming.NameServer;
import org.jacorb.test.common.ServerSetup;
import org.jacorb.test.common.TestUtils;

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
