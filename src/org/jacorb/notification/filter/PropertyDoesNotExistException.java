package org.jacorb.notification.filter;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class PropertyDoesNotExistException extends EvaluationException
{
    private static final long serialVersionUID = 1L;

    public PropertyDoesNotExistException(String name)
    {
        super("the property $" + name + " does not exist");
    }
}
