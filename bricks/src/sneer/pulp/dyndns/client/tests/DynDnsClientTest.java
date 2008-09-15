package sneer.pulp.dyndns.client.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;

import sneer.kernel.container.Container;
import sneer.kernel.container.ContainerUtils;
import sneer.pulp.blinkinglights.BlinkingLights;
import sneer.pulp.blinkinglights.Light;
import sneer.pulp.clock.Clock;
import sneer.pulp.dyndns.client.DynDnsClient;
import sneer.pulp.dyndns.ownaccount.Account;
import sneer.pulp.dyndns.ownaccount.OwnAccountKeeper;
import sneer.pulp.dyndns.ownip.OwnIpDiscoverer;
import sneer.pulp.dyndns.updater.BadAuthException;
import sneer.pulp.dyndns.updater.Updater;
import sneer.pulp.dyndns.updater.UpdaterException;
import sneer.pulp.propertystore.mocks.TransientPropertyStore;
import wheel.lang.exceptions.FriendlyException;
import wheel.reactive.Register;
import wheel.reactive.impl.RegisterImpl;
import wheel.reactive.lists.ListSignal;

public class DynDnsClientTest {
	
	/*

Required Client Behavior

    * Send a unique user agent which includes company name, model number, and software build revision.
    * Check that all input is in valid form before updating.
    * Check that any IP obtained through web-based IP detection is a valid dotted quad numeric IP (eg: 1.2.3.4) before sending it in an update.
    * Only update when the IP address is different from the IP of the last update.

Unacceptable Client Behavior

    * Send requests to or access anything other than /nic/update at the host members.dyndns.org.
    * Reverse engineer web requests to our website to create or delete hostnames.
    * Hardcode the IP address of any of DynDNS servers.
    * Attempt to update after receiving the notfqdn, abuse, nohost, badagent, badauth, badsys return codes or repeated nochg return codes without user intervention.
    * Perform DNS updates to determine whether the client IP needs to be updated.
    * Access our web-based IP detection script (http://checkip.dyndns.com/) more than once every 10 minutes

	 */
	
	final Mockery _context = new JUnit4Mockery();
	final Register<String> _ownIp = new RegisterImpl<String>("123.45.67.89");
	final AccountMock _accountMock = new AccountMock();
	final RegisterImpl<Account> _ownAccount = new RegisterImpl<Account>(_accountMock);
	
	final OwnIpDiscoverer _ownIpDiscoverer = _context.mock(OwnIpDiscoverer.class);
	final OwnAccountKeeper _ownAccountKeeper = _context.mock(OwnAccountKeeper.class);
	final Updater updater = _context.mock(Updater.class);
	final TransientPropertyStore _propertyStore = new TransientPropertyStore();
	
	@Test
	public void updateOnIpChange() throws Exception {
		_context.checking(new Expectations() {{
			exactly(2).of(_ownIpDiscoverer).ownIp();
				will(returnValue(_ownIp.output()));
				
			atLeast(1).of(_ownAccountKeeper).ownAccount();
				will(returnValue(_ownAccount.output()));
				
			final Account account = _ownAccount.output().currentValue();
			exactly(1).of(updater).update(account.host(), account.user(), account.password(), _ownIp.output().currentValue());
		}});
		

		startDynDnsClient();
		
		startDynDnsClient();
		
		_context.assertIsSatisfied();
	}
	
	@Test
	public void retryAfterIOException() throws Exception {
		
		final IOException error = new IOException();
		
		_context.checking(new Expectations() {{
			allowing(_ownIpDiscoverer).ownIp();
				will(returnValue(_ownIp.output()));
				
			allowing(_ownAccountKeeper).ownAccount();
				will(returnValue(_ownAccount.output()));
				
			final Account account = _ownAccount.output().currentValue();
			exactly(1).of(updater).update(account.host(), account.user(), account.password(), _ownIp.output().currentValue());
				will(throwException(error));
				
			exactly(1).of(updater).update(account.host(), account.user(), account.password(), _ownIp.output().currentValue());
		}});
		

		final Container container = startDynDnsClient();
		final Light light = assertBlinkingLight(error, container);
		
		container.produce(Clock.class).advanceTime(300001);
		assertFalse(light.isOn());
		_context.assertIsSatisfied();
	}
	
	@Test
	public void userInterventionRequiredAfterFailure() throws UpdaterException, IOException {
		
		final BadAuthException error = new BadAuthException();
		final Account account = _ownAccount.output().currentValue();
		final String newIp = "111.111.111.111";
		
		_context.checking(new Expectations() {{
			allowing(_ownIpDiscoverer).ownIp();
				will(returnValue(_ownIp.output()));
			allowing(_ownAccountKeeper).ownAccount();
				will(returnValue(_ownAccount.output()));
			
			exactly(1).of(updater).update(account.host(), account.user(), account.password(), _ownIp.output().currentValue());
				will(throwException(error));
				
			exactly(1).of(updater).update(account.host(), account.user(), "*" + account.password(), newIp);
		}});
		
		final Container container = startDynDnsClient();
		final Light light = assertBlinkingLight(error, container);
		
		// new ip should be ignored while new account is not provided
		_ownIp.setter().consume(newIp);
		
		// providing a new account should cause it
		// to resume updating dyndns
		AccountMock mock = new AccountMock();
		mock.password = "*test";
		_ownAccount.setter().consume(mock);
		assertFalse(light.isOn());
		
		_context.assertIsSatisfied();
	}

	private Light assertBlinkingLight(final Exception expectedError, final Container container) {
		final ListSignal<Light> lights = container.produce(BlinkingLights.class).lights();
		assertEquals(1, lights.currentSize());
		final Light light = lights.currentGet(0);
		assertTrue(light.isOn());
		if (expectedError instanceof FriendlyException) {
			assertEquals(((FriendlyException)expectedError).getHelp(), light.message());
		}
		assertSame(expectedError, light.error());
		return light;
	}

	private Container startDynDnsClient() {
		final Container container = ContainerUtils.newContainer(_ownIpDiscoverer, _ownAccountKeeper, updater, _propertyStore);
		container.produce(DynDnsClient.class);
		return container;
	}
}

class AccountMock implements Account{
	String password = "test";
	@Override public String host() { return "test.dyndns.org";}
	@Override public String user() {return "test";}
	@Override public String password() {return password;}
};