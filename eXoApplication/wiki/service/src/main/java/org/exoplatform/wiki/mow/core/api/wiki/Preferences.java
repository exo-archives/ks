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
package org.exoplatform.wiki.mow.core.api.wiki;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.exoplatform.wiki.mow.api.WikiNodeType;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 26, 2010  
 */
@PrimaryType(name=WikiNodeType.WIKI_PREFERENCES)
public abstract class Preferences {

  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.PREFERENCES_SYNTAX)
  protected abstract PreferencesSyntax getPreferencesSyntaxByChromattic();
  protected abstract void setPreferencesSyntaxByChromattic(PreferencesSyntax preferencesSyntax);
  
  @Path
  public abstract String getPath();
  
  @Create
  protected abstract PreferencesSyntax createPreferencesSyntax();
  
  public PreferencesSyntax getPreferencesSyntax()
  {
    PreferencesSyntax preferencesSyntax = getPreferencesSyntaxByChromattic();
    if (preferencesSyntax == null) {
      preferencesSyntax = createPreferencesSyntax();     
      setPreferencesSyntaxByChromattic(preferencesSyntax);
      preferencesSyntax.setAllowMutipleSyntaxes(false);
    }
    return preferencesSyntax;
  }
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.TEMPLATE_CONTAINER)
  protected abstract TemplateContainer getTemplateContainerByChromattic();

  protected abstract void setTemplateContainerByChromattic(TemplateContainer templContainer);

  @Create
  protected abstract TemplateContainer createTemplateContainer();

  public TemplateContainer getTemplateContainer() {
    TemplateContainer templatecontainer = getTemplateContainerByChromattic();
    if (templatecontainer == null) {
      templatecontainer = createTemplateContainer();
      setTemplateContainerByChromattic(templatecontainer);
    }
    return templatecontainer;
  }
  
}
