/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.service;

import static org.exoplatform.commons.testing.AssertUtils.assertContains;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestUserProfile extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testSetLastReadPostOfTopic() {

    String[] array = new String[] { "foo,foo", "bar,bar" };
    UserProfile profile = new UserProfile();
    profile.setLastReadPostOfTopic(array);
    String[] actual = profile.getLastReadPostOfTopic();
    assertContains(actual, "foo,foo", "bar,bar");
    assertEquals("foo", profile.getLastPostIdReadOfTopic("foo"));
    assertEquals("", profile.getLastPostIdReadOfTopic("zed"));

  }

  public void testSetLastReadPostOfForum() {

    String[] array = new String[] { "foo,foo", "bar,bar" };
    UserProfile profile = new UserProfile();
    profile.setLastReadPostOfForum(array);
    String[] actual = profile.getLastReadPostOfForum();
    assertContains(actual, "foo,foo", "bar,bar");
    assertEquals("foo", profile.getLastPostIdReadOfForum("foo"));
    assertEquals("", profile.getLastPostIdReadOfForum("zed"));

  }

  public void testGetLastTimeAccessForum() throws Exception {
    UserProfile profile = new UserProfile();
    long actual = profile.getLastTimeAccessForum("foo");
    assertEquals("Last access time of unknown forum should be zero", 0, actual);

    profile.setLastTimeAccessForum("bar", 1);
    actual = profile.getLastTimeAccessForum("bar");
    assertEquals(1, actual);
  }

  public void testGetLastTimeAccessTopic() throws Exception {
    UserProfile profile = new UserProfile();
    long actual = profile.getLastTimeAccessTopic("foo");
    assertEquals("Last access time of unknown topic should be zero", 0, actual);

    profile.setLastTimeAccessTopic("bar", 1);
    actual = profile.getLastTimeAccessTopic("bar");
    assertEquals(1, actual);
  }

  public void testGetScreenname() {

    // not set defaults to default user id
    UserProfile profile = new UserProfile();
    String actual = profile.getScreenName();
    assertEquals(UserProfile.USER_GUEST, actual);

    // null or empty defaults to user id
    profile.setUserId("foo");
    profile.setScreenName(null);
    actual = profile.getScreenName();
    assertEquals("foo", actual);
    profile.setScreenName("");
    actual = profile.getScreenName();
    assertEquals("foo", actual);

    // if set, don't use user id
    profile.setUserId("foo");
    profile.setScreenName("bar");
    actual = profile.getScreenName();
    assertEquals("bar", actual);

  }

}
