
package org.jacorb.orb;

import java.lang.reflect.*;
import java.util.*;

import org.omg.IOP.*;

/**
 * Represents a list of tagged components from an IOR, along with some
 * generic methods to find and access individual components.
 * <p>
 * @author Andre Spiegel
 * @version $Id$
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
		CDRInputStream in = new CDRInputStream (null, data);
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
            CDROutputStream out = new CDROutputStream();
            out.beginEncapsulatedArray();
            writeMethod.invoke
            (
                null,
                new Object[]{ out, data }
            );
            addComponent (tag, out.getBufferCopy());
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
	 * Returns the first component with the given tag, which is assumed
	 * to be a CDR string.  If no component with the given tag exists,
	 * returns null.
	 */
	public String getStringComponent (int tag)
	{
		for (int i=0; i < components.length; i++)
		{
			if (components[i].tag == tag)
			{
				CDRInputStream in = 
				    new CDRInputStream (null, 
				                        components[i].component_data);	
				in.openEncapsulatedArray();
				return in.read_string();
			}
		}
		return null;
	}
	
	/**
	 * Returns a List of all components with the given tag from this
	 * TaggedComponentList.  Each individual component is read with
	 * the given helper class.  If no components with the given tag
     * can be found, an empty list is returned.
	 */
	public List getComponents (int tag, Class helper)
	{
		List result = new ArrayList();
		for (int i=0; i < components.length; i++)
		{
			if (components[i].tag == tag)
			{
				result.add (getComponentData (components[i].component_data, 
										      helper));
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
		try
		{
			Method readMethod = 
				helper.getMethod ("read", 
							      new Class[] { org.omg.CORBA.portable.InputStream.class });
			CDRInputStream in = new CDRInputStream (null, data);
			in.openEncapsulatedArray();
			return readMethod.invoke (null, new Object[] { in });
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
