package demo.concurrency;

import org.omg.CosConcurrencyControl.*;
import org.omg.CosTransactions.*;
import org.omg.CosNaming.*;


public class Client
{
    private static Control ctrl = null;
    private static TransactionalLockSet lockset = null;
    private static TransactionFactory tf = null;

    public static void main (String[] args)
    {
        try
        {
            // jacorb.security.SecurityRoot.start( args );
            // org.omg.CORBA.ORB orb = jacorb.orb.Local.get_orb();
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, null);

            org.omg.PortableServer.POA poa = org.omg.PortableServer.POAHelper.narrow (orb.resolve_initial_references ("RootPOA"));

            // Session ss = get_session( orb );
            // org.omg.CosNaming.NamingContextExt nc = ss.get_naming();
            NamingContextExt nc = NamingContextExtHelper.narrow (orb.resolve_initial_references ("NameService"));
            NameComponent[] name = new NameComponent[1];
            name[0] = new NameComponent ("LogicLand", "transaction");
            tf = TransactionFactoryHelper.narrow (nc.resolve (name));

            name[0] = new NameComponent ("LogicLand", "lock");
            LockSetFactory ls = LockSetFactoryHelper.narrow (nc.resolve (name));

            name[0] = new NameComponent ("LogicLand", "lockset");
            lockset = TransactionalLockSetHelper.narrow (nc.resolve (name));
            print_help ();
            while (exec_command ())
            {
                // empty body
            }

        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }
    }

    static void print_help ()
    {
        System.out.println (" ---------------------------------------------------------------------------");
        System.out.println (" LogicLand group, Concurrency service test program");
        System.out.println (" ---------------------------------------------------------------------------");
        System.out.println (" LockMode: Read - r, Write - w, Upgrade - u, IRead - ir, IWrite iw");
        System.out.println (" ---------------------------------------------------------------------------");
        System.out.println (" Commands :");
        System.out.println (" ---------------------------------------------------------------------------");
        System.out.println (" Lock     ::= l<LockMode>           // Lock resource ");
        System.out.println (" Try_Lock ::= t<LockMode>           // Try lock resource ");
        System.out.println (" Unlock   ::= u<LockMode>           // Unlock resource");
        System.out.println (" Change   ::= c<LockMode><LockMode> // Change lock from->to");
        System.out.println (" Start    ::= start[=n]             // n minuts timeout, default - 5");
        System.out.println (" Commit   ::= commit                // commit transaction ");
        System.out.println (" Rollback ::= rollback              // rollback transaction");
        System.out.println (" Print    ::= print                 // print LockSet contents");
        System.out.println (" Help     ::= help                  // this screen");
        System.out.println (" Quit     ::= quit                  // quit from test");
        System.out.println (" ---------------------------------------------------------------------------");
    };

    static boolean exec_command ()
    {
        System.out.print ("Ready ;-)");
        String cmd = InConsole.read ();
        System.out.println ("Accept:" + cmd);
        try
        {
            if (cmd.equals ("quit"))
            {
                return false;
            }
            else if (cmd.equals ("help"))
            {
                print_help ();
            }
            else if (cmd.equals ("print"))
            {
                // lockset.print();
            }
            else if (cmd.equals ("commit"))
            {
                if (ctrl == null)
                {
                    System.out.println ("Error: Transaction not active");
                }
                else
                {
                    try
                    {
                        ctrl.get_terminator ().commit (false);
                        System.out.println ("Commit complete");
                    }
                    catch (org.omg.CORBA.OBJECT_NOT_EXIST e)
                    {
                        System.out.println ("Error: Transaction finnished");
                    }
                    ctrl = null;
                }
            }
            else if (cmd.equals ("rollback"))
            {
                if (ctrl == null)
                {
                    System.out.println ("Error: Transaction not active");
                }
                else
                {
                    try
                    {
                        ctrl.get_terminator ().rollback ();
                        System.out.println ("Rollback complete");
                    }
                    catch (org.omg.CORBA.OBJECT_NOT_EXIST e)
                    {
                        System.out.println ("Error: Transaction finnished");
                    }
                    ctrl = null;
                }
            }
            else if (cmd.length () >= 5
                    && cmd.substring (0, 5).equals ("start"))
            {
                if (ctrl != null)
                {
                    System.out.println ("Error: Transaction is active");
                }
                else
                {
                    int minute = 5;
                    try
                    {
                        minute = Integer.parseInt (cmd.substring (6));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace ();
                        System.out.println ("Use default 5 min.");
                    }
                    ctrl = tf.create (minute * 60);
                    System.out.println ("Transaction started with " + minute
                            + "min timeout");
                }
            }
            else
            {
                if (ctrl == null)
                {
                    System.out.println ("Error: Transaction not active");
                    return true;
                }
                try
                {
                    if (cmd.length () >= 2 && cmd.substring (0, 1).equals ("l"))
                    {
                        lock_mode mode = parse_mode (cmd.substring (1));
                        lockset.lock (ctrl.get_coordinator (), mode);
                        System.out.println ("Lock complete");
                    }
                    else if (cmd.length () >= 2
                            && cmd.substring (0, 1).equals ("t"))
                    {
                        lock_mode mode = parse_mode (cmd.substring (1));
                        if (lockset.try_lock (ctrl.get_coordinator (), mode))
                        {
                            System.out.println ("Lock complete");
                        }
                        else
                        {
                            System.out.println ("Lock uncomplete");
                        }
                    }
                    else if (cmd.length () >= 2
                            && cmd.substring (0, 1).equals ("u"))
                    {
                        lock_mode mode = parse_mode (cmd.substring (1));
                        lockset.unlock (ctrl.get_coordinator (), mode);
                        System.out.println ("Unlock complete");
                    }
                    else if (cmd.length () >= 3
                            && cmd.substring (0, 1).equals ("c"))
                    {
                        lock_mode mode = parse_mode (cmd.substring (1));
                        lock_mode mode1;
                        if (mode.equals (lock_mode.intention_read)
                                || mode.equals (lock_mode.intention_write))
                        {
                            mode1 = parse_mode (cmd.substring (3));
                        }
                        else
                        {
                            mode1 = parse_mode (cmd.substring (2));
                        }
                        lockset.change_mode (ctrl.get_coordinator (), mode,
                                             mode1);
                        System.out.println ("Change mode complete");
                    }
                }
                catch (org.omg.CORBA.TRANSACTION_ROLLEDBACK e)
                {
                    System.out.println ("Error: Transaction RolledBack");
                    ctrl = null;
                }
                catch (org.omg.CORBA.INVALID_TRANSACTION e)
                {
                    System.out.println ("Error: Transaction invalid");
                    ctrl = null;
                }
                catch (org.omg.CORBA.OBJECT_NOT_EXIST e)
                {
                    System.out.println ("Error: Transaction finnished");
                    ctrl = null;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }
        return true;
    }

    static lock_mode parse_mode (String mode)
    {
        if (mode.substring (0, 1).equals ("r"))
        {
            return lock_mode.read;
        }
        else if (mode.substring (0, 1).equals ("w"))
        {
            return lock_mode.write;
        }
        else if (mode.substring (0, 1).equals ("u"))
        {
            return lock_mode.upgrade;
        }
        else if (mode.substring (0, 2).equals ("ir"))
        {
            return lock_mode.intention_read;
        }
        else if (mode.substring (0, 2).equals ("iw"))
        {
            return lock_mode.intention_write;
        }
        return lock_mode.read;
    };
    /*
     * static Session get_session( org.omg.CORBA.ORB orb ) throws Exception {
     * String line; BufferedReader in; Socket quote = new Socket("192.168.1.9",
     * 9000); in = new BufferedReader(new
     * InputStreamReader(quote.getInputStream())); line = in.readLine(); while
     * (line.indexOf("IOR:") != 0) { line = in.readLine(); } in.close();
     * quote.close();
     * 
     * org.omg.CORBA.Object o = orb.string_to_object(line); return
     * SessionHelper.narrow(o); }
     */
};
