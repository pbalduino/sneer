package sneer.pulp.tuples;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import sneer.pulp.keymanager.PublicKey;

public abstract class Tuple {

	protected Tuple() {}
	
	protected Tuple(PublicKey publisher, long publicationTime) {
		_publisher = publisher;
		_publicationTime = publicationTime;
	}

	private PublicKey _publisher;
	private long _publicationTime;
	
	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		
		for (Field field : fields())
			result = (prime * result) + hashCode(getFieldValue(field, this));
		
		return result;
	}

	private Field[] fields() {
		//Optimize Implement a WEAK cache using the class as key
		
		List<Field> list = new ArrayList<Field>();
		accumulateFields(list, getClass());
	    
		Field[] result = list.toArray(new Field[list.size()]);
		sort(result);
		return result;
	}

	private void accumulateFields(List<Field> list,	Class<?> clazz) {
		if (clazz == Object.class) return;
		
		for (Field f : clazz.getDeclaredFields())
			list.add(f);

		accumulateFields(list, clazz.getSuperclass());
	}

	private void sort(Field[] fields) {
		Arrays.sort(fields, new Comparator<Field>() { @Override public int compare(Field f1, Field f2) {
			return f1.getName().compareTo(f2.getName()); 
		}});
	}

	private int hashCode(Object obj) {
		if (obj == null) return 0;
		if (obj.getClass().isArray()) return ArrayUtils.hashCode(obj);
		return obj.hashCode();
	}

	@Override
	public final boolean equals(Object other) {
		if (this == other) return true;
		if (other == null) return false;
		if (getClass() != other.getClass()) return false;

		for (Field field : fields())
			if (!isSameFieldValue(field, other)) return false;
		
		return true;
	}

	private Object getFieldValue(Field field, Object object) {
		if (field.getName().equals("_publisher")) return ((Tuple)object)._publisher; //Refactor Remove these fields from here and keep them somewhere else in the tupleSpace
		if (field.getName().equals("_publicationTime")) return ((Tuple)object)._publicationTime;
		
		try {
			return field.get(object);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("All fields in a Tuple should be public and not static. This was not the case with: " + object.getClass() + "." + field.getName() + " Also, tuple classes declared as inner classes dont work.", e);
		}
	}

	private boolean isSameFieldValue(Field field, Object other) {
		Object myValue = getFieldValue(field, this);
		Object hisValue = getFieldValue(field, other);
		
		return equals(myValue, hisValue);
	}

	private boolean equals(Object myValue, Object hisValue) {
		if (myValue == null) return hisValue == null;
		if (myValue.getClass().isArray()) return ArrayUtils.isEquals(myValue, hisValue);
		return myValue.equals(hisValue);
	}


	public PublicKey publisher() {
		return _publisher;
	}


	public long publicationTime() {
		return _publicationTime;
	}

	public void stamp(PublicKey publisher, long time) {
		if (_publisher != null)
			throw new IllegalStateException("Tuple was already stamped.");
		
		_publisher = publisher;
		_publicationTime = time; 
	}
	
	
}
