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
package org.exoplatform.faq.service;

/**
 * Constants for Forum nodetypes and properties.
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public interface FAQNodeTypes {

  public static final String EXO_IS_MODERATE_QUESTIONS    = "exo:isModerateQuestions";

  public static final String EXO_COMMENT_BY               = "exo:commentBy";

  public static final String EXO_FAQ_QUESTION             = "exo:faqQuestion";

  public static final String EXO_EMAIL_WATCHING           = "exo:emailWatching";

  public static final String EXO_FAQ_HOME                 = "exo:faqHome";

  public static final String EXO_FAQ_QUESTION_HOME        = "exo:faqQuestionHome";

  public static final String EXO_AUTHOR                   = "exo:author";

  public static final String EXO_NAME                     = "exo:name";

  public static final String EXO_ANSWER_HOME              = "exo:answerHome";

  public static final String MIX_FAQI_1_8N                = "mix:faqi18n";

  public static final String EXO_POST_ID                  = "exo:postId";

  public static final String EXO_SORT_QUESTION_BY_VOTE    = "exo:sortQuestionByVote";

  public static final String EXO_ORDE_TYPE                = "exo:ordeType";

  public static final String EXO_FAQ_ATTACHMENT           = "exo:faqAttachment";

  public static final String EXO_COMMENT_LANGUAGE         = "exo:commentLanguage";

  public static final String EXO_ACTIVATE_RESPONSES       = "exo:activateResponses";

  public static final String EXO_USERS_VOTE               = "exo:usersVote";

  public static final String EXO_IS_APPROVED              = "exo:isApproved";

  public static final String EXO_CONTENT                  = "exo:content";

  public static final String EXO_FAQ_LANGUAGE             = "exo:faqLanguage";

  public static final String EXO_FAQ_CATEGORY             = "exo:faqCategory";

  public static final String EXO_FAQ_USER_SETTING_HOME    = "exo:faqUserSettingHome";

  public static final String EXO_DESCRIPTION              = "exo:description";

  public static final String EXO_ORDE_BY                  = "exo:ordeBy";

  public static final String EXO_QUESTION_LANGUAGE_HOME   = "exo:questionLanguageHome";

  public static final String EXO_FAQ_RESOURCE             = "exo:faqResource";

  public static final String EXO_CATEGORY_ID              = "exo:categoryId";

  public static final String EXO_IS_ACTIVATED             = "exo:isActivated";

  public static final String EXO_LAST_ACTIVITY            = "exo:lastActivity";

  public static final String EXO_RSS_WATCHING             = "exo:rssWatching";

  public static final String EXO_EMAIL                    = "exo:email";

  public static final String EXO_MARK_VOTE                = "exo:markVote";

  public static final String EXO_ANSWER                   = "exo:answer";

  public static final String EXO_TITLE                    = "exo:title";

  public static final String EXO_FAQ_R_S_S                = "exo:faqRSS";

  public static final String EXO_MARK_VOTES              = "exo:MarkVotes";

  public static final String EXO_MODERATORS               = "exo:moderators";

  public static final String EXO_QUESTION_PATH            = "exo:questionPath";

  public static final String EXO_FULL_NAME                = "exo:fullName";

  public static final String EXO_TOPIC_ID_DISCUSS         = "exo:topicIdDiscuss";

  public static final String EXO_ANSWER_PATH              = "exo:answerPath";

  public static final String EXO_QUESTION_ID              = "exo:questionId";

  public static final String EXO_IS_MODERATE_ANSWERS      = "exo:isModerateAnswers";

  public static final String EXO_COMMENTS                 = "exo:comments";

  public static final String EXO_USER_WATCHING            = "exo:userWatching";

  public static final String EXO_FAQ_WATCHING             = "exo:faqWatching";

  public static final String EXO_IS_VIEW                  = "exo:isView";

  public static final String EXO_ID                       = "exo:id";

  public static final String EXO_DATE_RESPONSE            = "exo:dateResponse";

  public static final String EXO_APPROVE_RESPONSES        = "exo:approveResponses";

  public static final String EXO_RESPONSE_LANGUAGE        = "exo:responseLanguage";

  public static final String EXO_RESPONSE_BY              = "exo:responseBy";

  public static final String EXO_USER_PRIVATE             = "exo:userPrivate";

  public static final String EXO_FAQ_USER_SETTING         = "exo:faqUserSetting";

  public static final String EXO_INDEX                    = "exo:index";

  public static final String EXO_COMMENT                  = "exo:comment";

  public static final String EXO_LANGUAGE                 = "exo:language";

  public static final String EXO_RESPONSES                = "exo:responses";

  public static final String EXO_CREATED_DATE             = "exo:createdDate";

  public static final String EXO_RELATIVES                = "exo:relatives";

  public static final String EXO_FILE_NAME                = "exo:fileName";

  public static final String EXO_USERS_VOTE_ANSWER        = "exo:usersVoteAnswer";

  public static final String EXO_DATE_COMMENT             = "exo:dateComment";

  public static final String EXO_TEMPLATE_HOME            = "exo:templateHome";

  public static final String EXO_COMMENT_HOME             = "exo:commentHome";

  public static final String EXO_NAME_ATTACHS             = "exo:nameAttachs";

  public static final String EXO_NUMBER_OF_PUBLIC_ANSWERS = "exo:numberOfPublicAnswers";

  public static final String MIX_FAQ_SUB_CATEGORY         = "mix:faqSubCategory";

  public static final String EXO_LINK                     = "exo:link";

  public static final String EXO_FAQ_SETTING_HOME         = "exo:faqSettingHome";

  public static final String EXO_FAQ_CATEGORY_HOME        = "exo:faqCategoryHome";

  public static final String EXO_VIEW_AUTHOR_INFOR        = "exo:viewAuthorInfor";

  public static final String TEXT_HTML                    = "text/html";

  public static final String NT_FILE                      = "nt:file";

  public static final String JCR_ROOT                     = "/jcr:root";

  public static final String JCR_CONTENT                  = "jcr:content";

  public static final String JCR_MIME_TYPE                = "jcr:mimeType";

  public static final String JCR_LAST_MODIFIED            = "jcr:lastModified";

  public static final String JCR_DATA                     = "jcr:data";

  public static final String NT_RESOURCE                  = "nt:resource";

  public static final String AT                           = "@";

  public static final String EMPTY_STR                    = "";

}
