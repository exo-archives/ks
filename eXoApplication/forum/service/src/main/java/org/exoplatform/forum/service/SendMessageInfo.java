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

import java.util.List;

import org.exoplatform.services.mail.Message;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *         hung.nguyen@exoplatform.com
 * Aug 26, 2008
 */
public class SendMessageInfo {
  private List<String> emailAddresses;

  private Message      message;

  public SendMessageInfo(List<String> emails, Message mes) {
    emailAddresses = emails;
    message = mes;
  }

  public void setEmailAddresses(List<String> emailAddresses_) {
    emailAddresses = emailAddresses_;
  }

  public List<String> getEmailAddresses() {
    return emailAddresses;
  }

  public void setMessage(Message message) {
    this.message = message;
  }

  public Message getMessage() {
    return message;
  }

}
