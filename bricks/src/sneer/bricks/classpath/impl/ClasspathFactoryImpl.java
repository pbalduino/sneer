package sneer.bricks.classpath.impl;

import java.io.File;
import java.util.List;

import sneer.bricks.classpath.Classpath;
import sneer.bricks.classpath.ClasspathFactory;
import sneer.bricks.config.SneerConfig;
import sneer.lego.Brick;
import sneer.lego.Inject;
import wheel.io.Jars;

public class ClasspathFactoryImpl implements ClasspathFactory {

	@Inject
	private SneerConfig _config;
	
	private Classpath _sneerApi;
	
	@Override
	public Classpath fromLibDir(File folder) {
		return new LibdirClasspath(folder);
	}

	@Override
	public Classpath newClasspath() {
		return new SimpleClasspath();
	}

	@Override
	public Classpath fromDirectory(File rootFolder) {
		return new DirectoryBasedClasspath(rootFolder);
	}

	@Override
	public Classpath sneerApi() {
		if(_sneerApi != null) {
			return _sneerApi;
		}
		
		try {
			/* try to load from sneer.jar */
			Jars.jarGiven(Brick.class);
			throw new wheel.lang.exceptions.NotImplementedYet();	
		} catch(StringIndexOutOfBoundsException e) {
			/*  
			 * running from eclipse ?
			 */

			Classpath cp = newClasspath();
			Classpath kernel_plus_wheel = new DirectoryBasedClasspath(_config.eclipseDirectory());
			_sneerApi = cp.compose(kernel_plus_wheel);
			return _sneerApi;
		}
	}

	@Override
	public Classpath fromJarFiles(List<File> jarFiles) {
		return new JarBasedClasspath(jarFiles);
	}

}

class SimpleClasspath extends JarBasedClasspath {
	SimpleClasspath() {
		super(RT_JAR);
	}
}
