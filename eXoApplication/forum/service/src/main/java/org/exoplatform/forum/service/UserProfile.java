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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserProfile {

  public static final long    ADMIN                  = 0;

  public static final long    MODERATOR              = 1;

  public static final long    USER                   = 2;

  public static final long    GUEST                  = 3;

  public static final long    USER_DELETED           = 4;

  public static final String  USER_GUEST             = "user_gest_uoom";

  public static final String  USER_REMOVED           = "User deleted";

  private String              userId;

  private String              screenName;

  private String              userTitle;                                             // Rank of user

  private long                userRole;                                              // values: 0: Admin ; 1: Moderator ; 2: User ; 3 guest

  private String              signature              = "";

  private long                totalPost              = 0;

  private long                totalTopic             = 0;

  private long                jobWattingForModerator = 0;

  private boolean             isOnline               = false;

  private String[]            moderateForums;                                        // store Ids of forum this user is moderator

  private String[]            moderateCategory;                                      // store Ids of category this user is moderator

  private String[]            readTopic;                                             // for check read/unread topic

  private Map<String, Long>   lastAccessTopics       = new HashMap<String, Long>();

  private String[]            readForum;                                             // for check read/unread forum

  private Map<String, Long>   lastAccessForums       = new HashMap<String, Long>();

  private String[]            bookmark;

  private String[]            lastReadPostOfTopic;

  private String[]            lastReadPostOfForum;

  private Map<String, String> lastPostIdReadOfTopic  = new HashMap<String, String>();

  private Map<String, String> lastPostIdReadOfForum  = new HashMap<String, String>();

  private Date                joinedDate             = null;

  private Date                lastLoginDate          = null;

  private String              fullName               = "";

  private String              firstName              = "";

  private String              lastName               = "";

  private String              email                  = "";

  private Date                lastPostDate           = null;

  private boolean             isDisplaySignature     = true;

  private boolean             isDisplayAvatar        = true;

  // UserOption
  private Double              timeZone;

  private String              shortDateformat;

  private String              longDateformat;

  private String              timeFormat;

  private long                maxTopic               = 10;

  private long                maxPost                = 10;

  private boolean             isShowForumJump        = true;

  private boolean             isAutoWatchMyTopics    = false;

  private boolean             isAutoWatchTopicIPost  = false;

  private String[]            collapCategories;

  // UserBan
  private boolean             isBanned               = false;

  private long                banUntil               = 0;

  private String              banReason;

  private int                 banCounter             = 0;

  private String[]            banReasonSummary;                                      // value: Ban reason + fromDate - toDate

  private Date                createdDateBan;

  private long                newMessage             = 0;

  private long                totalMessage           = 0;

  @SuppressWarnings("deprecation")
  public UserProfile() {
    userId = USER_GUEST;
    userTitle = "Guest";
    userRole = GUEST;
    moderateForums = new String[] {};
    readTopic = new String[] {};
    bookmark = new String[] {};
    banReasonSummary = new String[] {};
    collapCategories = new String[] {};
    lastReadPostOfTopic = new String[] { "" };
    lastReadPostOfForum = new String[] { "" };
    Date dateHost = new Date();
    timeZone = (double) dateHost.getTimezoneOffset() / 60;
    shortDateformat = "MM/dd/yyyy";
    longDateformat = "EEE,MMM dd,yyyy";
    timeFormat = "hh:mm a";
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return this.userId;
  }

  public void setUserTitle(String userTitle) {
    this.userTitle = userTitle;
  }

  public String getUserTitle() {
    return this.userTitle;
  }

  public void setUserRole(long userRole) {
    this.userRole = userRole;
  }

  public long getUserRole() {
    return this.userRole;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public String getSignature() {
    return this.signature;
  }

  public void setTotalPost(long totalPost) {
    this.totalPost = totalPost;
  }

  public long getTotalPost() {
    return this.totalPost;
  }

  public void setTotalTopic(long totalTopic) {
    this.totalTopic = totalTopic;
  }

  public long getTotalTopic() {
    return this.totalTopic;
  }

  public long getJobWattingForModerator() {
    return jobWattingForModerator;
  }

  public void setJobWattingForModerator(long jobWattingForModerator) {
    this.jobWattingForModerator = jobWattingForModerator;
  }

  public void setModerateForums(String[] moderateForums) {
    this.moderateForums = Utils.arrayCopy(moderateForums);
  }

  public String[] getModerateForums() {
    return moderateForums;
  }

  public String[] getModerateCategory() {
    return moderateCategory;
  }

  public void setModerateCategory(String[] moderateCategory) {
    this.moderateCategory = Utils.arrayCopy(moderateCategory);
  }

  public String[] getReadTopic() {
    return readTopic;
  }

  public void setReadTopic(String[] readTopic) {
    this.readTopic = readTopic;
  }

  public String[] getReadForum() {
    return readForum;
  }

  public void setReadForum(String[] readForum) {
    this.readForum = readForum;
  }

  public void setLastLoginDate(Date lastLoginDate) {
    this.lastLoginDate = lastLoginDate;
  }

  public Date getLastLoginDate() {
    return lastLoginDate;
  }

  public void setJoinedDate(Date joinDate) {
    this.joinedDate = joinDate;
  }

  public Date getJoinedDate() {
    return joinedDate;
  }

  public void setLastPostDate(Date lastPostDate) {
    this.lastPostDate = lastPostDate;
  }

  public Date getLastPostDate() {
    return lastPostDate;
  }

  public void setIsDisplaySignature(boolean isDisplaySignature) {
    this.isDisplaySignature = isDisplaySignature;
  }

  public boolean getIsDisplaySignature() {
    return isDisplaySignature;
  }

  public void setIsDisplayAvatar(boolean isDisplayAvatar) {
    this.isDisplayAvatar = isDisplayAvatar;
  }

  public boolean getIsDisplayAvatar() {
    return isDisplayAvatar;
  }

  // Option
  public void setTimeZone(Double timeZone) {
    this.timeZone = timeZone;
  }

  public double getTimeZone() {
    return this.timeZone;
  }

  public void setShortDateFormat(String shortDateformat) {
    this.shortDateformat = shortDateformat;
  }

  public String getShortDateFormat() {
    return this.shortDateformat;
  }

  public void setLongDateFormat(String longDateformat) {
    this.longDateformat = longDateformat;
  }

  public String getLongDateFormat() {
    return this.longDateformat;
  }

  public void setTimeFormat(String timeFormat) {
    this.timeFormat = timeFormat;
  }

  public String getTimeFormat() {
    return this.timeFormat;
  }

  public void setMaxTopicInPage(long maxTopic) {
    this.maxTopic = maxTopic;
  }

  public long getMaxTopicInPage() {
    return this.maxTopic;
  }

  public void setMaxPostInPage(long maxPost) {
    this.maxPost = maxPost;
  }

  public long getMaxPostInPage() {
    return this.maxPost;
  }

  public void setIsShowForumJump(boolean isShowForumJump) {
    this.isShowForumJump = isShowForumJump;
  }

  public boolean getIsShowForumJump() {
    return this.isShowForumJump;
  }

  public boolean getIsAutoWatchMyTopics() {
    return isAutoWatchMyTopics;
  }

  public void setIsAutoWatchMyTopics(boolean isAutoWatchMyTopics) {
    this.isAutoWatchMyTopics = isAutoWatchMyTopics;
  }

  public boolean getIsAutoWatchTopicIPost() {
    return isAutoWatchTopicIPost;
  }

  public void setIsAutoWatchTopicIPost(boolean isAutoWatchTopicIPost) {
    this.isAutoWatchTopicIPost = isAutoWatchTopicIPost;
  }

  public String[] getCollapCategories() {
    return collapCategories;
  }

  public void setCollapCategories(String[] collapCategories) {
    this.collapCategories = Utils.arrayCopy(collapCategories);
  }

  // Ban
  public void setIsBanned(boolean isBanned) {
    this.isBanned = isBanned;
  }

  public boolean getIsBanned() {
    return isBanned;
  }

  public void setBanUntil(long banUntil) {
    this.banUntil = banUntil;
  }

  public long getBanUntil() {
    return banUntil;
  }

  public void setBanReason(String banReason) {
    this.banReason = banReason;
  }

  public String getBanReason() {
    return banReason;
  }

  public void setBanCounter(int banCounter) {
    this.banCounter = banCounter;
  }

  public int getBanCounter() {
    return banCounter;
  }

  public void setBanReasonSummary(String[] banReasonSummary) {
    this.banReasonSummary = Utils.arrayCopy(banReasonSummary);
  }

  public String[] getBanReasonSummary() {
    return banReasonSummary;
  }

  public void setCreatedDateBan(Date createdDate) {
    this.createdDateBan = createdDate;
  }

  public Date getCreatedDateBan() {
    return createdDateBan;
  }

  public String[] getBookmark() {
    return bookmark;
  }

  public void setBookmark(String[] bookmark) {
    this.bookmark = Utils.arrayCopy(bookmark);
  }

  public String[] getLastReadPostOfTopic() {
    return lastReadPostOfTopic;
  }

  public void setLastReadPostOfTopic(String[] lastReadPostOfTopic) {
    this.lastReadPostOfTopic = lastReadPostOfTopic;
    lastPostIdReadOfTopic = Utils.arrayToMap(lastReadPostOfTopic);
  }

  public String getLastPostIdReadOfTopic(String topicId) {
    if (lastPostIdReadOfTopic.containsKey(topicId))
      return lastPostIdReadOfTopic.get(topicId);
    else
      return "";
  }

  public void addLastPostIdReadOfTopic(String topicId, String postId) {
    lastPostIdReadOfTopic.put(topicId, postId);
    lastReadPostOfTopic = Utils.mapToArray(lastPostIdReadOfTopic);
  }

  public String[] getLastReadPostOfForum() {
    return lastReadPostOfForum;
  }

  public void setLastReadPostOfForum(String[] lastReadPostOfForum) {
    this.lastReadPostOfForum = lastReadPostOfForum;
    lastPostIdReadOfForum = Utils.arrayToMap(lastReadPostOfForum);
  }

  public String getLastPostIdReadOfForum(String forumId) {
    if (lastPostIdReadOfForum.containsKey(forumId))
      return lastPostIdReadOfForum.get(forumId);
    else
      return "";
  }

  public void addLastPostIdReadOfForum(String forumId, String postId) {
    lastPostIdReadOfForum.put(forumId, postId);
    lastReadPostOfForum = Utils.mapToArray(lastPostIdReadOfForum);
  }

  public boolean getIsOnline() {
    return isOnline;
  }

  public void setIsOnline(boolean isOnline) {
    this.isOnline = isOnline;
  }

  public long getNewMessage() {
    return newMessage;
  }

  public void setNewMessage(long isNewMessage) {
    this.newMessage = isNewMessage;
  }

  public long getTotalMessage() {
    return totalMessage;
  }

  public void setTotalMessage(long totalMessage) {
    this.totalMessage = totalMessage;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getScreenName() {
    if (screenName == null || screenName.trim().length() == 0)
      screenName = userId;
    return screenName;
  }

  public void setScreenName(String screenName) {
    this.screenName = screenName;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public void setLastTimeAccessTopic(String topicId, long lastTime) throws Exception {
    lastAccessTopics.put(topicId, lastTime);
  }

  public long getLastTimeAccessTopic(String topicId) throws Exception {
    if (lastAccessTopics.get(topicId) != null) {
      return lastAccessTopics.get(topicId);
    }
    return 0;
  }

  public void setLastTimeAccessForum(String forumId, long lastTime) throws Exception {
    lastAccessForums.put(forumId, lastTime);
  }

  public long getLastTimeAccessForum(String forumId) throws Exception {
    if (lastAccessForums.get(forumId) != null) {
      return lastAccessForums.get(forumId);
    }
    return 0;
  }
}
