package demo.notification.whiteboard;

import java.util.Vector;

import org.jacorb.notification.util.AbstractObjectPool;

import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNaming.*;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumer;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerHelper;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplier;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierHelper;
import org.omg.CosNotifyComm.StructuredPushConsumer;
import org.omg.CosNotifyComm.StructuredPushConsumerPOA;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosNotifyComm.StructuredPushSupplierPOA;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.PortableServer.*;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * Workgroup.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class Workgroup
    extends IWorkgroupPOA implements WorkgroupController, WhiteboardVars {

    Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

    protected ORB orb_;
    protected POA poa_;

    // mein Malfenster
    protected WorkgroupFrame workgroupFrame_;
    protected BrushSizePixelImage image_;

    // meine Id
    protected int myId;
    protected IWhiteBoard whiteboard_;
    protected IFactory factory_;

    TotalImageHandler totalImageHandler_;
    TotalImageHandler getTotalImageHandler() {
        if (totalImageHandler_ == null) {
            synchronized(this) {
                if (totalImageHandler_ == null) {
                    totalImageHandler_ = new TotalImageHandler(this);
                }
            }
        }
        return totalImageHandler_;
    }

    LineHandler lineHandler_;
    LineHandler getLineHandler() {
        if (lineHandler_ == null) {
            synchronized(this) {
                if (lineHandler_ == null) {
                    lineHandler_ = new LineHandler(this);
                }
            }
        }
        return lineHandler_;
    }

    ClearHandler clearHandler_;
    ClearHandler getClearHandler() {
        if (clearHandler_ == null) {
            synchronized(this) {
                if (clearHandler_ == null) {
                    clearHandler_ = new ClearHandler(this);
                }
            }
        }
        return clearHandler_;
    }

    ImageHandler imageHandler_;
    ImageHandler getImageHandler() {
        if (imageHandler_ == null) {
            synchronized(this) {
                if (imageHandler_ == null) {
                    imageHandler_ = new ImageHandler(this);
                }
            }
        }
        return imageHandler_;
    }

    public Workgroup(ORB orb, POA poa, IFactory factory) {
        orb_ = orb;
        poa_ = poa;
        _this(orb);
        factory_ = factory;
        image_ = new BrushSizePixelImage(400, 400);
        workgroupFrame_ = new WorkgroupFrame(this, "Whiteboard");
        clearAll();
    }

    public String[] getListOfWhiteboards() {
        return factory_.listAllWhiteboards();
    }

    public void selectWhiteboard(String name) {
        logger_.info("join " + name);

        // Referenz auf WhiteBoard holen
        whiteboard_ = factory_.getCreateWhiteboard(name);

        // an board anmelden dabei Id merken
        IRegistrationInfo _info =
            whiteboard_.join(_this(orb_));

        myId = _info.workgroup_identifier;

        IntHolder imageListenerId_ = new IntHolder();

        try {
            StructuredProxyPushConsumer _consumer =
                StructuredProxyPushConsumerHelper.narrow(_info.supplier_admin.obtain_notification_push_consumer(ClientType.STRUCTURED_EVENT,imageListenerId_));

            imageHandler_ = getImageHandler();
            imageHandler_.connect(_consumer);

//          totalImageHandler_ = new FetchTotalImage(this,
//                                                   orb_,
//                                                   StructuredProxyPushSupplierHelper.narrow(_info.consumer_admin.obtain_notification_push_supplier(ClientType.STRUCTURED_EVENT, imageListenerId_)));
//          totalImageHandler_.connect();

            lineHandler_ = getLineHandler();

            lineHandler_.connect(StructuredProxyPushSupplierHelper.narrow(_info.consumer_admin.obtain_notification_push_supplier(ClientType.STRUCTURED_EVENT, imageListenerId_)), _info.filter_factory);

            clearHandler_ = getClearHandler();
            clearHandler_.connect(StructuredProxyPushSupplierHelper.narrow(_info.consumer_admin.obtain_notification_push_supplier(ClientType.STRUCTURED_EVENT, imageListenerId_)), _info.filter_factory);

            workgroupFrame_.setCurrentBoardText(name);
            workgroupFrame_.setLeaveMenuItem(true);

            logger_.debug("imagelistener connected successful");
        } catch (Exception e) {
            e.printStackTrace();
        }
        workgroupFrame_.repaint();
    }

    // von board abmelden
    // keine updates mehr versenden
    // lokale update queue lo"schen
    public void leaveWhiteboard() {
        imageHandler_.shutdown();
        lineHandler_.shutdown();
        clearHandler_.shutdown();

        imageHandler_ = null;
        lineHandler_ = null;
        clearHandler_ = null;

        whiteboard_.leave(myId);
    }

    public void exit() {
        try {
            leaveWhiteboard();
        } finally {
            System.exit(0);
        }
    }

    public int getWorkgroupId() {
        return myId;
    }

    public void drawLine(int x0,int y0,int x1,int y1,int red,int green,int blue) {
        image_.drawLine(x0, y0, x1, y1, red, green, blue);
        if (imageHandler_ != null) {
            imageHandler_.drawLine(x0, y0, x1, y1, red, green, blue, image_.getBrushSize());
        }
    }

    public void drawLineLocal(int x0, int y0, int x1, int y1, int red, int green, int blue, int brushsize) {
        int _savedBrushsize = image_.getBrushSize();
        image_.setBrushSize(brushsize);
        image_.drawLine(x0, y0, x1, y1, red, green, blue);
        image_.setBrushSize(_savedBrushsize);
        workgroupFrame_.getDrawCanvas().repaint();
    }

    public PixelImage getImage() {
        return image_;
    }

    public void clearAll() {
        if (imageHandler_ != null) {
            imageHandler_.clear();
        }
        clearAllLocal();
    }

    public void clearAllLocal() {
        image_.clearAll();
        workgroupFrame_.getDrawCanvas().repaint();
    }

    public void setBrushSize(int i) {
        image_.setBrushSize(i);
    }

    public ORB getOrb() {
        return orb_;
    }

    public void updateWholeImage(int[] data) {
        image_.setPixelBuffer(data);
        workgroupFrame_.getDrawCanvas().repaint();

        try {
            totalImageHandler_.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            totalImageHandler_ = null;
        }
    }

    public static void main (String[] args) {
        ORB _orb = ORB.init(args, null);
        try {
            // CORBA stuff
            POA _poa = POAHelper.narrow(_orb.resolve_initial_references("RootPOA"));

            NamingContext nc =
                NamingContextHelper.narrow(_orb.resolve_initial_references("NameService"));

            NameComponent [] name = new NameComponent[1];
            name[0] = new NameComponent( "WhiteBoard", "Factory");

            IFactory _factory = IFactoryHelper.narrow(nc.resolve(name));

            // OK Workgroup starten
            Workgroup wg = new Workgroup(_orb, _poa, _factory);

            _poa.the_POAManager().activate();

            wg.workgroupFrame_.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

} // Workgroup

class TotalImageHandler extends StructuredPushConsumerPOA implements WhiteboardVars {

    boolean connected_ = false;
    StructuredProxyPushSupplier mySupplier_;
    StructuredPushConsumer thisRef_;
    WorkgroupController control_;

    Logger logger_ =
        Hierarchy.getDefaultHierarchy().getLoggerFor("fetch_total_image");

    Filter filter_;
    int filterId_;

    TotalImageHandler(WorkgroupController control)  {
        control_ = control;
        thisRef_ = _this(control.getOrb());
    }

    void connect(StructuredProxyPushSupplier supplier,
                 FilterFactory filterFactory) throws Exception {

        filter_ = filterFactory.create_filter("EXTENDED_TCL");
        ConstraintExp[] _filter = new ConstraintExp[1];
        _filter[0] = new ConstraintExp();
        _filter[0].constraint_expr = "$type_name == 'IMAGE'";
        _filter[0].event_types = new EventType[1];
        _filter[0].event_types[0] = new EventType("*", "*");
        filter_.add_constraints(_filter);

        mySupplier_ = supplier;
        filterId_ = mySupplier_.add_filter(filter_);
        mySupplier_.connect_structured_push_consumer(thisRef_);
        connected_ = true;

    }

    void disconnect() throws Exception  {
        mySupplier_.remove_filter(filterId_);
        mySupplier_.disconnect_structured_push_supplier();
        connected_ = false;
    }

    public void push_structured_event(StructuredEvent event)
        throws Disconnected {

        if (!connected_) {
            throw new Disconnected();
        }

        WhiteboardUpdate _update =
            WhiteboardUpdateHelper.extract(event.remainder_of_body);

        control_.updateWholeImage(_update.image());
    }

    public void disconnect_structured_push_consumer() {
        connected_ = false;
    }

    public void offer_change(EventType[] e1, EventType[] e2) {
    }
}

class LineHandler extends StructuredPushConsumerPOA implements WhiteboardVars {
    ORB orb_;
    StructuredProxyPushSupplier mySupplier_;
    WorkgroupController control_;
    StructuredPushConsumer ref_;

    Logger logger_ =
        Hierarchy.getDefaultHierarchy().getLoggerFor("UpdateHandler");

    Filter filter_;

    LineHandler(WorkgroupController control) {
        control_ = control;
        ref_ = _this(control.getOrb());
    }

    void connect(StructuredProxyPushSupplier supplier,
                FilterFactory filterFactory) throws Exception {

        filter_ = filterFactory.create_filter("EXTENDED_TCL");
        ConstraintExp[] _filter = new ConstraintExp[1];
        _filter[0] = new ConstraintExp();
        _filter[0].constraint_expr =
            "$type_name == 'LINE'";

        _filter[0].event_types = new EventType[1];
        _filter[0].event_types[0] = new EventType("*", "*");
        filter_.add_constraints(_filter);

        mySupplier_ = supplier;
        mySupplier_.connect_structured_push_consumer(ref_);
        mySupplier_.add_filter(filter_);
    }

    void shutdown() {
        mySupplier_.disconnect_structured_push_supplier();
    }

    public void push_structured_event(StructuredEvent event) {
        WhiteboardUpdate _update =
            WhiteboardUpdateHelper.extract(event.remainder_of_body);

        LineData _lineData = _update.line();

        control_.drawLineLocal(_lineData.x0,
                               _lineData.y0,
                               _lineData.x1,
                               _lineData.y1,
                               _lineData.red,
                               _lineData.green,
                               _lineData.blue,
                               _lineData.brushSize);

    }

    public void disconnect_structured_push_consumer() {
    }

    public void offer_change(EventType[] e1, EventType[] e2) {
    }
}

class ClearHandler extends StructuredPushConsumerPOA implements WhiteboardVars {
    ORB orb_;
    StructuredProxyPushSupplier mySupplier_;
    WorkgroupController control_;
    StructuredPushConsumer thisRef_;
    Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor("ClearHandler");
    Filter myFilter_;

    ClearHandler(WorkgroupController control) {
        control_ = control;
        orb_ = control.getOrb();
        thisRef_ = _this(orb_);
    }

    void connect(StructuredProxyPushSupplier mySupplier, FilterFactory factory) throws Exception {
        myFilter_ = factory.create_filter("EXTENDED_TCL");
        ConstraintExp[] _filter = new ConstraintExp[1];
        _filter[0] = new ConstraintExp();

        _filter[0].constraint_expr = "$type_name == 'CLEAR'";
        _filter[0].event_types = new EventType[1];
        _filter[0].event_types[0] = new EventType("*", "*");
        myFilter_.add_constraints(_filter);

        mySupplier_ = mySupplier;
        mySupplier_.connect_structured_push_consumer(thisRef_);
        mySupplier_.add_filter(myFilter_);
    }

    void shutdown() {
        mySupplier_.disconnect_structured_push_supplier();
    }

    public void push_structured_event(StructuredEvent event) {

        WhiteboardUpdate _update =
            WhiteboardUpdateHelper.extract(event.remainder_of_body);

        boolean _clear = _update.clear();

        control_.clearAllLocal();
    }

    public void disconnect_structured_push_consumer() {
    }

    public void offer_change(EventType[] e1, EventType[] e2) {
    }
}

class ImageHandler extends StructuredPushSupplierPOA implements WhiteboardVars, Runnable {

    ORB orb_;
    boolean connected_ = false;
    boolean active_ = false;

    StructuredProxyPushConsumer imageListener_;
    StructuredPushSupplier thisRef_;
    StructuredEvent event_;
    Thread thisThread_;

    AbstractObjectPool lineDataPool_;

    AbstractObjectPool updatePool_;

    Vector queue_ = new Vector();
    Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor("ImageHandler");
    WorkgroupController control_;

    public void run() {
        while(active_) {
            WhiteboardUpdate _update = null;
            synchronized(queue_) {
                while(queue_.isEmpty()) {
                    try {
                        queue_.wait();
                    } catch (InterruptedException ie) {
                        if (!active_) {
                            return;
                        }
                    }
                }
                _update = (WhiteboardUpdate)queue_.firstElement();
                queue_.removeElementAt(0);
            }

            StructuredEvent _event = getEvent();
            if (_update.discriminator() == UpdateType.clear) {
                _event.header.fixed_header.event_type.type_name = "CLEAR";
            } else if (_update.discriminator() == UpdateType.line) {
                _event.header.fixed_header.event_type.type_name = "LINE";
            }

            try {
                WhiteboardUpdateHelper.insert(_event.remainder_of_body, _update);
                imageListener_.push_structured_event(_event);
            } catch (Disconnected d) {
                d.printStackTrace();
                connected_ = false;
            }

            if (_update.discriminator() == UpdateType.line) {
                lineDataPool_.returnObject(_update.line());
            }
            updatePool_.returnObject(_update);
        }
    }

    ImageHandler(WorkgroupController control) {
        orb_ = control.getOrb();
        thisRef_ = _this(orb_);
        thisThread_ = new Thread(this);
        thisThread_.setPriority(3);
        control_ = control;

        lineDataPool_ = new AbstractObjectPool() {
                public Object newInstance() {
                    return new LineData();
                }
            };
        lineDataPool_.init();

        updatePool_ = new AbstractObjectPool() {
            public Object newInstance() {
                return new WhiteboardUpdate();
            }
        };
        updatePool_.init();
    }

    void shutdown() {
        active_ = false;
        thisThread_.interrupt();

        if (connected_) {
            imageListener_.disconnect_structured_push_consumer();
            connected_ = false;
        }
    }

    StructuredEvent getEvent() {
        if (event_ == null) {
            event_ = new StructuredEvent();
            event_.header = new EventHeader();
            event_.header.fixed_header = new FixedEventHeader();
            event_.header.fixed_header.event_type = new EventType();
            event_.header.fixed_header.event_type.domain_name = EVENT_DOMAIN;

            event_.header.fixed_header.event_name = "";
            event_.header.variable_header = new Property[1];
            Any _any = orb_.create_any();
            _any.insert_long(control_.getWorkgroupId());
            event_.header.variable_header[0] = new Property(WORKGROUP_ID, _any);
            event_.filterable_data = new Property[0];
            event_.remainder_of_body = orb_.create_any();
        }
        return event_;
    }

    void connect(StructuredProxyPushConsumer myConsumer) throws AlreadyConnected {
        imageListener_ = myConsumer;
        myConsumer.connect_structured_push_supplier(thisRef_);
        active_ = true;
        thisThread_.start();
    }

    public void disconnect_structured_push_supplier() {
        connected_ = false;
    }

    public void subscription_change(EventType[] e1, EventType[] e2) {
    }

    public void clear() {
        if (!active_) {
            return;
        }

        WhiteboardUpdate _update = (WhiteboardUpdate)updatePool_.lendObject();
        _update.clear(true);
        logger_.debug("clear()");

        synchronized(queue_) {
            queue_.addElement(_update);
            queue_.notifyAll();
        }
    }

    public void drawLine(int x0,
                         int y0,
                         int x1,
                         int y1,
                         int red,
                         int green,
                         int blue,
                         int brushSize) {

        if (!active_) {
            return;
        }

        WhiteboardUpdate _update = (WhiteboardUpdate)updatePool_.lendObject();
        LineData _data = (LineData)lineDataPool_.lendObject();

        _data.x0 = x0;
        _data.y0 = y0;
        _data.x1 = x1;
        _data.y1 = y1;
        _data.red = red;
        _data.green = green;
        _data.blue = blue;
        _data.brushSize = brushSize;

        _update.line(_data);

        synchronized(queue_) {
            queue_.addElement(_update);
            queue_.notifyAll();
        }
    }
}
