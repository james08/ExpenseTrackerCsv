package com.jkukard.expensetrackercsv;

/**
 * Contains the substring of a transaction description used to 
 * categorise other transactions with descriptions that contain the
 * same substring.
 * 
 * Some keys may be contained in others, therefore we need to order them
 * so as to match off the more specific keys first.
 * 
 * @author James Kukard
 *
 */
public class CatMatchKey implements Comparable<CatMatchKey> {
	
	private String matchKey;
	
	public CatMatchKey(String matchKey) {
		this.matchKey = matchKey.toUpperCase();
	}
	
	public String getMatchKey() {
		return matchKey;
	}

	@Override
	public int compareTo(CatMatchKey cmk) {
		String a = matchKey;
		String b = cmk.getMatchKey();
		//System.out.println("Comparing " + a + " and " + b);
		if (a.equals(b)) {
			return 0;
		} else if (a.contains(b)) {
			return 1;
		} else if (b.contains(a)) {
			return -1;
		} else {
			return a.compareTo(b);
		}
	}

	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		if (obj instanceof CatMatchKey) {
			ret = matchKey.equalsIgnoreCase(((CatMatchKey)obj).getMatchKey());
		}
		return ret;
	}

	@Override
	public int hashCode() {
		return matchKey.hashCode();
	}
	

}
