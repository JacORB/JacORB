package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * information the has to be saved for each encapsulation and
 * restored later
 * @author Gerald Brose
 */

public class EncapsInfo
{
    public boolean littleEndian;
    public final int index;
    public final int start;
    public int size;
    public Map<Serializable, Integer> valueMap;
    public Map<String, Integer> repIdMap;
    public Map<String, Integer> codebaseMap;

    /** constructor used by CDRInputStream */

    public EncapsInfo(boolean le, int index, int start, int size)
    {
        littleEndian = le;
        this.index = index;
        this.start = start;
        this.size = size;
    }

    /**
     * constructor used by CDROutputStream:
     * record the index a new encapsulation starts with
     * and the start position in the buffer. CORBA specifies that "indirections
     * may not cross encapsulation boundaries", so the new encapsulation must
     * set up its own indirection maps for values, repository ids and codebase
     * strings. The maps currently in use are also recorded, to be restored at
     * the end of the encapsulation.
     */

    public EncapsInfo(int index, int start,
                      Map<Serializable,Integer> vMap, Map<String,Integer> rMap, Map<String,Integer> cMap)
    {
        this.index = index;
        this.start = start;
        this.valueMap = vMap;
        this.repIdMap = rMap;
        this.codebaseMap = cMap;

        if (valueMap == null)
        {
            valueMap = new HashMap<Serializable, Integer> ();
        }
        if (repIdMap == null)
        {
            repIdMap = new HashMap<String, Integer> ();
        }
        if (codebaseMap == null)
        {
            codebaseMap = new HashMap<String, Integer> ();
        }
    }
}
