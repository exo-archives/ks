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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jcr.Value;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ks.rendering.MarkupRenderingService;
import org.exoplatform.ks.rendering.api.Renderer;
import org.exoplatform.ks.rendering.core.SupportedSyntaxes;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.w3c.dom.Document;


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
  
  static public String generateCheckSum(byte[] b) throws Exception {
    try{
      MessageDigest md = MessageDigest.getInstance("SHA1");
      md.update(b) ;
      byte[] mdbytes = md.digest();
   
      //convert the byte to hex format
      StringBuffer sb = new StringBuffer("");
      for (int i = 0; i < mdbytes.length; i++) {
        sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
      }
       return sb.toString() ;
    }catch(Exception e) {
      log.warn("Can not generate checksum for exporting data") ;
      return "" ;
    }
  }
  
  static public File getXMLFile(ByteArrayOutputStream bos, String appName, String objectType, Date createDate, String fileName) throws Exception {
    byte[] byteData = bos.toByteArray() ;
    
    DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();   
    InputStream is  = new ByteArrayInputStream(byteData) ;
    Document document = docBuilder.parse(is) ;
    
    org.w3c.dom.Attr namespace = document.createAttribute("xmlns:exoks") ;
    namespace.setValue("http://www.exoplatform.com/exoks/2.0") ;
    document.getFirstChild().getAttributes().setNamedItem(namespace) ;
    
    org.w3c.dom.Attr attName = document.createAttribute("exoks:applicationName") ;
    attName.setValue(appName) ;
    document.getFirstChild().getAttributes().setNamedItem(attName) ;
    
    org.w3c.dom.Attr dataType = document.createAttribute("exoks:objectType") ;
    dataType.setValue(objectType) ;
    document.getFirstChild().getAttributes().setNamedItem(dataType) ;
    
    org.w3c.dom.Attr exportDate = document.createAttribute("exoks:exportDate") ;
    exportDate.setValue(createDate.toString()) ;
    document.getFirstChild().getAttributes().setNamedItem(exportDate) ;
    
    org.w3c.dom.Attr checkSum = document.createAttribute("exoks:checkSum") ;
    checkSum.setValue(generateCheckSum(byteData)) ;
    document.getFirstChild().getAttributes().setNamedItem(checkSum) ;
    
    DOMSource source = new DOMSource(document.getFirstChild()) ;
    
    File file = new File(fileName + ".xml");
    file.deleteOnExit();
    file.createNewFile();
    StreamResult result = new StreamResult(file) ;
    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer();
    transformer.transform(source, result) ;
    return file ;
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
