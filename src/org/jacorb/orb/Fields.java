package org.jacorb.orb;

import java.util.*;
import java.lang.reflect.*;

/**
 * Provides static methods to access the serializable fields of a class.
 */
public class Fields 
{

    private static final class FieldComparator 
        implements java.util.Comparator 
    {
        public int compare (Object o1, Object o2) 
        {
            Field f1 = (Field)o1;
            Field f2 = (Field)o2;

            if (f1.getType().isPrimitive())
                if (f2.getType().isPrimitive())
                    return f1.getName().compareTo (f2.getName());
                else
                    return -1;
            else
                if (f2.getType().isPrimitive())
                    return +1;
                else
                    return f1.getName().compareTo (f2.getName());
        }
    }

    /**
     * Returns the non-constant, non-static, non-transient fields of
     * class clz.  The resulting set contains the primitive fields first,
     * sorted lexicographically according to field name, then the 
     * non-primitive fields, also sorted lexicographically according to 
     * field name.
     */
    public static SortedSet getFields (Class clz) 
    {
        Field[]   fields = clz.getDeclaredFields();
        AccessibleObject.setAccessible (fields, true);
        SortedSet result = new TreeSet (new FieldComparator());
        for (int i=0; i < fields.length; i++) 
        {
            int modifiers = fields[i].getModifiers();
            if ((modifiers 
                 & (Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT))
                == 0)
                result.add (fields[i]);
        }
        return result;
    }

    // main method for testing

    public static void main (String[] args) throws Exception
    {
        Class c = Class.forName (args[0]);
        for (Iterator i = getFields(c).iterator(); i.hasNext();)
            System.out.println (i.next());
    }


}
