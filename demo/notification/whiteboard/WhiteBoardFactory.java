package demo.notification.whiteboard;

import java.util.Enumeration;
import java.util.Hashtable;

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedAdmin;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import org.jacorb.notification.EventChannelFactoryImpl;

/**
 * WhiteBoardFactory.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class WhiteBoardFactory extends IFactoryPOA implements IFactoryOperations {

    Hashtable boards;
    EventChannelFactory channelFactory_;
    POA poa_;
    ORB orb_;

    public WhiteBoardFactory(ORB orb, POA poa, EventChannelFactory ecf) {
        _this(orb);
        boards = new Hashtable();
        orb_ = orb;
        poa_ = poa;
        channelFactory_ = ecf;
    }

    public IWhiteBoard getCreateWhiteboard(String name) {
        try {
            WhiteBoard board = null;
            board = (WhiteBoard)boards.get(name);
            if (board == null) {
                synchronized(boards) {
                    board = (WhiteBoard)boards.get(name);
                    if (board == null) {
                        System.out.println("Create board "+name);
                        IntHolder _channelId = new IntHolder();
                        EventChannel _channel =
                            channelFactory_.create_channel(new Property[0], new Property[0], _channelId);

                        IntHolder _adminId = new IntHolder();

                        board = new WhiteBoard(orb_, _channel);
                        boards.put(name,board);
                    }
                }
            }
            System.out.println("return board");

            return board._this(orb_);
        } catch (UnsupportedAdmin ua) {
            ua.printStackTrace();
        } catch (UnsupportedQoS uqos) {
            uqos.printStackTrace();
        } catch (AdminLimitExceeded ale) {
            ale.printStackTrace();
        }
        throw new RuntimeException();
    }

    public String[] listAllWhiteboards() {
        Enumeration e = boards.keys();
        java.util.Vector v = new java.util.Vector();

        while ( e.hasMoreElements() )
            v.addElement(e.nextElement() );

        String[] s = new String[v.size()];
        v.copyInto(s);

        return s;
    }

    public static void main(String[] args) {
        // CORBA initialisierung


        try {
            ORB _orb = ORB.init(args, null);

            POA _poa =
                POAHelper.narrow(_orb.resolve_initial_references("RootPOA"));

            NamingContext nc =
                NamingContextHelper.narrow(_orb.resolve_initial_references("NameService"));

            EventChannelFactory _factory;
            if (args != null && args.length == 1) {
                _factory =
                    EventChannelFactoryHelper.narrow(_orb.string_to_object(args[0]));
            } else {
                _factory = EventChannelFactoryHelper.narrow(_orb.resolve_initial_references("NotificationService"));
            }

            org.omg.CORBA.Object cob =
                _poa.servant_to_reference(new WhiteBoardFactory(_orb, _poa, _factory));

            NameComponent [] name = new NameComponent[1];
            name[0] = new NameComponent( "WhiteBoard", "Factory");

            // an Namen binden
            nc.rebind(name, cob);
            _poa.the_POAManager().activate();

            System.out.println("Whiteboard online !");

            if (_factory._non_existent()) {
                System.out.println("NotificationService not available !");
                System.exit(1);
            }

            _orb.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
} // WhiteBoardFactory
