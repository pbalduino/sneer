package sneer.bricks.pulp.reactive.collections.tests;

import static basis.environments.Environments.my;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import basis.lang.Consumer;
import basis.testsupport.AssertUtils;
import basis.util.concurrent.Latch;

import sneer.bricks.hardware.cpu.lang.contracts.WeakContract;
import sneer.bricks.pulp.reactive.collections.CollectionChange;
import sneer.bricks.pulp.reactive.collections.CollectionSignals;
import sneer.bricks.pulp.reactive.collections.SetRegister;
import sneer.bricks.software.folderconfig.testsupport.BrickTestBase;

public class SetRegisterTest extends BrickTestBase {

	private final SetRegister<String> _subject = my(CollectionSignals.class).newSetRegister();

	@Test (timeout = 2000)
	public void addAll() {
		final Latch latch = new Latch();

		final ArrayList<Integer> _sizes = new ArrayList<Integer>();
		@SuppressWarnings("unused") final Object sizeContract = _subject.output().size().addReceiver(new Consumer<Integer>() {@Override public void consume(Integer value) {
			_sizes.add(value);
		}});

		@SuppressWarnings("unused") final Object changeContract = _subject.output().addReceiver(new Consumer<CollectionChange<String>>() {@Override public void consume(CollectionChange<String> change) {
			if (change.elementsAdded().isEmpty()) return; //First time.
			AssertUtils.assertContents(sorted(change.elementsAdded()), "bacon", "eggs", "spam");
			latch.open();
		}});

		assertEquals(0, _subject.output().size().currentValue().intValue());
		assertContents(_sizes, 0);

		_subject.addAll(Arrays.asList("spam", "eggs", "bacon"));
		assertEquals(3, _subject.output().size().currentValue().intValue());
		assertContents(_sizes, 0, 3);
		
		latch.waitTillOpen();
	}

	@Test
	public void size() {
		final ArrayList<Integer> _sizes = new ArrayList<Integer>();

		@SuppressWarnings("unused") final WeakContract referenceToAvoidGc = _subject.output().size().addReceiver(new Consumer<Integer>() {@Override public void consume(Integer value) {
			_sizes.add(value);
		}});

		assertEquals(0, _subject.output().size().currentValue().intValue());
		assertContents(_sizes, 0);

		_subject.add("spam");
		assertEquals(1, _subject.output().size().currentValue().intValue());
		assertContents(_sizes, 0, 1);

		_subject.add("eggs");
		assertEquals(2, _subject.output().size().currentValue().intValue());
		assertContents(_sizes, 0, 1, 2);

		_subject.remove("spam");
		assertEquals(1, _subject.output().size().currentValue().intValue());
		assertContents(_sizes, 0, 1, 2, 1);
	}

	private Iterable<String> sorted(Collection<String> elements) {
		ArrayList<String> result = new ArrayList<String>(elements);
		Collections.sort(result);
		return result;
	}


}
