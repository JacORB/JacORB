package org.jacorb.orb.factory;

/* 
 * 
 * @author Nicolas Noffke
 * $Id$
 */

import java.net.*;

public interface SSLServerSocketFactory
    extends ServerSocketFactory
{
    public void switchToClientMode( Socket socket );
    
    public boolean isSSL( ServerSocket socket );
}







