package org.jacorb.notification;

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

import java.util.Hashtable;
import java.util.Map;

import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.interfaces.AbstractPoolable;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.node.EvaluationResult;
import org.omg.CORBA.Any;
import org.omg.DynamicAny.DynAnyFactory;

/**
 * EvaluationContext.java
 *
 *
 * Created: Sat Nov 30 16:02:34 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EvaluationContext extends AbstractPoolable {

    private DynamicEvaluator dynamicEvaluator_;
    private ResultExtractor resultExtractor_;
    private Message event_;
    private Map resultCache_;
    private Map anyCache_;

    public EvaluationContext() {
        resultCache_ = new Hashtable();
        anyCache_ = new Hashtable();
    }

    public void reset() {
        resultCache_.clear();
        anyCache_.clear();
    }

    public void setDynamicEvaluator(DynamicEvaluator e) {
        dynamicEvaluator_ = e;
    }

    public void setResultExtractor(ResultExtractor r) {
        resultExtractor_ = r;
    }

    public DynamicEvaluator getDynamicEvaluator() {
        return dynamicEvaluator_;
    }

    public ResultExtractor getResultExtractor() {
        return resultExtractor_;
    }

    public Message getNotificationEvent() {
        return event_;
    }

    public void setEvent(Message event) {
        event_ = event;
    }

    public void storeResult(String name, EvaluationResult value) {
        resultCache_.put(name, value);
    }

    public EvaluationResult lookupResult(String name) {
        return (EvaluationResult)resultCache_.get(name);
    }

    public void eraseResult(String name) {
        resultCache_.remove(name);
    }

    public void storeAny(String name, Any any) {
        anyCache_.put(name, any);
    }

    public Any lookupAny(String name) {
        return (Any)anyCache_.get(name);
    }

    public void eraseAny(String name) {
        anyCache_.remove(name);
    }

}// EvaluationContext
