package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-98  Gerald Brose.
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

import java.util.Map;

/** 
 * information the has to be saved for each encapsulation and
 * restored later
 * @author Gerald Brose
 * @version $Id$
 */

public class EncapsInfo
{
    public boolean littleEndian;
    public int index;
    public int start;
    public int size;
    public Map valueMap;
    public Map repIdMap;
    public Map codebaseMap;

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
                      Map valueMap, Map repIdMap, Map codebaseMap)
    {
	this.index = index;
	this.start = start;
        this.valueMap = valueMap;
        this.repIdMap = repIdMap;
        this.codebaseMap = codebaseMap;
    }
}








