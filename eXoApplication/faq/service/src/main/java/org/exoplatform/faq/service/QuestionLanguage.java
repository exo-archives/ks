/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 **/
package org.exoplatform.faq.service;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class QuestionLanguage {
  private String language ;
  private String question ;
  private String response ;
  
  public QuestionLanguage() { }
  
  public String getLanguage() { return language ; }
  public void setLanguage(String lang) { this.language = lang ; }
  
  public String getQuestion() { return question ; }
  public void setQuestion(String q) { this.question = q ; }

  public String getResponse() { return response ; }
  public void setResponse(String res) { this.response = res ; }	
  
}
