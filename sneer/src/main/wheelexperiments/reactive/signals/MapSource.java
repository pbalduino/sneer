//Copyright (C) 2004 Klaus Wuestefeld
//This is free software. It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the license distributed along with this file for more details.
//Contributions: Kalecser Kurtz, Fabio Roger Manera.

package wheelexperiments.reactive.signals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import wheelexperiments.reactive.SetSignal;
import wheelexperiments.reactive.SetSource;


public class MapSource<K,V> implements MapSignal<K,V> {

	private final Map<K, V> _contents = new HashMap<K, V>(); 
	private final SetSource<K> _keys = new SetSource<K>(); 
	private final Set<Observer<K,V>> _observers = new HashSet<Observer<K,V>>();
	
	
	private void notifyAddition(K key, V value) {
		Iterator<Observer<K,V>> it = _observers.iterator();
		while (it.hasNext())
			it.next().entryAdded(key, value);
	}

	private void notifyRemoval(K key, V value) {
		Iterator<Observer<K,V>> it = _observers.iterator();
		while (it.hasNext())
			it.next().keyRemoved(key, value);
	}

	public synchronized void addObserver(Observer<K,V> observer) {
		_observers.add(observer);
	}

	public void put(K key, V value) {
		_keys.add(key);
		
		_contents.put(key, value);
		notifyAddition(key, value);
	}

	public void remove(K key) {
		_keys.remove(key);
		
		V value = _contents.remove(key);
		notifyRemoval(key, value);
	}

	public SetSignal<K> keys() {
		return _keys;
	}

	public V get(K key) {
		return _contents.get(key);
	}

	public Map<K, V> sighting() {
		return new HashMap<K, V>(_contents);
	}

}
