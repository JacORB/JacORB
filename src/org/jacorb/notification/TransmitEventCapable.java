package org.jacorb.notification;

/**
 *
 *
 * Created: Tue Nov 05 14:46:54 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version
 */

public interface TransmitEventCapable {
    public void transmit_event(NotificationEvent event);
}// PushCapable
