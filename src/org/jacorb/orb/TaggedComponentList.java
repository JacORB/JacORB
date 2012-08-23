
package org.jacorb.orb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.omg.CONV_FRAME.CodeSetComponentHelper;
import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.CONV_FRAME.CodeSetComponentInfoHelper;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedComponentSeqHelper;
/**
 * Represents a list of tagged components from an IOR, along with some
 * generic methods to find and access individual components.
 * <p>
 * @author Andre Spiegel
 */
public class TaggedComponentList implements Cloneable
{
    private TaggedComponent[] components = null;

    /**
     * Constructs a TaggedComponentList object from a CDR representation
     * of an array of tagged components.
     */
    public TaggedComponentList (org.omg.CORBA.portable.InputStream in)
    {
        components = TaggedComponentSeqHelper.read (in);
    }

    /**
     * Constructs a TaggedComponentList from a CDR encapsulation of
     * an array of tagged components.
     */
    public TaggedComponentList (byte[] data)
    {
        CDRInputStream in = new CDRInputStream (data);
        in.openEncapsulatedArray();
        components = TaggedComponentSeqHelper.read (in);
    }

    /**
     * Constructs a new, empty TaggedComponentList.
     */
    public TaggedComponentList()
    {
        components = new TaggedComponent[0];
    }

    public int size()
    {
        return components.length;
    }

    public boolean isEmpty()
    {
        return components.length == 0;
    }

    public TaggedComponent get (int index)
    {
        return components[index];
    }

    public Object clone() throws CloneNotSupportedException
    {
        TaggedComponentList result = (TaggedComponentList)super.clone();
        result.components = new TaggedComponent[this.components.length];
        for (int i=0; i<this.components.length; i++)
        {
            result.components[i] = new TaggedComponent
            (
                this.components[i].tag,
                new byte [this.components[i].component_data.length]
            );
            System.arraycopy (this.components[i].component_data, 0,
                              result.components[i].component_data, 0,
                              this.components[i].component_data.length);
        }
        return result;
    }

    public TaggedComponent[] asArray()
    {
        return components;
    }

    /**
     * Adds a tagged component to this list. The component's data
     * is created by marshaling the given data Object using the
     * write() method of the given helper class.
     */
    public void addComponent (int tag, Object data, Class helper)
    {
        try
        {
            Method writeMethod = helper.getMethod
            (
                "write",
                new Class[]
                {
                    org.omg.CORBA.portable.OutputStream.class,
                    data.getClass()
                }
            );

            final CDROutputStream out = new CDROutputStream();

            try
            {
                out.beginEncapsulatedArray();
                writeMethod.invoke
                (
                        null,
                        new Object[]{ out, data }
                );
                addComponent (tag, out.getBufferCopy());
            }
            finally
            {
                out.close();
            }
        }
        catch (NoSuchMethodException ex)
        {
            throw new RuntimeException ("Helper " + helper.getName()
                                        + " has no appropriate write() method.");
        }
        catch (IllegalAccessException ex)
        {
            throw new RuntimeException ("Cannot access write() method of helper "
                                        + helper.getName());
        }
        catch (InvocationTargetException ex)
        {
            throw new RuntimeException ("Exception while marshaling component data: "
                                        + ex.getTargetException());
        }
    }

    /**
     * Adds a tagged component to this list.
     */
    public void addComponent (int tag, byte[] data)
    {
        addComponent (new TaggedComponent (tag, data));
    }

    /**
     * Adds a tagged component to this list.
     */
    public void addComponent (TaggedComponent component)
    {
        TaggedComponent[] newComponents =
            new TaggedComponent [components.length + 1];
        System.arraycopy (components, 0, newComponents, 0, components.length);
        newComponents [components.length] = component;
        components = newComponents;
    }

    /**
     * Adds an entire TaggedComponentList to this list.
     */
    public void addAll (TaggedComponentList other)
    {
        TaggedComponent[] newComponents =
            new TaggedComponent [components.length + other.components.length];
        System.arraycopy (components, 0, newComponents, 0, components.length);
        System.arraycopy (other.components, 0, newComponents, components.length,
                          other.components.length);
        components = newComponents;
    }

    /**
     * Searches for a component with the given tag in this component list.
     * If one is found, this method reads the corresponding data with the given
     * helper class, and returns the resulting object, otherwise returns
     * null.
     */
    public Object getComponent (int tag, Class helper)
    {
        for (int i=0; i < components.length; i++)
        {
            if (components[i].tag == tag)
            {
                return getComponentData (components[i].component_data, helper);
            }
        }
        return null;
    }


    /**
     * Returns a List of all components with the given tag from this
     * TaggedComponentList.  Each individual component is read with
     * the given helper class.  If no components with the given tag
     * can be found, an empty list is returned.
     *
     * The only caller of this currently is IIOPProfile using a helper
     * of IIOPAddress. To prevent non-configured IIOPAddresses we configure
     * them here.
     */
    public List getComponents (Configuration configuration, int tag, Class helper)
    {
        List result = new ArrayList();
        for (int i=0; i < components.length; i++)
        {
            if (components[i].tag == tag)
            {
               Object element = getComponentData (components[i].component_data, helper);
               if (element instanceof Configurable)
               {
                  try
                  {
                     ((Configurable)element).configure(configuration);
                  }
                  catch( ConfigurationException e )
                  {
                     throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + e.toString());
                  }
               }
               result.add (element);
            }
        }
        return result;
    }

    /**
     * Uses the given helper class to read a CDR-encapsulated component_data
     * field from the given byte array, data.
     */
    private Object getComponentData (byte[] data, Class helper)
    {
        java.lang.Object result = null;

        final CDRInputStream in = new CDRInputStream (data);

        try
        {
            in.openEncapsulatedArray();

            if (helper == CodeSetComponentInfoHelper.class)
            {
                result = new CodeSetComponentInfo();

                ((CodeSetComponentInfo)result).ForCharData =
                    (CodeSetComponentHelper.read(in));
                ((CodeSetComponentInfo)result).ForWcharData =
                    (CodeSetComponentHelper.read(in));
            }
            else if (helper == ParsedIOR.LongHelper.class)
            {
                result = new Integer (in.read_long());
            }
            else if (helper == ParsedIOR.StringHelper.class)
            {
                result = in.read_string();
            }
            else
            {
                try
                {
                    Method readMethod = helper.getMethod
                        ("read", new Class[] { org.omg.CORBA.portable.InputStream.class });
                    result = readMethod.invoke (null, new Object[] { in });
                }
                catch (NoSuchMethodException ex)
                {
                    throw new RuntimeException ("Helper " + helper.getName()
                                                + " has no appropriate read() method.");
                }
                catch (IllegalAccessException ex)
                {
                    throw new RuntimeException ("Cannot access read() method of helper "
                                                + helper.getName());
                }
                catch (InvocationTargetException ex)
                {
                    throw new RuntimeException ("Exception while reading component data: "
                                                + ex.getTargetException());
                }
            }
        }
        finally
        {
            in.close();
        }
        return result;
    }
}
