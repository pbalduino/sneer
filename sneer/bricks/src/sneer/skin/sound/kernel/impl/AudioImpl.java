package sneer.skin.sound.kernel.impl;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import sneer.skin.sound.kernel.Audio;

class AudioImpl implements Audio {

	@Override
	public SourceDataLine openSourceDataLine() throws LineUnavailableException {
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat());
		SourceDataLine dataLine = (SourceDataLine) AudioSystem	.getLine(info);
		dataLine.open();
		dataLine.start();
		return dataLine;
	}

	@Override
	public TargetDataLine openTargetDataLine() throws LineUnavailableException {
		TargetDataLine dataLine = AudioSystem	.getTargetDataLine(audioFormat());
		dataLine.open();
		dataLine.start();
		return dataLine;
	}

	@Override
	public AudioFormat audioFormat() {
		return AudioUtil.AUDIO_FORMAT;
	}

	@Override
	public int framesPerAudioPacket() {
		return AudioUtil.FRAMES_PER_AUDIO_PACKET;
	}

	@Override
	public int narrowbandEncoding() {
		return AudioUtil.NARROWBAND_ENCODING; 
	}

	@Override
	public int sampleRate() {
		return AudioUtil.SAMPLE_RATE;
	}

	@Override
	public int sampleSizeInBits() {
		return AudioUtil.SAMPLE_SIZE_IN_BITS;
	}

	@Override
	public boolean signed() {
		return AudioUtil.SIGNED;
	}

	@Override
	public int soundQuality() {
		return AudioUtil.SOUND_QUALITY;
	}

	@Override
	public SourceDataLine openSourceDataLine(AudioFormat audioFormat) {
		throw new wheel.lang.exceptions.NotImplementedYet(); // Implement
	}
}
