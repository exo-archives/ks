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
package org.exoplatform.wiki.webui.core;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Oct 1, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIAdvancePageIterator.gtmpl",
  events = {
    @EventConfig(listeners = UIAdvancePageIterator.GoPageActionListener.class),
    @EventConfig(listeners = UIAdvancePageIterator.GoNumberPageActionListener.class)
  }
)
public class UIAdvancePageIterator extends UIForm {

  public static String PREVIOUS = "previous".intern();
  
  public static String NEXT = "next".intern();
  
  public static String FIRST = "first".intern();
  
  public static String LAST = "last".intern();
  
  public static String GOPAGE = "GoPage".intern();
  
  public static String GOTOPPAGE = "goPageTop".intern();
  
  private PageList pageList;

  private int selectedPage = 1;

  private int beginPageRange = 0;
  
  private int endPageRange = 0;

  public UIAdvancePageIterator(){
    addUIFormInput( new UIFormStringInput(GOTOPPAGE, null)) ;
    this.setSubmitAction("return false;");
  }
  
  public void setPageList(PageList pageList) {
    this.pageList = pageList;
  }
  
  public PageList getPageList() {
    return pageList;
  }
  
  @SuppressWarnings("unused")
  public int getSelectedPage() {
    return this.selectedPage;
  }
  
  public void setSelectedPage(long selectedPage) {
    this.selectedPage = (int) selectedPage;
  }

  @SuppressWarnings("unused")
  public List<String> getDisplayedRange() throws Exception {
    int max_Page = (int) pageList.getAvailablePage();
    if (this.selectedPage > max_Page) {
      this.selectedPage = max_Page;
    }
    long page = this.selectedPage;
    if (page <= 3) {
      beginPageRange = 1;
      if (max_Page <= 7) {
        endPageRange = max_Page;
      } else {
        endPageRange = 7;
      }
    } else {
      if (max_Page > (page + 3)) {
        endPageRange = (int) (page + 3);
        beginPageRange = (int) (page - 3);
      } else {
        endPageRange = max_Page;
        if (max_Page > 7) {
          beginPageRange = max_Page - 6;
        } else {
          beginPageRange = 1;
        }
      }
    }
    List<String> temp = new ArrayList<String>();
    for (int i = beginPageRange; i <= endPageRange; i++) {
      temp.add(String.valueOf(i));
    }
    return temp;
  }

  static public class GoPageActionListener extends EventListener<UIAdvancePageIterator> {
    @Override
    public void execute(Event<UIAdvancePageIterator> event) throws Exception {
      UIAdvancePageIterator pageIterator = event.getSource();
      String changeToPage = event.getRequestContext().getRequestParameter(OBJECTID).trim();
      int maxPage = pageIterator.pageList.getAvailablePage();
      int presentPage = pageIterator.selectedPage;
      if (UIAdvancePageIterator.NEXT.equalsIgnoreCase(changeToPage)) {
        if (presentPage < maxPage) {
          pageIterator.selectedPage = presentPage + 1;
          //event.getRequestContext().addUIComponentToUpdateByAjax(pageIterator.getParent());
        }
      } else if (UIAdvancePageIterator.PREVIOUS.equalsIgnoreCase(changeToPage)) {
        if (presentPage > 1) {
          pageIterator.selectedPage = presentPage - 1;
          //event.getRequestContext().addUIComponentToUpdateByAjax(pageIterator.getParent());
        }
      } else if (UIAdvancePageIterator.LAST.equalsIgnoreCase(changeToPage)) {
        if (presentPage != maxPage) {
          pageIterator.selectedPage = maxPage;
          //event.getRequestContext().addUIComponentToUpdateByAjax(pageIterator.getParent());
        }
      } else if (UIAdvancePageIterator.FIRST.equalsIgnoreCase(changeToPage)) {
        if (presentPage != 1) {
          pageIterator.selectedPage = 1;
          //event.getRequestContext().addUIComponentToUpdateByAjax(pageIterator.getParent());
        }
      } else {
        int temp = Integer.parseInt(changeToPage);
        if (temp > 0 && temp <= maxPage && temp != presentPage) {
          pageIterator.selectedPage = temp;
          //event.getRequestContext().addUIComponentToUpdateByAjax(pageIterator.getParent());
        }
      }
    }
  }
  
  static public class GoNumberPageActionListener extends EventListener<UIAdvancePageIterator> {
    @Override
    public void execute(Event<UIAdvancePageIterator> event) throws Exception {
      UIAdvancePageIterator pageIterator = event.getSource();
      UIApplication uiApp = pageIterator.getAncestorOfType(UIApplication.class) ;
      UIFormStringInput stringInput = pageIterator.getUIStringInput(UIAdvancePageIterator.GOTOPPAGE) ;
      String numberPage = "" ;
      numberPage = stringInput.getValue() ;
      stringInput.setValue("") ;
      int maxPage = pageIterator.pageList.getAvailablePage();
      int presentPage = pageIterator.selectedPage;
      int page = 0;
      if (numberPage != null && numberPage.length() > 0) {
        try {
          page = Integer.parseInt(numberPage.trim());
          if (page < 0) {
            uiApp.addMessage(new ApplicationMessage("NameValidator.msg.Invalid-number",
                                                    new String[] { pageIterator.getLabel(pageIterator.GOPAGE) },
                                                    ApplicationMessage.WARNING));
            ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          } else {
            if (page == 0) {
              page = 1;
            } else if (page > pageIterator.pageList.getAvailablePage()) {
              page = pageIterator.pageList.getAvailablePage();
            }
            pageIterator.selectedPage = page;
          }
        } catch (NumberFormatException e) {
          uiApp.addMessage(new ApplicationMessage("NameValidator.msg.Invalid-number",
                                                  new String[] { pageIterator.getLabel(pageIterator.GOPAGE) },
                                                  ApplicationMessage.WARNING));
          ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        }
      }
      if (page > 0 && page <= maxPage && page != presentPage) {
        pageIterator.selectedPage = page;
        // event.getRequestContext().addUIComponentToUpdateByAjax(pageIterator.getParent());
      }
    }
  }

}
