package org.jacorb.notification.filter;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class PropertyDoesNotExistException extends EvaluationException {

    public PropertyDoesNotExistException(String name) {
        super("the property $" + name + " does not exist");
    }

}
