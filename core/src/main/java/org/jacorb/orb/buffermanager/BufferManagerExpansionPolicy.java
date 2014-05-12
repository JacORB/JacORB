package org.jacorb.orb.buffermanager;

public interface BufferManagerExpansionPolicy
{
    public int getExpandedSize (int requestedSize);
}
