package org.exoplatform.ks.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/**
 * A collection of assertion helper to make UT easier to read
 * @author patricelamarque
 *
 */
public class AssertUtils {
	
  
  private AssertUtils() {
    // hidden
  }
  
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
				if (obj.equals(item)) {
				  found = true;
				}
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
   * Assert a set of expected string items to be all contained in a string array
   * @param actual containment
   * @param expected items expected to be contained
   */
	 public static void assertContains(String [] actual, String... expected) {
	    Assert.assertTrue(Arrays.asList(actual).containsAll(Arrays.asList(expected)));
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

	 public static <T> void assertEmpty(T[] value) {
	    Assert.assertNotNull(value);
	    Assert.assertEquals(0, value.length);
	  }
	
  /**
   * All elements of a list should be contained in the expected array of String
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertContainsAll(String message, List<String> expected, List<String> actual) {
    Assert.assertEquals(message, expected.size(), actual.size());
    Assert.assertTrue(message,expected.containsAll(actual));
  }

  /**
   * Assertion method on string arrays
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertEquals(String message, String []expected, String []actual) {
    Assert.assertEquals(message, expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals(message, expected[i], actual[i]);
    }
  }

  public static void assertException(Closure code) {
    try {
      code.dothis();
    } catch (Exception e) {
      return ;// Exception correctly thrown
    }
    throw new AssertionFailedError("An exception should have been thrown.");
  }
  
  
  /**
   * Assert an exception of a given type is thrown by he code in closure
   * @param exceptionType
   * @param code
   */
  public static void assertException(Class<? extends Exception> exceptionType, Closure code) {
    try {
      code.dothis();
    } catch (Exception e) {
      Assert.assertEquals("Wrong exception type", exceptionType, e.getClass());
      return ;// Exception correctly thrown
    }
    throw new AssertionFailedError("An exception should have been thrown.");
  }

}
