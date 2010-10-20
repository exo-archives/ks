/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.wiki.webui.tree ;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputInfo;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 6, 2010  
 */
@ComponentConfig(
                 lifecycle = UIApplicationLifecycle.class, 
                 template = "app:/templates/wiki/webui/tree/UITreeExplorer.gtmpl",
                 events = {
                     @EventConfig(listeners = UITreeExplorer.SelectNodeActionListener.class)
                     }
)
public class UITreeExplorer extends UIContainer {
  
  final static public String SELECT_NODE = "SelectNode";

  private String             currentPagePath;

  private String             restURL;

  private String             fillInput;
  
  public UITreeExplorer() throws Exception {
  }

  public String getCurrentPagePath() {
    return currentPagePath;
  }

  public void setCurrentPagePath(String currentPagePath) {
    this.currentPagePath = currentPagePath;
  }

  public String getRestURL() {
    return restURL;
  }

  public void setRestURL(String restURL) {
    this.restURL = restURL;
  }
  

  public String getFillInput() {
    return fillInput;
  }

  public void setFillInput(String fillInput) {
    this.fillInput = fillInput;
  }

  static public class SelectNodeActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {
      UITreeExplorer tree = event.getSource();
      String fillInput = tree.getFillInput();
      if (fillInput != null) {
        UIFormInputInfo input = tree.getParent().findComponentById(fillInput);
        String value = event.getRequestContext().getRequestParameter("param");
        input.setValue(value);
        event.getRequestContext().addUIComponentToUpdateByAjax(input);
      }
    }
  }
  
}
