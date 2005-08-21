package org.jacorb.notification.filter.etcl;

/*
 *        JacORB - a free Java ORB
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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.filter.AbstractFilter;
import org.jacorb.notification.filter.FilterConstraint;
import org.jacorb.notification.interfaces.EvaluationContextFactory;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.InvalidConstraint;
import org.omg.PortableServer.POA;

/**
 * @jmx.mbean   name = "ETCLFilter"
 *              extends = "org.jacorb.notification.filter.AbstractFilterMBean"
 *              
 * @jboss.xmbean             
 *              
 * @author Alphonse Bendt
 * @version $Id$
 */
public class ETCLFilter extends AbstractFilter implements ETCLFilterMBean
{
    public final static String CONSTRAINT_GRAMMAR = "EXTENDED_TCL";

    public ETCLFilter(Configuration config, 
            EvaluationContextFactory evaluationContextFactory,
            MessageFactory messageFactory, 
            ORB orb, POA poa) throws ConfigurationException
    {
        super(config, evaluationContextFactory, messageFactory, orb, poa);
    }

    public FilterConstraint newFilterConstraint(ConstraintExp constraintExp)
            throws InvalidConstraint
    {
        return new ETCLFilterConstraint(constraintExp);
    }

    public String constraint_grammar()
    {
        return CONSTRAINT_GRAMMAR;
    }
}