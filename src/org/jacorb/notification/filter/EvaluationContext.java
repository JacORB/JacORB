package org.jacorb.notification.filter;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import org.jacorb.notification.AbstractMessage;
import org.jacorb.notification.filter.etcl.ETCLComponentName;
import org.jacorb.notification.filter.etcl.MessageUtils;
import org.jacorb.notification.interfaces.AbstractPoolable;
import org.jacorb.notification.interfaces.Message;

import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.Any;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.util.Debug;

/**
 * @todo remove the static dependeny to package filter.etcl.
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EvaluationContext extends AbstractPoolable
{
    Logger logger_ =  Debug.getNamedLogger(getClass().getName());

    private DynamicEvaluator dynamicEvaluator_;
    private Message event_;
    private Map resultCache_;
    private Map anyCache_;

    ////////////////////////////////////////

    public EvaluationContext()
    {
        resultCache_ = new HashMap();
        anyCache_ = new HashMap();
    }

    ////////////////////////////////////////

    public void reset()
    {
        resultCache_.clear();
        anyCache_.clear();
    }


    public void setDynamicEvaluator(DynamicEvaluator e)
    {
        dynamicEvaluator_ = e;
    }


    public DynamicEvaluator getDynamicEvaluator()
    {
        return dynamicEvaluator_;
    }


    public Message getCurrentMessage()
    {
        return event_;
    }


    public void setCurrentMessagea(Message event)
    {
        event_ = event;
    }


    public void storeResult(String name, EvaluationResult value)
    {
        resultCache_.put(name, value);
    }


    public EvaluationResult lookupResult(String name)
    {
        return (EvaluationResult)resultCache_.get(name);
    }


    public void eraseResult(String name)
    {
        resultCache_.remove(name);
    }


    public void storeAny(String name, Any any)
    {
        anyCache_.put(name, any);
    }


    public Any lookupAny(String name)
    {
        return (Any)anyCache_.get(name);
    }


    public void eraseAny(String name)
    {
        anyCache_.remove(name);
    }

    /**
     * resolve the RuntimeVariable (e.g. $curtime). then see if some
     * more work has to be done (e.g. $curtime._repos_id)
     */
    public EvaluationResult extractFromMessage(AbstractMessage m,
                                               EvaluationResult r,
                                               ComponentName componentName,
                                               RuntimeVariable runtimeVariable)
        throws EvaluationException
     {
        ETCLComponentName _componentName = (ETCLComponentName)componentName;

        if (_componentName.right() != null) {

            return MessageUtils.extractFromAny(_componentName.right(),
                                               r.getAny(),
                                               this,
                                               runtimeVariable.toString());
        } else {
            return r;
        }
     }

    /**
     * fetch the values denoted by the provided ComponentName out of
     * the Message.
     */
    public EvaluationResult extractFromMessage(AbstractMessage m,
                                               ComponentName componentRootNode)
        throws EvaluationException
    {
        ETCLComponentName _componentName = (ETCLComponentName)componentRootNode;

        return MessageUtils.extractFromAny(_componentName.left(),
                                           m.toAny(),
                                           this,
                                           _componentName.toString());
    }
}

