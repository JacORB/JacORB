package org.jacorb.test.notification;

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
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.filter.ComponentName;
import org.jacorb.notification.filter.EvaluationResult;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyFilter.MappingFilter;

import java.util.Date;

import junit.framework.Assert;
import org.omg.CosNotifyFilter.Filter;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class MockMessage extends AbstractMessage {

    CallerResolver callerResolver = new CallerResolver();

    int type;
    int priority;
    Any any;
    StructuredEvent structuredEvent;
    String constraintKey;
    String name_;

    int refCount_;
    int refsExpected_;
    int referenceCalled_;
    int maxRef_;

    List refAdders = new ArrayList();

    public MockMessage() {
        super();
    }

    public MockMessage(String name) {
        super();
        name_ = name;
    }


    public void setName(String name) {
        name_ = name;
    }

    public String getConstraintKey() {
        return constraintKey;
    }

    public String toString() {
        return name_;
    }

    public Any toAny() {
        return any;
    }

    public void setAny(Any a) {
        any = a;
    }

    public StructuredEvent toStructuredEvent() {
        return structuredEvent;
    }

    public void setStructuredEvent(StructuredEvent event) {
        structuredEvent = event;
    }

    public int getType() {
        return type;
    }

    public void setType(int t) {
        type = t;
    }

    public EvaluationResult extractFilterableData(EvaluationContext context,
                                                  ComponentName compName,
                                                  String s) {
        return null;
    }

    public EvaluationResult extractVariableHeader(EvaluationContext context,
                                                  ComponentName compName,
                                                  String s) {
        return null;
    }

    public boolean match(FilterStage s) {
        return true;
    }

    public boolean match(MappingFilter m, AnyHolder r) {
        return true;
    }

    public boolean match(Filter filter) {
        return true;
    }

    public void validateRefCounter() {
        Assert.assertTrue("referenced: " + referenced_ + " callers: " + refAdders, referenced_ == 0);
        if (refsExpected_ != 0) {
            Assert.assertTrue("refs expected: " + refsExpected_, refsExpected_ == referenceCalled_);
        }
    }


    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int p) {
        priority = p;
    }

    public boolean hasStartTime() {
        return false;
    }

    public Date getStartTime() {
        return null;
    }

    public boolean hasStopTime() {
        return false;
    }

    public Date getStopTime() {
        return null;
    }

    public boolean hasTimeout() {
        return false;
    }

    public long getTimeout() {
        return -1;
    }

    /**
     * Describe <code>addReference</code> method here.
     *
     */
    public synchronized void addReference()
    {
        super.addReference();

        refAdders.add(callerResolver.makeStackTrace());

        referenceCalled_++;
        refCount_++;

        if (maxRef_ > 0 && refCount_ > maxRef_) {
            throw new RuntimeException(refCount_ + " > " + maxRef_);
        }
        logger_.info("Ref added");
    }

    /**
     * Describe <code>removeReference</code> method here.
     *
     */
    public synchronized void removeReference()
    {
        super.removeReference();
        refCount_--;
        logger_.info("Ref removed");
        //        throw new RuntimeException();
    }

    public void setMaxRef(int max) {
        maxRef_ = max;
    }

    public void setExpectedRef(int refsExpected) {
        refsExpected_ = refsExpected;
    }

}
