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
package org.exoplatform.wiki.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.Preferences;
import org.exoplatform.wiki.service.WikiService;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 26, 2010  
 */
public class UIWikiSyntaxPreferences extends UIFormInputSet {
  public static final String FIELD_SYNTAX = "DefaultSyntax";

  public static final String FIELD_ALLOW  = "AllowChoseOthers";

  public UIWikiSyntaxPreferences(String id) throws Exception {
   setId(id);  
   WikiService wservice = (WikiService)PortalContainer.getComponent(WikiService.class) ;
   
   List<SelectItemOption<String>> syntaxTypes = new ArrayList<SelectItemOption<String>>();
   syntaxTypes.add(new SelectItemOption<String>(Syntax.XWIKI_1_0.toString(),Syntax.XWIKI_1_0.toIdString()));
   syntaxTypes.add(new SelectItemOption<String>(Syntax.XWIKI_2_0.toString(),Syntax.XWIKI_2_0.toIdString()));
   syntaxTypes.add(new SelectItemOption<String>(Syntax.CREOLE_1_0.toString(),Syntax.CREOLE_1_0.toIdString()));
   syntaxTypes.add(new SelectItemOption<String>(Syntax.CONFLUENCE_1_0.toString(),Syntax.CONFLUENCE_1_0.toIdString()));
   syntaxTypes.add(new SelectItemOption<String>(Syntax.MEDIAWIKI_1_0.toString(),Syntax.MEDIAWIKI_1_0.toIdString()));
   syntaxTypes.add(new SelectItemOption<String>(Syntax.JSPWIKI_1_0.toString(),Syntax.JSPWIKI_1_0.toIdString()));
   syntaxTypes.add(new SelectItemOption<String>(Syntax.TWIKI_1_0.toString(),Syntax.TWIKI_1_0.toIdString()));
   UIFormSelectBox syntaxTypeSelectBox = new UIFormSelectBox(FIELD_SYNTAX,FIELD_SYNTAX,syntaxTypes);   
   
    Preferences currentPreferences = Utils.getCurrentPreferences();
    String currentDefaultSyntaxt = currentPreferences.getPreferencesSyntax().getDefaultSyntax();
    if (currentDefaultSyntaxt == null) {
      currentDefaultSyntaxt = wservice.getDefaultWikiSyntaxId();
    }
    syntaxTypeSelectBox.setValue(currentDefaultSyntaxt);
    addUIFormInput(syntaxTypeSelectBox).setRendered(true);
    UIFormCheckBoxInput<Boolean> allowSelect = new UIFormCheckBoxInput<Boolean>(FIELD_ALLOW,
                                                                                FIELD_ALLOW,
                                                                                null);
    boolean currentAllow= currentPreferences.getPreferencesSyntax().getAllowMutipleSyntaxes();  
    allowSelect.setChecked(currentAllow);
    
    addUIFormInput(allowSelect);
 }
}
