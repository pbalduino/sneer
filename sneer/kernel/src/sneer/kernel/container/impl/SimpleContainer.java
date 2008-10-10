package sneer.kernel.container.impl;

import java.io.File;
import java.lang.reflect.Constructor;

import sneer.kernel.container.Brick;
import sneer.kernel.container.ClassLoaderFactory;
import sneer.kernel.container.Container;
import sneer.kernel.container.Injector;
import sneer.kernel.container.LegoException;
import sneer.kernel.container.SneerConfig;
import sneer.kernel.container.impl.classloader.EclipseClassLoaderFactory;
import sneer.pulp.tuples.Tuple;
import wheel.io.Logger;
import wheel.lang.Types;

public class SimpleContainer implements Container {
	
	private ClassLoaderFactory _classloaderFactory;
	
	private final Injector _injector = new AnnotatedFieldInjector(this);
	
	private final SimpleBinder _binder = new SimpleBinder();
	
	private SneerConfig _sneerConfig;

	private ClassLoader _apiClassLoader;

	public SimpleContainer(Object... bindings) {
		for (Object implementation : bindings)
			_binder.bind(implementation);
		
		_binder.bind(this);
		_binder.bind(_injector);
		
		_injector.inject(Tuple.class); //Refactor this looks wierd here.
	}

	@Override
	public Class<? extends Brick> resolve(String brickName) throws ClassNotFoundException {
		if (null == _apiClassLoader) {
			_apiClassLoader = factory().newApiClassLoader();
		}
		return Types.cast(_apiClassLoader.loadClass(brickName));
	}

	@Override
	public <T> T produce(Class<T> type) {
		T result = (T)_binder.implementationFor(type);
		if (result == null) {
			result = instantiate(type);
			_binder.bind(result);
		}
		return result;
	}
	
	private <T> T instantiate(Class<T> intrface) throws LegoException {
		T component;
		try {
			component = lookup(intrface);
		} catch (ClassNotFoundException e) {
			Logger.log("Impl for {} not found. Please wait while generating.", intrface);
			String message = ImplementationGenerator.generateFor(intrface);
			throw new LegoException(message, e);
		} catch (Exception e) {
			throw new LegoException(" >>>" + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}

		inject(component);
		return component;
	}


	private <T> T lookup(Class<T> clazz) throws Exception {

		Object result = bindingFor(clazz);
	    if(result != null) return (T)result;

		String implementation = implementationFor(clazz); 
		File brickDirectory = sneerConfig().brickDirectory(clazz);
		ClassLoader cl = getClassLoader(clazz, brickDirectory);
		Class<?> impl = cl.loadClass(implementation);
		result = construct(impl);
		return (T)result;		

	}

	private <T> Object construct(Class<?> impl) throws Exception {
		Constructor<?> c = impl.getDeclaredConstructor();
		c.setAccessible(true);
		return c.newInstance();
	}

	private ClassLoader getClassLoader(Class<?> brickClass, File brickDirectory) {
		ClassLoader result = factory().produceBrickClassLoader(brickClass, brickDirectory);
		inject(result);
		return result;
	}

	private ClassLoaderFactory factory() {
		if(_classloaderFactory == null) {
			_classloaderFactory = new EclipseClassLoaderFactory(sneerConfig());
		}
		return _classloaderFactory;
	}

    private Object bindingFor(Class<?> type) {
        return _binder.implementationFor(type);
    }

	private String implementationFor(Class<?> type) {
		if(!type.isInterface())
			return type.getName();
		
		return ImplementationGenerator.implementationNameFor(type.getName());
	}

	//Fix: check if this code will work on production
	//Hack
	private SneerConfig sneerConfig() {
		if(_sneerConfig != null) 
			return _sneerConfig;
		
		Object result = bindingFor(SneerConfig.class);
		if(result != null) {
			_sneerConfig = (SneerConfig) result;
			return _sneerConfig;
		}
		_sneerConfig = new SneerConfigImpl();
		return _sneerConfig;
	}

	private void inject(Object component) {
		try {
			_injector.inject(component);
		} catch (Throwable t) {
			throw new LegoException("Error injecting dependencies on: "+component, t);
		}
	}
}
