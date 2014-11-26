package org.jacorb.test.bugs.bug228;

import java.io.Serializable;
import org.omg.CORBA_2_3.portable.InputStream;

/*
 *        JacORB  - a free Java ORB
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

/**
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 */
public class SampleDefaultFactory implements SampleValueFactory
{

    public Sample init_1()
    {
        return new SampleIm();
    }

    public Sample init_2(int alpha, double beta, String gamma)
    {
        Sample result = new SampleIm();
        result.alpha = alpha;
        result.beta = beta;
        result.gamma = gamma;
        return result;
    }

    public Serializable read_value(InputStream is)
    {
        Sample result = new SampleIm();
        result._read (is);
        return result;
    }

}
