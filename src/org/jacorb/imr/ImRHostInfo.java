package org.jacorb.imr;

import org.omg.CORBA.ORB;
/**
 * This class represents a host. It contains information about 
 * a server startup daemon residing on this host and provides
 * a method for starting a server on that host.
 *
 * @author Nicolas Noffke
 * 
 * @version $Id$
 */

public class ImRHostInfo implements java.io.Serializable {
    protected String host;

    private ServerStartupDaemon ssd_ref;
    private String object_string;

    private boolean reconnect = false;
    
    /**
     * The constructor of this class.
     *
     * @param host the HostInfo object to take the information from.
     * @param orb needed for calling object_to_string().
     **/
    public ImRHostInfo(HostInfo host) {
	this.host = host.name;
	ssd_ref = host.ssd_ref;
	object_string = host.ior_string;
    }
    
    /**
     * "Convert" this object to a HostInfo object
     *
     * @return a HostInfo instance
     **/
    public HostInfo toHostInfo(){
	// setting ref explicitely to null since it might be stale.
	return new HostInfo(host, null, object_string);
    }

    /**
     * This method tries to start a server with the daemon for this host.
     *
     * @param command the startup command of the server
     * @param orb needed for calling string_to_object().
     * @exception ServerStartupFailed propagated up from the daemon if something
     * went wrong. Likely to throw CORBA System Exceptions as well, especially
     * if the daemon is down.
     **/
    public void startServer(String command, ORB orb) 
	throws ServerStartupFailed{
       
	if (reconnect)
	    ssd_ref = ServerStartupDaemonHelper.narrow(orb.string_to_object(object_string));

	ssd_ref.start_server(command);
    }

    /**
     * Things to be done before serialization. Implemented from Serializable. 
     **/
    private void writeObject(java.io.ObjectOutputStream out)
	throws java.io.IOException{

	reconnect = true; // all ssd_references are stale after deserialization
	out.defaultWriteObject();
    }
} // ImRHostInfo








