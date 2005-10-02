package org.jacorb.notification.filter.etcl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */

import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.util.Time;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosTime.TimeService;
import org.omg.CosTime.TimeUnavailable;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class CurrentTimeNode extends ETCLComponentName
{
    public static final String SHORT_NAME = "curtime";

    private static final String COMP_NAME = "$curtime";

    private final static ORB orb_ = ORB.init();

    private TimeService optionalTimeService_;

    public EvaluationResult evaluate(EvaluationContext context) throws EvaluationException
    {
        EvaluationResult _result = new EvaluationResult();

        UtcT _curtime = getCurrentTime();

        Any _curAny = orb_.create_any();

        UtcTHelper.insert(_curAny, _curtime);

        _result.addAny(_curAny);

        return _result;
    }

    private UtcT getCurrentTime()
    {
        try
        {
            if (optionalTimeService_ != null)
            {
                return optionalTimeService_.universal_time().utc_time();
            }
        } catch (TimeUnavailable e)
        {
            return retryGetCurrentTime();
        }

        return getLocalTime();
    }

    private UtcT getLocalTime()
    {
        return Time.corbaTime();
    }
    
    private UtcT retryGetCurrentTime()
    {
        optionalTimeService_ = null;
        return getLocalTime();
    }

    public String toString()
    {
        return COMP_NAME;
    }
}
