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

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.ConsecutiveNewLineStateChainingListener;
import org.xwiki.rendering.listener.chaining.GroupStateChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.chaining.LookaheadChainingListener;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;
import org.xwiki.rendering.renderer.reference.ResourceReferenceSerializer;
import org.xwiki.rendering.transformation.icon.IconTransformationConfiguration;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 2, 2010  
 */

/**
 * Generates Confluence Syntax from {@link org.xwiki.rendering.block.XDOM}.
 */
@Component("confluence/1.0")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class ConfluenceSyntaxRenderer extends AbstractChainingPrintRenderer implements Initializable {
  /**
   * Needed by XWikiSyntaxChainingRenderer to serialize wiki link references.
   */
  @Inject
  @Named("confluence/1.0/link")
  private ResourceReferenceSerializer linkReferenceSerializer;
  
  @Inject
  private IconTransformationConfiguration iconTransformationConfiguration;

  /**
   * {@inheritDoc}
   * 
   * @see Initializable#initialize()
   */
  public void initialize() throws InitializationException {
    ListenerChain chain = new ConfluenceSyntaxListenerChain();
    setListenerChain(chain);

    // Construct the listener chain in the right order. Listeners early in the chain are called before listeners
    // placed later in the chain. This chain allows using several listeners that make it easier
    // to write the Confluence Syntax chaining listener, for example for saving states (are we in a list, in a
    // paragraph, are we starting a new line, etc).
    chain.addListener(this);
    chain.addListener(new LookaheadChainingListener(chain, 2));
    chain.addListener(new GroupStateChainingListener(chain));
    chain.addListener(new BlockStateChainingListener(chain));
    chain.addListener(new ConsecutiveNewLineStateChainingListener(chain));
    chain.addListener(new ConfluenceSyntaxChainingRenderer(chain, this.linkReferenceSerializer, iconTransformationConfiguration));
  }
}

