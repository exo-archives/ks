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
package org.exoplatform.wiki.service.wysiwyg;

import javax.inject.Inject;

import org.exoplatform.wiki.service.WikiContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.ModelContext;
import org.xwiki.model.internal.reference.DefaultEntityReferenceValueProvider;
import org.xwiki.model.reference.EntityReference;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 16, 2010  
 */
@Component("current")
public class CurrentEntityReferenceValueProvider extends DefaultEntityReferenceValueProvider {

  @Inject
  private ModelContext modelContext;

  @Inject
  private Execution execution;

  @Override
  public String getDefaultValue(EntityType type)
  {
      String result = null;

      if (type == EntityType.WIKI) {
          EntityReference wikiReference = this.modelContext.getCurrentEntityReference();
          if (wikiReference != null) {
              wikiReference = wikiReference.extractReference(EntityType.WIKI);
          }
          if (wikiReference != null) {
              result = wikiReference.getName();
          }
      } else if (type == EntityType.SPACE || type == EntityType.DOCUMENT) {
          WikiContext context = getContext();
          if (context != null) {
          }
      }

      if (result == null) {
          result = super.getDefaultValue(type);
      }

      return result;
  }

  private WikiContext getContext()
  {
      WikiContext context = null;

      if (this.execution.getContext() != null) {
          context = (WikiContext) this.execution.getContext().getProperty(WikiContext.WIKICONTEXT);
      }

      return context;
  }

  
}
