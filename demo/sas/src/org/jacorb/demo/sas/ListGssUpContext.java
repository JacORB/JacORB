package org.jacorb.demo.sas;

import org.jacorb.security.sas.GssUpContext;
import org.omg.PortableInterceptor.ServerRequestInfo;

public final class ListGssUpContext extends GssUpContext
{
    private final String[][] auth_data = { { "jay", "test"} };
    
    public boolean validateContext(ServerRequestInfo ri, byte[] contextToken)
    {
        boolean b = super.validateContext(ri, contextToken);
        if (b)
        {
            return validateUsernamePassword(initialContextToken.username, initialContextToken.password);
        }
        return b;
    }
    
    private boolean validateUsernamePassword(byte[] uname, byte[] pswd)
    {
        System.out.println("validating...");
        
        // Get username
        StringBuffer ubuff = new StringBuffer();
        for (int i=0; i < uname.length; i++) ubuff.append((char)uname[i]);
        String username = ubuff.toString();
        
        // Get Password
        StringBuffer buff = new StringBuffer();
        for (int i=0; i < pswd.length; i++) buff.append((char)pswd[i]);
        String password = buff.toString();
        
        System.out.println("---------> " + username + ", " + password);
        
        // Verify versus cached data
        boolean valid = false;
        for (int i=0; i < auth_data.length; i++)
        {
            if (auth_data[i][0].equals(username))
            {
                if (auth_data[i][1].equals(password)) valid = true;
                break;
            }
        }
        return valid;
    }
}