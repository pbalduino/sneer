package sneer.bricks.software.folderconfig.impl;

import static sneer.foundation.environments.Environments.my;

import java.io.File;

import sneer.bricks.hardware.ram.ref.immutable.Immutable;
import sneer.bricks.hardware.ram.ref.immutable.Immutables;
import sneer.bricks.software.folderconfig.FolderConfig;


public class FolderConfigImpl implements FolderConfig {

	private final Immutable<File> _ownSrcFolder = immutable();
	private final Immutable<File> _ownBinFolder = immutable();

	private final Immutable<File> _srcFolder = immutable();
	private final Immutable<File> _binFolder = immutable();
	private final Immutable<File> _stageFolder = immutable();

	private final Immutable<File> _storageFolder = immutable();
	private final Immutable<File> _tmpFolder = immutable();

	@Override
	public Immutable<File> ownBinFolder() {
		return _ownBinFolder;
	}

	@Override
	public Immutable<File> binFolder() {
		return _binFolder;
	}

	@Override
	public Immutable<File> storageFolder() {
		return _storageFolder;
	}

	@Override
	public File storageFolderFor(Class<?> brick) {
		return brickFolderIn(storageFolder().get(), brick);
	}

	private File brickFolderIn(File parent, Class<?> brick) {
		final File folder = new File(parent, brick.getName().replace(".", "/"));
		folder.mkdirs();
		return folder;
	}

	private static <T> Immutable<T> immutable() {
		return my(Immutables.class).newInstance();
	}

	@Override
	public Immutable<File> ownSrcFolder() {
		return _ownSrcFolder;
	}

	@Override
	public Immutable<File> srcFolder() {
		return _srcFolder;
	}

	@Override
	public File tmpFolderFor(Class<?> brick) {
		return brickFolderIn(tmpFolder().get(), brick);
	}

	@Override
	public Immutable<File> tmpFolder() {
		return _tmpFolder;
	}

	@Override
	public Immutable<File> stageFolder() {
		return _stageFolder;
	}
	
}