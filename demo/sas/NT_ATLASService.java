package demo.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import java.util.Iterator;

import org.omg.PortableServer.*;
import org.omg.CORBA.ORB;
import org.omg.ATLAS.*;
import org.omg.CSI.*;
import org.omg.CosNaming.*;
import org.omg.TimeBase.*;

import org.jacorb.util.*;
import org.jacorb.orb.*;

import java.security.Principal;
import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;
import com.tagish.auth.*;
import com.tagish.auth.win32.*;
import org.ietf.jgss.*;

/**
 * This is a reference implementation of the ATLAS service.
 * It is designed to return NT credentials
 *
 * @author Nicolas Noffke, Sebastian Müller, Steve Osselton
 */

class NT_ATLASService extends AuthTokenDispenserPOA implements CallbackHandler
{
    public static final int AUTHTYPE_NT_User_Principal     = ORBConstants.JACORB_ORB_ID | 0x01;
    public static final int AUTHTYPE_NT_Group_Principal    = ORBConstants.JACORB_ORB_ID | 0x02;
    public static final int AUTHTYPE_NT_Domain_Principal   = ORBConstants.JACORB_ORB_ID | 0x03;
    public static final int AUTHTYPE_NT_Unknown_Principal  = ORBConstants.JACORB_ORB_ID | 0x04;

    private org.jacorb.orb.ORB orb = null;
    private POA rootPOA = null;
    private POA atlasPOA = null;
    private POAManager poaMgr = null;

    private LoginContext lc = null;
    private org.omg.GSSUP.InitialContextToken initialContextToken = null;

    /**
     * Server main line
     */

    public static void main (String[] args)
    {
        //boolean dynamic = false;

        if (args.length < 2 || args.length > 2)
        {
            usage ();
        }

        java.util.Properties props = new java.util.Properties ();

        props.put ("OAPort", args[0]);
        //props.put ("org.omg.PortableInterceptor.ORBInitializerClass.org.jacorb.proxy.ProxyServerInitializer", "");
        props.put ("jacorb.implname", "ATLAS");

        ORB orb = org.omg.CORBA.ORB.init (args, props);

        NT_ATLASService authTokenDispenserImpl = new NT_ATLASService (orb, args[1]);

        orb.run ();
    }

    private static void usage ()
    {
        System.err.println ("usage: NT_ATLASService <port> <IOR-File>");
        System.exit (1);
    }

    public NT_ATLASService (ORB orb, String file)
    {
        this.orb = (org.jacorb.orb.ORB) orb;
        try
        {
            // get the root POA
            org.omg.CORBA.Object obj = orb.resolve_initial_references ("RootPOA");
            rootPOA = POAHelper.narrow (obj);
            poaMgr = rootPOA.the_POAManager ();

            // create the permanent POA
            org.omg.CORBA.Policy[] policies = new org.omg.CORBA.Policy[3];
            policies[0] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
            policies[1] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
            policies[2] = rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY);
            atlasPOA = rootPOA.create_POA ("ATLAS", poaMgr, policies);
            atlasPOA.activate_object_with_id("Dispenser".getBytes(), this);

            poaMgr.activate ();
        }
        catch (Exception ex)
        {
            Debug.output (1, "Unexpected exception while initializing Proxy: " + ex);
            System.exit (1);
        }

        try
        {
            java.io.FileWriter fout = new java.io.FileWriter (file);
            fout.write (orb.object_to_string (atlasPOA.servant_to_reference(this)));
            fout.close ();
        }
        catch (Exception e)
        {
            Debug.output (1, "Could not write IOR File: " + file + ": " + e);
        }

        // See if configured to register in name service
        String name = Environment.getProperty ("jacorb.ATLASServer.Name", "");
        if (name.length () > 0)
        {
            NamingContextExt nc = null;
            try
            {
                org.omg.CORBA.Object obj = orb.resolve_initial_references ("NameService");
                nc = NamingContextExtHelper.narrow (obj);
            }
            catch (org.omg.CORBA.ORBPackage.InvalidName ex)
            {
                Debug.output (2, ex);
            }

            if (nc == null)
            {
                Debug.output (1, "Name service not present. Trying without");
            }
            else
            {
                try
                {
                    nc.rebind (nc.to_name (name), atlasPOA.servant_to_reference(this));
                }
                catch (org.omg.CORBA.UserException ex)
                {
                    // Should not happen
                }
                catch (org.omg.CORBA.SystemException ex)
                {
                    // Server not actually running or otherwise unavailable

                    Debug.output (1, "Failed to register with name server");
                }
            }
        }

