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
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Mai Van Ha
 *          ha_mai_van@exoplatform.com
 * Apr 18, 2008 ,1:32:01 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UIAddRelationForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAddRelationForm.SelectedQuestionActionListener.class),
      @EventConfig(listeners = UIAddRelationForm.SaveActionListener.class),
      @EventConfig(listeners = UIAddRelationForm.CancelActionListener.class)
    }
)

public class UIAddRelationForm extends UIForm implements UIPopupComponent {
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
 
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
  
  @SuppressWarnings("unused")
  private static List<String> listCateSelected = new ArrayList<String>() ;
  private List<Cate> listCate = new ArrayList<Cate>() ;
  private static FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  private SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
  
  @SuppressWarnings("unused")
  private List<Cate> getListCate(){
    return this.listCate ;
  }
  
  public UIAddRelationForm() throws Exception {
    setActions(new String[]{"Save", "Cancel"}) ;
    setListCate() ;
    System.out.println(renderBox()) ;
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
  }
  
  private String renderBox() {
    List<String> listCateid = new ArrayList<String>() ;
    StringBuffer stringBuffer = new StringBuffer() ;
    int n = this.listCate.size() ;
    System.out.println("this number of cate in listCate: " + n);
    Cate cate = null ;
    for(int i = 0; i < n; i ++) {
      cate = listCate.get(i) ;
      listCateid.add(cate.getCategory().getId()) ;
      if(cate.getDeft() == 0) {
        if(i == 0) {
          System.out.println(cate.getCategory().getName());
        } else {
          if(listCate.get(i - 1).getDeft() - cate.getDeft() == 0) {
            System.out.println("<div>");
            for(Question question : getQuestions(listCateid.get(listCateid.size() - 2))) {
              System.out.println("\tQuestion: " + question.getQuestion());
            }
            listCateid.remove(listCateid.get(listCateid.size() - 2)) ;
            System.out.println("</div>");
          } else {
            for(int j = 0 ; j < listCate.get(i - 1).getDeft() - cate.getDeft(); j ++) {
              for(Question question : getQuestions(listCateid.get(listCateid.size() - 1))) {
                System.out.println("\tQuestion: " + question.getQuestion());
              }
              listCateid.remove(listCateid.get(listCateid.size() - 1)) ;
              System.out.println("</div>");
            }
          }
          System.out.println(cate.getCategory().getName());
        }
      } else {
        int sub = cate.getDeft() - listCate.get(i - 1).getDeft() ;
        if(sub == 0) {
          System.out.println("<div>");
          for(Question question : getQuestions(listCateid.get(listCateid.size() - 1))) {
            System.out.println("\tQuestion: " + question.getQuestion());
          }
          listCateid.remove(listCateid.get(listCateid.size() - 1)) ;
          System.out.println("</div>");
          System.out.println(cate.getCategory().getName());
        } else if(sub > 0) {
          System.out.println("<div>" + cate.getCategory().getName());
        } else {
          System.out.println("<div>");
          for(Question question : getQuestions(listCateid.get(listCateid.size() - 1))) {
            System.out.println("\tQuestion: " + question.getQuestion());
          }
          listCateid.remove(listCateid.get(listCateid.size() - 1)) ;
          System.out.println("</div>");
          for(int j = 0 ; j < (-1*sub); j ++) {
            for(Question question : getQuestions(listCateid.get(listCateid.size() - 1))) {
              System.out.println("\tQuestion: " + question.getQuestion());
            }
            listCateid.remove(listCateid.get(listCateid.size() - 1)) ;
            System.out.println("</div>");
          }
          System.out.println(cate.getCategory().getName());
        }
      }
    }
    for(int i = 0 ; i < listCate.get(n - 1).getDeft() ; i ++) {
      for(Question question : getQuestions(listCateid.get(listCateid.size() - 1))) {
        System.out.println("\tQuestion: " + question.getQuestion());
      }
      listCateid.remove(listCateid.get(listCateid.size() - 1)) ;
      System.out.println("</div>");
    }
    return stringBuffer.toString() ;
  }
  
  @SuppressWarnings("unused")
  private List<Question> getQuestions(String cateId) {
    try {
      return faqService.getQuestionsByCatetory(cateId, FAQUtils.getSystemProvider()).getAll() ;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null ;
    }
  }
  
  static public class SaveActionListener extends EventListener<UIAddRelationForm> {
    public void execute(Event<UIAddRelationForm> event) throws Exception {
      //UIAddRelationForm addRelationForm = event.getSource() ;
    }
  }
  static public class SelectedQuestionActionListener extends EventListener<UIAddRelationForm> {
    public void execute(Event<UIAddRelationForm> event) throws Exception {
      //UIAddRelationForm addRelationForm = event.getSource() ;
    }
  }
  static public class CancelActionListener extends EventListener<UIAddRelationForm> {
    public void execute(Event<UIAddRelationForm> event) throws Exception {
      UIAddRelationForm addRelationForm = event.getSource() ;     
      UIPopupContainer popupContainer = addRelationForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
