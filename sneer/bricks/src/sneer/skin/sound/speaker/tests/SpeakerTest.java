package sneer.skin.sound.speaker.tests;

import javax.sound.sampled.SourceDataLine;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.action.CustomAction;
import org.junit.Test;
import org.junit.runner.RunWith;

import sneer.kernel.container.Inject;
import sneer.pulp.clock.Clock;
import sneer.pulp.keymanager.KeyManager;
import sneer.pulp.keymanager.PublicKey;
import sneer.pulp.tuples.TupleSpace;
import sneer.skin.sound.PcmSoundPacket;
import sneer.skin.sound.kernel.Audio;
import sneer.skin.sound.speaker.Speaker;
import sneer.skin.sound.speaker.buffer.SpeakerBuffer;
import sneer.skin.sound.speaker.buffer.SpeakerBuffers;
import tests.TestThatIsInjected;
import wheel.lang.ImmutableByteArray;
import wheel.lang.Consumer;


@RunWith(JMock.class)
public class SpeakerTest extends TestThatIsInjected {
	
	@Inject private static Speaker _subject;
	@Inject private static Clock _clock;
	@Inject private static KeyManager _keyManager;
	@Inject private static TupleSpace _tupleSpace;

	private final Mockery _mockery = new JUnit4Mockery();
	
	private final Audio _audio = _mockery.mock(Audio.class);
	private final SourceDataLine _line = _mockery.mock(SourceDataLine.class);
	private final SpeakerBuffers _buffers = _mockery.mock(SpeakerBuffers.class);
	private final SpeakerBuffer _buffer = _mockery.mock(SpeakerBuffer.class);
	
	private Consumer<? super PcmSoundPacket> _consumer;
	
	@Override
	protected Object[] getBindings() {
		return new Object[]{ _audio, _buffers };
	}

	@Test
	public void testSilentChannel() throws Exception {
		_mockery.checking(new CommonExpectations());
		
		_subject.open();
		_subject.close();
	}
	
	@Test
	public void testOnlyTuplesFromContactsGetPlayed() throws Exception {
		_mockery.checking(new SoundExpectations());

		_subject.open();
		_tupleSpace.acquire(p1());
		_tupleSpace.acquire(p2());

		_tupleSpace.acquire(myPacket(new byte[] {-1, 17, 0, 42}));
		
		_subject.close();
	}
	
	@Test
	public void testTuplesPublishedAfterCloseAreNotPlayed() throws Exception {
		_mockery.checking(new SoundExpectations());
		
		_subject.open();
		
		_tupleSpace.acquire(p1());
		_tupleSpace.acquire(p2());

		_subject.close();
		_tupleSpace.acquire(p1());
	}

	@Test
	public void testPacketsAreSentToDataLine() throws Exception {
		final Sequence main = _mockery.sequence("main");
		_mockery.checking(new CommonExpectations(){{
			one(_audio).openSourceDataLine(); will(returnValue(_line));
			one(_line).isActive(); will(returnValue(false)); inSequence(main);
			one(_line).write(new byte[]{1, 2, 3, 5}, 0, 4); inSequence(main);
			one(_line).write(new byte[]{7, 11, 13, 17}, 0, 4); inSequence(main);
			one(_line).stop(); inSequence(main);
			one(_line).drain(); inSequence(main);
			one(_line).close(); inSequence(main);
			allowing(_line).isActive(); will(returnValue(true));
		}});
		_subject.open();
		
		_consumer.consume(p1());
		_consumer.consume(p2());

		_subject.close();
	}
	
	class SoundExpectations extends CommonExpectations {
		private final Sequence _mainSequence = _mockery.sequence("main");
		
		SoundExpectations() throws Exception {
			one(_buffer).consume(p1()); inSequence(_mainSequence);
			one(_buffer).consume(p2()); inSequence(_mainSequence);
		}
	}

	class CommonExpectations extends Expectations {
		CommonExpectations() throws Exception {
			one(_buffers).createBufferFor(with(aNonNull(Consumer.class))); will(new CustomAction("keep buffer") { @Override public Object invoke(Invocation invocation) {
				_consumer = (Consumer<? super PcmSoundPacket>) invocation.getParameter(0);
				return _buffer;
			}});;
		}
	}

	@SuppressWarnings("deprecation")
	private PublicKey contactKey() {
		return _keyManager.generateMickeyMouseKey("contact");
	}
	
	private PcmSoundPacket myPacket(byte[] pcm) {
		return pcmSoundPacketFor(_keyManager.ownPublicKey(), pcm, (short)1);
	}

	private PcmSoundPacket contactPacket(byte[] pcm, short sequence) {
		return pcmSoundPacketFor(contactKey(), pcm, sequence);
	}
	
	private PcmSoundPacket pcmSoundPacketFor(PublicKey publicKey, final byte[] pcmPayload, short sequence) {
		return new PcmSoundPacket(publicKey, _clock.time(), new ImmutableByteArray(pcmPayload, pcmPayload.length), sequence);
	}
	
	private PcmSoundPacket p1() {
		return contactPacket(new byte[] { 1, 2, 3, 5 }, (short)1);
	}
	
	private PcmSoundPacket p2() {
		return contactPacket(new byte[] { 7, 11, 13, 17 }, (short)2);
	}
}