        try
        {
            // create my identity
            GSSManager gssManager = org.jacorb.security.sas.TSSInitializer.gssManager;
            Oid myMechOid = new Oid(org.omg.GSSUP.GSSUPMechOID.value.replaceFirst("oid:", ""));
            GSSName myName = gssManager.createName("".getBytes(), GSSName.NT_ANONYMOUS, myMechOid);
            GSSCredential myCred = gssManager.createCredential(myName, GSSCredential.DEFAULT_LIFETIME, myMechOid, GSSCredential.ACCEPT_ONLY);
            org.jacorb.security.sas.TSSInvocationInterceptor.setMyCredential(myCred);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        // use the configured LoginModules for the "Login" entry
        try
        {
            lc = new LoginContext("NTLogin", this);
        }
        catch (LoginException le)
        {
            le.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Invoke an array of Callbacks.
     *
     * <p>
     *
     * @param callbacks an array of <code>Callback</code> objects which contain
     *			the information requested by an underlying security
     *			service to be retrieved or displayed.
     *
     * @exception java.io.IOException if an input or output error occurs. <p>
     *
     * @exception UnsupportedCallbackException if the implementation of this
     *			method does not support one or more of the Callbacks
     *			specified in the <code>callbacks</code> parameter.
     */
    public void handle(Callback[] callbacks) throws java.io.IOException, UnsupportedCallbackException
    {
        for (int i = 0; i < callbacks.length; i++)
        {
            if (callbacks[i] instanceof TextOutputCallback)
            {
            //	TextOutputCallback toc = (TextOutputCallback)callbacks[i];
            //	switch (toc.getMessageType())
            //	{
            //	case TextOutputCallback.INFORMATION:
            //		System.out.println(toc.getMessage());
            //		break;
            //	case TextOutputCallback.ERROR:
            //		System.out.println("ERROR: " + toc.getMessage());
            //		break;
            //	case TextOutputCallback.WARNING:
            //		System.out.println("WARNING: " + toc.getMessage());
            //		break;
            //	default:
            //		throw new IOException("Unsupported message type: " + toc.getMessageType());
            //	}
            }
            else if (callbacks[i] instanceof NameCallback)
            {
                // prompt the user for a username
                NameCallback nc = (NameCallback) callbacks[i];
                nc.setName(new String(initialContextToken.username));
            }
            else if (callbacks[i] instanceof TextInputCallback)
            {
                // prompt the user for a username
                TextInputCallback tc = (TextInputCallback) callbacks[i];
                tc.setText(new String(initialContextToken.target_name));
            }
            else if (callbacks[i] instanceof PasswordCallback)
            {
                // prompt the user for sensitive information
                PasswordCallback pc = (PasswordCallback)callbacks[i];
                pc.setPassword(new String(initialContextToken.password).toCharArray());
            }
            else
            {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }

    public synchronized AuthTokenData get_my_authorization_token() throws IllegalTokenRequest
    {
        // parse who I am
        try
        {
            org.omg.PortableInterceptor.Current current = (org.omg.PortableInterceptor.Current) orb.resolve_initial_references( "PICurrent" );
            org.omg.CORBA.Any any = current.get_slot( org.jacorb.security.sas.TSSInitializer.sourceNameSlotID );
            initialContextToken = org.jacorb.security.sas.GSSUPNameSpi.decode(any.extract_string().getBytes());
        }
        catch (Exception e)
        {
            Debug.output(1, "Error parsing Initial Context Token: " + e);
            throw new IllegalTokenRequest(100, "Error parsing Initial Context Token: " + e);
        }

        // do login and get principals
        try
        {
            // attempt authentication
            lc.login();
        }
        catch (AccountExpiredException aee)
        {
            Debug.output(1, "Your account has expired. Please notify your administrator.");
            throw new IllegalTokenRequest(100, "Your account has expired. Please notify your administrator.");
        }
        catch (CredentialExpiredException cee)
        {
            Debug.output(1, "Your credentials have expired.");
            throw new IllegalTokenRequest(100, "Your credentials have expired.");
        }
        catch (FailedLoginException fle)
        {
            Debug.output(1, "Authentication Failed");
            throw new IllegalTokenRequest(100, "Authentication Failed");
        }
        catch (Exception e)
        {
            Debug.output(1, "Unexpected Exception - unable to continue: " + e);
            e.printStackTrace();
            throw new IllegalTokenRequest(100, "Unexpected Exception - unable to continue");
        }

        // create return data
        AuthTokenData data = new AuthTokenData();
        data.ident_token = new IdentityToken[0];
        data.expiry_time = new UtcT[0];

        // build list of principals
        Iterator principalIterator = lc.getSubject().getPrincipals().iterator();
        data.auth_token = new AuthorizationElement[lc.getSubject().getPrincipals().size()];
        for (int i = 0; principalIterator.hasNext(); i++)
        {
            NTPrincipal p = (NTPrincipal) principalIterator.next();
            switch (p.getType())
            {
            case NTPrincipal.USER:
              data.auth_token[i] = new AuthorizationElement(AUTHTYPE_NT_User_Principal, p.getName().getBytes());
              break;
            case NTPrincipal.GROUP:
              data.auth_token[i] = new AuthorizationElement(AUTHTYPE_NT_Group_Principal, p.getName().getBytes());
              break;
            case NTPrincipal.DOMAIN:
              data.auth_token[i] = new AuthorizationElement(AUTHTYPE_NT_Domain_Principal, p.getName().getBytes());
              break;
            case NTPrincipal.UNKNOWN:
              data.auth_token[i] = new AuthorizationElement(AUTHTYPE_NT_Unknown_Principal, p.getName().getBytes());
              break;
            }
        }

        // logout and return
        try
        {
            lc.logout();
        }
        catch (Exception e)
        {
            Debug.output(1, "Failed to logout");
        }

        return data;
    }

    public AuthTokenData translate_authorization_token(IdentityToken the_subject, AuthorizationElement[] the_token) throws IllegalTokenRequest,TokenOkay
    {
        throw new IllegalTokenRequest(100, "Operation not supported");
    }
}