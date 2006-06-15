package org.jacorb.test.bugs.bugjac149;

import java.io.Serializable;

/**
 * Supplied by Cisco
 */
public class Model
        implements Serializable
{
    private String name;
    private Object transientData;

    /**
     * Creates a new Model object.
     *
     * @param name +
     */
    public Model(String name)
    {
        this.name = name;
        transientData = new Long(System.currentTimeMillis());
    }

    /**
     * Creates a new Model object.
     *
     * @param m +
     */
    public Model(ModelReplacement m)
    {

        String name = m.getName();
        this.name = name.substring(0, name.length() -
                                   ModelReplacement.SUFFIX.length());
        transientData = new Long(m.getState());
    }

    /**
     * name accessor
     *
     * @return +
     */
    public String getName()
    {
        return name;
    }

    /**
     * (transient) state accessor
     *
     * @return +
     */
    public long getState()
    {
        return ((Long) transientData).longValue();
    }

    /**
     * write-replace model to a serializable 'state' object
     */
    public Object writeReplace()
    {
        return new ModelReplacement(this);
    }

    /**
     * string repr
     *
     * @return +
     */
    public String toString()
    {
        return "model: " + getName() + " (" + transientData + ")";
    }
}
