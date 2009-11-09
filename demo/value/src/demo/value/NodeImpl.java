package demo.value;

public class NodeImpl extends Node
{
    public NodeImpl()
    {
        // This no-arg constructor is used at unmarshalling time.
        // Because of its presence, there is no need for a separate
        // value factory for this type.  This is a special JacORB feature,
        // see README and org.jacorb.orb.ORB.lookup_value_factory() for
        // details.
    }

    public NodeImpl (int id)
    {
        this.id = id;
    }

    public String toString()
    {
        return "#" + Integer.toString (id) + "#";
    }
}
