package org.jacorb.test.bugs.bugjac149;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * Serialiable replacement object for Model
 * Supplied by Cisco
 */
public class ModelReplacement
        implements Externalizable
{
    private String repName;
    private long state;

    public static final String SUFFIX = "-replacement";

    /**
     * Creates a new ModelReplacement object.
     */
    public ModelReplacement()
    {
    }

    /**
     * Creates a new ModelReplacement object.
     */
    public ModelReplacement(Model m)
    {
        repName = m.getName() + SUFFIX;
        state = m.getState();
    }

    /**
     * name accessor
     */
    public String getName()
    {
        return repName;
    }

    /**
     * (transient) state accessor
     */
    public long getState()
    {
        return state;
    }

    /**
     * Externalizable.writeExternal() impl
     *
     * @param oos +
     *
     * @throws IOException +
     */
    public void writeExternal(ObjectOutput oos)
            throws IOException
    {
        oos.writeObject(repName);
        oos.writeLong(state);
    }

    /**
     * Externalizable.readExternal() impl
     *
     * @param ois +
     *
     * @throws IOException +
     * @throws ClassNotFoundException +
     */
    public void readExternal(ObjectInput ois)
            throws IOException, ClassNotFoundException
    {

        repName = (String) ois.readObject();
        state = ois.readLong();
    }

    /**
     * readResolve() method
     */
    public Object readResolve()
    {
        Model resolvedModel = null;

            String name = getName();
            name = name.substring(0, name.length() -
                                  ModelReplacement.SUFFIX.length());
            resolvedModel = new Model(name);
        return resolvedModel;
    }

    /**
     * string repr
     */
    public String toString()
    {
        return "model replacement: " + getName() + " [" + state + "]";
    }
}
