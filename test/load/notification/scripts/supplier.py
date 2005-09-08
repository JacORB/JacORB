from net.grinder.script.Grinder import grinder
from net.grinder.script import Test
from jarray import array
import sys
from java.lang import *
from java.util import Properties
from org.omg.CORBA import *
from org.omg.CosNotifyChannelAdmin import *
from org.omg.CosNotifyComm import *
from org.omg.CosNotification import *
from thread import *

log = grinder.logger.output

# setup ORB
args = array([], String)
props = Properties()
props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB")
props.put("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton")
orb = ORB.init(args, props)
poa = orb.resolve_initial_references("RootPOA")
poa.the_POAManager().activate();

# start ORB thread
def run_orb():
    orb.run()

start_new_thread(run_orb, ())

# resolve NotificationService
obj = orb.resolve_initial_references("NotificationService")
ecf = EventChannelFactoryHelper.narrow( obj )
channel = ecf.get_event_channel(0)

class StructuredPushSender(StructuredPushSupplierOperations):
    def disconnect_structured_push_supplier(self):
        log("disconnect_structured_push_supplier")
    
    def subscription_change(self, added, removed):
        log("subscription_change")
        
    def connect(self, channel):
        log("connect")
        
        senderTie = StructuredPushSupplierPOATie(self);
        sender = senderTie._this(orb);
        supplierAdmin = channel.default_supplier_admin();
        intHolder = IntHolder();
        pushConsumer = StructuredProxyPushConsumerHelper.narrow(supplierAdmin.obtain_notification_push_consumer(ClientType.STRUCTURED_EVENT, intHolder));
        pushConsumer.connect_structured_push_supplier(sender);
        return pushConsumer
       
def createEvent(size):
    event = StructuredEvent()
    eventType = EventType("testDomain", "testType");
    fixedHeader = FixedEventHeader(eventType, "testing");
    variableHeader = array([], Property);
    event.header = EventHeader(fixedHeader, variableHeader);
    event.filterable_data = array([], Property)
    event.remainder_of_body = orb.create_any()
    event.remainder_of_body.insert_longlong(System.currentTimeMillis())
    return event
            
sender = StructuredPushSender()
pushConsumer = sender.connect(channel)
instrumentedConsumer = Test(1, "Push Events").wrap(pushConsumer)

# An instance of this class is created for every thread.
class TestRunner:
    # This method is called for every run.
    def __call__(self):
        # Per thread scripting goes here.
        log("push events")
        grinder.sleep(1000)
        for i in range (0, 1000):
            instrumentedConsumer.push_structured_event(createEvent(1))
	    grinder.sleep(250)

        log("done")
        
    def __del__(self):
        pushConsumer.disconnect_structured_push_consumer()
        orb.shutdown(1)
        