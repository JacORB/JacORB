package org.jacorb.test.listenendpoints.echo_corbaloc;

import java.util.ArrayList;
import java.util.List;

public class ListenEndpoints {

    /**
     * Pickup endpoint addresses from command-line arguments -ORBListenEndPoints
     */
    public static List<Endpoint> getEndpointList(String[] args) throws Exception
    {

        List<Endpoint> endpointList = new ArrayList<Endpoint>();

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
                    throw new Exception ("Invalid ORBListenEndpoints <value> format: -ORBListenEndpoints argument without value" );
                }

                /**
                 * an example of listen endpoint specification:
                 *    -ORBListenEndPoint 'iiop://foo:9999/resuse_addr=1&foobar=50; ;iiop://;sliop://localhost:1234;;'
                 */

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
                        String[] options_args = null;

                        String addr_arg = indiv_list[xxx].trim();
                        if (addr_arg.equals(""))
                        {
                            continue;
                        }

                        // locate the first colon delimiter and
                        // pickup the protocol identifier string
                        int delim = addr_arg.indexOf(":");
                        String proto = addr_arg.substring (0,delim).toLowerCase();

                        // locate the double slash delimiter
                        int db_slash = addr_arg.indexOf("//", delim+1);
                        // System.out.println("xxx=" + xxx + ": delim=<" + db_slash + ">");
                        if (db_slash == -1)
                        {
                            throw new Exception ("Invalid ORBListenEndPoints <value;value;...> format: listen endpoint \'" + addr_arg + "\' is malformed!" );
                        }

                        // check if additional option delimiter is present
                        // and pick up the protocol address
                        String dbs = "/";
                        if (proto.equals("uiop"))
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
                                    throw new Exception ("error: listen endpoint options \'" + options_args[y] + "\' is malformed!");
                                }
                                else
                                {
                                    String opt_str = options_args_trim.substring(0, opt_delim);
                                    String opt_value = options_args_trim.substring(opt_delim+1);

                                    if(opt_str.equalsIgnoreCase("ssl_port"))
                                    {
                                        ssl_port = new String(opt_value);

                                    }
                                    else
                                    {
                                        throw new Exception ("error: listen endpoint options \'" + options_args[y] + "\' is not supported!");
                                    }
                                }

                            }
                        }

                        if(address_str != null)
                        {
                            String address_trim = address_str.trim();
                            // build an iiop/ssliop protocol address.
                            // create_protocol_address will allow iiop and ssliop only
                            if (proto.equals("iiop"))
                            {
                                Endpoint addr = null;
                                try
                                {
                                    addr = createProtocolAddress(address_trim);
                                }
                                catch (Exception e)
                                {
                                    throw new Exception(e.getMessage());
                                }
                                if (ssl_port != null)
                                {
                                    addr.setSSLPort(Integer.parseInt(ssl_port));
                                }
                                endpointList.add(addr);
                            }
                            else
                            {
                                Endpoint addr = null;
                                try
                                {
                                    addr = createProtocolAddress(address_trim);
                                }
                                catch (Exception e)
                                {
                                    throw new Exception(e.getMessage());
                                }
                                endpointList.add(addr);

                            }


                        }
                    } //end for inner
                } //end for
            } //end for
        } // end if
        return endpointList;

    }

    private static Endpoint createProtocolAddress(String address_str) throws Exception
    {
        final Endpoint epoint = new Endpoint();
        if (address_str != null)
        {
            int proto_delim = address_str.indexOf (':');
            String proto = address_str.substring (0,proto_delim).toLowerCase();
            epoint.setProtocol(proto);
            final int addresss_start_ofs = proto_delim + 3;
            if (!epoint.fromString(address_str.substring(addresss_start_ofs)))
            {
                throw new Exception("Invalid protocol address string: " + address_str);
            }
        }
        else
        {
            throw new Exception("Invalid protocol address string (is null)");
        }

        return epoint;
    }
}
