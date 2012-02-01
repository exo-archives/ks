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
package org.exoplatform.wiki.rendering.render.confluence;

import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.ConsecutiveNewLineStateChainingListener;
import org.xwiki.rendering.listener.chaining.GroupStateChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.chaining.LookaheadChainingListener;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 2, 2010  
 */

/**
 * Provides convenient access to listeners in the chain used for
 * {@link org.exoplatform.wiki.rendering.render.confluence.ConfluenceSyntaxListenerChain}.
 */
public class ConfluenceSyntaxListenerChain extends ListenerChain {
  /**
   * @return the stateful {@link LookaheadChainingListener} for this rendering session.
   */
  public LookaheadChainingListener getLookaheadChainingListener() {
    return (LookaheadChainingListener) getListener(LookaheadChainingListener.class);
  }

  /**
   * @return the stateful {@link BlockStateChainingListener} for this rendering session.
   */
  public BlockStateChainingListener getBlockStateChainingListener() {
    return (BlockStateChainingListener) getListener(BlockStateChainingListener.class);
  }

  /**
   * @return the stateful {@link ConsecutiveNewLineStateChainingListener} for this rendering session.
   */
  public ConsecutiveNewLineStateChainingListener getConsecutiveNewLineStateChainingListener() {
    return (ConsecutiveNewLineStateChainingListener) getListener(ConsecutiveNewLineStateChainingListener.class);
  }

  /**
   * @return the stateful {@link ConsecutiveNewLineStateChainingListener} for this rendering session.
   */
  public ConsecutiveNewLineStateChainingListener getTextOnNewLineStateChainingListener() {
    return (ConsecutiveNewLineStateChainingListener) getListener(ConsecutiveNewLineStateChainingListener.class);
  }

  /**
   * @return the stateful {@link GroupStateChainingListener} for this rendering session.
   */
  public GroupStateChainingListener getGroupStateChainingListener() {
    return (GroupStateChainingListener) getListener(GroupStateChainingListener.class);
  }
}

