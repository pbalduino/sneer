//Implement Tests:
//Incoming - PK same as mine - Reject connection
//Incoming - PK Known (Happy Day)
//Incoming - PK Unknown - Create new UNCONFIRMED contact
//Outgoing - Veio PK do Proprio Contato (Happy Day)
//Outgoing - Veio PK Igual de Outro Contato: Sinalizar com um status de erro em ambos. Voltar ambos para UNCONFIRMED.
//Outgoing - Veio PK Nova - Contato N tinha PK: persiste PK - UNCONFIRMED (Happy Day)
//Outgoing - Veio PK Nova - Contato Já Tinha PK Diferente (Contato pode ter reinstalado o Sneer, por exemplo): Mostra warning e seta status do contato p unconfirmed.
//UNCONFIRMED - Confiabilidade zero. Pode tornar-se CONFIRMED.
//
//Merge de dois contatos que representam o mesmo cara q gerou nova chave publica pra si.

package sneer.kernel.communication.impl;


import static wheel.i18n.Language.translate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sneer.kernel.business.Business;
import sneer.kernel.business.contacts.ContactAttributes;
import sneer.kernel.business.contacts.ContactInfo;
import sneer.kernel.business.contacts.ContactManager;
import sneer.kernel.business.contacts.ContactPublicKeyInfo;
import sneer.kernel.communication.Channel;
import sneer.kernel.communication.Operator;
import sneer.kernel.communication.Packet;
import wheel.io.Log;
import wheel.io.network.ObjectSocket;
import wheel.io.network.OldNetwork;
import wheel.io.ui.CancelledByUser;
import wheel.io.ui.User;
import wheel.lang.Consumer;
import wheel.lang.Omnivore;
import wheel.lang.exceptions.IllegalParameter;
import wheel.reactive.Signal;

public class Communicator {

	public Communicator(User user, OldNetwork network, Business business, ContactManager contactManager) {
		_user = user;
		_business = business;
		_contactManager = contactManager;
		_spider = new Spider(network, _business.contactAttributes(), outgoingConnectionValidator(), myPacketReceiver());
		new SocketAccepter(user, network, _business.sneerPort(), mySocketServer());
	}

	private final ContactManager _contactManager;
	private final Business _business;
	private final User _user;
	private Spider _spider;
	private Map<String, ChannelImpl> _channelsById = new HashMap<String, ChannelImpl>();
	
	private Omnivore<ChannelPacket> myPacketReceiver() {
		return new Omnivore<ChannelPacket>() { public void consume(ChannelPacket received) {
			receive(received);
		}};
	}

	private void receive(ChannelPacket receivedPacket) {
		ChannelImpl channel = _channelsById.get(receivedPacket._channelId);
		if (channel == null) {
			Log.log(translate("Unknown channel being used by some contact: %1$s\nHe might have a different Sneer version or some Application you dont have.", receivedPacket._channelId));
			return;
		}
		try {
			channel.receive(receivedPacket._packet);
		} catch (ClassNotFoundException e) {
			Log.log(translate("Unknown packet class being used by some contact: %1$s\nHe might have a different Sneer version or some Application you dont have.", e.getMessage()));
		} catch (Throwable t) {
			Log.log("Channel throwed Throwable below. Channel: " + receivedPacket._channelId);
			Log.log(t);
		}
	}

	private Consumer<OutgoingConnectionAttempt> outgoingConnectionValidator() {
		return new Consumer<OutgoingConnectionAttempt>() { public void consume(OutgoingConnectionAttempt attempt) throws IllegalParameter {
			ObjectSocket socket = attempt._outgoingSocket;
			String remotePK;
			try {
				socket.writeObject(ownPublicKey().currentValue());
				socket.writeObject(ownName().currentValue());
				remotePK = (String)socket.readObject();
			} catch (IOException e) {
				throw new IllegalParameter("");
			} catch (ClassNotFoundException e) {
				//Implement: Log properly. See how ConnectionImpl handles ClassNotFoundExceptions.
				throw new IllegalParameter("");
			}

			String contactsPK = attempt._contact.publicKey().currentValue();
			if (remotePK.equals(contactsPK)) return;

			String nick = attempt._contact.nick().currentValue();
			
			ContactAttributes thirdParty = findContactGivenPublicKey(remotePK);
			if (thirdParty != null) {
				handleDuplicatePK(nick, thirdParty);
				throw new IllegalParameter(translate("Remote contact has same public key as another contact."));
			}
			
			if (!contactsPK.isEmpty()) notifyUserOfPKMismatch(nick);
			
			_contactManager.contactPublicKeyUpdater().consume(new ContactPublicKeyInfo(nick, remotePK));
		} };

	}

