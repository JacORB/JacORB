package org.jacorb.test.bugs.bugjac149;

import junit.framework.*;


import java.util.Properties;
import org.jacorb.test.common.ClientServerSetup;


public class ObjectReplacementSetup extends ClientServerSetup
{
    public ObjectReplacementSetup(Test test, Properties clientProperties, Properties serverProperties)
    {
        super(test,
              "org.jacorb.test.bugs.bugjac149.ObjRepServer",
              "org.jacorb.test.bugs.bugjac149.ObjRepServer",
              clientProperties,
              serverProperties);
    }
}
