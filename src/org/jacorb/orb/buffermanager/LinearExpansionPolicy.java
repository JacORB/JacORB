package org.jacorb.orb.buffermanager;

public class LinearExpansionPolicy implements BufferManagerExpansionPolicy
{
    public int getExpandedSize (int requestedSize)
    {
        return requestedSize;
    }
}
