package org.jacorb.notification.node;

import org.omg.CORBA.Any;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.evaluate.EvaluationException;

/*
 *        JacORB - a free Java ORB
 */

/**
 * Operator.java
 *
 *
 * Created: Thu Oct 24 11:31:29 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public interface Operator {
    public Any evaluateImplicitOperator(DynamicEvaluator evaluator, Any value) throws EvaluationException;
}
