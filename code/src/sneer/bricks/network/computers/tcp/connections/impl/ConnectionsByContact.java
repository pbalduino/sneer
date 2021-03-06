package sneer.bricks.network.computers.tcp.connections.impl;

import basis.lang.CacheMap;
import basis.lang.Producer;
import sneer.bricks.network.social.Contact;

class ConnectionsByContact {

	static private final CacheMap<Contact, ByteConnectionImpl> _cache = CacheMap.newInstance();


	static ByteConnectionImpl get(Contact contact) {
		return _cache.get(contact, new Producer<ByteConnectionImpl>() { @Override public ByteConnectionImpl produce() {
			return new ByteConnectionImpl();
		}});
	}


	static ByteConnectionImpl remove(Contact contact) {
		return _cache.remove(contact);
	}


	static Iterable<ByteConnectionImpl> all() {
		return _cache.values();
	}

}
