/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.render.xwiki;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.internal.renderer.xwiki20.AbstractXWikiSyntaxRenderer;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.reference.ResourceReferenceSerializer;
import org.xwiki.rendering.transformation.icon.IconTransformationConfiguration;

/**
 * Created by The eXo Platform SAS
 * Author : phongth
 *          phongth@exoplatform.com
 * March 16, 2012  
 */
@Component
@Named("xwiki/2.0")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultXWikiSyntaxRenderer extends AbstractXWikiSyntaxRenderer {
  /**
   * Needed by XWikiSyntaxChainingRenderer to serialize wiki link references.
   */
  @Inject
  @Named("xwiki/2.0/link")
  private ResourceReferenceSerializer linkReferenceSerializer;

  /**
   * Needed by XWikiSyntaxChainingRenderer to serialize wiki image references.
   */
  @Inject
  @Named("xwiki/2.0/image")
  private ResourceReferenceSerializer imageReferenceSerializer;
  
  @Inject
  private IconTransformationConfiguration iconTransformationConfiguration;

  /**
   * {@inheritDoc}
   * 
   * @see AbstractXWikiSyntaxRenderer#createXWikiSyntaxChainingRenderer(ListenerChain)
   */
  @Override
  protected ChainingListener createXWikiSyntaxChainingRenderer(ListenerChain chain) {
    return new DefaultXWikiSyntaxChainingRenderer(chain, this.linkReferenceSerializer, this.imageReferenceSerializer, iconTransformationConfiguration);
  }
}
