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

import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.wiki.mow.api.WikiNodeType;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 26, 2010  
 */
@PrimaryType(name=WikiNodeType.WIKI_PREFERENCES_SYNTAX)
public abstract class PreferencesSyntax {
  
  @Property(name=WikiNodeType.Definition.DEFAULT_SYNTAX)
  public abstract String getDefaultSyntax(); 
  
  public abstract String setDefaultSyntax(String defaulSyntax); 
  
  @Property(name=WikiNodeType.Definition.ALLOW_MULTIPLE_SYNTAXES)
  public abstract boolean getAllowMutipleSyntaxes(); 
  
  public abstract String setAllowMutipleSyntaxes(boolean allowMutipleSyntaxes); 
  
  @OneToOne  
  @MappedBy(WikiNodeType.Definition.PREFERENCES_SYNTAX)
  public abstract Preferences getPreferences();  
}
