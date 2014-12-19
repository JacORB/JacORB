package demo.value;

import java.util.*;

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
            return "shared string: " + s1;
        else
            return "two strings: `" + s1 + "', `" + s2 + "'";
    }

    public String receive_list (Node n)
    {
        List l = new ArrayList();
        Node x = n;

        while (x != null && !l.contains (x))
        {
            l.add (x);
            x = x.next;
        }

        StringBuffer result = new StringBuffer ("list of length: "
                                                + l.size() + " -- ");
        for (Iterator i = l.iterator(); i.hasNext();)
        {
            Node q = (Node)i.next();
            result.append (q.id);
            if (i.hasNext()) result.append (" ");
        }
        return result.toString();
    }

}
