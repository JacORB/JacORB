package org.jacorb.test.bugs.bug351;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

/**
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 * @version $Id$
 */
public class RetrievalResultImpl extends RetrievalResult
{
    public RetrievalResultImpl()
    {
        super();   
    }
    
    public RetrievalResultImpl (float[] scores, String[] ids, int size)
    {
        this.scores = scores;
        this.ids = ids;
        this.size = size;
    }
    
    public float[] getScores()
    {
        return scores;
    }

    public String[] getIds()
    {
        return ids;
    }

    public int getSize()
    {
        return size;
    }

    public void setScores(float[] scores)
    {
        this.scores = scores;
    }

    public void setIds(String[] ids)
    {
        this.ids = ids;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

}
