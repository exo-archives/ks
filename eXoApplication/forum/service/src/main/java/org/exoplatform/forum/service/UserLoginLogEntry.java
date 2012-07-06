/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.util.Calendar;

public class UserLoginLogEntry {

  public String   tenantName = Utils.DEFAULT_TENANT_NAME;

  public String   userName;

  public int      totalOnline;

  public Calendar loginTime;

  public UserLoginLogEntry(String tenantName, String username, int totalonline, Calendar time) {
    this.userName = username;
    this.totalOnline = totalonline;
    this.loginTime = time;
    this.tenantName = tenantName;
  }

}
