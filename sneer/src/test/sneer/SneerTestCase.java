package sneer;

import java.io.File;
import java.io.IOException;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import sneer.Sneer.User;
import wheel.experiments.environment.network.mocks.NetworkMock;

public class SneerTestCase extends MockObjectTestCase {
	
	private String _testDirectory;

	@Override
	protected void setUp() {
		_testDirectory = "testruns" + File.separator + System.nanoTime();
	}
	
	public void testPersistence() throws IOException {
		
		Mock mocker = mock(User.class);
		User user = (User) mocker.proxy();
		
		mocker.stubs().method("lookAtMe");
		
		mocker.expects(once()).method("confirmName")
			.will(returnValue("Neide da Silva"));
		mocker.expects(once()).method("confirmServerPort")
			.with(eq(Home.DEFAULT_PORT))
			.will(returnValue(4242));
		Sneer sneer = new Sneer(user, new NetworkMock(), _testDirectory);

		mocker.expects(once()).method("confirmName")
			.will(returnValue("Neide da Silva"));
		mocker.expects(once()).method("thoughtOfTheDay")
			.will(returnValue("Las llamas son majores que las ranas!"));
		mocker.stubs().method("confirmPicture");
		sneer.editPersonalInfo();		
		
		sneer = new Sneer(user, new NetworkMock(), _testDirectory);
		assertEquals("Las llamas son majores que las ranas!", sneer.life().thoughtOfTheDay().currentValue()); 
	}

	public void testAddContact() throws IOException {
		
		Mock mocker = mock(User.class);
		User user = (User) mocker.proxy();
		
		mocker.stubs().method("lookAtMe");
		mocker.stubs().method("confirmName")
			.will(returnValue("ignored"));
		mocker.stubs().method("confirmServerPort")
			.will(returnValue(4242));
		
		Sneer sneer = new Sneer(user, new NetworkMock(), _testDirectory);		
		
		mocker.expects(once()).method("giveNickname")
			.will(returnValue("fefe"));
		mocker.expects(once()).method("informTcpAddress")
			.will(returnValue("localhost:4242"));
		sneer.addContact();

		mocker.expects(once()).method("giveNickname")
			.will(returnValue("bamboo"));
		mocker.expects(once()).method("informTcpAddress")
			.will(returnValue("localhost:4243"));
		sneer.addContact();

		assertTrue(sneer.life().nicknames().contains("fefe"));
		assertTrue(sneer.life().nicknames().contains("bamboo"));

		sneer = new Sneer(user, new NetworkMock(), _testDirectory);		
		assertTrue(sneer.life().nicknames().contains("fefe"));
		assertTrue(sneer.life().nicknames().contains("bamboo"));
	}
	
}
