package org.jacorb.notification.engine;

import java.util.List;
import org.jacorb.notification.TransmitEventCapable;

/**
 * Destination.java
 *
 *
 * Created: Thu Nov 14 20:37:21 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version
 */

public interface Destination {

    List getSubsequentDestinations();
    List getFilters();
    TransmitEventCapable getEventSink();

}// Destination
