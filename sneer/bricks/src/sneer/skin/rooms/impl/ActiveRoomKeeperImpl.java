package sneer.skin.rooms.impl;

import sneer.pulp.reactive.Register;
import sneer.pulp.reactive.Signal;
import sneer.pulp.reactive.impl.RegisterImpl;
import sneer.skin.rooms.ActiveRoomKeeper;
import wheel.lang.Consumer;

class ActiveRoomKeeperImpl implements ActiveRoomKeeper {

	private final Register<String> _register = new RegisterImpl<String>("");

	@Override
	public Signal<String> room() {
		return _register.output();
	}

	@Override
	public Consumer<String> setter() {
		return _register.setter();
	}

}
