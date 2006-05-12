package org.jacorb.test.orb.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.omg.CORBA.Any;

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


    public String receive_list_in_any(Any any) {
        Node n = NodeHelper.extract(any);

        return receive_list(n);
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

    /**
     * <code>getNodes</code> returns an embedded valuetype.
     *
     * @return a <code>NodeData[]</code> value
     */
    public NodeData[] getNodes()
    {
        NodeData[] res = new NodeData[2];
        res[0] = new NodeDataImpl();
        res[0].data2 = new DataImpl();
        res[0].data2.id = 17;
        res[0].id = 1;

        res[1] = new NodeDataImpl();
        res[1] = new NodeDataImpl();
        res[1].data1 = new DataImpl();
        res[1].data1.id = 22;
        res[1].id = 2;
        return res;
    }

    public RowListData getData()
    {
        RowListData ret = new RowListDataImpl();
        ColumnData[] cols = new ColumnDataImpl[2];
        ret.columns = cols;
        for(int i=0; i<2;i++)
        {
            cols[i] = createColumnData();
        }
        return ret;
    }

    private ColumnData createColumnData()
    {
        ColumnData ret = new ColumnDataImpl();
        String[] val = new String[] {"Teststring1", "TestString2"};
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();
        org.omg.CORBA.Any any = orb.create_any();

        ValStringListHelper.insert(any, val);

        ret.values = any;
        ret.nulls = new boolean[0];
        return ret;
    }
}
