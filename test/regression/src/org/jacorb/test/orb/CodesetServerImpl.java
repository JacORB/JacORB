package org.jacorb.test.orb;

import java.util.Arrays;

import org.jacorb.test.CodesetServerPOA;


/**
 * <code>CodesetServerImpl</code> is the server implementation for testing
 * codesets.
 * Chars, wchars, strings and wstrings will be passed in here with codesets
 * turned on and off. The first parameter denotes the static field within
 * the CodesetTest class - this may be used to retrieve the value to match
 * against. Check CodesetTest::codesetEnabled for whether we expect the
 * values to match or not - will NOT match if
 * codeSet is disabled AND value if not in Latin-1
 *
 *
 * @author <a href="mailto:rnc@prismtechnologies.com"></a>
 * @version 1.0
 */
public class CodesetServerImpl extends CodesetServerPOA
{
    /**
     * <code>pass_in_char</code> is the implementation for CodesetServer.
     *
     * @param name a <code>String</code> representing the variable name of the
     * character as defined in CodesetTest.
     * @param x a <code>char</code>, the transmitted value.
     * @return a <code>boolean</code>, denoting whether the transmitted value is
     * equal to the original.
     */
    public boolean pass_in_char(String name, char x)
    {
        boolean result = false;
        try
        {
            char value = CodesetTest.class.getField( name ).getChar(null);

            if( x == value )
            {
                result = true;
            }
        }
        catch( NoSuchFieldException e )
        {
            throw new RuntimeException( "Incorrect name for variable: " + e);
        }
        catch( IllegalAccessException e )
        {
            throw new RuntimeException( "Internal error - failed to get field: " + e);
        }
        return result;
    }

    /**
     * <code>pass_in_string</code> is the implementation for CodesetServer.
     *
     * @param name See {@link CodesetServerImpl#pass_in_char(String,char)}
     * @param x a <code>String</code> the transmitted value.
     * @return See {@link CodesetServerImpl#pass_in_char(String,char)}
     */
    public boolean pass_in_string(String name, String x)
    {
        boolean result = false;
        try
        {
            String value = (String)CodesetTest.class.getField( name ).get( null );

            if( value != null && value.equals( x ) )
            {
                result = true;
            }
        }
        catch( NoSuchFieldException e )
        {
            throw new RuntimeException( "Incorrect name for variable: " + e);
        }
        catch( IllegalAccessException e )
        {
            throw new RuntimeException( "Internal error - failed to get field: " + e);
        }
        return result;
    }

    /**
     * <code>pass_in_wchar</code> is the implementation for CodesetServer.
     *
     * @param name See {@link CodesetServerImpl#pass_in_char(String,char)}
     * @param x a <code>char</code>, the transmitted value.
     * @return See {@link CodesetServerImpl#pass_in_char(String,char)}
     */
    public boolean pass_in_wchar(String name, char x)
    {
        throw new RuntimeException( "NYI");
    }

    /**
     * <code>pass_in_wstring</code>  is the implementation for CodesetServer.
     *
     * @param name See {@link CodesetServerImpl#pass_in_char(String,char)}
     * @param x a <code>String</code> the transmitted value.
     * @return See {@link CodesetServerImpl#pass_in_char(String,char)}
     */
    public boolean pass_in_wstring(String name, String x)
    {
        throw new RuntimeException( "NYI");
    }


    /**
     * <code>pass_in_char_array</code> is the implementation for CodesetServer.
     *
     * @param name See {@link CodesetServerImpl#pass_in_char(String,char)}
     * @param x a <code>char[]</code>, the transmitted value.
     * @return See {@link CodesetServerImpl#pass_in_char(String,char)}
     */
    public boolean pass_in_char_array(String name, char[] x)
    {
        boolean result = false;
        try
        {
            if ( ! name.equals("multibyte"))
            {
                char[] value = (char[])CodesetTest.class.getField( name ).get(null);

                if( Arrays.equals( x, value ) )
                {
                    result = true;
                }
            }
            else
            {
                // Special handling for multibyte char array where bytes
                // are in char[0...n]
                byte []temp =
                    (new String(new char []{CodesetTest.E_ACUTE})).getBytes("UTF-8");
                char []topass = new char[2];
                topass[0]=(char)(temp[0] & 0xFF);
                topass[1]=(char)(temp[1] & 0xFF);

                if (x.length == 2 && x[0] == topass[0] && x[1] == topass[1])
                {
                    result = true;
                }
            }
        }
        catch( NoSuchFieldException e )
        {
            throw new RuntimeException( "Incorrect name for variable: " + e);
        }
        catch( IllegalAccessException e )
        {
            throw new RuntimeException( "Internal error - failed to get field: " + e);
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            throw new RuntimeException( "Internal error - encoding issue " + e);
        }
        return result;
    }
}
