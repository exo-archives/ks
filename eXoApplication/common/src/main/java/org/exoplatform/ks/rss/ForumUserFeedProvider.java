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
package org.exoplatform.ks.rss;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.ks.common.jcr.JCRTask;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class ForumUserFeedProvider extends RSSProcess implements FeedContentProvider {

  private static final Log LOG = ExoLogger.getLogger(ForumUserFeedProvider.class);

  public InputStream getFeedContent(String userId) {
    return dataLocator.getSessionManager().executeAndSave(new GetUserFeedStreamTask(userId));
  }

  class GetUserFeedStreamTask implements JCRTask<InputStream> {

    private String userId;

    public GetUserFeedStreamTask(String userId) {
      this.userId = userId;
    }

    public InputStream execute(Session session) throws Exception {
      if (userId == null || userId.trim().length() == 0) {
        LOG.warn("no feed stream was generated for null user");
        return null;
      }
      InputStream inputStream = null;
      Map<String, SyndEntry> mapEntries = new HashMap<String, SyndEntry>();

      for (String objectId : getForumSubscription(userId)) {

        SyndEntry syndEntry = null;
        try {
          Node node = getNodeById(objectId);
          RSS data = new RSS(node);
          SyndFeed feed = data.read();
          for (Object entry : feed.getEntries()) {
            syndEntry = (SyndEntry) entry;
            mapEntries.put(syndEntry.getUri(), syndEntry);
          }
        } catch (Exception e) {
          LOG.warn("Failed to get user subscription " + objectId,e);
        }
      }
      SyndFeed feed = RSS.createNewFeed("Forum subscriptions for " + userId, new Date());
      feed.setDescription(" ");
      feed.setEntries(Arrays.asList(mapEntries.values().toArray(new SyndEntry[0])));
      SyndFeedOutput output = new SyndFeedOutput();
      inputStream = new ByteArrayInputStream(output.outputString(feed).getBytes());
      return inputStream;
    }

  }

  List<String> getForumSubscription(String userId) throws Exception {
    List<String> list = new ArrayList<String>();
    String subscriptionsPath = dataLocator.getUserSubscriptionLocation(userId);
    Node subscriptionNode = getForumServiceHome().getNode(subscriptionsPath);
    PropertyReader reader = new PropertyReader(subscriptionNode);
    list.addAll(reader.list("exo:categoryIds"));
    list.addAll(reader.list("exo:forumIds"));
    list.addAll(reader.list("exo:topicIds"));
    return list;
  }

}
