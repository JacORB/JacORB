package org.jacorb.orb.connection;

import java.io.IOException;

/**
 * TimeOutException.java
 *
 *
 * Created: Thu Oct  4 15:50:30 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class TimeOutException 
    extends IOException 
{
    public TimeOutException( String reason )
    {
        super( reason );
    }
    
}// TimeOutException