	private void handleDuplicatePK(String nick, ContactAttributes thirdParty) {
		_user.acknowledgeNotification(translate("%1$s has the same public key as %2$s. You must delete one of them.",nick,thirdParty.nick().currentValue())); //Fix: update error state for the contact. 
	}

	private void notifyUserOfPKMismatch(String nick) {
		 //Fix: Security implementation: Revert the identity of the contact to unconfirmed, so that the user has to confirm the remote PK again.
		String notification = translate(
			"SECURITY ALERT FOR CONTACT: %1$s\n\n" +
			"Either this contact has changed its public key or\n" +
			"someone else is trying to trick you and impersonate it.\n\n" +
			"This contact's identity will be changed to 'UNCONFIRMED',\n" +
			"so that you can confirm its public key again.", nick);
		_user.acknowledgeNotification(notification);
	}

	private Signal<String> ownPublicKey() {
		return _business.publicKey();
	}

	private Signal<String> ownName() {
		return _business.ownName();
	}

	public Channel openChannel(String channelId, int priority) {
		return openChannel(channelId, priority, this.getClass().getClassLoader());
	}

	public Channel openChannel(String channelId, int priority, ClassLoader classLoader) {
		ChannelImpl result = new ChannelImpl(outputFor(channelId, priority), classLoader);
		_channelsById.put(channelId, result);
		return result;
	}
	
	public void crashChannel(String channelId){
		_channelsById.remove(channelId);
	}

	private Omnivore<Packet> outputFor(final String channelId, final int priority) {
		return new Omnivore<Packet>() { public void consume(Packet packet) {
			ConnectionImpl connection = _spider.connectMeWith(packet._contactId);
			connection.send(new ChannelPacket(channelId, packet), priority);
		}};
	}


	private Omnivore<ObjectSocket> mySocketServer() {
		return new Omnivore<ObjectSocket>() { public void consume(ObjectSocket socket) {
			serve(socket);
		} };
	}

	private void serve(ObjectSocket socket) {
		if (tryToServe(socket)) return;
		
		try {
			socket.close();
		} catch (IOException e) {}
	};

	private boolean tryToServe(final ObjectSocket socket) {
		String publicKey;
		String name;
		try {
			publicKey = (String)socket.readObject();
			name = (String)socket.readObject();
		} catch (IOException ignored) {
			ignored.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		if (ownPublicKey().currentValue().equals(publicKey)) return false;
		
		ContactAttributes contact = findContactGivenPublicKey(publicKey);
		
		try {
			if (contact == null) contact = produceContactWithNewPublicKey(name, publicKey);
		} catch (CancelledByUser e) {
			return false;
		}

		try {
			socket.writeObject(ownPublicKey().currentValue());
		} catch (IOException ignored) {
			return false;
		}
		
		_spider.connectMeWith(contact.id()).serveAcceptedSocket(socket);
		return true;
	}


	private ContactAttributes produceContactWithNewPublicKey(String name, String publicKey) throws CancelledByUser {
		String prompt = translate(
				"Someone claiming to be\n\n%1$s\n\n is trying to connect to you. Do you want\n" +
				"to accept the connection?",name);
		if (!_user.confirm(prompt)) throw new CancelledByUser();

		String nick;
		ContactAttributes existing;
		while (true) {
			nick = _user.answer(translate("Enter a nickname for your new contact:"), name);
			
			existing = findContactGivenNick(nick);
			if (existing == null) return createContact(publicKey, nick);
			
			if (existing.publicKey().currentValue().isEmpty()) break;
			_user.acknowledgeNotification(translate("There already is another contact with this nickname:\n\n%1$s",nick), translate("Choose Another..."));
		}
		
		_contactManager.contactPublicKeyUpdater().consume(new ContactPublicKeyInfo(nick, publicKey)); //Refactor: Use contactId instead of nick;
		
		return existing;
	}


	private ContactAttributes createContact(String publicKey, String nick) throws CancelledByUser {
		try {
			_contactManager.contactAdder().consume(new ContactInfo(nick, "", 0, publicKey)); //Implement: get actual host addresses from contact.
			return findContactGivenNick(nick);
		} catch (IllegalParameter e) {
			_user.acknowledge(e);
			throw new CancelledByUser();
		}
	}


	private ContactAttributes findContactGivenNick(String nick) {
		for (ContactAttributes contact : _business.contactAttributes())
			if (nick.equals(contact.nick().currentValue())) return contact;
		return null;
	}


	private ContactAttributes findContactGivenPublicKey(String publicKey) {
		for (ContactAttributes contact : _business.contactAttributes())
			if (publicKey.equals(contact.publicKey().currentValue())) return contact;
		return null;
	}

	public Operator operator() {
		return _spider;
	}

}
