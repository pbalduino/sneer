package dfcsantos.wusic.tests;

import static sneer.foundation.environments.Environments.my;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import sneer.bricks.hardware.clock.Clock;
import sneer.bricks.hardware.io.IO;
import sneer.bricks.pulp.blinkinglights.BlinkingLights;
import sneer.bricks.pulp.reactive.Signal;
import sneer.bricks.pulp.reactive.SignalUtils;
import sneer.bricks.pulp.tuples.TupleSpace;
import sneer.bricks.software.folderconfig.tests.BrickTest;
import sneer.foundation.environments.Environment;
import sneer.foundation.environments.Environments;
import sneer.foundation.lang.Closure;
import sneer.foundation.lang.ClosureX;
import dfcsantos.wusic.Wusic;
import dfcsantos.wusic.Wusic.OperatingMode;

public class WusicTest extends BrickTest {

	private Wusic _subject1;
	private Wusic _subject2;

//	@Bind private TrackPlayer _trackPlayer = mock(TrackPlayer.class);

	@Test
	public void basicStuff() {
		_subject1 = my(Wusic.class);

		assertSignalValue(_subject1.sharedTracksFolder(), new File(tmpFolder(), "data/media/tracks"));

		assertSignalValue(_subject1.operatingMode(), OperatingMode.OWN);
		_subject1.setOperatingMode(OperatingMode.PEERS);
		assertSignalValue(_subject1.operatingMode(), OperatingMode.PEERS);

		_subject1.start();
		if (!my(BlinkingLights.class).lights().currentGet(0).caption().equals("No Tracks Found")) fail();
		assertSignalValue(_subject1.playingTrack(), null);
		assertSignalValue(_subject1.isPlaying(), false);

		assertSignalValue(_subject1.numberOfPeerTracks(), 0);
		assertSignalValue(_subject1.isTrackDownloadActive(), false);

		_subject1.trackDownloadActivator().consume(true);

		assertSignalValue(_subject1.isTrackDownloadActive(), true);		
		assertSignalValue(_subject1.trackDownloadAllowance(), Wusic.DEFAULT_TRACKS_DOWNLOAD_ALLOWANCE);
	}

	@Ignore
	@Test (timeout = 5000)
	public void peersMode() throws IOException {
		Environment remoteEnvironment = newTestEnvironment(my(TupleSpace.class), my(Clock.class));
		configureStorageFolder(remoteEnvironment, "remote/data");
		configureTmpFolder(remoteEnvironment, "remote/tmp");

		final String[] trackNames = new String[] { "track1.mp3", "track2.mp3", "track3.mp3" };
		Environments.runWith(remoteEnvironment, new ClosureX<IOException>() { @Override public void run() throws IOException {
			createSampleTracks(trackNames);
			assertEquals(3, sharedTracksFolder().listFiles().length);
			_subject2 = my(Wusic.class);
			_subject2.trackDownloadActivator().consume(true);
		}});

		_subject1 = my(Wusic.class);
		_subject1.trackDownloadActivator().consume(true);

		while(true) {
			publishRandomEndorsement(remoteEnvironment);
			my(TupleSpace.class).waitForAllDispatchingToFinish();
			if (_subject1.numberOfPeerTracks().currentValue() == 3) break;
		}

//		checking(new Expectations() {{
//			oneOf(_trackPlayer).startPlaying(with(any(Track.class)), with(any(Signal.class)), with(any(Runnable.class)));
//		}});
//
//		_subject1.setOperatingMode(OperatingMode.PEERS);
//		_subject1.start();
//		assertSignalValue(_subject1.isPlaying(), true);
//		_subject1.pauseResume();
//		assertSignalValue(_subject1.isPlaying(), false);

		crash(remoteEnvironment);
	}

	private void publishRandomEndorsement(Environment environment) {
		Environments.runWith(environment, new Closure() { @Override public void run() {
			my(Clock.class).advanceTime(60 * 1000);
		}});
	}

	private <T> void assertSignalValue(Signal<T> signal, T value) {
		my(SignalUtils.class).waitForValue(signal, value);
	}

	private void createSampleTracks(String... tracks) throws IOException {
		for (String track : tracks)
			my(IO.class).files().writeString(new File(sharedTracksFolder(), track), track);
	}

	private File sharedTracksFolder() {
		return my(Wusic.class).sharedTracksFolder().currentValue();
	}

}
