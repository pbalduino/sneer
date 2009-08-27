package sneer.foundation.brickness.testsupport;


import static sneer.foundation.environments.Environments.my;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.internal.ExpectationBuilder;
import org.junit.After;
import org.junit.runner.RunWith;

import sneer.bricks.hardware.cpu.threads.Threads;
import sneer.bricks.hardware.io.log.Logger;
import sneer.bricks.software.folderconfig.FolderConfig;
import sneer.foundation.environments.Environment;
import sneer.foundation.testsupport.CleanTest;

@RunWith(BrickTestWithMockRunner.class)
public abstract class BrickTest extends CleanTest {
	
	private final Mockery _mockery = new JUnit4Mockery();
	
	@Bind private final LoggerMocks _loggerMocks = new LoggerMocks(); 
	@SuppressWarnings("unused") @Bind private final Logger _logger = _loggerMocks.newInstance(); 
    
	{
		my(BrickTestRunner.class).instanceBeingInitialized(this);
		my(FolderConfig.class).storageFolder().set(new File(tmpFolderName(), "data"));
		my(FolderConfig.class).tmpFolder().set(new File(tmpFolderName(), "tmp"));
	}


	
	protected Sequence newSequence(String name) {
		return _mockery.sequence(name);
	}
	
	protected <T> T mock(Class<T> type) {
		return _mockery.mock(type);
	}

	protected <T> T mock(String name, Class<T> type) {
		return _mockery.mock(type, name);
	}
	
	protected void checking(ExpectationBuilder expectations) {
		_mockery.checking(expectations);
	}

	protected Environment newTestEnvironment(Object... bindings) {
		return my(BrickTestRunner.class).cloneTestEnvironment(bindings);
	}

	
	
	@After
	public void afterBrickTest() {
		my(Threads.class).crashAllThreads();
		my(BrickTestRunner.class).dispose();
	}
	
	
	@Override
	protected void afterFailedtest(Method method, Throwable thrown) {
		try {
			printContext(method, thrown);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}


	private void printContext(Method method, Throwable thrown) {
		System.out.println();
		System.out.println();
		System.out.println("Failed Test: =====================================================================================================");
		System.out.println(getClass().getName() + "  " + method.getName());
		System.out.println();
		System.out.println("Filtered Stack: ==================================================================================================");
		System.out.println(filteredStack(thrown));
		System.out.println("Log: =============================================================================================================");
		my(LoggerMocks.class).printAllKeptMessages();
		System.out.println("==================================================================================================================");
		System.out.println();
		System.out.println();
	}


	private String filteredStack(Throwable thrown) {
		StringBuilder result = new StringBuilder();
		
		for (String line : getTrace(thrown).split("\n"))
			if (isInteresting(line))
				result.append(line + "\n");
		
		return result.toString();
	}


	private boolean isInteresting(String stackLine) {
		return !startsWithAny(stackLine,
			"java",
			"sun",
			"$Proxy",
			"org.junit",
			"sneer.foundation.brickness.testsupport",
			"sneer.tests.adapters.ProxyInEnvironment",
			"sneer.foundation.environments.Environments"
		);
	}


	private boolean startsWithAny(String stackLine, String... prefixes) {
		for (String prefix : prefixes)
			if (has(stackLine, "at " + prefix)) return true;
		return false;
	}


	private boolean has(String line, String token) {
		return line.indexOf(token) != -1;
	}
	
	private static String getTrace(Throwable throwable) {
		StringWriter result= new StringWriter();
		throwable.printStackTrace(new PrintWriter(result));
		return result.getBuffer().toString();
	}
}
