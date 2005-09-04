from net.grinder.script.Grinder import grinder
from net.grinder.script import Test
from net.grinder.statistics import ExpressionView, StatisticsIndexMap, StatisticsView
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

# start ORB Thread
def run_orb():
	orb.run()

start_new_thread(run_orb, ())

# resolve NotificationService
obj = orb.resolve_initial_references("NotificationService")
ecf = EventChannelFactoryHelper.narrow( obj )
channel = ecf.get_event_channel(0)


class StructuredPushConsumer(StructuredPushConsumerOperations):
	def __init__(self):
		self.messageQueue = []          # Queue of received messages not yet recorded.
		self.lock = allocate_lock()
		
	def push_structured_event(self, notification):
		deliveryTime = System.currentTimeMillis() - notification.remainder_of_body.extract_longlong()
		self.lock.acquire()
		self.messageQueue.append(deliveryTime)
		self.lock.release()
		
	def disconnect_structured_push_consumer(self):
		log("disconnect_structured_push_consumer")
	
	def offer_change(self, added, removed):
		log("offer_change")

	def connect(self, channel):
		pushConsumerTie = StructuredPushConsumerPOATie(self)
		consumerAdmin = channel.default_consumer_admin()
		intHolder = IntHolder()
		pushSupplier = StructuredProxyPushSupplierHelper.narrow(consumerAdmin.obtain_notification_push_supplier(ClientType.STRUCTURED_EVENT, intHolder))
		pushSupplier.connect_structured_push_consumer(StructuredPushConsumerHelper.narrow(pushConsumerTie._this(orb)))
		return pushSupplier
		
	def get_delivery_time(self):
		# Wait until we have received a message.
		timeOut = System.currentTimeMillis() + 5000            
		self.lock.acquire()
		while not self.messageQueue and System.currentTimeMillis() < timeOut:
			self.lock.release() 
			grinder.sleep(100)
			self.lock.acquire()
		try:
			if self.messageQueue:
				# Pop delivery time from first message in message queue
				return self.messageQueue.pop(0)
			else:
				raise Exception, 'Timeout'
		finally:
			self.lock.release()

# Use userLong0 statistic to represent the "delivery time".
deliveryTimeIndex = StatisticsIndexMap.getInstance().getLongIndex("userLong0")

# Add two statistics views:
# 1. Delivery time:- the mean time taken between the server sending
# the message and the receiver receiving the message.
# 2. Mean delivery time:- the delivery time averaged over all
# tests.

detailView = StatisticsView()
detailView.add(ExpressionView("Delivery time", "", "userLong0"))
            
summaryView = StatisticsView()
summaryView.add(ExpressionView(
    "Mean delivery time",
    "statistic.deliveryTime",
    "(/ userLong0(+ (count timedTests) untimedTests))"))
            
grinder.registerDetailStatisticsView(detailView)
grinder.registerSummaryStatisticsView(summaryView)

# We record each message receipt against a single test. The
# test time is meaningless.
def recordDeliveryTime(deliveryTime):
    grinder.statistics.setValue(deliveryTimeIndex, deliveryTime)

recordTest = Test(2, "Receive messages").wrap(recordDeliveryTime)

receiver = StructuredPushConsumer()
pushSupplier = receiver.connect(channel)

class TestRunner:
	def __call__(self):
		statistics = grinder.statistics
		# Read 10 messages from the queue.
		try:
			for i in range(0, 1000):
				deliveryTime = receiver.get_delivery_time()
			
				log("Received message")
				# We record the test a here rather than in onMessage
				# because we must do so from a worker thread.
				recordTest(deliveryTime)
		except Exception:
			grinder.logger.error("Timout occured")
			statistics.delayReports = 1
			recordTest(-1)
			statistics.success = 0
			statistics.report()
			
	def __del__(self):
		pushSupplier.disconnect_structured_push_supplier()
		orb.shutdown(1)
