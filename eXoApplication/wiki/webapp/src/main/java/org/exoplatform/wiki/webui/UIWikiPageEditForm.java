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
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.Preferences;
import org.exoplatform.wiki.mow.core.api.wiki.WikiImpl;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.control.UIPageToolBar;
import org.exoplatform.wiki.webui.core.UIWikiForm;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageEditForm.gtmpl",
  events = {
      @EventConfig(listeners = UIWikiPageEditForm.SelectSyntaxActionListener.class)     
  }
  
)
public class UIWikiPageEditForm extends UIWikiForm {

  public static final String UNTITLED      = "Untitled";
  public static final String FIELD_CONTENT = "Markup";
  public static final String FIELD_SYNTAX  = "SyntaxType";
  public static final String TITLE_CONTROL = "UIWikiPageTitleControlForm_PageEditForm";
  public static final String PAGE_TOOLBAR  = "UIWikiPageEditForm_PageToolBar";
  public static final String HELP_PANEL    = "UIWikiSidePanelArea";
  public static final String RICHTEXT_AREA = "UIWikiRichTextArea";
  
  private String  title ;
  
  public UIWikiPageEditForm() throws Exception{
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.EDIT, WikiMode.NEW });
    
    WikiService wservice = (WikiService)PortalContainer.getComponent(WikiService.class) ;
    
    addChild(UIWikiPageTitleControlArea.class, null, TITLE_CONTROL).toInputMode();
    addChild(UIPageToolBar.class, null, PAGE_TOOLBAR);
    addChild(UIWikiSidePanelArea.class, null, HELP_PANEL);
    addChild(UIWikiRichTextArea.class, null, RICHTEXT_AREA).setRendered(false);
    UIFormTextAreaInput markupInput = new UIFormTextAreaInput(FIELD_CONTENT, FIELD_CONTENT, "This is **sample content**");
    addUIFormInput(markupInput).setRendered(true);
    List<SelectItemOption<String>> syntaxTypes = new ArrayList<SelectItemOption<String>>();
    syntaxTypes.add(new SelectItemOption<String>(Syntax.XWIKI_1_0.toString(),Syntax.XWIKI_1_0.toIdString()));
    syntaxTypes.add(new SelectItemOption<String>(Syntax.XWIKI_2_0.toString(),Syntax.XWIKI_2_0.toIdString()));
    syntaxTypes.add(new SelectItemOption<String>(Syntax.CREOLE_1_0.toString(),Syntax.CREOLE_1_0.toIdString()));
    syntaxTypes.add(new SelectItemOption<String>(Syntax.CONFLUENCE_1_0.toString(),Syntax.CONFLUENCE_1_0.toIdString()));
    syntaxTypes.add(new SelectItemOption<String>(Syntax.MEDIAWIKI_1_0.toString(),Syntax.MEDIAWIKI_1_0.toIdString()));
    syntaxTypes.add(new SelectItemOption<String>(Syntax.JSPWIKI_1_0.toString(),Syntax.JSPWIKI_1_0.toIdString()));
    syntaxTypes.add(new SelectItemOption<String>(Syntax.TWIKI_1_0.toString(),Syntax.TWIKI_1_0.toIdString()));
    UIFormSelectBox syntaxTypeSelectBox = new UIFormSelectBox(FIELD_SYNTAX,FIELD_SYNTAX,syntaxTypes);
    syntaxTypeSelectBox.setOnChange("SelectSyntax");
    
    Preferences currentPreferences= ((WikiImpl)Utils.getCurrentWiki()).getPreferences();
    String currentDefaultSyntaxt = currentPreferences.getPreferencesSyntax().getDefaultSyntax();
    if (currentDefaultSyntaxt == null) {
      currentDefaultSyntaxt = wservice.getDefaultWikiSyntaxId();
    }
    syntaxTypeSelectBox.setValue(currentDefaultSyntaxt);
    boolean allowSelect= currentPreferences.getPreferencesSyntax().getAllowMutipleSyntaxes();
    syntaxTypeSelectBox.setEnable(allowSelect);
    addUIFormInput(syntaxTypeSelectBox);
  }   
  
  public void setTitle(String title){ this.title = title ;}
  public String getTitle(){ return title ;}
  
  public boolean isSidePanelRendered(){
    return getChild(UIWikiSidePanelArea.class).isRendered();
  }
  public static class SelectSyntaxActionListener extends EventListener<UIWikiPageEditForm> {
    @Override
    public void execute(Event<UIWikiPageEditForm> event) throws Exception {
      UIWikiPageEditForm pageEditForm = event.getSource();
      UIWikiSidePanelArea sidePanelForm = pageEditForm.getChild(UIWikiSidePanelArea.class);
      UIFormSelectBox syntaxTypeSelectBox = pageEditForm.getChild(UIFormSelectBox.class);
      sidePanelForm.renderHelpContent(syntaxTypeSelectBox.getValue());
    }
  }
}
