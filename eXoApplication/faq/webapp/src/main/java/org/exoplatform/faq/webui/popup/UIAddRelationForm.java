/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : Mai Van Ha
 *          ha_mai_van@exoplatform.com
 * Apr 18, 2008 ,1:32:01 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UIAddRelationForms.gtmpl",
    events = {
      @EventConfig(listeners = UILanguageForm.SelectedLanguageActionListener.class),
      @EventConfig(listeners = UILanguageForm.SaveActionListener.class),
      @EventConfig(listeners = UILanguageForm.CancelActionListener.class)
    }
)

public class UIAddRelationForm {
  public class Cate{
    private Category category;
    private int deft ;
    public Category getCategory() {
      return category;
    }
    public void setCategory(Category category) {
      this.category = category;
    }
    public int getDeft() {
      return deft;
    }
    public void setDeft(int deft) {
      this.deft = deft;
    }
  }
  
  private static List<String> listCateSelected = new ArrayList<String>() ;
  private List<Cate> listCate = new ArrayList<Cate>() ;
  private static FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  private SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
  
  public UIAddRelationForm() throws Exception {
    setListCate() ;
    renderBox() ;
  }
  
  private void setListCate() throws Exception {
    List<Cate> listCate = new ArrayList<Cate>() ;
    Cate parentCate = null ;
    Cate childCate = null ;
    
    for(Category category : faqService.getSubCategories(null, sessionProvider)) {
      if(category != null) {
        Cate cate = new Cate() ;
        cate.setCategory(category) ;
        cate.setDeft(0) ;
        listCate.add(cate) ;
      }
    }
    
    while (!listCate.isEmpty()) {
      parentCate = new Cate() ;
      parentCate = listCate.get(listCate.size() - 1) ;
      listCate.remove(parentCate) ;
      this.listCate.add(parentCate) ;
      for(Category category : faqService.getSubCategories(parentCate.getCategory().getId(), sessionProvider)){
        if(category != null) {
          childCate = new Cate() ;
          childCate.setCategory(category) ;
          childCate.setDeft(parentCate.getDeft() + 1) ;
          listCate.add(childCate) ;
        }
      }
    }
    
    System.out.println("~~~~~~~~~~> fiish set listCate --> number of category: " + this.listCate.size());
    for(Cate cate : this.listCate) {
      System.out.println("\t" + cate.getCategory().getName() + "\t\t" + cate.getDeft());
    }
    System.out.println("~~~~~~~~~~> fiish view listCate --> number of category: " + this.listCate.size());
  }
  
  private void renderBox() {
    int n = this.listCate.size() ;
    System.out.println("this number of cate in listCate: " + n);
    Cate cate = null ;
    for(int i = 0; i < n; i ++) {
      cate = listCate.get(i) ;
      if(cate.getDeft() == 0) {
        if(i == 0) {
          System.out.println(cate.getCategory().getName());
        } else {
          for(int j = 0 ; j < listCate.get(i - 1).getDeft() - cate.getDeft(); j ++) {
            System.out.println("</div>");
          }
          System.out.println(cate.getCategory().getName());
        }
      } else {
        int sub = cate.getDeft() - listCate.get(i - 1).getDeft() ;
        if(sub == 0) {
          System.out.println(cate.getCategory().getName());
        } else if(sub > 0) {
          System.out.println("<div>" + cate.getCategory().getName()) ;
        } else {
          for(int j = 0 ; j < (-1*sub); j ++)
            System.out.println("</div>");
          System.out.println(cate.getCategory().getName());
        }
      }
    }
    for(int i = 0 ; i < listCate.get(n - 1).getDeft() ; i ++) 
      System.out.println("</div>");
  }
}
