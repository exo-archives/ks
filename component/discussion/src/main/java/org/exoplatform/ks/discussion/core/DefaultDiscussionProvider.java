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

import java.util.Date;

import org.chromattic.api.BuilderException;
import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticBuilder;
import org.chromattic.api.ChromatticSession;
import org.chromattic.apt.InstrumentorImpl;
import org.exoplatform.ks.discussion.api.Channel;
import org.exoplatform.ks.discussion.api.Discussion;
import org.exoplatform.ks.discussion.api.DiscussionException;
import org.exoplatform.ks.discussion.api.Message;
import org.exoplatform.ks.discussion.api.ObjectNotFoundException;
import org.exoplatform.ks.discussion.spi.DiscussionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class DefaultDiscussionProvider implements DiscussionProvider {

  private static final Log    LOG = ExoLogger.getLogger(DefaultDiscussionProvider.class);

  private Workspace           workspace;

  private ChromatticSession   session;

  protected Chromattic        chromattic;

  protected ChromatticBuilder builder;

  public DefaultDiscussionProvider() {
    builder = ChromatticBuilder.create();
  }
  
  public ChromatticBuilder getChromatticBuilder() {
    return builder;
  }

  public String getServedChannel() {
    return Channel.DEFAULT_CHANNEL;
  }

  /**
   * {@inheritDoc}
   */
  public Discussion startDiscussion(Message message) {
    String channelId = getServedChannel();
    // find Channel
    ChannelImpl channel = findChannelById(channelId);

    // create the discussion in the channel
    String discussionName = generateChildName(channel);
    DiscussionImpl discussion = session.insert(channel, DiscussionImpl.class, discussionName);

    // create the start message
    MessageImpl startMessage = (MessageImpl) discussion.getStartMessage();
    startMessage.copy(message);
    session.save();
    return discussion;
  }

  /**
   * {@inheritDoc}
   */
  public Discussion findDiscussion(String discussionId) {
    Discussion discussion = session.findById(Discussion.class, discussionId);
    return discussion;
  }

  /**
   * {@inheritDoc}
   */
  public Message findMessage(String messageId) {
    Message message = session.findById(Message.class, messageId);
    return message;
  }

  /**
   * {@inheritDoc}
   */
  public Message reply(String messageId, Message reply) {

    MessageImpl parentMessage = findMessageById(messageId);

    String name = generateChildName(parentMessage);
    MessageImpl addedReply = session.insert(parentMessage, MessageImpl.class, name);
    addedReply.copy(reply);

    if (reply.getTimestamp() == null)
      addedReply.setTimestamp(new Date());
    if (reply.getTitle() == null)
      addedReply.setTitle(parentMessage.getTitle());
    if (reply.getBody() == null || reply.getBody().length() <= 0) {
      throw new IllegalArgumentException("a message cannot have an empty body");
    }
    parentMessage.getReplies().add(addedReply);
    session.save();
    return addedReply;
  }

  /**
   * Generate a valid child node name. The name is based on the current
   * timestamp. For the extremely rare cases where the name would already exist,
   * 100 consecutive attempts are made to find a name in the same way.
   * 
   * @param <T> must be a Chromattic managed type
   * @param parent parent node where a child name should be generated
   * @return name of the child node
   * @throws DiscussionException when the name already exists after 100 attempts
   * @see System#currentTimeMillis()
   */
  private <T> String generateChildName(T parent) {
    String name = String.valueOf(System.currentTimeMillis());
    Object discussion = session.findByPath(parent, parent.getClass(), name);
    String path = session.getPath(parent);
    byte max = 100;
    while (discussion != null) {
      LOG.warn("Child node name '" + name + "' already exists in " + path
          + ". Trying to generate a new one.");
      name = String.valueOf(System.currentTimeMillis());
      discussion = session.findByPath(parent, parent.getClass(), name);

      if (--max == 0) {
        throw new DiscussionException("Failed to generate an available child node name in " + path
            + "after 100 attempts.");
      }
    }
    return name;
  }

  protected Chromattic getChromattic() {
    if (chromattic == null) {
      builder.setOption(ChromatticBuilder.INSTRUMENTOR_CLASSNAME, InstrumentorImpl.class.getName());
      builder.add(Workspace.class);
      builder.add(ChannelImpl.class);
      builder.add(DiscussionImpl.class);
      builder.add(MessageImpl.class);

      // unfortunately builder.build() does not use an unchecked exception, I'm
      // working around that here
      // TODO : Should be fixed in next chromattic version
      try {
        chromattic = builder.build();
      } catch (Exception e) {
        throw new BuilderException(e.getMessage());
      }
    }

    return chromattic;
  }

  /**
   * load a message by id
   * 
   * @param messageId
   * @return
   * @throws ObjectNotFoundException if te message was not found
   */
  private MessageImpl findMessageById(String messageId) {
    MessageImpl message = session.findById(MessageImpl.class, messageId);
    if (message == null) {
      throw new ObjectNotFoundException(messageId);
    }
    return message;
  }

  /**
   * Get the discussion workspace. That is : the parent node of all channels.
   * 
   * @return
   */
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

  private ChannelImpl findChannelById(String channelId) {
    ChannelImpl channel = session.findById(ChannelImpl.class, channelId);
    if (channel == null) {
      throw new ObjectNotFoundException(channelId);
    }
    return channel;
  }

}
