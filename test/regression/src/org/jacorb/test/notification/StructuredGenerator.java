package org.jacorb.test.notification;

import org.omg.CORBA.ORB;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.Property;

public class StructuredGenerator {
    AnyGenerator anyGenerator_;

    public StructuredGenerator(ORB orb) {
	anyGenerator_ = new AnyGenerator(orb);
    }

    public StructuredEvent getNextEvent() {
	FixedEventHeader _fixedHeader = new FixedEventHeader();

	_fixedHeader.event_name = "TIMING";
	_fixedHeader.event_type = new EventType("TIMING", "TIMING");
	EventHeader _header = new EventHeader(_fixedHeader, new Property[0]);
	StructuredEvent _event = new StructuredEvent(_header, new Property[0], anyGenerator_.getNextEvent());
	
	return _event;
    }
}
