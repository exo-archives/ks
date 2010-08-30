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
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectOption;
import org.exoplatform.webui.core.model.SelectOptionGroup;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.service.SearchData;
import org.exoplatform.wiki.service.SearchResult;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiAdvanceSearchForm.gtmpl",
  events = {
      @EventConfig(listeners = UIWikiAdvanceSearchForm.SearchActionListener.class)            
    }
)

public class UIWikiAdvanceSearchForm extends UIForm {
  final static String TEXT        = "text".intern();

  final static String WIKI_SPACES = "wikiSpaces".intern();
  
  public UIWikiAdvanceSearchForm() throws Exception {
    addChild(new UIFormStringInput(TEXT, TEXT, null));
    List<SelectOptionGroup> spaces = renderSpacesOptions();
    UIFormSelectBoxWithGroups selectSpaces = new UIFormSelectBoxWithGroups(WIKI_SPACES,
                                                                           WIKI_SPACES,
                                                                           new ArrayList<SelectItem>(spaces));
    
    selectSpaces.setValue(getDefaultSelectSpaceValue());
    addChild(selectSpaces);
    
    this.setActions(new String[] { "Search" });
  }

  public void resetWikiSpaces() throws Exception {
    getChild(UIFormSelectBoxWithGroups.class).setOptions(new ArrayList<SelectItem>(renderSpacesOptions()))
                                             .setValue(getDefaultSelectSpaceValue());
  }
  
  public List<SelectOptionGroup> renderSpacesOptions() throws Exception {
    List<SelectOptionGroup> listOptions = new ArrayList<SelectOptionGroup>();

    if (getAllSpacesOptions().getOptions().size() > 0)
      listOptions.add(getAllSpacesOptions());
    if (getPortalSpacesOptions().getOptions().size() > 0)
      listOptions.add(getPortalSpacesOptions());
    if (getGroupSpacesOptions().getOptions().size() > 0)
      listOptions.add(getGroupSpacesOptions());
    if (getUserSpacesOptions().getOptions().size() > 0)
      listOptions.add(getUserSpacesOptions());    
    String currentWikiOwner = org.exoplatform.wiki.commons.Utils.getCurrentWikiPageParams().getOwner();    
    return listOptions;
  }

  public SelectOptionGroup getAllSpacesOptions() {
    SelectOptionGroup allSpaceOptions = new SelectOptionGroup("");
    allSpaceOptions.addOption(new SelectOption("All Spaces", ""));
    allSpaceOptions.addOption(new SelectOption("All Portal Spaces", PortalConfig.PORTAL_TYPE));
    allSpaceOptions.addOption(new SelectOption("All Group Spaces", PortalConfig.GROUP_TYPE));
    allSpaceOptions.addOption(new SelectOption("All User Spaces", PortalConfig.USER_TYPE));
    return allSpaceOptions;
  }

  public SelectOptionGroup getPortalSpacesOptions() {
    SelectOptionGroup portalSpaceOptions = new SelectOptionGroup("Portal Spaces");
    Collection<Wiki> portalWikis = Utils.getWikisByType(WikiType.PORTAL);
    for (Wiki wiki : portalWikis) {
      portalSpaceOptions.addOption(new SelectOption(wiki.getOwner(), PortalConfig.PORTAL_TYPE + "/"
          + wiki.getOwner()));
    }
    return portalSpaceOptions;
  }

  public SelectOptionGroup getGroupSpacesOptions() {
    SelectOptionGroup groupSpaceOptions = new SelectOptionGroup("Group Spaces");
    Collection<Wiki> groupWikis = Utils.getWikisByType(WikiType.GROUP);
    for (Wiki wiki : groupWikis) {
      groupSpaceOptions.addOption(new SelectOption(wiki.getOwner(), PortalConfig.GROUP_TYPE + "/"
          + wiki.getOwner()));
    }
    return groupSpaceOptions;
  }

  public SelectOptionGroup getUserSpacesOptions() {
    SelectOptionGroup userSpaceOptions = new SelectOptionGroup("User Spaces");
    Collection<Wiki> userWikis = Utils.getWikisByType(WikiType.USER);
    for (Wiki wiki : userWikis) {
      userSpaceOptions.addOption(new SelectOption(wiki.getOwner(), PortalConfig.USER_TYPE + "/"
          + wiki.getOwner()));
    }
    return userSpaceOptions;
  }
  
  public void processSearchAction() throws Exception {
    WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
    String text = getUIStringInput(TEXT).getValue().trim();
    UIFormSelectBoxWithGroups spaces= getChild(UIFormSelectBoxWithGroups.class);   
    String path = getChild(UIFormSelectBoxWithGroups.class).getValue();
    String wikiType = null;
    String wikiOwner = null;
    if (!path.equals("")) {
      String[] arrayParams = path.split("/");
      if (arrayParams.length >= 1) {
        wikiType = arrayParams[0];
        if (arrayParams.length >= 2)
          wikiOwner = arrayParams[1];
      }
    }
    SearchData data = new SearchData(text, null, null, wikiType, wikiOwner);
    PageList<SearchResult> results = wservice.search(data);
    UIWikiAdvanceSearchResult uiSearchResults = getParent().findFirstComponentOfType(UIWikiAdvanceSearchResult.class);
    uiSearchResults.setKeyword(text);
    uiSearchResults.setResult(results);
  }
  
  private String getDefaultSelectSpaceValue() throws Exception
  {
    return org.exoplatform.wiki.commons.Utils.getCurrentWikiPageParams().getType()
    + "/" + org.exoplatform.wiki.commons.Utils.getCurrentWikiPageParams().getOwner();
  }
  
  static public class SearchActionListener extends EventListener<UIWikiAdvanceSearchForm> {
    public void execute(Event<UIWikiAdvanceSearchForm> event) throws Exception {
      UIWikiAdvanceSearchForm uiSearch = event.getSource() ;
      uiSearch.processSearchAction();
    }
  }  

}
