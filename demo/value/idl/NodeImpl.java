package demo.value.idl;

public class NodeImpl extends Node
{

    public NodeImpl()
    {
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
