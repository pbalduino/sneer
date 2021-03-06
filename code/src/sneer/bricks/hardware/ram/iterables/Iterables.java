package sneer.bricks.hardware.ram.iterables;

import java.util.Iterator;
import java.util.List;

import basis.brickness.Brick;
import basis.lang.Functor;


@Brick
public interface Iterables {

	<T> List<T> sortByToString(Iterable<T> iterable);

	<A, B> Iterable<B> map(Iterable<A> source, Functor<A, B> functor);

	<E> List<E> toList(Iterable<E> iterable);

	<E> List<E> toList(Iterator<E> iterator);

}