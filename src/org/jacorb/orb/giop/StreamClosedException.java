package org.jacorb.orb.connection;

import java.io.IOException;

/**
 * StreamClosedException.java
 *
 *
 * Created: Thu Oct  4 15:50:30 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class StreamClosedException 
    extends IOException 
{
    public StreamClosedException( String reason )
    {
        super( reason );
    }
    
}// StreamClosedException
