package org.joverseer.support;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Implements cache functionality for the Container class
 * 
 * @author Marios Skounakis
 */
public class ContainerCache<X> implements Serializable {
	private static final long serialVersionUID = -8253812529870087895L;
	String propertyName;
	HashMap<Object, ArrayList<X>> cache;
	transient HashMap<Object, Object> reverseMap;

	public ContainerCache(String propertyName) {
		this.propertyName = propertyName;
		cache = new HashMap<Object, ArrayList<X>>();
		reverseMap = new HashMap<Object, Object>();
	}

	public void addItem(X obj) {
		Object value = getPropertyValue(obj);
		ArrayList<X> objects = cache.get(value);
		if (objects == null) {
			objects = new ArrayList<X>();
			cache.put(value, objects);
		}
		getReverseMap().put(obj, value);
		objects.add(obj);
	}

	public void clear() {
		cache.clear();
	}

	public String getPropertyName() {
		return propertyName;
	}

	private Object getPropertyValue(X obj) {
		try {
			return PropertyUtils.getProperty(obj, getPropertyName());
		} catch (Exception exc) {
		}
		return null;
	}

	private HashMap<Object, Object> getReverseMap() {
		if (reverseMap == null) {
			reconstructReverseMap();
		}
		return reverseMap;
	}

	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		// for backwards compatibility with older versions (before 1.0.7)
		Object o1 = in.readObject();
		Object o2 = in.readObject();
		if (HashMap.class.isInstance(o1)) {
			cache = (HashMap<Object, ArrayList<X>>) o1;
			propertyName = (String) o2;
		} else {
			propertyName = (String) o1;
			cache = (HashMap<Object, ArrayList<X>>) o2;
		}
		reconstructReverseMap();
	}

	private void reconstructReverseMap() {
		reverseMap = new HashMap<Object, Object>();
		for (ArrayList<X> list : cache.values()) {
			for (X obj : list) {
				Object value = getPropertyValue(obj);
				reverseMap.put(obj, value);
			}
		}
	}

	public void removeAll(Collection<X> col) {
		for (X obj : col) {
			removeItem(obj);
		}
	}

	public void removeItem(X obj) {
		Object value = getPropertyValue(obj);
		try {
			value = getReverseMap().get(obj);
		} catch (Exception e) {
			try {
				value = PropertyUtils.getProperty(obj, getPropertyName());
			} catch (Exception exc) {
			}
		}
		ArrayList<X> objects = cache.get(value);
		if (objects == null) {
			return;
		}
		objects.remove(obj);
	}

	public ArrayList<X> retrieveItems(Object value) {
		ArrayList<X> objects = cache.get(value);
		return objects;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}
}
