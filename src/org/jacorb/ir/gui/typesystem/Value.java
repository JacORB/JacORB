package org.jacorb.ir.gui.typesystem;


/**
 *  The interface of our representation of value types.
 */
public interface Value 
{
    /**
     *  Return the concrete base value of this value, or null
     *  if this base value has no base value.
     */
    public Value getBaseValue();

    /**
     * Returns all value members defined here, including value members from
     * the base value.
     */
    public TypeSystemNode[] getAllMembers();

    /**
     * Returns all fields defined here, including fields from
     * the base value and interfaces.
     */
    public TypeSystemNode[] getAllFields();

    /**
     * Returns all operations defined here, including operations from
     * the base value and interfaces, but excluding initializers.
     */ 
    public TypeSystemNode[] getAllOperations();

    /**
     * Return an array of the interfaces that this value implements.
     */
    public Interface[] getInterfaces();
}
