package spikes.sneer.kernel.container.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FieldUtils
{
	public static List<Field> getAllFields(Class<?> beanClass)
	{
		List<Field> result = new ArrayList<Field>();
		do
		{
			collect(beanClass, result); 
			beanClass = beanClass.getSuperclass();	
			 
		}
		while(beanClass != null);
		return result;
	}

	private static void collect(Class<?> beanClass, List<Field> result)
    {
		Field[] declared = beanClass.getDeclaredFields();
		for (Field field : declared)
        {
	        result.add(field);
        }
    }
	
	public static Field getField(Class<?> beanClass,String name)
	{
		List<Field> allFields = getAllFields(beanClass);
		for (Field field : allFields)
        {
	        if(field.getName().equals(name))
	        {
	        	return field;
	        }
        }
		return null;
	}
}
