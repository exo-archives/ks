/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.ks.rendering;

import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class MarkupRenderingService {

    Map<String,Renderer> rendererRegistry;

    public MarkupRenderingService() {
      rendererRegistry = new HashMap<String, Renderer>();
    }
    
    
    public void registerRenderer(Renderer renderer) {
      rendererRegistry.put(renderer.getSyntax(), renderer);
    }
    
    public Renderer getRenderer(String syntax) throws UnsupportedSyntaxException {
      Renderer r = rendererRegistry.get(syntax);
      if (r == null) {
        throw new UnsupportedSyntaxException("No renderer has been registered for syntax " + syntax);
      } 
      return r;
    }

  
}
