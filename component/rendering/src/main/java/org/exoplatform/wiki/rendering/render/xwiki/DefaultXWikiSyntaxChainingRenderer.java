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

import java.util.Map;

import org.xwiki.rendering.internal.renderer.xwiki20.reference.XWikiSyntaxResourceRenderer;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.renderer.reference.ResourceReferenceSerializer;
import org.xwiki.rendering.transformation.icon.IconTransformationConfiguration;

/**
 * Created by The eXo Platform SAS
 * Author : phongth
 *          phongth@exoplatform.com
 * March 16, 2012  
 */
public class DefaultXWikiSyntaxChainingRenderer extends org.xwiki.rendering.internal.renderer.xwiki20.XWikiSyntaxChainingRenderer {

  private XWikiSyntaxResourceRenderer imageResourceRenderer;
  
  private XWikiSyntaxIconRenderer  iconRenderer;

  public DefaultXWikiSyntaxChainingRenderer(ListenerChain listenerChain, ResourceReferenceSerializer linkReferenceSerializer, 
      ResourceReferenceSerializer imageReferenceSerializer, IconTransformationConfiguration iconTransformationConfiguration) {
    
    super(listenerChain, linkReferenceSerializer, imageReferenceSerializer);
    
    this.iconRenderer = new XWikiSyntaxIconRenderer(iconTransformationConfiguration);
    this.imageResourceRenderer = createXWikiSyntaxImageRenderer(getListenerChain(), imageReferenceSerializer);
  }

  private XWikiSyntaxResourceRenderer getImageRenderer() {
    return this.imageResourceRenderer;
  }
  
  private XWikiSyntaxIconRenderer getIconRenderer() {
    return this.iconRenderer;
  }
  
  @Override
  public void onImage(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters) {
    if (ResourceType.ICON.equals(reference.getType())) {
      getIconRenderer().renderIcon(getXWikiPrinter(), reference);
    } else{
      getImageRenderer().beginRenderLink(getXWikiPrinter(), reference, isFreeStandingURI, parameters);
      getImageRenderer().endRenderLink(getXWikiPrinter(), reference, isFreeStandingURI, parameters);
    }
  }
}
