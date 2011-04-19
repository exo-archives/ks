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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import java.util.Properties;

import javax.jcr.Value;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ks.rendering.MarkupRenderingService;
import org.exoplatform.ks.rendering.api.Renderer;
import org.exoplatform.ks.rendering.core.SupportedSyntaxes;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.form.wysiwyg.FCKEditorConfig;
import org.quartz.JobExecutionContext;
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
  
  public static String getRemoteIP() {
    String remoteAddr = "";
    try {
      PortalRequestContext context = Util.getPortalRequestContext();
      remoteAddr = ((HttpServletRequest)context.getRequest()).getRemoteAddr() ;
    } catch (Exception e) { 
      log.error("Can not get remote IP", e);
    }
    return remoteAddr;
  }
  
  public static String getImageUrl(String imagePath) throws Exception {
    StringBuilder url = new StringBuilder() ;
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    try {
      ExoContainerContext exoContext = (ExoContainerContext)container.getComponentInstanceOfType(ExoContainerContext.class);
      url.append("/").append(exoContext.getRestContextName());
    } catch (Exception e) {
      url.append("/portal");
      log.error("Can not get portal name or rest context name, exception: ",e);
    }
    RepositoryService rService = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class) ;
    url.append("/jcr/").append(rService.getCurrentRepository().getConfiguration().getName()).append(imagePath).append("/");
    return url.toString();
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
  
  /**
   * This function will change email address in 'from' field by address of mail service which is configured as system property : <code>gatein.email.smtp.from</code> or
   * <code>mail.from</code>. <br>
   * That ensures that 'emailAddress' part of 'from' field in a message object is always the same identity with authentication of smtp configuration.<br>
   * It's because of 2 reasons: <li>we don't want notification message to show email address of user as sender. Instead, we use mail service of kernel.</li> <li>Almost
   * authenticated smtp systems do not allow to separate email address in <code>from</code> field of message from smtp authentication</b> (for now, GMX, MS exchange deny, Gmail
   * efforts to modify the such value)</li>
   * 
   * @param from
   * @return null if can not find suitable sender.
   */
  public static String makeNotificationSender(String from) {
    if (from == null)
      return null;
    Properties props = new Properties(System.getProperties());
    String mailAddr = props.getProperty("gatein.email.smtp.from");
    if (mailAddr == null || mailAddr.length() == 0)
      mailAddr = props.getProperty("mail.from");
    if (mailAddr != null) {
      try {
        return new InternetAddress(from + "<" + mailAddr + ">").toUnicodeString();
      } catch (AddressException e) {
        if (log.isDebugEnabled()) {
          log.debug("value of 'gatein.email.smtp.from' or 'mail.from' in configuration file is not in format of mail address", e);
        }
        return null;
      }
    } else {
      return null;
    }
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
  
  public static ExoContainer getExoContainer(JobExecutionContext context) {
    if(context == null) return null;
    String portalName = context.getJobDetail().getGroup();
    if(portalName == null) {
      portalName = PortalContainer.getCurrentPortalContainerName();
    }
    if(portalName.indexOf(":") > 0) portalName = portalName.substring(0, portalName.indexOf(":"));
    return ExoContainerContext.getContainerByName(portalName);
  }
  
  public static FCKEditorConfig getFCKConfig(){
    FCKEditorConfig fckconfig = new FCKEditorConfig();
    fckconfig.put("CustomConfigurationsPath", "/ksResources/fckconfig/fckconfig.js");
    return fckconfig;
  }
  
  public static String getRSSLink(String appType, String portalName, String objectId) {
    return "/" + PortalContainer.getInstance().getRestContextName() + "/ks/" + appType + "/rss/" + objectId;
  }

  public static String getUserRSSLink(String apptype, String userId) {
    return "/" + PortalContainer.getInstance().getRestContextName() + "/ks/" + apptype + "/rss/user/" + userId;
  }
}
