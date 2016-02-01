package org.jacorb.orb.giop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.DefaultProfileSelector;
import org.jacorb.orb.ProfileSelector;
import org.jacorb.orb.diop.DIOPFactories;
import org.jacorb.orb.etf.ListenEndpoint;
import org.jacorb.orb.etf.ListenEndpoint.Protocol;
import org.jacorb.orb.etf.ProtocolAddressBase;
import org.jacorb.orb.factory.SocketFactoryManager;
import org.jacorb.orb.giop.TransportListener.Event;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.BAD_PARAM;
import org.omg.ETF.Factories;
import org.slf4j.Logger;

/**
 * This class manages Transports. On the one hand it creates them, and
 * on the other it enforces an upper limit on the open transports.
 *
 * The class also receives notifications from threads that are about do use a
 * Transport and notifies any interested listeners. "Use" is defined as
 * sending (or handling) a request.
 *
 * @author Nicolas Noffke
 * */

public class TransportManager
    implements Configurable
{
    /** the configuration object  */
    private org.jacorb.config.Configuration configuration = null;

    /** configuration properties */
    private Logger logger = null;
    private List<String> factoryClassNames = null;
    private ProfileSelector profileSelector = null;
    private final SocketFactoryManager socketFactoryManager;

    /**
     * Denotes whether to use NIO for IIOP transport.
     */
    private boolean useNonBlockingIIOPTransport = false;

    /**
     * Maps ETF Profile tags (Integer) to ETF Factories objects.
     */
    private Map<Integer,Factories>  factoriesMap  = null;

    /**
     * List of all installed ETF Factories.  This list contains an
     * instance of each Factories class, ordered in the same way as
     * they were specified in the jacorb.transport.factories property.
     */
    private List<Factories> factoriesList = null;

    /**
     * List of all IIOP/SLIOP endpoint address-pairs
     */
    private HashMap<Protocol,ArrayList<ListenEndpoint>> listenEndpointList = null;


    /**
     * The first listener (in a chain of instances), representing
     * parties with interest in Transport events.
     */
    private TransportListener listener = null;

    public TransportManager( )
    {
        socketFactoryManager = new SocketFactoryManager();
    }

    @Override
    public void configure(Configuration myConfiguration)
    		throws ConfigurationException
    {
        configuration = myConfiguration;
        logger = configuration.getLogger("org.jacorb.orb.giop");
        useNonBlockingIIOPTransport = configuration.getAttributeAsBoolean
            ("jacorb.connection.nonblocking", false);

        socketFactoryManager.configure(configuration);

        // get factory class names
        factoryClassNames = configuration.getAttributeList("jacorb.transport.factories");

        if (factoryClassNames.isEmpty())
        {
           factoryClassNames.add("org.jacorb.orb.iiop.IIOPFactories");
        }

        // pickup listen endpoints specified by arguments -ORBListenEndpoints
        // and populate the array list listenEndpointList
        updateListenEndpointAddresses();

        // pickup the default OAAdress/OASSLAddress from prop file
        // and add them to the end of the array list listenEndpointList as well.
        updateDefaultEndpointAddresses();

        // get profile selector info
        profileSelector =
            (ProfileSelector)configuration.getAttributeAsObject("jacorb.transport.client.selector");

        if (profileSelector == null)
        {
            profileSelector = new DefaultProfileSelector();
        }
    }

    public ProfileSelector getProfileSelector()
    {
        return profileSelector;
    }

    public SocketFactoryManager getSocketFactoryManager()
    {
        return socketFactoryManager;
    }

    /**
     * Returns an ETF Factories object for the given tag, or null
     * if no Factories class has been defined for this tag.
     */
    public synchronized org.omg.ETF.Factories getFactories(int tag)
    {
        // This isn't ideal. If DIOPFactories was a full implementation then
        // this class should be added to the
        // TransportManager::loadFactories. This shortcut block (which is used
        // by ParsedIOR) and the static caching in DIOPFactories wouldn't be
        // needed.
        if (tag == DIOPFactories.TAG_DIOP_UDP)
        {
            return DIOPFactories.getDIOPFactory();
        }

        if (factoriesMap == null)
        {
            loadFactories();
        }
        return factoriesMap.get (tag);
    }

    /**
     * Returns a list of Factories for all configured transport plugins,
     * in the same order as they were specified in the
     * jacorb.transport.factories property.
     */
    public synchronized List<Factories> getFactoriesList()
    {
        if (factoriesList == null)
        {
            loadFactories();
        }
        return Collections.unmodifiableList(factoriesList);
    }


    public ArrayList<ListenEndpoint> getListenEndpoints (Protocol p)
    {
        ArrayList<ListenEndpoint> endpoints = listenEndpointList.get (p);

        if (endpoints == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug ("Unable to find endpoints for " + p);
            }
            ListenEndpoint l = new ListenEndpoint();
            l.setProtocol(p);
            endpoints = new ArrayList<ListenEndpoint>();
            endpoints.add(l);
            listenEndpointList.put(p, endpoints);
        }

        return endpoints;
    }

    /**
     * Build the factoriesMap and factoriesList.
     */
    private void loadFactories()
    {
        if (configuration == null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER("TransportManager not configured!");
        }

        if (factoryClassNames == null )
        {
            throw new org.omg.CORBA.INTERNAL("factoryClassNames may not be null");
        }

        factoriesMap  = new HashMap<Integer,Factories>();
        factoriesList = new ArrayList<Factories>();

        for (Iterator<String> i = factoryClassNames.iterator(); i.hasNext();)
        {
            String className = i.next();

            Factories factories = instantiateFactories(className);
            factoriesMap.put(factories.profile_tag(), factories); // NOPMD
            factoriesList.add (factories);
        }
    }

    /**
     * Instantiates the given Factories class.
     */
    private org.omg.ETF.Factories instantiateFactories (String className)
    {
        try
        {
           if (useNonBlockingIIOPTransport &&
               "org.jacorb.orb.iiop.IIOPFactories".equals (className))
           {
              className = "org.jacorb.orb.nio.NIOFactories";
           }

            // ObjectUtil.classForName() uses the context class loader.
            // This is important here because JacORB might be on the
            // bootclasspath, and the external transport on the normal
            // classpath.
            Class<?> clazz = ObjectUtil.classForName(className);
            Object instance = clazz.newInstance();

            if (instance instanceof Configurable)
            {
                Configurable configurable = (Configurable)instance;
                configurable.configure(configuration);
            }

            logger.debug("created org.omg.ETF.Factories: " + className);

            return (Factories)instance;
        }
        catch (Exception e)
        {
            throw new BAD_PARAM
                ("could not instantiate Factories class " + className
                 + ", exception: " + e);
        }
    }

    public void notifyTransportListeners(GIOPConnection giopc)
    {
        if (listener != null)
        {
            listener.transportSelected (new Event (giopc));
        }
    }

    public void addTransportListener(TransportListener tl)
    {

        if (logger.isInfoEnabled ())
        {
            logger.info ("Transport listener to add: " + tl);
        }

        if (tl != null)
        {
            addTransportListenerImpl (tl);
        }
    }

    private synchronized void addTransportListenerImpl(final TransportListener tl)
    {
        if (listener == null)
        {
            listener = tl;
        }
        else
        {
            listener = new TransportListener ()
            {

                private final TransportListener next_ = listener;

                @Override
                public void transportSelected(Event event)
                {
                    try
                    {
                        tl.transportSelected (event);
                    }
                    finally
                    {
                        next_.transportSelected (event);
                    }
                }
            };
        }
    }

    private ProtocolAddressBase createProtocolAddress(String address_str) throws ConfigurationException
    {
        final IIOPAddress address = new IIOPAddress();
        address.configure(configuration);

        int proto_delim = address_str.indexOf (':');
        Protocol proto;
        try
        {
            proto = Protocol.valueOf(address_str.substring (0,proto_delim).toUpperCase(Locale.ENGLISH));
        }
        catch (IllegalArgumentException e)
        {
            throw new BAD_PARAM("Invalid protocol " + address_str);
        }
        address.setProtocol(proto);
        final int addresss_start_ofs = proto_delim + 3;

        if (!address.fromString(address_str.substring(addresss_start_ofs)))
        {
            throw new org.omg.CORBA.INTERNAL("Invalid protocol address string: " + address_str);
        }

        // set protocol string
        return address;
    }

    /**
     * Pick default OAAdress/OASSLAddress pair from property file
     * and add them to the list of endpoint address list.
     */
    private void updateDefaultEndpointAddresses() throws ConfigurationException
    {
        IIOPAddress address = null;
        IIOPAddress ssl_address = null;


        // If we already have an endpoint list defined there is no point creating a default.
        if (listenEndpointList != null && listenEndpointList.size() > 0)
        {
            return;
        }

        ListenEndpoint defaultEndpoint = new ListenEndpoint();

        String address_str = configuration.getAttribute("OAAddress",null);
        if (address_str != null)
        {
            // build an iiop/ssliop protocol address.
            // create_protocol_address will allow iiop and ssliop only
            ProtocolAddressBase addr = createProtocolAddress(address_str);
            address = (IIOPAddress)addr;
            address.configure(configuration);
        }
        else
        {
            int oaPort = configuration.getAttributeAsInteger("OAPort",0);
            String oaHost = configuration.getAttribute("OAIAddr","");
            address = new IIOPAddress(oaHost,oaPort);
            address.configure(configuration);
        }

        String ssl_address_str = configuration.getAttribute("OASSLAddress",null);
        if (ssl_address_str != null)
        {
            // build a protocol address
            ProtocolAddressBase ssl_addr = createProtocolAddress(ssl_address_str);
            ssl_address = (IIOPAddress)ssl_addr;
            ssl_address.configure(configuration);

        }
        else
        {
            int ssl_oaPort = configuration.getAttributeAsInteger("OASSLPort",0);
            String ssl_oaHost = configuration.getAttribute("OAIAddr","");
            ssl_address = new IIOPAddress(ssl_oaHost,ssl_oaPort);
            ssl_address.configure(configuration);
        }

        if (address.getProtocol() == null)
        {
            address.setProtocol(Protocol.IIOP);
        }
        if (ssl_address.getProtocol() == null || ssl_address.getProtocol() == Protocol.SSLIOP)
        {
            ssl_address.setProtocol(Protocol.IIOP);
        }
        defaultEndpoint.setAddress(address);
        defaultEndpoint.setSSLAddress(ssl_address);
        defaultEndpoint.setProtocol(address.getProtocol());

        ArrayList<ListenEndpoint> s = listenEndpointList.get(address.getProtocol());
        s = new ArrayList<ListenEndpoint>();
        s.add (defaultEndpoint);
        listenEndpointList.put(address.getProtocol(), s);
    }

    /**
     * Pickup endpoint addresses from command-line arguments -ORBListenEndPoints
     */
    private void updateListenEndpointAddresses() throws ConfigurationException
    {
        listenEndpointList = new HashMap <Protocol, ArrayList<ListenEndpoint>>();

        // get original argument list from ORB
        String[] args = configuration.getORB().getArgs();

        if (args != null)
        {
            for (int i = 0; i < args.length; i++)
            {
                if (args[i] == null) {
                    continue;
                }

                if (!args[i].equalsIgnoreCase("-ORBListenEndpoints"))
                {
                    continue;
                }

                if (i+1 >= args.length || args[i+1] == null)
                {
                    throw new BAD_PARAM("Invalid ORBListenEndpoint <value> format: -ORBListenEndpoints argument without value" );
                }

                String ep_args = args[i+1];
                String ep_args_trim = ep_args.trim();

                //check and remove single quotes if needed
                if (ep_args_trim.charAt(0) == '\'' &&
                        ep_args_trim.charAt(ep_args_trim.length()-1) == '\'')
                {
                    ep_args_trim = ep_args.trim().substring(1,ep_args.trim().length()-1);
                }

                // split up argument into segments using the semi-clone as delimiters
                String[] seg_addr_list = ep_args_trim.split(";");

                for (int xx = 0; xx < seg_addr_list.length; xx++)
                {
                    String seg_args = seg_addr_list[xx].trim();

                    if (seg_args.equals(""))
                    {
                        continue;
                    }

                    // split up group of args into individual arg segments
                    // using the coma as delimiters
                    String[] indiv_list = seg_args.trim().split(",");
                    for (int xxx = 0; xxx < indiv_list.length; xxx++)
                    {
                        String address_str = null;
                        String ssl_port = null;
                        String host_str = "";
                        String[] options_args = null;

                        String addr_arg = indiv_list[xxx].trim();
                        if (addr_arg.equals(""))
                        {
                            continue;
                        }

                        // locate the first colon delimiter and
                        // pickup the protocol identifier string
                        int delim = addr_arg.indexOf(":");
                        Protocol protocol;
                        try
                        {
                            protocol = Protocol.valueOf (addr_arg.substring (0,delim).toUpperCase(Locale.ENGLISH));
                        }
                        catch (IllegalArgumentException e)
                        {
                            throw new BAD_PARAM("Invalid ORBListenEndPoints protocol " + addr_arg);
                        }

                        // locate the double slash delimiter
                        int db_slash = addr_arg.indexOf("//", delim+1);
                        if (db_slash == -1)
                        {
                            throw new BAD_PARAM("Invalid ORBListenEndPoints <value;value;...> format: listen endpoint \'" + addr_arg + "\' is malformed!" );
                        }

                        // check if additional option delimiter is present
                        // and pick up the protocol address
                        String dbs = "/";
                        if (protocol == Protocol.UIOP)
                        {
                            dbs = "|";
                        }
                        int opt_slash = addr_arg.indexOf(dbs, db_slash + 2);
                        if (opt_slash == -1)
                        {
                            address_str = addr_arg.substring(0);

                        }
                        else
                        {
                            address_str = addr_arg.substring(0, opt_slash);
                        }

                        // pick up optional arguments if present
                        if (opt_slash != -1)
                        {
                            options_args = addr_arg.substring(opt_slash+1).split("&");
                            for (int y = 0; y < options_args.length; y++)
                            {
                                String options_args_trim = options_args[xxx].trim();

                                int opt_delim = options_args_trim.indexOf('=');
                                if(opt_delim == -1)
                                {
                                    throw new BAD_PARAM("error: listen endpoint options \'" + options_args[y] + "\' is malformed!");
                                }
                                else
                                {
                                    String opt_str = options_args_trim.substring(0, opt_delim);
                                    String opt_value = options_args_trim.substring(opt_delim+1);

                                    if(opt_str.equalsIgnoreCase("ssl_port"))
                                    {
                                        ssl_port = opt_value;
                                    }
                                    else
                                    {
                                        throw new BAD_PARAM("error: listen endpoint options \'" + options_args[y] + "\' is not supported!");
                                    }
                                }

                            }
                        }

                        if(address_str != null)
                        {
                            String address_trim = address_str.trim();
                            // build an iiop/ssliop protocol address.
                            // create_protocol_address will allow iiop and ssliop only
                            IIOPAddress address = null;
                            IIOPAddress ssl_address = null;
                            if (protocol == Protocol.IIOP)
                            {
                                ProtocolAddressBase addr1 = createProtocolAddress(address_trim);
                                if (addr1 instanceof IIOPAddress)
                                {
                                    address = (IIOPAddress)addr1;
                                    address.configure(configuration);
                                }

                                if (ssl_port != null)
                                {
                                    int colon_delim = address_trim.indexOf(":");
                                    int port_delim = address_trim.indexOf(":", colon_delim+2);
                                    if (port_delim > 0)
                                    {
                                        host_str = address_trim.substring(colon_delim+3, port_delim);
                                    }
                                    else
                                    {
                                        host_str = "";
                                    }

                                    ssl_address = new IIOPAddress(host_str,Integer.parseInt(ssl_port));
                                    ssl_address.configure(configuration);
                                }

                            }
                            else if(protocol == Protocol.SSLIOP)
                            {
                                ProtocolAddressBase addr2 = createProtocolAddress(address_trim);
                                if (addr2 instanceof IIOPAddress)
                                {
                                    ssl_address = (IIOPAddress)addr2;
                                    ssl_address.configure(configuration);
                                }

                                //  Set the protocol to IIOP for using IIOP Protocol Factory
                                protocol = Protocol.IIOP;
                            }
                            else
                            {
                                ProtocolAddressBase addr1 = createProtocolAddress(address_trim);
                                if (addr1 instanceof IIOPAddress)
                                {
                                    address = (IIOPAddress)addr1;
                                    address.configure(configuration);
                                }
                            }

                            if (configuration.getAttributeAsBoolean("jacorb.security.support_ssl", false) &&
                                ssl_address == null)
                            {
                                throw new org.omg.CORBA.BAD_PARAM
                                    ("Error: an SSL port (ssl_port) is required when property jacorb.security.support_ssl is enabled");
                            }

                            ListenEndpoint listen_ep = new ListenEndpoint();
                            listen_ep.setAddress(address);
                            listen_ep.setSSLAddress(ssl_address);
                            listen_ep.setProtocol(protocol);

                            ArrayList<ListenEndpoint> s = listenEndpointList.get(protocol);
                            if ( s == null)
                            {
                                s = new ArrayList<ListenEndpoint>();
                                listenEndpointList.put(protocol, s);
                            }
                            s.add(listen_ep);
                        }
                    } // end for - inner
                } //end for - outter
            } //end for
        } // end if
    }
}
