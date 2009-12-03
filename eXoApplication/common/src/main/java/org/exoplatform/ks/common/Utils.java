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
package org.exoplatform.ks.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Value;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ks.rendering.MarkupRenderingService;
import org.exoplatform.ks.rendering.api.Renderer;
import org.exoplatform.ks.rendering.core.SupportedSyntaxes;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;


/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class Utils {
  
  private static Log log = ExoLogger.getLogger(Utils.class);

  /**
   * This method return value[] to list String
   * @param values
   * @return list string
   * @throws Exception
   */
  static public List<String> ValuesToList(Value[] values) throws Exception {
  	List<String> list = new ArrayList<String>() ;
  	if(values.length < 1) return list ;
  	if(values.length == 1) {
  		list.add(values[0].getString()) ;
  		return list ;
  	}
  	for(int i = 0; i < values.length; ++i) {
  		list.add(values[i].getString() );
  	}
  	return list;
  }

  static public String getStandardId(String s) {
  	int i=0;
  	StringBuilder builder = new StringBuilder();
  	while(i < s.length()) {
  		int t = s.codePointAt(i);
  		if(t > 48 && t < 122){
  			builder.append(s.charAt(i)) ;
  		} else {
  			builder.append("id") ;
  		}
  		++i;
  	}
  	return builder.toString();
  }

  static public String[] compareStr(String arr1[], String arr2[]) throws Exception {
  	List<String> list = new ArrayList<String>();
  	list.addAll(Arrays.asList(arr1));
  	if(list.isEmpty() || list.get(0).equals(" ")) return new String[]{" "};
  	for (int i = 0; i < arr2.length; i++) {
  		if(!list.contains(arr2[i])) {
  			list.add(arr2[i]);
  		}
    }
  	return list.toArray(new String[]{});
  }

  public static String convertCodeHTML(String s) {
  	if (s == null || s.length() <= 0)
  		return "";
  	s = s.replaceAll("(<p>((\\&nbsp;)*)(\\s*)?</p>)|(<p>((\\&nbsp;)*)?(\\s*)</p>)", "<br/>").trim();
  	s = s.replaceFirst("(<br/>)*", "");
  	s = s.replaceAll("(\\w|\\$)(>?,?\\.?\\*?\\!?\\&?\\%?\\]?\\)?\\}?)(<br/><br/>)*", "$1$2");
  	try {
  		s = Utils.processBBCode(s);
  		s = s.replaceAll("(https?|ftp)://", " $0").replaceAll("(=\"|=\'|\'>|\">)( )(https?|ftp)", "$1$3")
  				 .replaceAll("[^=\"|^=\'|^\'>|^\">](https?://|ftp://)([-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])", "<a target=\"_blank\" href=\"$1$2\">$1$2</a>");
  		s = s.replaceAll("&apos;", "'");
    } catch (Exception e) {
      log.error("Failed to convert HTML",e);
    	return "";
    }
  	return s;
  }

  public static String processBBCode(String s) {
    MarkupRenderingService markupRenderingService = (MarkupRenderingService) ExoContainerContext.getCurrentContainer()
    .getComponentInstanceOfType(MarkupRenderingService.class);
    Renderer r = markupRenderingService.getRenderer(SupportedSyntaxes.bbcode.name());
    return r.render(s);
  }
  
  /**
   * Get a Component from the current container context
   * @param <T> type of the expected component
   * @param type key for the component
   * @return
   */
  public static <T>T getComponent(Class<T> type) {
    return type.cast(ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(type));
  }
  

}
