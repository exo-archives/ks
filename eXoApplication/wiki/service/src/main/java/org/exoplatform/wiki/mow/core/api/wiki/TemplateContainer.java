/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.mow.core.api.wiki;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.exoplatform.wiki.mow.api.WikiNodeType;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 26 Jan 2011  
 */
@PrimaryType(name = WikiNodeType.WIKI_TEMPLATE_CONTAINER)
public abstract class TemplateContainer {
  

  @Create
  public abstract Template createTemplatePage();
  
  @Path
  public abstract String getPath();

  @OneToMany
  public abstract Map<String,Template> getTemplates();
  public abstract void setTemplates(Map<String,Template> templates);
  
  public Template addPage(String templateName, Template template) throws Exception {
    if (templateName == null) {
      throw new NullPointerException();
    }
    if (template == null) {
      throw new NullPointerException();
    }
    Map<String, Template> children = getTemplates();
    if (children.containsKey(templateName)) {
      return template;
    }
    children.put(templateName, template);
    template.setNonePermission();
    return template;
  }
  
  public Template getTemplate(String name) {
    Iterator<Entry<String, Template>> iter = getTemplates().entrySet().iterator();
    while (iter.hasNext()) {
      Entry<String, Template> entry = iter.next();
      Template template = entry.getValue();
      if (template.getName().equals(name)) {
        return template;
      }
    }
    return null;
  };  

}
