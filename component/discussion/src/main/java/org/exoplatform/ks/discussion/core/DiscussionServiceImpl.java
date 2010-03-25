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

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.ks.discussion.api.Channel;
import org.exoplatform.ks.discussion.api.Discussion;
import org.exoplatform.ks.discussion.api.DiscussionService;
import org.exoplatform.ks.discussion.api.Message;
import org.exoplatform.ks.discussion.spi.DiscussionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class DiscussionServiceImpl implements DiscussionService {

  private static final Log                LOG = ExoLogger.getLogger(DiscussionServiceImpl.class);

  private Map<String, DiscussionProvider> providersByChannel;

  public DiscussionServiceImpl() {
    providersByChannel = new HashMap<String, DiscussionProvider>();
  }

  public void registerProvider(String channel, DiscussionProvider provider) {
    LOG.info("registering discussion provider " + provider.getClass() + " for channel " + channel);
    providersByChannel.put(channel, provider);
  }

  /**
   * {@inheritDoc}
   */
  public Discussion startDiscussion(Message startMessage) {
    return startDiscussion(Channel.DEFAULT_CHANNEL, startMessage);
  }

  /**
   * {@inheritDoc}
   */
  public Discussion startDiscussion(String channel, Message message) {
    if (message == null) {
      throw new IllegalArgumentException("An initial message is mandatory to start a discussion");
    }
    DiscussionProvider provider = getProvider(channel);
    return provider.startDiscussion(message);
  }

  /**
   * {@inheritDoc}
   */
  public Discussion findDiscussion(String channel, String discussionId) {
    DiscussionProvider provider = getProvider(channel);
    return provider.findDiscussion(discussionId);
  }

  /**
   * {@inheritDoc}
   */
  public Message findMessage(String channel, String messageId) {
    DiscussionProvider provider = getProvider(channel);
    return provider.findMessage(messageId);
  }

  /**
   * {@inheritDoc}
   */
  public Message reply(String channel, String messageId, Message reply) {
    DiscussionProvider provider = getProvider(channel);
    return provider.reply(messageId, reply);
  }

  /**
   * Get the discussion provider for a channel. If no provider is assi
   * 
   * @param channel
   * @return
   */
  private DiscussionProvider getProvider(String channel) {
    DiscussionProvider provider = this.providersByChannel.get(channel);
    if (provider == null) {
      LOG.info("Could not find a discussion provider for channel " + channel
          + ". using the default provider");
      provider = new DefaultDiscussionProvider();
      this.providersByChannel.put(channel, provider);
    }
    return provider;
  }

}
