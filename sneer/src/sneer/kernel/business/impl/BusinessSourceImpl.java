package sneer.kernel.business.impl;

import java.io.Serializable;

import sneer.kernel.business.Business;
import sneer.kernel.business.BusinessSource;
import sneer.kernel.business.contacts.Contact;
import sneer.kernel.business.contacts.ContactInfo;
import sneer.kernel.business.contacts.ContactSource;
import sneer.kernel.business.contacts.impl.ContactAdder;

import wheel.io.network.PortNumberSource;
import wheel.lang.Consumer;
import wheel.lang.IntegerConsumerBoundaries;
import wheel.lang.Omnivore;
import wheel.lang.exceptions.NotImplementedYet;
import wheel.reactive.Signal;
import wheel.reactive.SourceImpl;
import wheel.reactive.lists.ListSignal;
import wheel.reactive.lists.ListSource;
import wheel.reactive.lists.impl.ListSourceImpl;


public class BusinessSourceImpl implements BusinessSource, Business  { //Refactor: Create a separate class for BusinessImpl.

	private SourceImpl<String> _ownName = new SourceImpl<String>("");

	private PortNumberSource _sneerPortNumber = new PortNumberSource(0);

	private final ListSource<ContactSource> _contactSources = new ListSourceImpl<ContactSource>();
	private final ListSource<Contact> _contacts = new ListSourceImpl<Contact>(); 	//Refactor: use a reactive "ListCollector" instead of keeping this redundant list.


	
	public Signal<String> ownName() {
		return _ownName.output();
	}
	
	public Omnivore<String> ownNameSetter() {
		return _ownName.setter();
	}

	public Signal<Integer> sneerPort() {
		return _sneerPortNumber.output();
	}
	
	public Consumer<Integer> sneerPortSetter() {
		return _sneerPortNumber.setter();
	}

	public ListSignal<Contact> contacts() {
		return _contacts.output();
	}

	public Consumer<ContactInfo> contactAdder() {
		return new ContactAdder(_contactSources, _contacts);
	}

	public Business output() {
		return this;
	}
}
