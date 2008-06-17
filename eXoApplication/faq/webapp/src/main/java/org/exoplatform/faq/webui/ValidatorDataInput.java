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
package org.exoplatform.faq.webui;

import java.util.Arrays;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Mai Van Ha
 *          ha_mai_van@exoplatform.com
 * May 3, 2008 ,1:48:11 AM 
 */
public class ValidatorDataInput {
  private List<String> typeImage = Arrays.asList(new String[]{"gif", "jpg", "png"}) ;
  
  public boolean isNotEmptyInput(String inputString) {
    if(inputString == null || inputString.trim().length() < 1)
      return false ;
    return true ;
  }
  
  public boolean isImage(String fileName) {
    String fileType = fileName.substring(fileName.lastIndexOf(".") + 1) ;
    if(typeImage.contains(fileType.toLowerCase()))
      return true ;
    return false ;
  }
  
  public boolean fckContentIsNotEmpty(String input) {
    input = input.replaceAll("<p>", "") ;
    input = input.replaceAll("</p>", "") ;
    input = input.replaceAll("&nbsp;", " ") ;
    if(input.trim().length() > 0) {
      return true ;
    } else {
      return false ;
    }
  }
}
