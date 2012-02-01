/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.rendering.internal.parser;

import java.util.Map;

import org.xwiki.rendering.internal.parser.wikimodel.DefaultXWikiGeneratorListener;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.IdGenerator;

/**
 * Created by The eXo Platform SAS
 * Author : haidd 
 *          haidd@exoplatform.com
 * Jan 14, 2012  
 */
public class DefaultXWikiConfluenceGeneratorListener extends DefaultXWikiGeneratorListener {
  
  public DefaultXWikiConfluenceGeneratorListener(StreamParser parser,
                                                 Listener listener,
                                                 ResourceReferenceParser linkReferenceParser,
                                                 ResourceReferenceParser imageReferenceParser,
                                                 PrintRendererFactory plainRendererFactory,
                                                 IdGenerator idGenerator,
                                                 Syntax syntax) {
    super(parser, listener, linkReferenceParser, imageReferenceParser, plainRendererFactory, idGenerator, syntax);
  }

  @Override
  public void onEscape(String str) {
    Map<String, String> hasTable = new java.util.Hashtable<String, String>();
    getListener().onVerbatim(str, true, hasTable);
  }
}
