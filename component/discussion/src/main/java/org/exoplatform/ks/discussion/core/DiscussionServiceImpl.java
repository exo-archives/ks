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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.discussion.core;

import org.chromattic.api.BuilderException;
import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticBuilder;
import org.chromattic.api.ChromatticSession;
import org.exoplatform.ks.discussion.api.Channel;
import org.exoplatform.ks.discussion.api.Discussion;
import org.exoplatform.ks.discussion.api.DiscussionException;
import org.exoplatform.ks.discussion.api.DiscussionService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class DiscussionServiceImpl implements DiscussionService {

  private static final Log  LOG = ExoLogger.getLogger(DiscussionServiceImpl.class);

  private Workspace         workspace;

  private ChromatticSession session;

  protected Chromattic        chromattic;

  protected ChromatticBuilder builder;
  
  public DiscussionServiceImpl() {
    builder = ChromatticBuilder.create();
  }

  Workspace getWorkspace() {
    Chromattic chromattic = getChromattic();
    session = chromattic.openSession();
    if (workspace == null) {
      workspace = session.findByPath(Workspace.class, "discussion:workspace");
      if (workspace == null) {
        workspace = session.insert(Workspace.class, "discussion:workspace");
      }
    }
    return workspace;
  }
  


  protected Chromattic getChromattic() {
    if (chromattic == null) {
      

      //
      builder.setOption(ChromatticBuilder.INSTRUMENTOR_CLASSNAME,
                        "org.chromattic.apt.InstrumentorImpl");
      builder.add(Workspace.class);
      builder.add(ChannelImpl.class);
      builder.add(DiscussionImpl.class);
      builder.add(MessageImpl.class);

      // unfortunately builder.build() does not use an unchecked exception, I'm
      // working around that here
      try {
        chromattic = builder.build();
      } catch (Exception e) {
        throw new BuilderException(e.getMessage());
      }

    }

    return chromattic;
  }

  public Discussion createDiscussion() {
    Channel target = getWorkspace().getDefaultChannel();
    return createDiscussion(target.getId());
  }

  public Discussion createDiscussion(String channelId) {
    ChannelImpl channel = session.findById(ChannelImpl.class, channelId);
    Discussion discussion = channel.createDiscussion();
    String name = generateDiscussionName(channel);
    session.persist(channel, discussion, name);
    return discussion;
  }

  private String generateDiscussionName(ChannelImpl channel) {
    String name = String.valueOf(System.currentTimeMillis());
    DiscussionImpl discussion = session.findByPath(channel, DiscussionImpl.class, name);
    byte max = 100;
    while (discussion != null) {
      LOG.warn("discussion " + name + " already exists in " + channel.getPath()
          + " attempting to generate a new one.");
      name = String.valueOf(System.currentTimeMillis());
      discussion = session.findByPath(channel, DiscussionImpl.class, name);

      if (--max == 0) {
        throw new DiscussionException("Failed to create a new name for discussion in "
            + channel.getPath() + "after 100 attempts.");
      }
    }
    return name;
  }

  public Discussion findDiscussion(String discussionId) {
    Discussion discussion = session.findById(Discussion.class, discussionId);
    return discussion;
  }

}
