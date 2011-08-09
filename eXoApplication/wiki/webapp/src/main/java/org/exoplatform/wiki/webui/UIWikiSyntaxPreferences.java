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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.Preferences;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.core.UISyntaxSelectBoxFactory;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 26, 2010  
 */
public class UIWikiSyntaxPreferences extends UIFormInputSet {
  public static final String FIELD_SYNTAX = "DefaultSyntax";

  public static final String FIELD_ALLOW = "AllowChooseOthers";

  public UIWikiSyntaxPreferences(String id) throws Exception {
    setId(id);

    UIFormSelectBox selectSyntax = UISyntaxSelectBoxFactory.newInstance(FIELD_SYNTAX, FIELD_SYNTAX);
    this.addChild(selectSyntax);

    UIFormCheckBoxInput<Boolean> allowSelect = new UIFormCheckBoxInput<Boolean>(FIELD_ALLOW, FIELD_ALLOW, null);
    addUIFormInput(allowSelect);

    updateData();
  }

  public void updateData() throws Exception {
    WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
    Preferences currentPreferences = Utils.getCurrentPreferences();
    String currentDefaultSyntaxt = currentPreferences.getPreferencesSyntax().getDefaultSyntax();
    if (currentDefaultSyntaxt == null) {
      currentDefaultSyntaxt = wservice.getDefaultWikiSyntaxId();
    }

    UIFormSelectBox selectSyntax = getUIFormSelectBox(FIELD_SYNTAX);
    selectSyntax.setValue(currentDefaultSyntaxt);

    UIFormCheckBoxInput<Boolean> allowSelect = getUIFormCheckBoxInput(FIELD_ALLOW);
    boolean currentAllow = currentPreferences.getPreferencesSyntax().getAllowMutipleSyntaxes();
    allowSelect.setChecked(currentAllow);
  }
}
