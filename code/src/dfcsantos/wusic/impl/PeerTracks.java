package dfcsantos.wusic.impl;

import static sneer.foundation.environments.Environments.my;

import java.io.File;
import java.io.IOException;

import sneer.bricks.hardware.cpu.lang.contracts.WeakContract;
import sneer.bricks.hardware.io.IO;
import sneer.bricks.pulp.blinkinglights.BlinkingLights;
import sneer.bricks.pulp.blinkinglights.LightType;
import sneer.foundation.lang.Consumer;
import dfcsantos.tracks.Track;
import dfcsantos.tracks.folder.TracksFolderKeeper;
import dfcsantos.tracks.playlist.Playlist;
import dfcsantos.tracks.playlist.Playlists;

class PeerTracks extends TrackSourceStrategy {

	static final TrackSourceStrategy INSTANCE = new PeerTracks();

	@SuppressWarnings("unused")	private final WeakContract _refToAvoidGC;

	private PeerTracks() {
		_refToAvoidGC = my(TracksFolderKeeper.class).peerTracksFolder().addReceiver(new Consumer<File>() {@Override public void consume(File peerTracksFolder) {
			setTracksFolder(peerTracksFolder);
			initPlaylist();
		}});
	};

	@Override
	Playlist createPlaylist(File tracksFolder) {
		return my(Playlists.class).newRandomPlaylist(tracksFolder);
	}

	void meToo(Track trackToKeep) {
		final File destFolder = my(TracksFolderKeeper.class).peerTracksFolder().currentValue();
		try {
			my(IO.class).files().copyFileToFolder(trackToKeep.file(), destFolder);
		} catch (IOException e) {
			my(BlinkingLights.class).turnOn(LightType.WARN, "Unable to copy track", "Unable to copy track: " + trackToKeep.file(), 7000);
		}
	}

	void noWay(Track trackToDiscard) {
		if (!trackToDiscard.file().delete())
			my(BlinkingLights.class).turnOn(LightType.WARN, "Unable to delete track", "Unable to delete track: " + trackToDiscard.file(), 7000);
	}

}
