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
import org.jacorb.notification.interfaces.Message;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class DomainNameShorthandNode
    extends ETCLComponentName {

    static AbstractTCLNode expandedPath_;
    static final String COMP_NAME = "$.header.fixed_header.event_type.domain_name";
    public static final String SHORT_NAME = "domain_name";

    static {
        try {
            expandedPath_ = TCLParser.parse( COMP_NAME );
            expandedPath_.acceptInOrder( new TCLCleanUp() );
        } catch (Exception e) {
        }
    }

    public DomainNameShorthandNode() {
        setName("DomainNameShorthandNode");
    }

    public String getComponentName() {
        return COMP_NAME;
    }

    public void acceptInOrder(AbstractTCLVisitor v) {

    }

    public void acceptPostOrder(AbstractTCLVisitor v) {

    }

    public void acceptPreOrder(AbstractTCLVisitor v) {

    }

    public EvaluationResult evaluate( EvaluationContext context )
        throws EvaluationException {

        Message _event = context.getCurrentMessage();

        EvaluationResult _result = new EvaluationResult();

        switch (_event.getType()) {
        case Message.TYPE_ANY:
            _result = expandedPath_.evaluate(context);
            break;
        case Message.TYPE_STRUCTURED:
            String _domainName = _event.toStructuredEvent().header.fixed_header.event_type.domain_name;
            _result.setString(_domainName);
            break;
        default:
            throw new RuntimeException();
        }

        return _result;
    }

    public String toString() {
        return COMP_NAME;
    }

}
