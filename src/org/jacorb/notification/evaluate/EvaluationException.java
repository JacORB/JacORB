package org.jacorb.notification.evaluate;

import java.lang.Exception;



/**
 * EvaluationException.java
 *
 *
 * Created: Thu Sep 26 14:44:25 2002
 *
 * @author <a href="mailto:a.bendt@berlin.de">Alphonse Bendt</a>
 * @version
 */

public class EvaluationException extends Exception {
    public EvaluationException() {
	super();
    }

    public EvaluationException(String description) {
	super(description);
    }
}// EvaluationException
