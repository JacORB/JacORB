package org.jacorb.orb.buffermanager;

public class DoubleExpansionPolicy implements BufferManagerExpansionPolicy
{
    public int getExpandedSize (int requestedSize)
    {
        return requestedSize * 2;
    }
}
