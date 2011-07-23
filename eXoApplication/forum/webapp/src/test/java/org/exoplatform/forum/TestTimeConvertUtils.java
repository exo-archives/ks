/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

import java.util.Calendar;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jul 5, 2011  
 */
public class TestTimeConvertUtils extends TestCase {
  public TestTimeConvertUtils() throws Exception {
    super();
  }

  public void testConvertDateTime() throws Exception {
    long timeNow = ForumUtils.getInstanceTempCalendar().getTimeInMillis();
    Calendar calendar = Calendar.getInstance();
    long day = 24 * 60 * 60 * 1000;
    // test for 1 year ago
    calendar.setTimeInMillis(timeNow - 366 * day);
    assertEquals("1 YEAR", TimeConvertUtils.convertDateTime(calendar.getTime()));
    // test for 2 years ago
    calendar.setTimeInMillis(timeNow - 2 * 366 * day);
    assertEquals("2 YEARS", TimeConvertUtils.convertDateTime(calendar.getTime()));
    // test for 1 month ago
    calendar.setTimeInMillis(timeNow - 31 * day);
    assertEquals("1 MONTH", TimeConvertUtils.convertDateTime(calendar.getTime()));
    // test for 2 months ago
    calendar.setTimeInMillis(timeNow - (2 * 31 * day));
    assertEquals("2 MONTHS", TimeConvertUtils.convertDateTime(calendar.getTime()));
    // test for 1 week ago
    calendar.setTimeInMillis(timeNow - 7 * day);
    assertEquals("1 WEEK", TimeConvertUtils.convertDateTime(calendar.getTime()));
    // test for 2 weeks ago
    calendar.setTimeInMillis(timeNow - 2 * 7 * day);
    assertEquals("2 WEEKS", TimeConvertUtils.convertDateTime(calendar.getTime()));
    // test for 1 day ago
    calendar.setTimeInMillis(timeNow - day);
    assertEquals("1 DAY", TimeConvertUtils.convertDateTime(calendar.getTime()));
    // test for 2 days ago
    calendar.setTimeInMillis(timeNow - 2 * day);
    assertEquals("2 DAYS", TimeConvertUtils.convertDateTime(calendar.getTime()));
    // test for 1 hour ago
    calendar.setTimeInMillis(timeNow - 60 * 60 * 1000);
    assertEquals("1 HOUR", TimeConvertUtils.convertDateTime(calendar.getTime()));
    // test for 2 hours ago
    calendar.setTimeInMillis(timeNow - 2 * 60 * 60 * 1000);
    assertEquals("2 HOURS", TimeConvertUtils.convertDateTime(calendar.getTime()));
    // test for 1 minute ago
    calendar.setTimeInMillis(timeNow - 60 * 1000);
    assertEquals("1 MINUTE", TimeConvertUtils.convertDateTime(calendar.getTime()));
    // test for 2 minute ago
    calendar.setTimeInMillis(timeNow - 2 * 60 * 1000);
    assertEquals("2 MINUTES", TimeConvertUtils.convertDateTime(calendar.getTime()));
    // test for less than 1 minute ago
    calendar.setTimeInMillis(timeNow - 59 * 1000);
    assertEquals("JUSTNOW", TimeConvertUtils.convertDateTime(calendar.getTime()));
  }

  public void testConvertDateTimeResourceBundle() {
    // Can not test this function because: can not get resource bundle from test case.
  }
}
