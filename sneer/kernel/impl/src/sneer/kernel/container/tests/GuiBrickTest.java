package sneer.kernel.container.tests;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import javax.swing.SwingUtilities;

import org.junit.Ignore;
import org.junit.Test;

import sneer.brickness.ByRef;
import sneer.kernel.container.Container;
import sneer.kernel.container.Containers;
import wheel.io.ui.TimeboxedEventQueue;
import wheel.lang.exceptions.NotImplementedYet;
import wheel.lang.exceptions.TimeIsUp;

public class GuiBrickTest {
	
	@Test
	public void guiBrickRunsInSwingThread() throws Exception {
		final Container container = Containers.newContainer();
		final SomeGuiBrick brick = container.provide(SomeGuiBrick.class);
		assertSame(swingThread(), brick.currentThread());
	}

	@Test
	public void injectedGuiBrickRunsInSwingThread() throws Exception {
		final Container container = Containers.newContainer(new SomeGuiBrick() {			
			@Override
			public Thread currentThread() {
				return Thread.currentThread();
			}

			@Override
			public void slowMethod() {
				throw new IllegalStateException();
			}
		});
		final SomeGuiBrick brick = container.provide(SomeGuiBrick.class);
		assertSame(swingThread(), brick.currentThread());
	}

	@Test
	@Ignore
	public void guiBrickCallbacksComeInSwingThread() throws Exception {
		throw new NotImplementedYet();
	}
	
	@Test
	public void testGuiBrickRunsInsideTimebox() throws Exception {
		int timeoutForGuiEvents = 10;
		TimeboxedEventQueue.startQueueing(timeoutForGuiEvents);

		try {
			runInsideTimebox();
		} finally {
			TimeboxedEventQueue.stopQueueing();
		}
	}


	private void runInsideTimebox() {
		final Container container = Containers.newContainer();
		final SomeGuiBrick brick = container.provide(SomeGuiBrick.class);
		try {
			brick.slowMethod();
		} catch (TimeIsUp expected) {
			return;
		}
		fail("timebox should have stopped the method");
	}

	@Test
	public void testNonGuiBrickRunsInCurrentThread() throws Exception {
		final SomeVanillaBrick brick = Containers.newContainer().provide(SomeVanillaBrick.class);
		assertSame(Thread.currentThread(), brick.brickThread());
	}
	
	private Thread swingThread() throws Exception {
		final ByRef<Thread> swingThread = ByRef.newInstance();
		SwingUtilities.invokeAndWait(new Runnable() { @Override public void run() {
			swingThread.value = Thread.currentThread();
		}});
		return swingThread.value;
	}
}
