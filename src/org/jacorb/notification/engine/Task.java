package org.jacorb.notification.engine;

/*
 *        JacORB - a free Java ORB
 */

/**
 * Task.java
 *
 *
 * Created: Thu Nov 14 18:33:57 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public interface Task extends Runnable {

    public static int DELIVERED = 0;
    public static int NEW = 1;
    public static int PROXY_CONSUMER_FILTERED = 2;
    public static int CONSUMER_ADMIN_FILTERED = 3;
    public static int SUPPLIER_ADMIN_FILTERED = 4;
    public static int PROXY_SUPPLIER_FILTERED = 5;

    public int getStatus();
}// Task
