package demo.ac;

import jacorb.orb.domain.*;
/**
 * DummyResolver.java
 *
 *
 * Created: Thu Jul 13 10:31:23 2000
 *
 * @author Nicolas Noffke
 * @version
 */

public class DummyResolver 
    extends ACConflictResolver 
{
    public static String resolve(Domain start)
    {
        return "AccessDenied";
    }
} // DummyResolver
