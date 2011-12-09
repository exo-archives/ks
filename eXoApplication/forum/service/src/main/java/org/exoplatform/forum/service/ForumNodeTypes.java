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
package org.exoplatform.forum.service;

/**
 * Constants for Forum nodetypes and properties.
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public interface ForumNodeTypes {

  public static final String EXO_FORUM_TAG                  = "exo:forumTag";

  public static final String EXO_USER_TAG                   = "exo:userTag";

  public static final String EXO_USE_COUNT                  = "exo:useCount";

  public static final String EXO_POLL                       = "exo:poll";

  public static final String EXO_IS_AGAIN_VOTE              = "exo:isAgainVote";

  public static final String EXO_IS_MULTI_CHECK             = "exo:isMultiCheck";

  public static final String EXO_USER_VOTE                  = "exo:userVote";

  public static final String EXO_VOTE                       = "exo:vote";

  public static final String EXO_OPTION                     = "exo:option";

  public static final String EXO_QUESTION                   = "exo:question";

  public static final String EXO_TIME_OUT                   = "exo:timeOut";

  public static final String EXO_USER_WATCHING              = "exo:userWatching";

  public static final String EXO_USER_PRI                   = "exoUserPri";

  public static final String EXO_READ_FORUM                 = "exo:readForum";

  public static final String EXO_LAST_READ_POST_OF_TOPIC    = "exo:lastReadPostOfTopic";

  public static final String EXO_LAST_READ_POST_OF_FORUM    = "exo:lastReadPostOfForum";

  public static final String TEXT_HTML                      = "text/html";

  public static final String EXO_EMAIL                      = "exo:email";

  public static final String EXO_FULL_NAME                  = "exo:fullName";

  public static final String KNOWLEDGE_SUITE_FORUM_JOBS     = "KnowledgeSuite-forum";

  public static final String EXO_TOTAL_TOPIC                = "exo:totalTopic";

  public static final String EXO_IS_STICKY                  = "exo:isSticky";

  public static final String EXO_VOTE_RATING                = "exo:voteRating";

  public static final String EXO_TAG_ID                     = "exo:tagId";

  public static final String EXO_IS_NOTIFY_WHEN_ADD_POST    = "exo:isNotifyWhenAddPost";

  public static final String EXO_USER_VOTE_RATING           = "exo:userVoteRating";

  public static final String EXO_NUMBER_ATTACHMENTS         = "exo:numberAttachments";

  public static final String EXO_TOPIC_TYPE                 = "exo:topicType";

  public static final String EXO_IS_POLL                    = "exo:isPoll";

  public static final String EXO_LAST_POST_BY               = "exo:lastPostBy";

  public static final String EXO_VIEW_COUNT                 = "exo:viewCount";

  public static final String JCR_ROOT                       = "/jcr:root";

  public static final String EXO_IS_ACTIVE                  = "exo:isActive";

  public static final String EXO_IS_WAITING                 = "exo:isWaiting";

  public static final String EXO_IS_ACTIVE_BY_FORUM         = "exo:isActiveByForum";

  public static final String EXO_TOPIC                      = "exo:topic";

  public static final String EXO_IS_LOCK                    = "exo:isLock";

  public static final String EXO_IS_CLOSED                  = "exo:isClosed";

  public static final String EXO_IS_MODERATE_POST           = "exo:isModeratePost";

  public static final String EXO_NOTIFY_WHEN_ADD_TOPIC      = "exo:notifyWhenAddTopic";

  public static final String EXO_NOTIFY_WHEN_ADD_POST       = "exo:notifyWhenAddPost";

  public static final String EXO_IS_AUTO_ADD_EMAIL_NOTIFY   = "exo:isAutoAddEmailNotify";

  public static final String EXO_FORUM_ORDER                = "exo:forumOrder";

  public static final String EXO_IS_MODERATE_TOPIC          = "exo:isModerateTopic";

  public static final String EXO_BAN_I_PS                   = "exo:banIPs";

  public static final String EXO_TOPIC_COUNT                = "exo:topicCount";

  public static final String EXO_POST_COUNT                 = "exo:postCount";

  public static final String EXO_LAST_TOPIC_PATH            = "exo:lastTopicPath";

  public static final String EXO_USER_ROLE                  = "exo:userRole";

  public static final String EXO_MODERATE_FORUMS            = "exo:moderateForums";

  public static final String EXO_MODERATE_CATEGORY          = "exo:moderateCategory";

  public static final String EXO_EMAIL_WATCHING             = "exo:emailWatching";

  public static final String EXO_FORUM_WATCHING             = "exo:forumWatching";

  public static final String EXO_FORUM_COUNT                = "exo:forumCount";

  public static final String EXO_CAN_VIEW                   = "exo:canView";

  public static final String EXO_CAN_POST                   = "exo:canPost";

  public static final String EXO_CREATE_TOPIC_ROLE          = "exo:createTopicRole";

  public static final String EXO_DESCRIPTION                = "exo:description";

  public static final String EXO_CATEGORY_ORDER             = "exo:categoryOrder";

  public static final String EXO_VIEWER                     = "exo:viewer";

  public static final String EXO_POSTER                     = "exo:poster";

  public static final String EXO_TEMP_MODERATORS            = "exo:tempModerators";

  public static final String EXO_MODERATORS                 = "exo:moderators";

  public static final String EXO_ADMINISTRATION             = "exo:administration";

  public static final String EXO_NOTIFY_EMAIL_MOVED         = "exo:notifyEmailMoved";

  public static final String EXO_NOTIFY_EMAIL_CONTENT       = "exo:notifyEmailContent";

  public static final String EXO_HEADER_SUBJECT             = "exo:headerSubject";

  public static final String EXO_ENABLE_HEADER_SUBJECT      = "exo:enableHeaderSubject";

  public static final String EXO_CENSORED_KEYWORD           = "exo:censoredKeyword";

  public static final String EXO_TOPIC_SORT_BY_TYPE         = "exo:topicSortByType";

  public static final String EXO_TOPIC_SORT_BY              = "exo:topicSortBy";

  public static final String EXO_FORUM_SORT_BY_TYPE         = "exo:forumSortByType";

  public static final String EXO_FORUM_SORT_BY              = "exo:forumSortBy";

  public static final String EXO_FORUM_RESOURCE             = "exo:forumResource";

  public static final String EXO_LAST_POST_DATE             = "exo:lastPostDate";

  public static final String EXO_USER_TITLE                 = "exo:userTitle";

  public static final String EXO_USER_ID                    = "exo:userId";

  public static final String EXO_TOTAL_POST                 = "exo:totalPost";

  public static final String EXO_IS_FIRST_POST              = "exo:isFirstPost";

  public static final String EXO_PATH                       = "exo:path";

  public static final String EXO_ID                         = "exo:id";

  public static final String EXO_POST                       = "exo:post";

  public static final String EXO_FILE_NAME                  = "exo:fileName";

  public static final String EXO_FILE_SIZE                  = "exo:fileSize";

  public static final String EXO_FORUM_ATTACHMENT           = "exo:forumAttachment";

  public static final String EXO_NUMBER_ATTACH              = "exo:numberAttach";

  public static final String EXO_USER_PRIVATE               = "exo:userPrivate";

  public static final String EXO_IS_ACTIVE_BY_TOPIC         = "exo:isActiveByTopic";

  public static final String EXO_IS_HIDDEN                  = "exo:isHidden";

  public static final String EXO_IS_APPROVED                = "exo:isApproved";

  public static final String EXO_LINK                       = "exo:link";

  public static final String EXO_ICON                       = "exo:icon";

  public static final String EXO_REMOTE_ADDR                = "exo:remoteAddr";

  public static final String EXO_MESSAGE                    = "exo:message";

  public static final String EXO_NAME                       = "exo:name";

  public static final String EXO_EDIT_REASON                = "exo:editReason";

  public static final String EXO_MODIFIED_DATE              = "exo:modifiedDate";

  public static final String EXO_MODIFIED_BY                = "exo:modifiedBy";

  public static final String EXO_CREATED_DATE               = "exo:createdDate";

  public static final String EXO_OWNER                      = "exo:owner";

  public static final String EXO_FORUM                      = "exo:forum";

  public static final String EXO_FORUM_CATEGORY             = "exo:forumCategory";

  public static final String EXO_IS_AUTO_WATCH_MY_TOPICS    = "exo:isAutoWatchMyTopics";

  public static final String EXO_IS_AUTO_WATCH_TOPIC_I_POST = "exo:isAutoWatchTopicIPost";

  public static final String EXO_USER_DELETED               = "exo:userDeleted";

  public static final String EXO_SCREEN_NAME                = "exo:screenName";

  public static final String EXO_FIRST_NAME                 = "exo:firstName";

  public static final String EXO_LAST_NAME                  = "exo:lastName";

  public static final String EXO_SIGNATURE                  = "exo:signature";

  public static final String EXO_IS_DISPLAY_SIGNATURE       = "exo:isDisplaySignature";

  public static final String EXO_IS_DISPLAY_AVATAR          = "exo:isDisplayAvatar";

  public static final String EXO_TIME_ZONE                  = "exo:timeZone";

  public static final String EXO_SHORT_DATEFORMAT           = "exo:shortDateformat";

  public static final String EXO_LONG_DATEFORMAT            = "exo:longDateformat";

  public static final String EXO_TIME_FORMAT                = "exo:timeFormat";

  public static final String EXO_MAX_POST                   = "exo:maxPost";

  public static final String EXO_MAX_TOPIC                  = "exo:maxTopic";

  public static final String EXO_IS_SHOW_FORUM_JUMP         = "exo:isShowForumJump";

  public static final String EXO_BAN_UNTIL                  = "exo:banUntil";

  public static final String EXO_BAN_REASON                 = "exo:banReason";

  public static final String EXO_BAN_COUNTER                = "exo:banCounter";

  public static final String EXO_BAN_REASON_SUMMARY         = "exo:banReasonSummary";

  public static final String EXO_CREATED_DATE_BAN           = "exo:createdDateBan";

  public static final String EXO_COLLAP_CATEGORIES          = "exo:collapCategories";

  public static final String EXO_IS_BANNED                  = "exo:isBanned";

  public static final String EXO_PRIVATE_MESSAGE            = "exo:privateMessage";

  public static final String EXO_FROM                       = "exo:from";

  public static final String EXO_SEND_TO                    = "exo:sendTo";

  public static final String EXO_RECEIVED_DATE              = "exo:receivedDate";

  public static final String EXO_TYPE                       = "exo:type";

  public static final String EXO_IS_UNREAD                  = "exo:isUnread";

  public static final String EXO_NEW_MESSAGE                = "exo:newMessage";

  public static final String EXO_FORUM_SUBSCRIPTION         = "exo:forumSubscription";

  public static final String EXO_CATEGORY_IDS               = "exo:categoryIds";

  public static final String EXO_FORUM_IDS                  = "exo:forumIds";

  public static final String EXO_TOPIC_IDS                  = "exo:topicIds";

  public static final String EXO_MEMBERS_COUNT              = "exo:membersCount";

  public static final String EXO_NEW_MEMBERS                = "exo:newMembers";

  public static final String EXO_MOST_USERS_ONLINE          = "exo:mostUsersOnline";

  public static final String EXO_RSS_WATCHING               = "exo:rssWatching";

  public static final String EXO_JOB_WATTING_FOR_MODERATOR  = "exo:jobWattingForModerator";

  public static final String EXO_ACTIVE_USERS               = "exo:activeUsers";

  public static final String EXO_CATEGORY_HOME              = "exo:categoryHome";

  public static final String EXO_USER_PROFILE_HOME          = "exo:userProfileHome";

  public static final String EXO_TAG_HOME                   = "exo:tagHome";

  public static final String EXO_FORUM_BB_CODE_HOME         = "exo:forumBBCodeHome";

  public static final String EXO_ADMINISTRATION_HOME        = "exo:administrationHome";

  public static final String EXO_BAN_IP_HOME                = "exo:banIPHome";

  public static final String EXO_JOINED_DATE                = "exo:joinedDate";

  public static final String EXO_LAST_LOGIN_DATE            = "exo:lastLoginDate";

  public static final String EXO_PRUNE_SETTING              = "exo:pruneSetting";

  public static final String EXO_LAST_RUN_DATE              = "exo:lastRunDate";

  public static final String EXO_PERIOD_TIME                = "exo:periodTime";

  public static final String EXO_IN_ACTIVE_DAY              = "exo:inActiveDay";

  public static final String EXO_IPS                        = "exo:ips";

  public static final String EXO_BOOKMARK                   = "exo:bookmark";

  public static final String EXO_READ_TOPIC                 = "exo:readTopic";

  public static final String NT_FILE                        = "nt:file";

  public static final String JCR_CONTENT                    = "jcr:content";

  public static final String JCR_MIME_TYPE                  = "jcr:mimeType";

  public static final String JCR_LAST_MODIFIED              = "jcr:lastModified";

  public static final String JCR_DATA                       = "jcr:data";

  public static final String NT_RESOURCE                    = "nt:resource";

  public static final String ASCENDING                      = " ascending";

  public static final String DESCENDING                     = " descending";

}
