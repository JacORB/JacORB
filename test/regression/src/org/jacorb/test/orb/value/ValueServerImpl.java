package org.jacorb.test.orb.value;

import java.util.*;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;

public class ValueServerImpl extends ValueServerPOA
{
    public String receive_long (boxedLong p1, boxedLong p2)
    {
        if (p1 == null || p2 == null)
            return "one or two null values";
        else if (p1 == p2)
            return "shared long: " + p1.value;
        else
            return "two longs: " + p1.value + ", " + p2.value;
    }
    
    public String receive_string (String s1, String s2)
    {
        if (s1 == null || s2 == null)
            return "one or two null values";
        else if (s1 == s2)
            return "shared string: `" + s1 + "'";
        else
            return "two strings: `" + s1 + "', `" + s2 + "'";
    }

    public String receive_list (Node n)
    {
        List l = new ArrayList();
        Node x = n;
        boolean shared = false;
        
        while (x != null)
        {
            l.add (x);
            x = x.next;
            if (l.contains(x))
            {
                shared = true;
                break;
            }
        }

        StringBuffer result = new StringBuffer ("list of length: " 
                                                + l.size() + " -- ");
        for (Iterator i = l.iterator(); i.hasNext();)
        {
            Node q = (Node)i.next();
            result.append (q.id);
            if (i.hasNext()) result.append (" ");
        }
        
        if (shared) 
            result.append(" -- shared");
        
        return result.toString();
    }

    public String receive_record_sequence(Record[] list)
    {
        StringBuffer result = new StringBuffer("list of length " + list.length);
        result.append (", null values: ");
        for (int i=0; i<list.length; i++)
        {
            if (list[i] == null)
                result.append (i + " ");
        }
        for (int i=0; i<list.length/2; i++)
        {
            if (list[i] != list[list.length-i-1])
                return result.toString() + ", no palindrome";
        }
        return result.toString() + ", palindrome";
    }

    public Record[] return_record_sequence(int length)
    {
        Record[] result = new Record[length];
        for (int i=0; i<result.length; i++)
        {
            result[i] = new RecordImpl(i, "node: " + i);
        }
        return result; 
    }

}
