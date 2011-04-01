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
package org.exoplatform.forum.service;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.ks.common.jcr.JCRListAccess;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.ks.common.jcr.SessionManager;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl;

/**
 * List access for topics.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TopicListAccess extends JCRListAccess<Topic> {
  String topicQuery;

  /**
   * 
   * @param topicQuery the JCR xpath query that will match topic nodes
   */
  public TopicListAccess(SessionManager sessionManager, String topicQuery) {
    super(sessionManager);
    this.topicQuery = topicQuery;
  }

  @Override
  protected int getSize(Session session) throws Exception {
    QueryManager qm = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) qm.createQuery(topicQuery, Query.XPATH);
    QueryResultImpl result = (QueryResultImpl) query.execute();
    return result.getTotalSize();
  }

  @Override
  protected Topic[] load(Session session, int index, int length) throws Exception, IllegalArgumentException {
    QueryManager qm = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) qm.createQuery(topicQuery, Query.XPATH);
    query.setOffset(index);
    query.setLimit(index + length);
    QueryResult result = query.execute();
    List<Topic> topicList = new ArrayList<Topic>();
    NodeIterator iter = result.getNodes();
    while (iter.hasNext()) {
      topicList.add(getTopic(iter.nextNode()));
    }
    Topic[] topics = topicList.toArray(new Topic[topicList.size()]);
    return topics;
  }

  private Topic getTopic(Node topicNode) throws Exception {
    if (topicNode == null)
      return null;
    PropertyReader reader = new PropertyReader(topicNode);
    Topic topicNew = new Topic();
    topicNew.setId(topicNode.getName());
    topicNew.setPath(topicNode.getPath());

    topicNew.setLastPostDate(reader.date("exo:lastPostDate"));
    topicNew.setLastPostBy(reader.string("exo:lastPostBy"));
    topicNew.setOwner(reader.string("exo:owner"));
    topicNew.setTopicName(reader.string("exo:name"));
    topicNew.setTopicType(reader.string("exo:topicType", " "));
    topicNew.setDescription(reader.string("exo:description"));
    topicNew.setPostCount(reader.l("exo:postCount"));
    topicNew.setViewCount(reader.l("exo:viewCount"));
    topicNew.setIsPoll(reader.bool("exo:isPoll"));
    topicNew.setIsSticky(reader.bool("exo:isSticky"));
    if (topicNode.getParent().getProperty("exo:isLock").getBoolean())
      topicNew.setIsLock(true);
    else
      topicNew.setIsLock(reader.bool("exo:isLock"));
    topicNew.setIsApproved(reader.bool("exo:isApproved"));
    topicNew.setNumberAttachment(reader.l("exo:numberAttachments"));
    topicNew.setIcon(reader.string("exo:icon"));
    topicNew.setIsModeratePost(reader.bool("exo:isModeratePost"));
    topicNew.setIsClosed(reader.bool("exo:isClosed"));
    topicNew.setIsWaiting(reader.bool("exo:isWaiting"));
    topicNew.setIsActive(reader.bool("exo:isActive"));
    topicNew.setVoteRating(reader.d("exo:voteRating"));
    topicNew.setUserVoteRating(reader.strings("exo:userVoteRating"));
    return topicNew;
  }

}
