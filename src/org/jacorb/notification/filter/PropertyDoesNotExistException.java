package org.jacorb.notification.filter;

/**
 * @author Alphonse Bendt
 */
public class PropertyDoesNotExistException extends EvaluationException
{
    private static final long serialVersionUID = 1L;

    public PropertyDoesNotExistException(String name)
    {
        super("the property $" + name + " does not exist");
    }
}
