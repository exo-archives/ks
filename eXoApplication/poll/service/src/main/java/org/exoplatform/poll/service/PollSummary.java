package org.exoplatform.poll.service;

import java.util.ArrayList;
import java.util.List;

public class PollSummary {
  private String       id;

  private List<String> pollId;

  private List<String> pollName;

  private List<String> groupPrivate;

  private String       isAdmin = "false";

  public PollSummary() {
    setId("Empty");
    pollId = pollName = groupPrivate = new ArrayList<String>();
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setIsAdmin(String isAdmin) {
    this.isAdmin = isAdmin;
  }

  public String getIsAdmin() {
    return isAdmin;
  }

  public List<String> getPollId() {
    return pollId;
  }

  public void setPollId(List<String> pollId) {
    this.pollId = pollId;
  }

  public List<String> getPollName() {
    return pollName;
  }

  public void setPollName(List<String> pollName) {
    this.pollName = pollName;
  }

  public List<String> getGroupPrivate() {
    return groupPrivate;
  }

  public void setGroupPrivate(List<String> groupPrivate) {
    this.groupPrivate = new ArrayList<String>();
    for (String string : groupPrivate) {
      if (string.indexOf(PollNodeTypes.APPLICATION_DATA) > 0) {
        string = string.substring(string.indexOf("/", 2) + 1, string.indexOf(PollNodeTypes.APPLICATION_DATA) - 1);
        this.groupPrivate.add(string);
      } else {
        this.groupPrivate.add("");
      }
    }
  }
}
