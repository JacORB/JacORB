package org.jacorb.notification.node;

/**
 * StaticTypeException.java
 *
 *
 * Created: Fri Jul 05 21:55:02 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version
 */

public class StaticTypeException extends VisitorException {
    public StaticTypeException(String msg) {
	super(msg);
    }

    public StaticTypeException() {
	super();
    }

}// StaticTypeException
