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
package org.exoplatform.ks.rendering.spi;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ks.rendering.api.Renderer;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * eXo kernel plugin to configure a renderer
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
@Managed
public class RendererPlugin extends BaseComponentPlugin {

  private Class<? extends Renderer> rendererType;
  
  @SuppressWarnings("unused")
  private static Log log = ExoLogger.getLogger(RendererPlugin.class);
  
  @SuppressWarnings("unchecked")
  public RendererPlugin(InitParams params) {
    String type;
    try {
      type = params.getValueParam("class").getValue();
    } catch (Exception e) {
      throw new RuntimeException("value-param class is required", e);
    }
    try {
      rendererType = (Class<? extends Renderer>) Class.forName(type);
    } catch (Exception e) {
      throw new RuntimeException(type + " is not a valid type", e);
    }
  }

  /**
   * Instantiates a new Renderer
   * @return
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  public Renderer createRenderer()  {
    try {
      //Constructor<? extends Renderer> constr = rendererType.getConstructor(); 
      //return constr.newInstance();
      return rendererType.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public Class<? extends Renderer> getRendererType() {
    return rendererType;
  }
  public void setRendererType(Class<? extends Renderer> rendererType) {
    this.rendererType = rendererType;
  }
  
  @Managed
  @ManagedName("Name")
  @ManagedDescription("The plugin name")
  public String getName() {
    return super.getName();
  }

  @Managed
  @ManagedName("Description")
  @ManagedDescription("The plugin description")
  public String getDescription() {
    return super.getDescription();
  }
  
  @Managed
  @ManagedName("ClassName")
  @ManagedDescription("The renderer class")
  public String getClassName() {
    return rendererType.getName();
  }
  
  @Managed
  @ManagedName("Syntax")
  @ManagedDescription("The syntax managed by this renderer")
  public String getSyntax() {
    return createRenderer().getSyntax();
  }
  
  
}
