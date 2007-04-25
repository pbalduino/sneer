package sneer.kernel.gui;

import java.net.URL;

import org.friends.ui.ShowContactsScreenAction;
import org.prevayler.Prevayler;

import sneer.kernel.business.Business;
import wheel.io.ui.CancelledByUser;
import wheel.io.ui.TrayIcon;
import wheel.io.ui.User;
import wheel.io.ui.TrayIcon.Action;
import wheel.io.ui.impl.TrayIconImpl;

public class Gui {

	public Gui(User user, Prevayler prevayler) throws Exception {
		_user = user;

		_prevayler = prevayler;
		_business = (Business)_prevayler.prevalentSystem();

		URL icon = Gui.class.getResource("/sneer/kernel/gui/traymenu/yourIconGoesHere.png");
		_trayIcon = new TrayIconImpl(icon, _user.catcher());
	
		tryToRun();
	}


	private final User _user;
	private final TrayIcon _trayIcon;
	
	private final Prevayler _prevayler;
	private final Business _business;
	
	
	private void tryToRun() {
		//Refactor remove this logic from the gui;
		if (_business.ownName() == null) nameChangeAction().run();
		if (_business.sneerPortNumber() == 0) changeSneerPort();

		_trayIcon.addAction(nameChangeAction());
		_trayIcon.addAction(addNewContactAction());
		_trayIcon.addAction(listContactsAction());
		_trayIcon.addAction(new ShowContactsScreenAction(_business));
		_trayIcon.addAction(exitAction());
	}

	

	private Action nameChangeAction() {
		return new NameChange(_user, _business.ownName(), _business.ownNameSetter());
	}

	private void changeSneerPort() {
		//Implement
	}

	
	private Action addNewContactAction() {
		return new CancellableAction(){

			public String caption() {
				return "Add New Contact";
			}

			@Override
			public void tryToRun() throws CancelledByUser {
				_prevayler.execute(new NewContactAddition(_user));
			}
		};
	}

	
	private Action listContactsAction() {
		return new Action(){
			public String caption() {
				return "List Contacts";
			}

			public void run() {
				new ContactsListing(_user, _business);
			}
		};
	}

	
	private Action exitAction() {
		return new Action(){

			public String caption() {
				return "Exit";
			}

			public void run() {
				System.exit(0);
			}
		};
	}

	
}
