package sneer.bricks.network.computers.addresses.dns.tests;


import static sneer.foundation.environments.Environments.my;

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;

import sneer.bricks.hardware.ram.arrays.ImmutableByteArray;
import sneer.bricks.identity.seals.Seal;
import sneer.bricks.identity.seals.contacts.ContactSeals;
import sneer.bricks.network.computers.addresses.dns.Dns;
import sneer.bricks.network.computers.addresses.dns.DnsEntry;
import sneer.bricks.network.computers.addresses.dns.tests.mock.MockContactSighting;
import sneer.bricks.network.computers.sockets.connections.ConnectionManager;
import sneer.bricks.network.computers.sockets.connections.ContactSighting;
import sneer.bricks.network.social.Contact;
import sneer.bricks.network.social.Contacts;
import sneer.bricks.pulp.events.EventNotifier;
import sneer.bricks.pulp.events.EventNotifiers;
import sneer.bricks.pulp.tuples.Tuple;
import sneer.bricks.pulp.tuples.TupleSpace;
import sneer.bricks.software.folderconfig.tests.BrickTest;
import sneer.foundation.brickness.testsupport.Bind;

public class DnsTest extends BrickTest {

	@Bind private final ConnectionManager _connectionManager = mock(ConnectionManager.class);
	@Bind private final ContactSeals _seals = mock(ContactSeals.class);
	private final EventNotifier<ContactSighting> _sightingsSource = my(EventNotifiers.class).newInstance();

	{
		checking(new Expectations() {{
			allowing(_connectionManager).contactSightings(); will(returnValue(_sightingsSource.output()));
			allowingContactGivenSeal("foo");
			allowingContactGivenSeal("bar");
		}

		private void allowingContactGivenSeal(String contact) {
			allowing(_seals).contactGiven(sealForContact(contact)); will(returnValue(my(Contacts.class).produceContact(contact)));
		}});	
	}
	
	private final Dns _subject = my(Dns.class);
	
	
	@Test (timeout = 2000)
	public void testDnsOneContactSighted(){
		see("foo", "10.42.10.42");
		Assert.assertArrayEquals(new String[]{"10.42.10.42"}, knownIpsForContact("foo"));
	}
	
	@Test (timeout = 2000)
	public void testDnsOneContactSightedTwice(){
		see("foo", "10.42.10.42");
		see("foo", "10.42.10.43");
		Assert.assertArrayEquals(new String[]{"10.42.10.42", "10.42.10.43"}, knownIpsForContact("foo"));
	}
	
	@Test (timeout = 2000)
	public void testDnsTwoContactsSighted(){
		see("foo", "10.42.10.42");
		see("bar", "10.42.10.43");
		Assert.assertArrayEquals(new String[]{"10.42.10.42"}, knownIpsForContact("foo"));
		Assert.assertArrayEquals(new String[]{"10.42.10.43"}, knownIpsForContact("bar"));
	}
	
	@Test (timeout = 2000)
	public void testSightingsAreConvertedToTuples(){
		see("foo", "10.42.10.42");
		assertDnsEntryKept("foo", "10.42.10.42");
	}

	private void assertDnsEntryKept(final String seal, final String ip) {
		assertTupleKept(new DnsEntry(sealForContact(seal), ip));
	}

	private void assertTupleKept(final Tuple expected) {
		assertTrue(my(TupleSpace.class).keptTuples().contains(expected));
	}

	
	private Object[] knownIpsForContact(String contact) {
		return  _subject.knownIpsForContact(contact(contact)).currentElements().toArray();
	}

	
	private Contact contact(String nickname) {
		return my(Contacts.class).produceContact(nickname);
	}

	private void see(String contact, String ip) {
		Seal seal = sealForContact(contact);
		_sightingsSource.notifyReceivers(new MockContactSighting(ip, seal));
	}

	
	private Seal sealForContact(String contact) {
		return new Seal(new ImmutableByteArray(contact.getBytes()));
	}
	
	

	
}
