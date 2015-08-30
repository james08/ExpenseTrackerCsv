package com.jkukard.expensetrackercsv;

import java.util.LinkedHashMap;

/**
 * Custom map which changes the behaviour of put() to <b>add<b/> values
 * if key already exists instead of replace.
 * 
 * @author James Kukard
 *
 */
public class CategoryTotalsMap<K, V> extends LinkedHashMap<String, Double> {

	@Override
	public Double put(String key, Double value) {
		String newKey;
		Double newValue;
		try {
			newKey = (String) key;
			newValue = (Double) value;
			if (super.containsKey(newKey)) {
				newValue = super.get(newKey) + newValue;
			}
		} catch (ClassCastException e) {
			return super.put(key, value);
		}
		return super.put(newKey, newValue);
	}

}
