package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 */

/**
 * FilterUtils.java
 *
 *
 * Created: Fri Nov 01 17:19:54 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class FilterUtils {

    public static String calcConstraintKey(String domain, String type) {
	return domain + "__%%__" + type;
    }

    
}// FilterUtils
