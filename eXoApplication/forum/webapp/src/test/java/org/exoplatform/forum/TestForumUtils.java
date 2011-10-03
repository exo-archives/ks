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
package org.exoplatform.forum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.forum.service.Utils;

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
    String actual = ForumUtils.buildLink("http://hostname/portal/classic/", "portal", "forum", ForumUtils.TOPIC, "1234", false);
    assertEquals("http://hostname/portal/classic/forum/topic/1234", actual);
    
    // case where url is private
    actual = ForumUtils.buildLink("http://hostname/portal/classic/","portal", "forum", ForumUtils.TOPIC, "1234", true);
    assertEquals("http://hostname/portal/login?&initialURI=/portal/classic/forum/topic/1234", actual);
    actual = ForumUtils.buildLink("http://hostname/portal/classic/","portal", "forum", null, null, true) ;
    assertEquals("http://hostname/portal/login?&initialURI=/portal/classic/forum/", actual);

    // case where url does not current nav
    assertEquals("http://hostname/portal/classic/forum/topic/1234", ForumUtils.buildLink("http://hostname/portal/classic/","portal", "forum", ForumUtils.TOPIC, "1234", false));
    // case where url does not match current portal
    assertNotSame("http://hostname/portal/foo/forum/topic/1234", ForumUtils.buildLink("http://hostname/portal/foo/forum/","portal", "forum", ForumUtils.TOPIC, "1234", false));
    
    // case where url does not match current portal nor current nav
    assertNotSame("http/topic/1234", ForumUtils.buildLink("http://hostname/portal/foo/bar/","portal", "forum", ForumUtils.TOPIC, "1234", false));
    
    // cases when type or id are empty
    assertEquals("http://hostname/portal/classic/forum/", ForumUtils.buildLink("http://hostname/portal/classic/","portal", "forum", null, null, false));
    assertEquals("http://hostname/portal/classic/forum/", ForumUtils.buildLink("http://hostname/portal/classic/","portal", "forum", ForumUtils.EMPTY_STR, null, false));
    assertEquals("http://hostname/portal/classic/forum/", ForumUtils.buildLink("http://hostname/portal/classic/","portal", "forum", null, ForumUtils.EMPTY_STR, false));
    assertEquals("http://hostname/portal/classic/forum/", ForumUtils.buildLink("http://hostname/portal/classic/","portal", "forum", ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR, false));
    assertEquals("http://hostname/portal/classic/forum/", ForumUtils.buildLink("http://hostname/portal/classic/","portal", "forum", ForumUtils.EMPTY_STR, "1234", false));
    assertEquals("http://hostname/portal/classic/forum/", ForumUtils.buildLink("http://hostname/portal/classic/","portal", "forum", "topic", ForumUtils.EMPTY_STR, false));
    
    // case for explicit no-id
    assertEquals("http://hostname/portal/classic/forum/category", ForumUtils.buildLink("http://hostname/portal/classic/","portal", "forum", "category", Utils.FORUM_SERVICE, false));
    
    //case for forum's link in space
    actual = ForumUtils.buildLink("http://hostname/portal/groups/:spaces:test/test/","portal", "forum", ForumUtils.TOPIC, "1234", true);
    assertEquals("http://hostname/portal/login?&initialURI=/portal/groups/:spaces:test/test/forum/topic/1234", actual);
  }

  public void testIsValidEmailAddresses() throws Exception {
    String emails = ForumUtils.EMPTY_STR;
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
    // basic case
    emails = "test@test.com";
    assertEquals(true, ForumUtils.isValidEmailAddresses(emails));
    emails = "test@test.com.vn";
    assertEquals(true, ForumUtils.isValidEmailAddresses(emails));
    emails = "test@test.com, demo@demo.com, ";
    assertEquals(true, ForumUtils.isValidEmailAddresses(emails));
  }

  public void testGetSizeFile() {
    // case where file size is 0 byte
    long fileZise = 0;
    assertEquals("0 Byte", ForumUtils.getSizeFile(fileZise));
    
    // case where file size is 999 byte
    fileZise = 1023;
    assertEquals("1023 Byte", ForumUtils.getSizeFile(fileZise));

        // case where file size is 1Kb
    fileZise = 1024;
    assertEquals("1 Kb", ForumUtils.getSizeFile(fileZise));

    // case where file size is more 1Kb
    fileZise = 1000000;
    assertEquals("976.6 Kb", ForumUtils.getSizeFile(fileZise));
    
    // case where file size is 1Mb
    fileZise = 1048576;
    assertEquals("1 Mb", ForumUtils.getSizeFile(fileZise));
    // case where file size is more 1Mb
    
    fileZise = 1200000;
    assertEquals("1.1 Mb", ForumUtils.getSizeFile(fileZise));
    
    fileZise = 1300000;
    assertEquals("1.2 Mb", ForumUtils.getSizeFile(fileZise));
  }

  public void testGetTimeZoneNumberInString() {
    // timeZone is empty
    String timeZone = ForumUtils.EMPTY_STR;
    assertEquals(null, ForumUtils.getTimeZoneNumberInString(timeZone));
    
    // timeZone at Eniwetok. ZoneTime is -12.
    timeZone =  "(GMT -12:00) Eniwetok, Kwajalein";
    assertEquals("-12.00", ForumUtils.getTimeZoneNumberInString(timeZone));
    
    // timeZone at Australia. ZoneTime is +10.
    timeZone =  "(GMT +10:00) Eastern Australia, Guam, Vladivostok";
    assertEquals("+10.00", ForumUtils.getTimeZoneNumberInString(timeZone));
    
    // timeZone at London. ZoneTime is 0.
    timeZone =  "(GMT 0:00) Greenwich Mean Time: Dublin, London, Lisbon, Casablanca";
    assertEquals("0.00", ForumUtils.getTimeZoneNumberInString(timeZone));
    
    // timeZone at Calcutta. ZoneTime is +5.5.
    timeZone =  "(GMT +5:30) Bombay, Calcutta, Madras, New Delhi";
    assertEquals("+5.50", ForumUtils.getTimeZoneNumberInString(timeZone));
  }

  public void testArraysMerge() {
    String[] str1 = null;
    String[] str2 = new String[]{"abc5", "abc4", "abc3", "abc7"};
    assertEquals("[abc5, abc4, abc3, abc7]", Arrays.asList(ForumUtils.arraysMerge(str1, str2)).toString());
    str1 = new String[]{"abc1", "abc2", "abc3", "abc1"};
    assertEquals("[abc1, abc4, abc5, abc2, abc3, abc7]", Arrays.asList(ForumUtils.arraysMerge(str1, str2)).toString());
  }
  
  public void testGetStarNumber() throws Exception {
//    class return arrays class css of VoteRatingForm.
    // vote max is 5, vote is value mean of users voting.
    //case not vote, vote is 0.
    double voteRating = 0;
    assertEquals("[notStar, notStar, notStar, notStar, notStar, 0]", Arrays.asList(ForumUtils.getStarNumber(voteRating)).toString());
    // vote is 4
    voteRating = 4; 
    assertEquals("[star, star, star, star, notStar, 4]", Arrays.asList(ForumUtils.getStarNumber(voteRating)).toString());
    // vote is 4.24
    voteRating = 4.24;
    assertEquals("[star, star, star, star, notStar, 4.2]", Arrays.asList(ForumUtils.getStarNumber(voteRating)).toString());
    // vote is 4.25
    voteRating = 4.25;
    assertEquals("[star, star, star, star, halfStar, 4.2]", Arrays.asList(ForumUtils.getStarNumber(voteRating)).toString());
    // vote is 4.74
    voteRating = 4.74;
    assertEquals("[star, star, star, star, halfStar, 4.7]", Arrays.asList(ForumUtils.getStarNumber(voteRating)).toString());
    // vote is 4.75
    voteRating = 4.75;
    assertEquals("[star, star, star, star, star, 4.7]", Arrays.asList(ForumUtils.getStarNumber(voteRating)).toString());
  }

  public void testGetCensoredKeyword() throws Exception {
    // for keys null
    String stringKey = null;
    assertEquals(0, ForumUtils.getCensoredKeyword(stringKey).length);
    // for keys is empty
    stringKey = "";
    assertEquals(0, ForumUtils.getCensoredKeyword(stringKey).length);

    // for keys one value
    stringKey = "key";
    assertEquals("[key]", Arrays.asList(ForumUtils.getCensoredKeyword(stringKey)).toString());

    // for keys one value and content some ',' and ';' and ' '
    stringKey = " ,;   key word,,;";
    assertEquals("[key word]", Arrays.asList(ForumUtils.getCensoredKeyword(stringKey)).toString());

    // for keys one value and content some ' ' and ';'
    stringKey = "    key key    ; ;";
    assertEquals("[key key]", Arrays.asList(ForumUtils.getCensoredKeyword(stringKey)).toString());

    // for keys more values and content ", " and " "
    stringKey = "key 1, key 2    ";
    assertEquals("[key 1, key 2]", Arrays.asList(ForumUtils.getCensoredKeyword(stringKey)).toString());

    // for keys more values and content ", " and " ," and " "
    stringKey = "key 1, key 2 ,        key 3";
    assertEquals("[key 1, key 2, key 3]", Arrays.asList(ForumUtils.getCensoredKeyword(stringKey)).toString());

    // for keys more values and content ", " and " ," and ",,"
    stringKey = "key 1, key 2 ,key 3 ,,,,key 4";
    assertEquals("[key 1, key 2, key 3, key 4]", Arrays.asList(ForumUtils.getCensoredKeyword(stringKey)).toString());

    // for keys more values and content ", " and " ," and ",," and ", ,"
    stringKey = "key 1, key 2 ,key 3 ,,, , , , , key 4, ";
    assertEquals("[key 1, key 2, key 3, key 4]", Arrays.asList(ForumUtils.getCensoredKeyword(stringKey)).toString());

    // for keys more values and content ", " and " ," and ",," and ", ," and ";"
    stringKey = "key 1, key 2 ,key 3 ,,, , , , , key 4,;key 5;";
    assertEquals("[key 1, key 2, key 3, key 4, key 5]", Arrays.asList(ForumUtils.getCensoredKeyword(stringKey)).toString());

    stringKey = "key 1, key 2 ,key 3 ,;,,,,key 4;key 5;,, , ,";
    assertEquals("[key 1, key 2, key 3, key 4, key 5]", Arrays.asList(ForumUtils.getCensoredKeyword(stringKey)).toString());
  }

  public void testGetOrderBy() throws Exception {
    String param = "dateTime", strOrderBy = ForumUtils.EMPTY_STR;
    assertEquals("dateTime ascending",ForumUtils.getOrderBy(strOrderBy, param));
    param = "dateTime";
    strOrderBy = "dateTime descending";
    assertEquals("dateTime ascending",ForumUtils.getOrderBy(strOrderBy, param));
    param = "dateTime";
    strOrderBy = "dateTime ascending";
    assertEquals("dateTime descending",ForumUtils.getOrderBy(strOrderBy, param));
    param = "dateTime";
    strOrderBy = "dateTime ascending";
    assertEquals("dateTime descending",ForumUtils.getOrderBy(strOrderBy, param));
    param = "dateTime";
    strOrderBy = "name ascending";
    assertEquals("dateTime ascending",ForumUtils.getOrderBy(strOrderBy, param));
  }

  public void testUpdateMultiValues() throws Exception {
    String value = "root,demo", values = ForumUtils.EMPTY_STR;
    assertEquals("root,demo", ForumUtils.updateMultiValues(value, values));
    value = "root,demo";
    values = "xxx,,aaa,eee,bbb  , , ddd";
    assertEquals("xxx,aaa,eee,bbb,ddd,root,demo", ForumUtils.updateMultiValues(value, values));
    value = "root,demo";
    values = "xxx,,aaa,eee,bbb  ,demo, ddd,";
    assertEquals("xxx,aaa,eee,bbb,demo,ddd,root,", ForumUtils.updateMultiValues(value, values));
    value = "root,demo";
    values = "xxx,,aaa,eee,bbb  ,root,demo, ddd,";
    assertEquals("xxx,aaa,eee,bbb,root,demo,ddd,", ForumUtils.updateMultiValues(value, values));
    value = "root,demo";
    values = "xxx,,root,eee,root,bbb  ,demo, ddd,demo,";
    assertEquals("xxx,root,eee,bbb,demo,ddd,", ForumUtils.updateMultiValues(value, values));
  }

  public void testSplitForForum() throws Exception {
    String str = ForumUtils.EMPTY_STR;
    assertEquals("[]", Arrays.asList(ForumUtils.splitForForum(str)).toString());
    str = "test, test, test";
    assertEquals("[test, test, test]", Arrays.asList(ForumUtils.splitForForum(str)).toString());
  }

  public void testUnSplitForForum() throws Exception {
    String []strs = new String[]{ForumUtils.EMPTY_STR};
    assertEquals(ForumUtils.EMPTY_STR, ForumUtils.unSplitForForum(strs));
    strs = new String[]{"test1", "test2", "test3"};
    assertEquals("test1,test2,test3", ForumUtils.unSplitForForum(strs));
  }

  public void testRemoveSpaceInString() throws Exception {
    String str = ForumUtils.EMPTY_STR;
    assertEquals(ForumUtils.EMPTY_STR, ForumUtils.removeSpaceInString(str));
    str = "test ,,test1,test2,   test3";
    assertEquals("test,test1,test2,test3", ForumUtils.removeSpaceInString(str));
  }

  public void testRemoveZeroFirstNumber() {
    String str = "0000";
    assertEquals("0", ForumUtils.removeZeroFirstNumber(str));
    str = "   000";
    assertEquals("0", ForumUtils.removeZeroFirstNumber(str));
    str = "00001230";
    assertEquals("1230", ForumUtils.removeZeroFirstNumber(str));
    str = "   00001230";
    assertEquals("1230", ForumUtils.removeZeroFirstNumber(str));
    str = "1230   ";
    assertEquals("1230", ForumUtils.removeZeroFirstNumber(str));
    str = "1230";
    assertEquals("1230", ForumUtils.removeZeroFirstNumber(str));
  }

  public void testRemoveStringResemble() throws Exception {
    String str = ForumUtils.EMPTY_STR;
    assertEquals(ForumUtils.EMPTY_STR, ForumUtils.removeStringResemble(str));
    str = "test,text, test,foo,test,abc";
    assertEquals("test,text,foo,abc", ForumUtils.removeStringResemble(str));
  }

  public void testIsEmpty() {
    assertEquals(true, ForumUtils.isEmpty(null));
    assertEquals(true, ForumUtils.isEmpty(ForumUtils.EMPTY_STR));
    assertEquals(true, ForumUtils.isEmpty(" "));
    assertEquals(false, ForumUtils.isEmpty("abc"));
  }

  public void testIsArrayEmpty() {
    assertEquals(true, ForumUtils.isArrayEmpty(null));
    assertEquals(true, ForumUtils.isArrayEmpty(new String[]{}));
    assertEquals(true, ForumUtils.isArrayEmpty(new String[]{" "}));
    assertEquals(false, ForumUtils.isArrayEmpty(new String[]{"abc"}));
  }
  
  public void testAddArrayToList() throws Exception {
    List<String> list = new ArrayList<String>();
    String[] arrs = new String[] {};
    assertEquals("[]", ForumUtils.addArrayToList(list, arrs).toString());
    arrs = new String[] {"abc","def"};
    assertEquals("[abc, def]", ForumUtils.addArrayToList(list, arrs).toString());
    list.add("test");
    assertEquals("[abc, def, test]", ForumUtils.addArrayToList(list, arrs).toString());
    list = new ArrayList<String>();
    list.add("test");
    list.add("abc");
    assertEquals("[test, abc, def]", ForumUtils.addArrayToList(list, arrs).toString());
    arrs = new String[] {"abc","def"," ","zyx"};
    assertEquals("[test, abc, def, zyx]", ForumUtils.addArrayToList(list, arrs).toString());
  }

  public void testAddStringToString() throws Exception {
    String input = ForumUtils.EMPTY_STR, output=ForumUtils.EMPTY_STR;
    assertEquals("[ ]", Arrays.asList(ForumUtils.addStringToString(input, output)).toString());
    input = "abc"; output=ForumUtils.EMPTY_STR;
    assertEquals("[ ]", Arrays.asList(ForumUtils.addStringToString(input, output)).toString());
    input = "abc"; output="abc";
    assertEquals("[abc]", Arrays.asList(ForumUtils.addStringToString(input, output)).toString());
    input = "abc"; output="def";
    assertEquals("[abc, def]", Arrays.asList(ForumUtils.addStringToString(input, output)).toString());
    input = "abc,xyz"; output="def,ghi";
    assertEquals("[abc, xyz, def, ghi]", Arrays.asList(ForumUtils.addStringToString(input, output)).toString());
  }

  public void testIsStringInStrings() {
    String []strs = new String[] {}; String str = " abc";
    assertEquals(false, ForumUtils.isStringInStrings(strs, str));
    strs = new String[] {"ab c", "xyz", "def", "foo"};
    assertEquals(false, ForumUtils.isStringInStrings(strs, str));
    strs = new String[] {"abc ", "xyz", "def", "foo"};
    assertEquals(true, ForumUtils.isStringInStrings(strs, str));
  }

  public void testIsStringInList() {
    List<String> strs = Arrays.asList(new String[] {}); String str = "abc";
    assertEquals(false, ForumUtils.isStringInList(strs, str));
    strs = Arrays.asList(new String[] {"ab c", "xyz", "def", "foo"});
    assertEquals(false, ForumUtils.isStringInList(strs, str));
    strs = Arrays.asList(new String[] {"abc", "xyz", "def", "foo"});
    assertEquals(true, ForumUtils.isStringInList(strs, str));
  }

  public void testGetSubString() {
    String title = ForumUtils.EMPTY_STR;
    assertEquals(ForumUtils.EMPTY_STR, ForumUtils.getSubString(title, 14));
    title = "test title test abcnde aads";
    assertEquals("test title...", ForumUtils.getSubString(title, 14));
    assertEquals("test title test...", ForumUtils.getSubString(title, 17));
  }

  public void testGetLabel() {
    String label = ForumUtils.EMPTY_STR, key=ForumUtils.EMPTY_STR;
    assertEquals(ForumUtils.EMPTY_STR, ForumUtils.getLabel(label, key));
    label = "label"; key="key";
    assertEquals("label", ForumUtils.getLabel(label, key));
    label = "label '<keyWord>'"; key="key";
    assertEquals("label 'key'", ForumUtils.getLabel(label, key));
    label = "label '<keyWord>'"; key="%^((&()(*(";
    assertEquals("label '%^((&()(*('", ForumUtils.getLabel(label, key));
    label = "label '<keyWord>'"; key="[\\w\\s]";
    assertEquals("label '[ws]'", ForumUtils.getLabel(label, key));
  }

  public void testGetColor() {
    assertEquals("DarkGoldenRod", ForumUtils.getColor()[1]);
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

  public void testGetListInValus() throws Exception {
    String values = ForumUtils.EMPTY_STR;
    assertEquals("[]", ForumUtils.getListInValus(values).toString());
    values = "value,value, value,   value,   value  ,value";
    assertEquals("[value]", ForumUtils.getListInValus(values).toString());
    values = "value,abc,   test, test, values, text, abc, abcd";
    assertEquals("[value, abc, test, values, text, abcd]", ForumUtils.getListInValus(values).toString());
  }

  public void testGetLimitUploadSize() {
    //fail("Not yet implemented");
  }

  public void testGetCalculateListEmail() throws Exception {
    String s = ForumUtils.EMPTY_STR;
    assertEquals(ForumUtils.EMPTY_STR, ForumUtils.getCalculateListEmail(s));
    
    s = "abc@abc.com";
    assertEquals("<span title='abc@abc.com'>abc@abc.com</span>", ForumUtils.getCalculateListEmail(s));
    
    s = "abc@abc.com, abz@abc.com";
    assertEquals("<span title='abc@abc.com'>abc@abc.com</span>,<br/><span title='abz@abc.com'>abz@abc.com</span>", 
        ForumUtils.getCalculateListEmail(s));
    
    s = "abc@abc.com, emailverylong@exoplaforum.com";
    assertEquals("<span title='abc@abc.com'>abc@abc.com</span>,<br/><span title='emailverylong@exoplaforum.com'>emailverylong@e...</span>", 
        ForumUtils.getCalculateListEmail(s));
  }

}
