/***** Copyright (c) 1999 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

package org.omg.PortableServer;

public interface POAManagerOperations {

    public void activate() throws
                            org.omg.PortableServer.POAManagerPackage.AdapterInactive;

    public void hold_requests(boolean wait_for_completion) throws
                            org.omg.PortableServer.POAManagerPackage.AdapterInactive;

    public void discard_requests(boolean wait_for_completion) throws
                            org.omg.PortableServer.POAManagerPackage.AdapterInactive;

    public void deactivate(boolean etherealize_objects, 
                                boolean wait_for_completion) throws
                            org.omg.PortableServer.POAManagerPackage.AdapterInactive;

    public org.omg.PortableServer.POAManagerPackage.State get_state();

}
