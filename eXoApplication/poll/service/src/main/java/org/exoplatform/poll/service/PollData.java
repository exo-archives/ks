/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.poll.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          tu.duy@exoplatform.com
 * Dec 21, 2010  
 */
public class PollData {
  public static final String DEFAULT_ID = PollNodeTypes.POLL + "DefaultDataPlugin";

  private String             parentPath;

  private String             owner;

  private String             question;

  private List<String>       options    = new ArrayList<String>();

  private String             timeOut;

  private String             isMultiCheck;

  private String             isClosed;

  private String             isAgainVote;

  public PollData() {
  }

  public String getParentPath() {
    return parentPath;
  }

  public void setParentPath(String parentPath) {
    this.parentPath = parentPath;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public List<String> getOptions() {
    return options;
  }

  public void setOptions(List<String> options) {
    this.options = options;
  }

  public void setTimeOut(String timeOut) {
    this.timeOut = timeOut;
  }

  public String getTimeOut() {
    return timeOut;
  }

  public String getIsMultiCheck() {
    return isMultiCheck;
  }

  public void setIsMultiCheck(String isMultiCheck) {
    this.isMultiCheck = isMultiCheck;
  }

  public String getIsClosed() {
    return isClosed;
  }

  public void setIsClosed(String isClosed) {
    this.isClosed = isClosed;
  }

  public String getIsAgainVote() {
    return isAgainVote;
  }

  public void setIsAgainVote(String isAgainVote) {
    this.isAgainVote = isAgainVote;
  }
}
