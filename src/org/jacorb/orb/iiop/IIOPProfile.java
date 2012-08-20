/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
package org.jacorb.orb.iiop;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.TaggedComponentList;
import org.jacorb.orb.etf.ProtocolAddressBase;
import org.omg.CORBA.INTERNAL;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.Confidentiality;
import org.omg.CSIIOP.DetectMisordering;
import org.omg.CSIIOP.DetectReplay;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.Integrity;
import org.omg.CSIIOP.TAG_TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANSHelper;
import org.omg.ETF.Profile;
import org.omg.IIOP.ListenPoint;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;
import org.omg.IOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedComponent;
import org.omg.SSLIOP.SSL;
import org.omg.SSLIOP.SSLHelper;
import org.omg.SSLIOP.TAG_SSL_SEC_TRANS;

/**
 * @author Andre Spiegel
 */
public class IIOPProfile
    extends org.jacorb.orb.etf.ProfileBase implements Cloneable
{
    private IIOPAddress  primaryAddress = null;
    private SSL ssl = null;
    private boolean isSSLSet;


    /** the following is used as a bit mask to check if any of these options are set */
    private static final int MINIMUM_OPTIONS = Integrity.value | Confidentiality.value | DetectReplay.value |
                                               DetectMisordering.value | EstablishTrustInTarget.value | EstablishTrustInClient.value;

    /**
     * flag to specify if we should check
     * components for alternate addresses.
     * @see #getAlternateAddresses()
     */
    private final boolean checkAlternateAddresses;

    private IIOPProfile(boolean checkAlternateAddresses)
    {
        super();
        this.checkAlternateAddresses = checkAlternateAddresses;
    }

    public IIOPProfile()
    {
        this(true);
    }


    public IIOPProfile(byte[] data)
    {
        this(true);

        initFromProfileData(data);
    }

    public IIOPProfile(IIOPAddress address, byte[] objectKey, int minor)
    {
        this(false);

        this.version        = new org.omg.GIOP.Version((byte)1,(byte)minor);
        this.primaryAddress = address;
        this.objectKey      = objectKey;
        this.components     = new TaggedComponentList();
    }

    /**
     * Constructs an IIOPProfile from a corbaloc URL.  Only to be used
     * from the corbaloc parser.
     */
    public IIOPProfile(String corbaloc)
    {
        this(false);

        this.version = null;
        this.primaryAddress = null;
        this.objectKey = null;
        this.components = null;
        this.corbalocStr = corbaloc;
    }

    public void configure(Configuration config)
        throws ConfigurationException
    {
        super.configure(config);

        logger = configuration.getLogger("jacorb.iiop.profile");

        if (primaryAddress != null)
        {
            primaryAddress.configure(configuration);
        }

        if (corbalocStr != null)
        {
            decode_corbaloc(corbalocStr);
        }

        addAlternateAddresses (configuration);
    }

    /**
     * An IPv6 corbaloc URL is of the format
     * corbaloc:iiop:[fe80:5443::3333%3]:2809/my_object
     * where the zone ID seperator is / or % depending on
     * what the underlying OS supports.
     *
     * This preserves compatilibility with TAO, and falls in
     * line with RFC 2732 and discussion on OMG news groups.
     */
    private void decode_corbaloc(final String address) throws ConfigurationException
    {
        String addr = address;
        String host = "127.0.0.1"; //default to localhost
        short port = 2809; // default IIOP port

        int major = 1; // should be 1 by default. see 13.6.10.3
        int minor = 0; // should be 0 by default. see 13.6.10.3

        String errorstr =
            "Illegal IIOP protocol format in object address format: " + addr;

        int sep = addr.indexOf(':');

        String protocol_identifier = "";
        if( sep != 0)
        {
            protocol_identifier = addr.substring(0, sep);
        }
        if( sep + 1 == addr.length())
        {
            throw new IllegalArgumentException(errorstr);
        }
        addr = addr.substring(sep + 1);
        // decode optional version number
        sep = addr.indexOf( '@' );
        if( sep > -1)
        {
            String ver_str =  addr.substring(0,sep);
            addr = addr.substring(sep+1);
            sep = ver_str.indexOf('.');
            if( sep != -1 )
            {
                try
                {
                    major = Integer.parseInt(ver_str.substring(0,sep));
                    minor = Integer.parseInt(ver_str.substring(sep+1));
                }
                catch( NumberFormatException nfe )
                {
                    throw new IllegalArgumentException(errorstr);
                }
            }
        }
        version = new org.omg.GIOP.Version((byte)major,(byte)minor);

        int ipv6SeperatorStart = -1;
        int ipv6SeperatorEnd = -1;
        ipv6SeperatorStart = addr.indexOf('[');
        if (ipv6SeperatorStart != -1)
        {
            ipv6SeperatorEnd = addr.indexOf(']');
            if (ipv6SeperatorEnd == -1)
            {
                throw new IllegalArgumentException(errorstr);
            }
        }

        sep = addr.indexOf(':');
        if( sep != -1 )
        {
            if (ipv6SeperatorStart != -1) //IPv6
            {
                host=addr.substring(ipv6SeperatorStart + 1, ipv6SeperatorEnd);
                if (addr.charAt(ipv6SeperatorEnd+1) == ':')
                {
                    port=(short)Integer.parseInt(addr.substring(ipv6SeperatorEnd+2));
                }
                else
                {
                    throw new IllegalArgumentException(errorstr);
                }
            }
            else //IPv4 or hostname
            {
                try
                {
                    port =(short)Integer.parseInt(addr.substring(sep+1));
                    host = addr.substring(0, sep);
                }
                catch( NumberFormatException ill )
                {
                    throw new IllegalArgumentException(errorstr);
                }
            }
        }
        primaryAddress = new IIOPAddress(host,port);
        primaryAddress.configure(configuration);

        decode_extensions(protocol_identifier.toLowerCase());
    }

    private void decode_extensions(String ident) throws ConfigurationException
    {
        this.components = new TaggedComponentList();
        if (ident.equals("ssliop"))
        {
            ssl = new SSL();
            ssl.port = (short)primaryAddress.getPort();
            ssl.target_supports = get_ssl_options("jacorb.security.ssl.corbaloc_ssliop.supported_options");
            ssl.target_requires = get_ssl_options("jacorb.security.ssl.corbaloc_ssliop.required_options");

            isSSLSet = true;

            //create the tagged component containing the ssl struct
            final CDROutputStream out = new CDROutputStream();
            try
            {
                out.beginEncapsulatedArray();
                SSLHelper.write( out, ssl );

                // TAG_SSL_SEC_TRANS must be disambiguated in case OpenORB-generated
                // OMG classes are in the classpath.
                components.addComponent
                (new TaggedComponent( org.omg.SSLIOP.TAG_SSL_SEC_TRANS.value,
                        out.getBufferCopy() )
                );
            }
            finally
            {
                out.close();
            }
        }
    }

    private short get_ssl_options(String propname) throws ConfigurationException
    {
        //For the time being, we only use EstablishTrustInTarget,
        //because we don't handle any of the other options anyway.
        // So this makes a reasonable default.

        short value =
            (short)configuration.getAttributeAsInteger(propname,EstablishTrustInTarget.value);
        return value;
    }

    /**
     * Adds further addresses to this profile as TAG_ALTERNATE_IIOP_ADDRESS,
     * if this has been configured in the Configuration.
     */
    private void addAlternateAddresses (org.jacorb.config.Configuration config) throws ConfigurationException
    {
        String value = config.getAttribute ("jacorb.iiop.alternate_addresses", null);
        if (value == null)
        {
            return;
        }
        else if (value.trim().equals ("auto"))
        {
            addNetworkAddresses();
        }
        else
        {
            List addresses = Arrays.asList (value.split(","));
            if (!addresses.isEmpty() && components == null)
            {
                components = new TaggedComponentList();
            }
            for (Iterator i = addresses.iterator(); i.hasNext();)
            {
                String addr = (String)i.next();
                IIOPAddress iaddr = new IIOPAddress();
                iaddr.configure (config);
                if (!iaddr.fromString (addr))
                {
                    logger.warn ("could not decode " + addr +
                                 " from jacorb.iiop.alternate_addresses");
                    continue;
                }
                else
                {
                    components.addComponent (TAG_ALTERNATE_IIOP_ADDRESS.value,
                                             iaddr.toCDR());
                }
            }
        }
    }

    /**
     * Adds all the network addresses of this machine to the profile
     * as TAG_ALTERNATE_IIOP_ADDRESS.  This excludes loopback addresses,
     * and the address that is already used as the primary address.
     * Also excluded are non-IPv4 addresses for the moment.
    * @throws ConfigurationException
     */
    private void addNetworkAddresses() throws ConfigurationException
    {
        if (primaryAddress == null) return;
        if (components == null) components = new TaggedComponentList();
        try
        {
            for (NetworkInterface ni :
                 Collections.list (NetworkInterface.getNetworkInterfaces()))
            {
                for (InetAddress addr :
                     Collections.list (ni.getInetAddresses()))
                {
                    if (!addr.isLoopbackAddress() &&
                        !addr.getHostAddress().equals (primaryAddress.getIP()))
                    {
                        IIOPAddress iaddr = new IIOPAddress();
                        iaddr.configure (configuration);
                        if (addr instanceof Inet4Address)
                        {
                            iaddr.fromString (addr.toString().substring(1) + ":"
                                              + primaryAddress.getPort());
                        }
                        else if (addr instanceof Inet6Address)
                        {
                            iaddr.fromString (
                               "[" + addr.toString().substring(1) + "]:"
                               + primaryAddress.getPort()
                            );
                        }
                        components.addComponent (TAG_ALTERNATE_IIOP_ADDRESS.value,
                                                 iaddr.toCDR());
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            logger.warn ("could not get network interfaces, will not add addresses");
        }
    }

    /**
    * Writes the bytes that would make up the ETF::AddressProfile bytes (new spec)
    * to a stream.
    * <p>
    * Writes GIOP version, host string, and port.
    */
    public void writeAddressProfile(CDROutputStream addressProfileStream)
    {
        org.omg.GIOP.VersionHelper.write( addressProfileStream, version);
        primaryAddress.write (addressProfileStream);
    }

    /**
    * Reads the bytes that make up the ETF::AddressProfile bytes (new spec)
    * from a stream.
    * <p>
    * Writes GIOP version, host string, and port.
    */
    public void readAddressProfile(CDRInputStream addressProfileStream)
    {
        this.version = org.omg.GIOP.VersionHelper.read(addressProfileStream);

        String hostname = addressProfileStream.read_string();
        short port = addressProfileStream.read_ushort();

        this.primaryAddress = new IIOPAddress (hostname, port);

        if (configuration != null)
        {
           try
           {
              primaryAddress.configure(configuration);
           }
           catch (ConfigurationException ex)
           {
              logger.error ("Error configuring address", ex);
              throw new INTERNAL ("Error configuring address" + ex);
           }
        }
    }

    /**
     * To improve the management of a large set of profile instances,
     * the author may provide a hash function using the data in a Profile
     * instance. The Profile shall always implement this function and either
     * return a hash number, or 0 (zero) if no hashing is supported.
     */
    public int hash()
    {
        return hashCode();
    }

    public Object clone() throws CloneNotSupportedException
    {
        IIOPProfile result = (IIOPProfile)super.clone();  // bitwise copy

        result.primaryAddress = new IIOPAddress(primaryAddress.getHostname(),
                                                primaryAddress.getPort());

        if (configuration != null)
        {
           try
           {
              result.primaryAddress.configure(configuration);
           }
           catch (ConfigurationException ex)
           {
              logger.error ("Error configuring address", ex);
              throw new INTERNAL ("Error configuring address" + ex);
           }
        }

        result.version = new org.omg.GIOP.Version(this.version.major,
                                                   this.version.minor);

        if (this.objectKey != null)
        {
            result.objectKey = new byte [this.objectKey.length];
            System.arraycopy(this.objectKey, 0, result.objectKey, 0,
                              this.objectKey.length);
        }

        if (this.components != null)
        {
            result.components = (TaggedComponentList)this.components.clone();
        }

        return result;
    }

    /**
     * This function shall determine if the passed profile, prof, is a match
     * to this profile.  The specifics of the match are left to the details
     * of the underlying transport, however profiles shall be considered a
     * match, if they would create connections that share the same attributes
     * relevant to the transport setup.  Among others, this could include
     * address information (eg. host address) and transport layer
     * characteristics (eg. encryption levels). If a match is found, it
     * shall return true, or false otherwise.
     */
    public boolean is_match(Profile prof)
    {
        if (prof == null)
        {
            return false;
        }

        if (prof instanceof IIOPProfile)
        {
            IIOPProfile other = (IIOPProfile)prof;
            return
            (
                this.getSSLPort() == other.getSSLPort()           &&
                this.primaryAddress.equals (other.primaryAddress) &&
                this.getAlternateAddresses().equals(other.getAlternateAddresses())
            );
        }

        return false;
    }

    public int tag()
    {
        return TAG_INTERNET_IOP.value;
    }

    public ProtocolAddressBase getAddress()
    {
        return primaryAddress;
    }

    /**
     * Replaces the host in this profile's primary address with newHost
     * (if it is not null), and the port with newPort (if it is not -1).
     */
    public void patchPrimaryAddress(ProtocolAddressBase replacement)
    {
        if (replacement instanceof IIOPAddress)
        {
            primaryAddress.replaceFrom((IIOPAddress)replacement);
        }
    }

    public List getAlternateAddresses()
    {
        if (checkAlternateAddresses)
        {
            return components.getComponents
                (configuration, TAG_ALTERNATE_IIOP_ADDRESS.value, IIOPAddress.class);
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    public SSL getSSL()
    {
        if (!isSSLSet)
        {
            // TAG_SSL_SEC_TRANS must be disambiguated in case OpenORB-generated
            // OMG classes are in the classpath.
            ssl = (SSL)components.getComponent( org.omg.SSLIOP.TAG_SSL_SEC_TRANS.value,
                                                SSLHelper.class );
            isSSLSet = true;
        }
        return ssl;
    }


    /**
     * Returns the port on which SSL is available according to this profile,
     * or -1 if SSL is not supported.
     */
    public int getSSLPort()
    {
        TLS_SEC_TRANS tls = getTlsSpecFromCSIComponent();
        if (tls != null && tls.addresses.length > 0)
        {
            return adjustedPortNum( tls.addresses[0].port );
        }
        else
        {
            getSSL();

            if (ssl != null)
            {
                return adjustedPortNum( ssl.port );
            }
            else
            {
                return -1;
            }
        }
    }

    /**
     * Returns a copy of this profile that is compatible with GIOP 1.0.
     */
    public IIOPProfile to_GIOP_1_0()
    {
        IIOPProfile result = new IIOPProfile(this.primaryAddress,
                                             this.objectKey,
                                             0);
        return result;
    }

    public boolean equals(Object other)
    {
        return other instanceof Profile && this.is_match( (Profile) other );
    }

    public int hashCode()
    {
        return primaryAddress.hashCode();
    }

    public String toString()
    {
        return primaryAddress.toString();
    }


    /**
     * Returns the SSL port which should be used to communicate with an IOR containing this profile.
     * Returns -1 if SSL should not be used.
     *
     * SSL is enabled only if both client and target support it, and at least one requires it. Target options
     * are determined by reading either the SSL_SEC_TRANS component or the CSI_SEC_MECH_LIST component, if it specifies
     * transport-level security. If both are present, the latter is used.
     *
     * @param client_required the CSIv2 features required by the client.
     * @param client_supported the CSIv2 features supported by the client.
     * @return an ssl port number or -1, if none.
     */
    int getSslPortIfSupported( int client_required, int client_supported )
    {
        TLS_SEC_TRANS tls = getTlsSpecFromCSIComponent();
        SSL ssl = (SSL) getComponent( TAG_SSL_SEC_TRANS.value, SSLHelper.class );

        if (tls != null && useSsl( client_supported, client_required, tls.target_supports, tls.target_requires ))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Selecting TLS for connection");
            }
            return adjustedPortNum( tls.addresses[0].port );
        }
        else if (ssl != null && useSsl( client_supported, client_required, ssl.target_supports, ssl.target_requires ))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Selecting SSL for connection");
            }
            return adjustedPortNum( ssl.port );
        }
        else if ((client_required & MINIMUM_OPTIONS) != 0)
        {
            throw new org.omg.CORBA.NO_PERMISSION( "Client-side policy requires SSL/TLS, but server doesn't support it" );
        }
        return -1;
    }


    private static int adjustedPortNum( short port )
    {
        return port < 0 ? port + 65536 : port;
    }


    private boolean useSsl( int clientSupports, int clientRequires, short targetSupports, short targetRequires)
    {
        return ((targetSupports & MINIMUM_OPTIONS) != 0) && // target supports ssl
               ((clientSupports & MINIMUM_OPTIONS) != 0) && // client supports ssl
               ( ((targetRequires & MINIMUM_OPTIONS) != 0) || ((clientRequires & MINIMUM_OPTIONS) != 0) ); // either side requires ssl
    }


    private TLS_SEC_TRANS getTlsSpecFromCSIComponent()
    {
        CompoundSecMechList sas = null;
        try
        {
            sas = (CompoundSecMechList) getComponent( TAG_CSI_SEC_MECH_LIST.value, CompoundSecMechListHelper.class );
        }
        catch (Exception ex)
        {
            logger.info("Not able to process security mech. component");
        }

        TLS_SEC_TRANS tls = null;
        if (sas != null && sas.mechanism_list[0].transport_mech.tag == TAG_TLS_SEC_TRANS.value)
        {
            try
            {
                byte[] tagData = sas.mechanism_list[0].transport_mech.component_data;
                final CDRInputStream in = new CDRInputStream( tagData );
                try
                {
                    in.openEncapsulatedArray();
                    tls = TLS_SEC_TRANSHelper.read( in );
                }
                finally
                {
                    in.close();
                }
            }
            catch ( Exception e )
            {
                logger.warn("Error parsing TLS_SEC_TRANS: "+e);
            }
        }
        return tls;
    }

    public Collection asListenPoints()
    {
        List result = new ArrayList();

        if (getSSL() == null)
        {
            result.add(new ListenPoint(primaryAddress.getHostname(), (short)primaryAddress.getPort()));
        }
        else
        {
            if (getSSLPort() == 0)
            {
                result.add(new ListenPoint(primaryAddress.getHostname(), (short)primaryAddress.getPort()));
            }
            else
            {
                result.add(new ListenPoint(primaryAddress.getHostname(), (short)getSSLPort()));
            }
        }

        Iterator it = getAlternateAddresses().iterator();

        while(it.hasNext())
        {
            IIOPAddress addr = (IIOPAddress) it.next();
            result.add(new ListenPoint(addr.getHostname(), (short)addr.getPort()));
        }

        return result;
    }

    /**
     * copy this (ssl) IIOPProfile. thereby replace the port of the primaryAddress with
     * the SSL port from the tagged components.
     */
    public IIOPProfile toNonSSL()
    {
        assert getSSL() != null;

        IIOPAddress address = null;
        IIOPProfile result = null;

        try
        {
           address = new IIOPAddress(primaryAddress.getHostname(), getSSLPort());
           address.configure (configuration);

           result = new IIOPProfile(address, objectKey, configuration.getORB().getGIOPMinorVersion());
           result.configure(configuration);
        }
        catch (ConfigurationException ex)
        {
           logger.error ("Error configuring address", ex);
           throw new INTERNAL ("Error configuring address" + ex);
        }

        TaggedComponent[] taggedComponents = components.asArray();
        for (int i = 0; i < taggedComponents.length; i++)
        {
            if (taggedComponents[i].tag == org.omg.SSLIOP.TAG_SSL_SEC_TRANS.value)
            {
                continue;
            }

            result.components.addComponent(taggedComponents[i]);
        }

        return result;
    }
}
