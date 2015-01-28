/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

package org.jacorb.tao_imr;

import java.util.List;

import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.orb.util.CorbaLoc;
import org.jacorb.tao_imr.ImplementationRepository.ServerObject;
import org.jacorb.tao_imr.ImplementationRepository.ServerObjectHelper;
import org.jacorb.tao_imr.ImplementationRepository.ServerObjectImpl;
import org.omg.CORBA.INTERNAL;
import org.omg.ETF.Profile;

/**
 * @author Quynh Nguyen
 *
 * This ImR adapter class contains hooks for a JacORB to register and
 * unregister with a TAO ImR.
 */
public class ImRAccessImpl
    implements org.jacorb.orb.ImRAccess
{
    private org.jacorb.tao_imr.ImplementationRepository.Administration imrLocator = null;
    private org.jacorb.orb.ORB orb_ = null;
    private org.jacorb.poa.POA poa_ = null;
    private org.jacorb.poa.POA root_poa_ = null;

    private ServerObjectImpl serverObjImpl = null;
    private ParsedIOR pior = null;
    private IIOPProfile profile = null;
    private String corbaloc = null;
    //private ImRInfo info = null;

    /**
     * <code>ImRAccessImpl</code> private; use the static connect method.
     */
    private ImRAccessImpl ()
    {
        // use the static connect method
    }

   /**
    * <code>connect</code> resolves the IMR and returns a new ImRAccessImpl.
    *
    * @param orb an <code>org.omg.CORBA.ORB</code> value
    * @return an <code>ImRAccessImpl</code> value
    */
    public static ImRAccessImpl connect (org.jacorb.orb.ORB orb)
    {
        final ImRAccessImpl result = new ImRAccessImpl ();

        result.orb_ = orb;

        try
        {
            result.imrLocator =
                    org.jacorb.tao_imr.ImplementationRepository.AdministrationHelper.narrow (
                            orb.resolve_initial_references("ImplRepoService"));
        }
        catch (org.omg.CORBA.ORBPackage.InvalidName e)
        {
            throw new INTERNAL ("ImRAccessImpl.connect: unable to resolve TAO ImplRepoService: " + e.toString());
        }

        boolean non_exist = true;
        if (result.imrLocator != null)
        {
            try
            {
                non_exist = result.imrLocator._non_existent ();
            }
            catch (org.omg.CORBA.SystemException e)
            {
                non_exist = true;
            }
        }

        if (non_exist)
        {
            throw new INTERNAL ("ImRAccessImpl.connect: Unable to resolve reference to TAO ImplRepoService");
        }

        result.setImRInfo();
        return result;
    }

    /**
     * This function setup all informations that will be needed later
     */
    private void setImRInfo ()
    {
        try
        {
            org.omg.CORBA.Object ref = this.orb_.resolve_initial_references("ImplRepoService");
            this.pior = new ParsedIOR(this.orb_,
                                    this.orb_.object_to_string (ref));

            this.corbaloc = CorbaLoc.generateCorbalocForMultiIIOPProfiles (this.orb_, ref);
            this.profile = (IIOPProfile) this.pior.getEffectiveProfile();
        }
        catch (org.omg.CORBA.ORBPackage.InvalidName e)
        {
            throw new INTERNAL ("ImRAccessImpl.setImRInfo: unable to resolve TAO ImplRepoService: " + e.toString());
        }
    }

    /**
     *
     * @return the primary address of the ImR of type org.jacorb.orb.etf.ProtocolAddressBase
     */
    @Override
    public org.jacorb.orb.etf.ProtocolAddressBase getImRAddress ()
    {
        if (this.profile == null)
        {
            setImRInfo();
        }
        return this.profile.getAddress();
    }

    /**
     *
     * @return a list of the ImR profiles of type <code>List&lt;Profile&gt;</code>
     */
    @Override
    public List<Profile> getImRProfiles ()
    {
        if (this.profile == null)
        {
            setImRInfo();
        }
        return this.pior.getProfiles();
    }

    /**
     * This function is just a place holder.
     * @return
     */
    @Override
    public String getImRHost ()
    {
        return null;
    }

    /**
     * This function is just a place holder.
     * @return
     */
    @Override
    public int getImRPort ()
    {
        return -1;
    }

    /**
     * This function returns the TAO ImR's corbaloc IOR string
     * @return
     */
    @Override
    public String getImRCorbaloc ()
    {
        if (this.corbaloc == null)
        {
            setImRInfo();
        }
        return this.corbaloc;
    }

    /**
     * This function is just a place holder.
     */
    @Override
    public void registerPOA (String name,
                             String server,
                             org.jacorb.orb.etf.ProtocolAddressBase address)
        throws INTERNAL
    {
        // throw new INTERNAL("Not implemented");
    }

    /**
     * This function is just a place holder.
     */
    @Override
    public void registerPOA (String name,
                             String server,
                             String host,
                             int port)
        throws INTERNAL
    {
        // throw new INTERNAL("Not implemented");
    }

    /**
     * This function provides a hook for registering the POA upon being created.
     * @param orb
     * @param poa
     * @param address
     * @param implname
     * @throws INTERNAL
     */
    @Override
    public void registerPOA (
                            org.jacorb.orb.ORB orb,
                            org.jacorb.poa.POA poa,
                            org.jacorb.orb.etf.ProtocolAddressBase address,
                            String implname)
        throws INTERNAL
    {
        if (address instanceof IIOPAddress)
        {
            if (orb == null)
            {
                throw new INTERNAL ( "ImRAccessImpl.registerPOA: orb must not be null");
            }
            if (poa == null)
            {
                throw new INTERNAL ( "ImRAccessImpl.registerPOA: poa must not be null");
            }

            registerPOA_TaoImR_i (orb, poa, implname);
        }
        else
        {
            throw new INTERNAL ("ImRAccessImpl.registerPOA: TAO ImR only supports IIOP based POAs");
        }
    }

    /**
     * This function performs POA registration with the TAO ImR server hy
     * calling the TAO ImR locator's server_is_running function.
     *
     * @param orb
     * @param poa
     * @param implname
     */
    private void registerPOA_TaoImR_i (
                                org.jacorb.orb.ORB orb,
                                org.jacorb.poa.POA poa,
                                String implname)
    {
        this.orb_ = orb;
        this.poa_ = poa;

        // build the server name for registering with the ImR in the form
        // JACORB:<implName>/<qualified POA name>
        String theName = "JACORB:" + implname + "/" + this.poa_._getQualifiedName();

        try
        {
            // instantiate an instance of ServerObjectImpl.
            // org.jacorb.poa.POA root_poa = this.orb_.getRootPOA();
            this.root_poa_ = this.orb_.getRootPOA();
            this.serverObjImpl = new ServerObjectImpl (this.orb_, this.poa_, this.root_poa_);
            if (this.serverObjImpl == null)
            {
                throw new INTERNAL ("ImRAccessImpl.registerPOA_TaoImR_i: can't create an instance of ServerObjectImpl");
            }

            // activate it in rootPOA

            this.root_poa_.activate_object (this.serverObjImpl);
            org.omg.CORBA.Object objRef = this.root_poa_.servant_to_reference (this.serverObjImpl);
            String corbaLoc = CorbaLoc.generateCorbalocForMultiIIOPProfiles (this.orb_, objRef);
            int slash = corbaLoc.indexOf("/");

            // save the endpoint part and discard the object_key part
            String partialCorbaLoc = corbaLoc;
            if (slash > 0)
            {
                partialCorbaLoc = corbaLoc.substring(0, slash+1);
            }

            ServerObject svr = ServerObjectHelper.narrow(objRef);

            // notfy TAO ImR that we are running
            this.imrLocator.server_is_running (theName,
                                          partialCorbaLoc,
                                          svr);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new INTERNAL (
                    "ImRAccessImpl.registerPOA_TaoImR_i: got an exception while registering "
                    + theName + " with TAO ImR, " + e.toString());
        }
    }

    @Override
    public void setServerDown (String implname)
            throws INTERNAL
    {
    }

    /**
     * This function will unregister the server from the TAO ImR by calling
     * the TAO ImR locator's server_is_shutting_down function.
     * @param orb
     * @param poa
     * @param implname
     * @throws INTERNAL
     */
    @Override
    public void setServerDown ( org.jacorb.orb.ORB orb,
                                org.jacorb.poa.POA poa,
                                String implname)
        throws INTERNAL
    {
            if (this.imrLocator != null)
            {
                try
                {
                    // notify ImR of being shutdown
                    this.imrLocator.server_is_shutting_down (poa._getQualifiedName());
                }
                catch (Exception e1)
                {
                    // ignored
                    e1.printStackTrace();
                }

                // deactivate the ServerObjectImpl
                if (this.serverObjImpl != null)
                {
                    try
                    {
                        // get root_poa from the ServerObject
                        org.omg.PortableServer.POA root_poa = this.serverObjImpl._default_POA();
                        if (root_poa != null)
                        {
                            byte[] id =
                                root_poa.servant_to_id (this.serverObjImpl);
                            root_poa.deactivate_object (id);
                            this.serverObjImpl = null;
                        }
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                        // force to exit
                        System.exit(1);
                    }
                }
            }
    }
}
