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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum;

import org.exoplatform.forum.service.Utils;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestForumUtils extends TestCase {


  public TestForumUtils() throws Exception {
    super();
    
  }

  public void testBuildForumLink() throws Exception {

    // basic case
    String actual = ForumUtils.buildForumLink("http://hostname/portal/private/classic/forum?param=value&param2=value2", "forum", "classic", ForumUtils.TOPIC, "1234");
    assertEquals("http://hostname/portal/private/classic/forum/topic/1234", actual);
    
    // case where url does not current nav
    assertEquals("http://hostname/portal/private/classic/forum/topic/1234", ForumUtils.buildForumLink("http://hostname/portal/private/classic/foo?param=value&param2=value2", "forum", "classic", ForumUtils.TOPIC, "1234"));
    
    // case where url does not match current portal (is it possible ?)
    assertEquals("http://hostname/portal/private/foo/forum/topic/1234", ForumUtils.buildForumLink("http://hostname/portal/private/foo/forum?param=value&param2=value2", "forum", "classic", ForumUtils.TOPIC, "1234"));
    
    // case where url does not match current portal nor current nav (is it possible ?)
    assertEquals("http/topic/1234", ForumUtils.buildForumLink("http://hostname/portal/private/foo/bar?param=value&param2=value2", "forum", "classic", ForumUtils.TOPIC, "1234"));
    
    
    // cases when type or id are empty
    assertEquals("http://hostname/portal/private/classic/forum", ForumUtils.buildForumLink("http://hostname/portal/private/classic/forum?param=value&param2=value2", "forum", "classic", null, null));
    assertEquals("http://hostname/portal/private/classic/forum", ForumUtils.buildForumLink("http://hostname/portal/private/classic/forum?param=value&param2=value2", "forum", "classic", "", null));
    assertEquals("http://hostname/portal/private/classic/forum", ForumUtils.buildForumLink("http://hostname/portal/private/classic/forum?param=value&param2=value2", "forum", "classic", null, ""));
    assertEquals("http://hostname/portal/private/classic/forum", ForumUtils.buildForumLink("http://hostname/portal/private/classic/forum?param=value&param2=value2", "forum", "classic", "", ""));
    assertEquals("http://hostname/portal/private/classic/forum", ForumUtils.buildForumLink("http://hostname/portal/private/classic/forum?param=value&param2=value2", "forum", "classic", "", "1234"));
    assertEquals("http://hostname/portal/private/classic/forum", ForumUtils.buildForumLink("http://hostname/portal/private/classic/forum?param=value&param2=value2", "forum", "classic", "topic", ""));
    
    // case for explicit no-id
    assertEquals("http://hostname/portal/private/classic/forum/category", ForumUtils.buildForumLink("http://hostname/portal/private/classic/forum?param=value&param2=value2", "forum", "classic", "category", Utils.FORUM_SERVICE));
    
  }

  public void testGetFormatDate() {
    //fail("Not yet implemented");
  }

  public void testGetInstanceTempCalendar() {
    //fail("Not yet implemented");
  }

  public void testIsValidEmailAddresses() {
    //fail("Not yet implemented");
  }

  public void testGetSizeFile() {
    //fail("Not yet implemented");
  }

  public void testGetTimeZoneNumberInString() {
    //fail("Not yet implemented");
  }

  public void testGetStarNumber() {
    //fail("Not yet implemented");
  }

  public void testSplitForForum() {
    //fail("Not yet implemented");
  }

  public void testUnSplitForForum() {
    //fail("Not yet implemented");
  }

  public void testRemoveSpaceInString() {
    //fail("Not yet implemented");
  }

  public void testRemoveZeroFirstNumber() {
    //fail("Not yet implemented");
  }

  public void testRemoveStringResemble() {
    //fail("Not yet implemented");
  }

  public void testIsEmpty() {
    //fail("Not yet implemented");
  }

  public void testIsArrayEmpty() {
    //fail("Not yet implemented");
  }

  public void testAddStringToString() {
    //fail("Not yet implemented");
  }

  public void testIsStringInStrings() {
    //fail("Not yet implemented");
  }

  public void testIsStringInList() {
    //fail("Not yet implemented");
  }

  public void testGetSubString() {
    //fail("Not yet implemented");
  }

  public void testAddArrayToList() {
    //fail("Not yet implemented");
  }

  public void testGetLabel() {
    //fail("Not yet implemented");
  }

  public void testGetColor() {
    //fail("Not yet implemented");
  }

  public void testGetDefaultMail() {
    //fail("Not yet implemented");
  }

  public void testEnableIPLogging() {
    //fail("Not yet implemented");
  }

  public void testSavePortletPreferenceStringString() {
    //fail("Not yet implemented");
  }

  public void testGetPorletPreference() {
    //fail("Not yet implemented");
  }

  public void testSavePortletPreferenceSettingPortletPreference() {
    //fail("Not yet implemented");
  }

  public void testGetListInValus() {
    //fail("Not yet implemented");
  }

  public void testGetLimitUploadSize() {
    //fail("Not yet implemented");
  }

  public void testGetActionViewInfoUser() {
    //fail("Not yet implemented");
  }

  public void testGetCalculateListEmail() {
    //fail("Not yet implemented");
  }

}
