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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

  @SuppressWarnings("deprecation")
  public void testGetInstanceTempCalendar() {
//  	get calendar return Time Zone is 0 with all server system.
  	assertEquals((new Date()).getTimezoneOffset(), GregorianCalendar.getInstance().getTimeZone().getRawOffset());
  	assertEquals(0, ForumUtils.getInstanceTempCalendar().getTimeZone().getRawOffset());
  }

  public void testIsValidEmailAddresses() throws Exception {
    String emails = "";
    // email is empty
    assertEquals(true, ForumUtils.isValidEmailAddresses(emails));
    // email only text not @
    emails = "test";
    assertEquals(false, ForumUtils.isValidEmailAddresses(emails));
    // email have @ but not '.'
    emails = "test@test";
    assertEquals(false, ForumUtils.isValidEmailAddresses(emails));
    // email have charter strange
    emails = "#%^&test@test.com";
    assertEquals(false, ForumUtils.isValidEmailAddresses(emails));
    // email have before '.' is number
    emails = "test@test.787";
    assertEquals(false, ForumUtils.isValidEmailAddresses(emails));
    
    emails = "test@test.com";
    assertEquals(true, ForumUtils.isValidEmailAddresses(emails));
    emails = "test@test.com.vn";
    assertEquals(true, ForumUtils.isValidEmailAddresses(emails));
    emails = "test@test.com, demo@demo.com, ";
    assertEquals(true, ForumUtils.isValidEmailAddresses(emails));
  }

  public void testGetSizeFile() {
  	double fileZise = 0;
  	assertEquals("0.0 Byte", ForumUtils.getSizeFile(fileZise));
  	
  	fileZise = 999;
  	assertEquals("999.0 Byte", ForumUtils.getSizeFile(fileZise));

  	fileZise = 1000;
  	assertEquals("0.976 Kb", ForumUtils.getSizeFile(fileZise));

  	fileZise = 1024;
  	assertEquals("1.0 Kb", ForumUtils.getSizeFile(fileZise));

  	fileZise = 1000000;
  	assertEquals("976.562 Kb", ForumUtils.getSizeFile(fileZise));

  	fileZise = 1200000;
  	assertEquals("1.144 Mb", ForumUtils.getSizeFile(fileZise));
  }

  public void testGetTimeZoneNumberInString() {
  	String timeZone = "";
  	assertEquals(null, ForumUtils.getTimeZoneNumberInString(timeZone));
  	
  	timeZone =  "(GMT -12:00) Eniwetok, Kwajalein";
  	assertEquals("-12.00", ForumUtils.getTimeZoneNumberInString(timeZone));
  	
  	timeZone =  "(GMT +10:00) Eastern Australia, Guam, Vladivostok";
  	assertEquals("+10.00", ForumUtils.getTimeZoneNumberInString(timeZone));
  	
  	timeZone =  "(GMT 0:00) Greenwich Mean Time: Dublin, London, Lisbon, Casablanca";
  	assertEquals("0.00", ForumUtils.getTimeZoneNumberInString(timeZone));
  	
  	timeZone =  "(GMT +5:30) Bombay, Calcutta, Madras, New Delhi";
  	assertEquals("+5.50", ForumUtils.getTimeZoneNumberInString(timeZone));
  }

  public void testGetStarNumber() throws Exception {
//  	class return arrays class css of VoteRatingForm 
  	double voteRating = 0;// voteRating is small more 5 
  	assertEquals("[notStar, notStar, notStar, notStar, notStar, 0]", Arrays.asList(ForumUtils.getStarNumber(voteRating)).toString());
  	voteRating = 4;
  	assertEquals("[star, star, star, star, notStar, 4]", Arrays.asList(ForumUtils.getStarNumber(voteRating)).toString());
  	voteRating = 4.24;
  	assertEquals("[star, star, star, star, notStar, 4.2]", Arrays.asList(ForumUtils.getStarNumber(voteRating)).toString());
  	voteRating = 4.25;
  	assertEquals("[star, star, star, star, halfStar, 4.2]", Arrays.asList(ForumUtils.getStarNumber(voteRating)).toString());
  	voteRating = 4.74;
  	assertEquals("[star, star, star, star, halfStar, 4.7]", Arrays.asList(ForumUtils.getStarNumber(voteRating)).toString());
  	voteRating = 4.75;
  	assertEquals("[star, star, star, star, star, 4.7]", Arrays.asList(ForumUtils.getStarNumber(voteRating)).toString());
  }

  public void testSplitForForum() throws Exception {
  	String str = "";
  	assertEquals("[]", Arrays.asList(ForumUtils.splitForForum(str)).toString());
  	str = "test, test, test";
  	assertEquals("[test, test, test]", Arrays.asList(ForumUtils.splitForForum(str)).toString());
  }

  public void testUnSplitForForum() throws Exception {
  	String []strs = new String[]{""};
  	assertEquals("", ForumUtils.unSplitForForum(strs));
  	strs = new String[]{"test1", "test2", "test3"};
  	assertEquals("test1,test2,test3", ForumUtils.unSplitForForum(strs));
  }

  public void testRemoveSpaceInString() throws Exception {
    String str = "";
    assertEquals("", ForumUtils.removeSpaceInString(str));
    str = "test ,,test1,test2,   test3";
    assertEquals("test,test1,test2,test3", ForumUtils.removeSpaceInString(str));
  }

  public void testRemoveZeroFirstNumber() {
    String str = "0000";
    assertEquals("0", ForumUtils.removeZeroFirstNumber(str));
    str = "   000";
    assertEquals("0", ForumUtils.removeZeroFirstNumber(str));
    str = "0000123";
    assertEquals("123", ForumUtils.removeZeroFirstNumber(str));
  }

  public void testRemoveStringResemble() throws Exception {
    String str = "";
    assertEquals("", ForumUtils.removeStringResemble(str));
    str = "test,text, test,foo,test,abc";
    assertEquals("test,text,foo,abc", ForumUtils.removeStringResemble(str));
  }

  public void testIsEmpty() {
  	assertEquals(true, ForumUtils.isEmpty(null));
  	assertEquals(true, ForumUtils.isEmpty(""));
  	assertEquals(true, ForumUtils.isEmpty(" "));
  	assertEquals(false, ForumUtils.isEmpty("abc"));
  }

  public void testIsArrayEmpty() {
  	assertEquals(true, ForumUtils.isArrayEmpty(null));
  	assertEquals(true, ForumUtils.isArrayEmpty(new String[]{}));
  	assertEquals(true, ForumUtils.isArrayEmpty(new String[]{" "}));
  	assertEquals(false, ForumUtils.isArrayEmpty(new String[]{"abc"}));
  }
  
  public void testAddArrayToList() {
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
