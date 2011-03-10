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

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.xwiki.rendering.internal.renderer.ParametersPrinter;
import org.xwiki.rendering.listener.reference.ResourceReference;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 2, 2010  
 */

/**
 * Generate a Confluence syntax string representation of an {@image ResourceReference}, using the format:
 * <code>(optional document name)^(attachment name)</code>.
 */
public class ConfluenceSyntaxImageRenderer {
  
  private ParametersPrinter parametersPrinter = new ParametersPrinter();
  
  public String renderImage(ResourceReference image) {
    return image.getReference().replace("@", "^");
  }
  
  public void beginRenderImage(ConfluenceSyntaxEscapeWikiPrinter printer) {
    printer.flush();
    printer.print("!");
  }
  
  public void renderImageContent(ConfluenceSyntaxEscapeWikiPrinter printer, String label) {
    if (!StringUtils.isEmpty(label)) {
      printer.print(label);
    }
  }
  
  public void endRenderImage(ConfluenceSyntaxEscapeWikiPrinter printer, Map<String, String> parameters) {

    // If there were parameters specified, output them separated by the "|" characters
    if (!parameters.isEmpty()) {
      printer.print("|");
      printer.print(this.parametersPrinter.print(parameters, '~'));
    }
    printer.print("!");
  }
}
