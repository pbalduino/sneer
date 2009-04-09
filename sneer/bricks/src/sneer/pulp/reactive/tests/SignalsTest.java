package sneer.pulp.reactive.tests;

import static sneer.commons.environments.Environments.my;

import org.junit.Test;

import sneer.brickness.testsupport.BrickTest;
import sneer.commons.lang.Functor;
import sneer.pulp.reactive.Register;
import sneer.pulp.reactive.Signal;
import sneer.pulp.reactive.Signals;
import sneer.pulp.reactive.impl.RegisterImpl;



public class SignalsTest extends BrickTest {

	private final Signals _subject = my(Signals.class);

	@Test
	public void adapt() {
		Register<Integer> register = new RegisterImpl<Integer>(1);
		
		Signal<String> output = _subject.adapt(register.output(), new Functor<Integer, String>() { @Override public String evaluate(Integer value) {
			return value == 1 ? "one" : "something else";
		}});
		
		assertEquals("one", output.currentValue());
		register.setter().consume(42);
		assertEquals("something else", output.currentValue());
	}

	@Test
	public void adaptSignal() {
		Register<Integer> chooser = new RegisterImpl<Integer>(1);
		final Register<String> register1 = new RegisterImpl<String>("1 foo");
		final Register<String> register2 = new RegisterImpl<String>("2 foo");
		
		Signal<String> output = _subject.adaptSignal(chooser.output(), new Functor<Integer, Signal<String>>() { @Override public Signal<String> evaluate(Integer value) {
			return value == 1 ? register1.output() : register2.output();
		}});
		
		assertEquals("1 foo", output.currentValue());
		chooser.setter().consume(2);
		assertEquals("2 foo", output.currentValue());
		register2.setter().consume("2 bar");
		assertEquals("2 bar", output.currentValue());
	}
	
}
