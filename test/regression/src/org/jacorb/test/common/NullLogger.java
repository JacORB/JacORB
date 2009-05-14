package org.jacorb.test.common;

import org.slf4j.helpers.MarkerIgnoringBase;

public class NullLogger 
    extends MarkerIgnoringBase
{
    private static final long serialVersionUID = 2251550968341634012L;

    public void debug(String arg0)
    {
        // default empty implementation
    }

    public void debug(String arg0, Object arg1)
    {
        // default empty implementation
    }

    public void debug(String arg0, Object[] arg1)
    {
        // default empty implementation
    }

    public void debug(String arg0, Throwable arg1)
    {
        // default empty implementation
    }

    public void debug(String arg0, Object arg1, Object arg2)
    {
        // default empty implementation
    }

    public void error(String arg0)
    {
        // default empty implementation
    }

    public void error(String arg0, Object arg1)
    {
        // default empty implementation
    }

    public void error(String arg0, Object[] arg1)
    {
        // default empty implementation
    }

    public void error(String arg0, Throwable arg1)
    {
        // default empty implementation
    }

    public void error(String arg0, Object arg1, Object arg2)
    {
        // default empty implementation
    }

    public void info(String arg0)
    {
        // default empty implementation
    }

    public void info(String arg0, Object arg1)
    {
        // default empty implementation
    }

    public void info(String arg0, Object[] arg1)
    {
        // default empty implementation
    }

    public void info(String arg0, Throwable arg1)
    {
        // default empty implementation
    }

    public void info(String arg0, Object arg1, Object arg2)
    {
        // default empty implementation
    }

    public boolean isDebugEnabled()
    {
        return true;
    }

    public boolean isErrorEnabled()
    {
        return true;
    }

    public boolean isInfoEnabled()
    {
        return false;
    }

    public boolean isTraceEnabled()
    {
        return isDebugEnabled();
    }

    public boolean isWarnEnabled()
    {
        return true;
    }

    public void trace(String arg0)
    {
        // default empty implementation
    }

    public void trace(String arg0, Object arg1)
    {
        // default empty implementation
    }

    public void trace(String arg0, Object[] arg1)
    {
        // default empty implementation
    }

    public void trace(String arg0, Throwable arg1)
    {
        // default empty implementation
    }

    public void trace(String arg0, Object arg1, Object arg2)
    {
        // default empty implementation
    }

    public void warn(String arg0)
    {
        // default empty implementation
    }

    public void warn(String arg0, Object arg1)
    {
        // default empty implementation
    }

    public void warn(String arg0, Object[] arg1)
    {
        // default empty implementation
    }

    public void warn(String arg0, Throwable arg1)
    {
        // default empty implementation
    }

    public void warn(String arg0, Object arg1, Object arg2)
    {
        // default empty implementation
    }
}
