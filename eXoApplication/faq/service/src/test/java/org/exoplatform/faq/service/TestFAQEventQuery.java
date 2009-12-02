/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.faq.service;



import java.util.Arrays;

import org.exoplatform.ks.test.Closure;

import junit.framework.TestCase;
import static org.exoplatform.ks.test.AssertUtils.*;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestFAQEventQuery extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }
  
  public void testQuickSearch() throws Exception {
    FAQEventQuery queryObject = new FAQEventQuery();
    
    /*       
        eventQuery.setAdmin(uiQuickSearch.faqSetting_.isAdmin()) ;
        eventQuery.setUserMembers(UserHelper.getAllGroupAndMembershipOfUser(FAQUtils.getCurrentUser()));
        eventQuery.setUserId(FAQUtils.getCurrentUser()) ;
        eventQuery.setText(text);
        eventQuery.setType("categoryAndQuestion");
     */

    queryObject.setPath("/foo");
    queryObject.setType(FAQEventQuery.CATEGORY_AND_QUESTION);
    queryObject.setAdmin(true);
    String selector = "/jcr:root/foo//*";

    assertEquals(selector + "[]", queryObject.getQuery());
    queryObject.setText("bar");
    assertEquals(selector + "[ jcr:contains(., 'bar')]", queryObject.getQuery());

    queryObject.setAdmin(false);
    String predicate = "jcr:contains(., 'bar') and ( not(@exo:isApproved) or @exo:isApproved='true' )";
    assertEquals(selector + "[ "+ predicate+" ]", queryObject.getQuery());
    
    queryObject.setUserId("zed");
    predicate = "jcr:contains(., 'bar') and ( not(@exo:isApproved) or @exo:isApproved='true' or exo:author='zed' )";
    assertEquals(selector + "[ " + predicate + " ]", queryObject.getQuery()); 
    
    queryObject.setViewingCategories(Arrays.asList("cat1"));
    
    predicate += "  and (@exo:categoryId='cat1' or @exo:id='cat1' and ( @exo:userPrivate='' ) )";
    assertEquals(selector + "[ " + predicate + "]", queryObject.getQuery());
    

    
  }
  
  public void testBuildCategoryQuery() throws Exception {
    final FAQEventQuery queryObject = new FAQEventQuery() ;
    queryObject.setPath("/foo");
    queryObject.setType(FAQEventQuery.FAQ_CATEGORY);
    queryObject.setAdmin(true);
    String selector = "/jcr:root/foo//element(*,exo:faqCategory)[(@exo:isView='true') ";
    assertEquals(selector + "]", queryObject.getQuery());
    
    queryObject.setText("bar");
    String predicate = " and (jcr:contains(., 'bar'))";
    assertEquals(selector + predicate + "]", queryObject.getQuery());
    
    queryObject.setName("zed");
    predicate += " and (jcr:contains(@exo:name, 'zed'))";
    assertEquals(selector + predicate + "]", queryObject.getQuery());

    queryObject.setIsModeQuestion("blah");
    predicate += " and (@exo:isModerateQuestions='blah')";
    assertEquals(selector + predicate + "]", queryObject.getQuery());
  
    queryObject.setModerator("john");
    predicate += " and (jcr:contains(@exo:moderators, 'john'))";
    assertEquals(selector + predicate + "]", queryObject.getQuery());    
    
    queryObject.setAdmin(false);
    queryObject.setUserMembers(Arrays.asList("jack", "jerry"));
    predicate += " and (not(@exo:userPrivate)  or @exo:userPrivate='jack' or @exo:moderators='jack' or @exo:userPrivate='jerry' or @exo:moderators='jerry')";
    assertEquals(selector + predicate + "]", queryObject.getQuery());    
  }
  
  public void testBuildQuestionQuery() throws Exception {
    final FAQEventQuery queryObject = new FAQEventQuery() ;
    queryObject.setPath("/foo");
    queryObject.setType(FAQEventQuery.FAQ_QUESTION);
    
    
    
    // throws a NPE
    //assertException(new Closure() {public void dothis() {eventQuery.getQuery();}});
    /*
    eventQuery.setType(type) ;
    eventQuery.setText(text) ;
    eventQuery.setName(categoryName) ;
    eventQuery.setIsModeQuestion(modeQuestion) ;
    eventQuery.setModerator(moderator) ;
    eventQuery.setFromDate(fromDate) ;
    eventQuery.setToDate(toDate) ;
    eventQuery.setAuthor(author) ;
    eventQuery.setEmail(emailAddress) ;
    eventQuery.setAttachment(nameAttachment);
    eventQuery.setQuestion(question) ;
    eventQuery.setResponse(response) ;
    eventQuery.setComment(comment) ;
    if(language != null && language.length() > 0 && !language.equals(advancedSearch.defaultLanguage_)) {
      eventQuery.setLanguage(language);
      eventQuery.setSearchOnDefaultLanguage(false) ;
    } else {
      eventQuery.setLanguage(advancedSearch.defaultLanguage_) ;
      eventQuery.setSearchOnDefaultLanguage(true) ;       
    }

    String userName = FAQUtils.getCurrentUser();
    eventQuery.setUserId(userName) ;
    eventQuery.setUserMembers(UserHelper.getAllGroupAndMembershipOfUser(userName));
    eventQuery.setAdmin(Boolean.parseBoolean(advancedSearch.faqSetting_.getIsAdmin()));  
    */
 
  }

}
