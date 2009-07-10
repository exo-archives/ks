package org.exoplatform.forum.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

/**
 * A collection of assertion helper to make UT easier to read
 * @author patricelamarque
 *
 */
public class AssertUtils {
	
	/**
	 * Assert a set of expected items to be all contained in a collection
	 * @param actual containment
	 * @param expected items expected to be contained
	 */
	@SuppressWarnings("unchecked")
	public static void assertContains(Collection actual, Object... expected) {
		
		// does not work, duno why...
		// Assert.assertTrue(actual.containsAll(Arrays.asList(expected)));
		
		// .. but it won't stop me!
		for (Object item : expected) {
			boolean found = false;
			for (Object obj : actual) {
				if (obj.equals(item)) found = true;
			}
			Assert.assertTrue("expected item was not found " + item + "@"+ item.hashCode(), found); 
		}
	}

	/**
	 * Assert a set of expected items NOT to be all contained in a collection
	 * @param actual containment
	 * @param expected items expected to be contained
	 */
	@SuppressWarnings("unchecked")
	public static void assertNotContains(Collection actual, Object... expected) {
		Assert.assertFalse(actual.containsAll(Arrays.asList(expected)));
	}
	
	/**
	 * Assert a set of expected string items to be all contained in a collection
	 * @param actual containment
	 * @param expected items expected to be contained
	 */
	public static void assertContains(List<String> actual, String... expected) {
		Assert.assertTrue(actual.containsAll(Arrays.asList(expected)));
	}
	
	/**
	 * Assert a set of expected string items NOT to be all contained in a collection
	 * @param actual containment
	 * @param expected items expected to be contained
	 */
	public static void assertNotContains(List<String> actual, String... expected) {
		Assert.assertFalse(actual.containsAll(Arrays.asList(expected)));
	}	
	
	/**
	 * Assert a collection is empty (not null)
	 * @param value
	 */
	@SuppressWarnings(value = "unchecked")
	public static void assertEmpty(Collection value) {
		Assert.assertNotNull(value);
		Assert.assertEquals(0, value.size());
	}

}
