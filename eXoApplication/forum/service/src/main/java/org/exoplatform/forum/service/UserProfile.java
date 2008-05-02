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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.util.Date;

import org.exoplatform.services.organization.User;

public class UserProfile {
	
	public static final long ADMIN = 0 ;
	public static final long MODERATOR = 1 ;
	public static final long USER = 2 ;
	public static final long GUEST = 3 ;
	
	private String userId ;
	private String userTitle ; //Rank of user
	private long userRole ; // values: 0: Admin ; 1: Moderator ; 2: User ; 3 guest
	private String signature ;
	private long totalPost = 0;
	private long totalTopic = 0;
	
	private String[] moderateForums ; //store Ids of forum this user is moderator
	private String[] moderateTopics ; //store Ids of topic this user is moderator
	private String[] readTopic ;
	private String[] bookmark ;
	
	private Date lastLoginDate = new Date();
	private Date lastPostDate;
	private boolean isDisplaySignature = true ;
	private boolean isDisplayAvatar = true ;
	//UserOption
	private Double timeZone ;
	private String shortDateformat;
	private String longDateformat ;
	private String timeFormat ;
	private long maxTopic = 10 ;
	private long maxPost = 10;
	private boolean isShowForumJump = true ;
	//UserBan
	private boolean isBanned = false ;
	private long banUntil = 0 ;
	private String banReason ;
	private int banCounter = 0 ;
	private String[] banReasonSummary ; // value: Ban reason + fromDate - toDate
	private Date createdDateBan ;
	
	private User user ;
	
	@SuppressWarnings("deprecation")
  public UserProfile() {
		userId = "Guest";
		userTitle = "Guest";
		userRole = GUEST ;
		moderateForums = new String[] {} ;
		moderateTopics = new String[] {} ;
		readTopic = new String[] {} ;
		bookmark = new String[] {} ;
		banReasonSummary = new String[] {} ;
		Date dateHost = new Date() ;
		timeZone = (double)dateHost.getTimezoneOffset()/ 60 ;
		shortDateformat = "MM/dd/yyyy";
		longDateformat = "DDD,MMM dd,yyyy";
		timeFormat = "hh:mm a";
	}
	
	public void setUserId(String userId) {this.userId = userId;}
	public String getUserId() {return this.userId ;}
	
	public void setUserTitle(String userTitle) {this.userTitle = userTitle;}
	public String getUserTitle() {return this.userTitle ;}
	
	public void setUserRole(Long userRole) {this.userRole = userRole;}
	public Long getUserRole() {return this.userRole ;}
	
	public void setSignature(String signature) {this.signature = signature;}
	public String getSignature() {return this.signature ;}
	
	public void setTotalPost(Long totalPost) {this.totalPost = totalPost;}
	public Long getTotalPost() {return this.totalPost ;}
	
	public void setTotalTopic(Long totalTopic) {this.totalTopic = totalTopic;}
	public Long getTotalTopic() {return this.totalTopic ;}

	public void setModerateForums(String[] moderateForums) { this.moderateForums = moderateForums ;	}
	public String[] getModerateForums() { return moderateForums ;	}

	public void setModerateTopics(String[] moderateTopics) { this.moderateTopics = moderateTopics ; }
	public String[] getModerateTopics() { return moderateTopics ;	}
	
	public String[] getReadTopic(){return readTopic;}
	public void setReadTopic(String[] readTopic){this.readTopic = readTopic;}

	public void setLastLoginDate(Date lastLoginDate) {this.lastLoginDate = lastLoginDate; }
	public Date getLastLoginDate() {return lastLoginDate; }
	
	public void setLastPostDate(Date lastPostDate) {this.lastPostDate = lastPostDate; }
	public Date getLastPostDate() {return lastPostDate; }
	
	public void setIsDisplaySignature(boolean isDisplaySignature) {this.isDisplaySignature = isDisplaySignature; }
	public boolean getIsDisplaySignature() {return isDisplaySignature; }
	
	public void setIsDisplayAvatar(boolean isDisplayAvatar) {this.isDisplayAvatar = isDisplayAvatar; }
	public boolean getIsDisplayAvatar() {return isDisplayAvatar; }
	//Option
	public void setTimeZone(Double timeZone) { this.timeZone = timeZone ; }
	public double getTimeZone() {return this.timeZone ;	}
	
	public void setShortDateFormat(String shortDateformat) { this.shortDateformat = shortDateformat ;}
	public String getShortDateFormat() { return this.shortDateformat;}

	public void setLongDateFormat(String longDateformat) { this.longDateformat = longDateformat ;}
	public String getLongDateFormat() { return this.longDateformat;}

	public void setTimeFormat(String timeFormat) {this.timeFormat = timeFormat;}
	public String getTimeFormat() { return this.timeFormat ;}
	
	public void setMaxTopicInPage(long maxTopic) {this.maxTopic = maxTopic ;}
	public Long getMaxTopicInPage() {return this.maxTopic ;}
	
	public void setMaxPostInPage(long maxPost) { this.maxPost = maxPost ;}
	public Long getMaxPostInPage() {return this.maxPost ;}
	
	public void setIsShowForumJump(boolean isShowForumJump) {this.isShowForumJump = isShowForumJump;}
	public boolean getIsShowForumJump() {return this.isShowForumJump ;}
	//Ban
	public void setIsBanned(boolean isBanned) {this.isBanned = isBanned; }
	public boolean getIsBanned() {return isBanned; }

	public void setBanUntil(long banUntil) {this.banUntil = banUntil; }
	public Long getBanUntil() {return banUntil; }

	public void setBanReason(String banReason) {this.banReason = banReason; }
	public String getBanReason() {return banReason; }

	public void setBanCounter(int banCounter) {this.banCounter = banCounter; }
	public int getBanCounter() {return banCounter; }

	public void setBanReasonSummary(String[] banReasonSummary) {this.banReasonSummary = banReasonSummary; }
	public String[] getBanReasonSummary() {return banReasonSummary; }

	public void setCreatedDateBan(Date createdDate) {this.createdDateBan = createdDate; }
	public Date getCreatedDateBan() {return createdDateBan; }

  public void setUser(User user) {this.user = user;}
  public User getUser() {return user; }

	public String[] getBookmark() {return bookmark;}
	public void setBookmark(String[] bookmark) {this.bookmark = bookmark;}

}