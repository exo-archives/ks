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
package org.exoplatform.wiki.webui.control.filter;

import java.util.Map;

import org.exoplatform.webui.ext.filter.UIExtensionAbstractFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilterType;
import org.exoplatform.wiki.webui.UIWikiPageArea;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiRichTextArea;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 22, 2010  
 */
public class IsMarkupModeFilter extends UIExtensionAbstractFilter {

  public IsMarkupModeFilter(){
    this(null);
  }
  
  public IsMarkupModeFilter(String messageKey){
    super(messageKey, UIExtensionFilterType.MANDATORY);
  }
  
  @Override
  public boolean accept(Map<String, Object> context) throws Exception {
    UIWikiPortlet wikiPortlet = (UIWikiPortlet) context.get(UIWikiPortlet.class.getName());
    UIWikiPageArea wikiPageArea = wikiPortlet.getChild(UIWikiPageArea.class);
    UIWikiPageEditForm wikiPageEditForm = wikiPageArea.getChild(UIWikiPageEditForm.class);
    UIWikiRichTextArea wikiRichTextArea = wikiPageEditForm.getChild(UIWikiRichTextArea.class);
    return !wikiRichTextArea.isRendered();
  }

  @Override
  public void onDeny(Map<String, Object> context) throws Exception {
    // TODO Auto-generated method stub
    
  }

}
