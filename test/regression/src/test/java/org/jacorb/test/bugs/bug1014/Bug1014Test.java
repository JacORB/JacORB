package org.jacorb.test.bugs.bug1014;

import java.util.Properties;
import org.jacorb.test.bugs.bug1014.DeniedService;
import org.jacorb.test.bugs.bug1014.DeniedServiceHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.IMRExcludedClientServerCategory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.omg.CORBA.NO_PERMISSION;

@Category(IMRExcludedClientServerCategory.class)
public class Bug1014Test extends ClientServerTestCase {

	@BeforeClass
	public static void beforeClassSetup() throws Exception {
		Properties props = new Properties();
		props.setProperty(
				"org.omg.PortableInterceptor.ORBInitializerClass.ORBInit",
				ORBInit.class.getName());
		setup = new ClientServerSetup(Servant.class.getName(), props, props);
	}

	@Test
	public void testConcurrentCalls() {
		final DeniedService service =
				DeniedServiceHelper.narrow(setup.getServerObject());
		Runnable task = new Runnable () {
			public void run() { service.resetWhenReach(10); }
		};
		ManyThreads threads = new ManyThreads(5, task);
		Assert.assertTrue(threads.checkCompletion(5000));
	}

	private static class ManyThreads {
		private final java.lang.Object sync;
		private final Runnable task;
		private int missing;

		public ManyThreads(int count, Runnable runnable) {
			sync = new java.lang.Object();
			task = runnable;
			missing = count;
			for (int i = 0; i < count; ++i) {
				Thread t = new Thread(new Runnable () {
					public void run() {
						task.run();
						synchronized (sync) {
							if (--missing == 0) {
								sync.notifyAll();
							}
						}
					}
				});
				t.start();
			}
		}

		public boolean checkCompletion(int timeout) {
			synchronized (sync) {
				if (missing > 0) {
					try { sync.wait(timeout); }
					catch (InterruptedException ex) {}
				}
				if (missing == 0) return true;
			}
			return false;
		}
	}
}
