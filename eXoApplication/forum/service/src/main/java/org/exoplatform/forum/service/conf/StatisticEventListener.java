/*
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
 */
package org.exoplatform.forum.service.conf;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class StatisticEventListener implements EventListener {
  private String workspace_;

  private String repository_;

  private String path_;

  public StatisticEventListener(String ws, String repo) throws Exception {
    workspace_ = ws;
    repository_ = repo;
  }

  public String getSrcWorkspace() {
    return workspace_;
  }

  public String getRepository() {
    return repository_;
  }

  public String getPath() {
    return path_;
  }

  public void setPath(String path) {
    path_ = path;
  }

  private Log log = ExoLogger.getLogger(StatisticEventListener.class);

  public void onEvent(EventIterator evIter) {
    try {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      ForumService forumService = (ForumService) container.getComponentInstanceOfType(ForumService.class);
      long topicCount = 0;
      long postCount = 0;
      while (evIter.hasNext()) {
        Event ev = evIter.nextEvent();
        if (ev.getType() == Event.NODE_ADDED) {
          String id = ev.getPath().substring(ev.getPath().lastIndexOf("/"));
          if (id.indexOf(Utils.POST) > 0) {
            postCount = postCount + 1;
          } else if (id.indexOf(Utils.TOPIC) > 0) {
            topicCount = topicCount + 1;
          }
        } else if (ev.getType() == Event.NODE_REMOVED) {
          String id = ev.getPath().substring(ev.getPath().lastIndexOf("/"));
          if (id.indexOf(Utils.POST) > 0) {
            postCount = postCount - 1;
          } else if (id.indexOf(Utils.TOPIC) > 0) {
            topicCount = topicCount - 1;
          }
        }
      }
      if (topicCount != 0 || postCount != 0) {
        forumService.updateStatisticCounts(topicCount, postCount);
      }
    } catch (Exception e) {
      log.error("\nThe StatisEvent could not listen: ", e);
    }
  }

}
