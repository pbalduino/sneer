package sneer.bricks.software.bricks.interception.tests.fixtures.combinedmethods.impl;

import sneer.bricks.software.bricks.interception.tests.fixtures.combinedmethods.Greeter;

class GreeterBase implements Greeter {

	@Override
	public String hello() {
		return "Hello!";
	}

}
