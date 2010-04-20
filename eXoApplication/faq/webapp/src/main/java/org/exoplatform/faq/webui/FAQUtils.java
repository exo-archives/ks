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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.portlet.PortletPreferences;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.record.formula.functions.Exec;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JcrInputProperty;
import org.exoplatform.ks.common.user.CommonContact;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *			truong.nguyen@exoplatform.com
 * Apr 14, 2008, 2:56:30 PM
 */
public class FAQUtils {
	public static String DISPLAYAPPROVED = "approved";
	public static String DISPLAYBOTH = "both";
	private static String AKONG = "@" ;
	public static String[] specialString = {"!", "#", "$", "%", "^", "&"
		, ":", ">", "<", "~", "`", "]", "'", "/"} ;
	public static String UPLOAD_FILE_SIZE = "uploadFileSizeLimitMB" ;

	static private Log log = ExoLogger.getLogger(FAQUtils.class);
	
	
	public static FAQService getFAQService() throws Exception {
		return (FAQService)PortalContainer.getComponent(FAQService.class) ;
	}
	
	public static String getDefaultLanguage() {
		WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    return context.getLocale().getDisplayLanguage();
	}
	
	public static List <String> getAllLanguages(UIComponent component) {
		LocaleConfigService configService = component.getApplicationComponent(LocaleConfigService.class) ;
		List<String> languages = new ArrayList<String>() ;
    for(LocaleConfig localeConfig : configService.getLocalConfigs()) {      
      languages.add(localeConfig.getLocale().getDisplayLanguage()) ;
    }
    return languages ;
	}
	/**
	 * Find category which is already exist.<br/>
	 * for example: when you are standing in category D in path: Root\A\B\C\D, you do some action
	 * (add new category, add question, go out to category C or B) but another moderator delete
	 * category C (or B, A). Then this function will be use to find the nearest category with
	 * category D (which is exist) and move you into this category.<br/>
	 * <u>Detail:</u><br/>
	 * the first, system get category C, if C is exist, you will be moved into C else jump to 
	 * B and test again.<br/>
	 * This processing is done until find a category already exist.
	 * @param faqService_			FAQ Service
	 * @param fAQContainer		UIAnswersContainer this component is used to updated data
	 * @param sessionProvider	SessionProvider
	 * @throws Exception
	 */
	public static void findCateExist(FAQService faqService_, UIAnswersContainer fAQContainer) throws Exception{
		UIBreadcumbs breadcumbs = fAQContainer.findFirstComponentOfType(UIBreadcumbs.class) ;
		String pathCate = "" ;
		for(String path : breadcumbs.pathList_.get(breadcumbs.pathList_.size() - 1).split("/")){
			if(path.equals("FAQService")){
				pathCate = path ;
				continue ;
			}
			try {
				faqService_.getCategoryById(path);
				if(pathCate.trim().length() > 0) pathCate += "/" ;
				pathCate += path ;
			} catch (Exception pathExc) {
				UIQuestions questions = fAQContainer.findFirstComponentOfType(UIQuestions.class);
				try {
					breadcumbs.setUpdataPath(pathCate) ;
				} catch (Exception exc) {
				  log.debug("Setting update path fail: "+ exc.getMessage(), exc);
				}
				if(pathCate.indexOf("/") > 0) {
					questions.setCategoryId(pathCate.substring(pathCate.lastIndexOf("/") + 1)) ;
				} else {
					questions.categoryId_ = null ;
					questions.setListObject();
					//questions.setIsNotChangeLanguage() ;
				}
				((UICategories)fAQContainer.findFirstComponentOfType(UICategories.class)).setPathCategory(pathCate);
				break;
			}
		}
	}

	public static String filterString(String text, boolean isEmail) {
		for (String str : specialString) {
			text = text.replaceAll(str, "") ;
		}
		if (!isEmail) text = text.replaceAll(AKONG, "") ;
		int i = 0 ;
		while (i < text.length()) {
			if (text.charAt(i) == '?' || text.charAt(i) == '[' || text.charAt(i) == '(' || text.charAt(i) == '|'
				|| text.charAt(i) == ')' || text.charAt(i) == '*' || text.charAt(i) == '\\' || text.charAt(i) == '+'
					|| text.charAt(i) == '}' || text.charAt(i) == '{' || text.charAt(i) == '^' || text.charAt(i) == '$'
						|| text.charAt(i) == '"'  ) {
				text = text.replace((text.charAt(i)) + "", "") ;
			} else {
				i ++ ;
			}
		}
		return text ;
	}

	public static boolean CheckSpecial(String text) {
		Boolean check = false ;
		if(text != null && text.trim().length() > 0) {
			int i = 0 ;
			while (i < text.length()) {
				if (text.charAt(i) == '?' || text.charAt(i) == '[' || text.charAt(i) == '(' || text.charAt(i) == '|'
					|| text.charAt(i) == ')' || text.charAt(i) == '*' || text.charAt(i) == '\\' || text.charAt(i) == '+'
						|| text.charAt(i) == '}' || text.charAt(i) == '{' || text.charAt(i) == '^' || text.charAt(i) == '$'
							|| text.charAt(i) == '"' || text.charAt(i) == '!' || text.charAt(i) == '#' || text.charAt(i) == '%'
								|| text.charAt(i) == ':' || text.charAt(i) == '&' || text.charAt(i) == '>' || text.charAt(i) == '<'
									|| text.charAt(i) == '~' || text.charAt(i) == '`' || text.charAt(i) == ']' || text.charAt(i) == '/'	) {
					text = text.replace((text.charAt(i)) + "", "") ;
					check = true ;
				} else {
					i ++ ;
				}
			}
			return check ;
		}
		return false ;
	}

	 public static InternetAddress[] getInternetAddress(String addressList) throws Exception {
	    if (addressList == null || addressList == "") 
	      return new InternetAddress[1];
	    try {
	      return InternetAddress.parse(addressList);
	    } catch (Exception e) {
	      return new InternetAddress[1];
	    }
	  }
	

	public static void setCommonContactInfor(String userId, CommonContact contact, FAQService faqService, DownloadService dservice) throws Exception {
		OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
		UserProfile profile = organizationService.getUserProfileHandler().findUserProfileByName(userId);
		if(profile.getAttribute("user.bdate") != null)contact.setBirthday(profile.getAttribute("user.bdate"));
		if(profile.getAttribute("user.gender") != null)contact.setGender(profile.getAttribute("user.gender"));
		if(profile.getAttribute("user.jobtitle") != null)contact.setJob(profile.getAttribute("user.jobtitle"));
		
		if(profile.getAttribute("user.business-info.online.email") != null)contact.setEmailAddress(profile.getAttribute("user.business-info.online.email"));
		if(profile.getAttribute("user.business-info.postal.city") != null)contact.setCity(profile.getAttribute("user.business-info.postal.city"));
		if(profile.getAttribute("user.business-info.postal.country") != null)contact.setCountry(profile.getAttribute("user.business-info.postal.country"));        
		if(profile.getAttribute("user.business-info.telecom.mobile.number") != null)contact.setMobile(profile.getAttribute("user.business-info.telecom.mobile.number"));
		if(profile.getAttribute("user.business-info.telecom.telephone.number") != null)contact.setPhone(profile.getAttribute("user.business-info.telecom.telephone.number"));
		if(profile.getAttribute("user.home-info.online.uri") != null)contact.setWebSite(profile.getAttribute("user.home-info.online.uri"));
		FileAttachment fileAttachment = faqService.getUserAvatar(userId);
		if(fileAttachment == null || fileAttachment.getSize() == 0){
			if(profile.getAttribute("user.other-info.avatar.url") != null)contact.setAvatarUrl(profile.getAttribute("user.other-info.avatar.url"));
		} else {
			contact.setAvatarUrl(getFileSource(fileAttachment, dservice));
		}
	}
	

	public static String[] splitForFAQ (String str) throws Exception {
		if(str != null && str.length() > 0) {
			String[] temp ;			
			if(str.contains(",")) temp = str.trim().split(",") ;
			else if (str.contains(";")) temp = str.split(";") ;
			else return new String[]{str} ;
			List<String> ids = new ArrayList<String>() ;
			for(String id : temp) {
				ids.add(id.trim()) ;
			}
			return ids.toArray(new String[ids.size()]) ;
		} else return new String[] {} ;
	}

	/*public static SessionProvider getSystemProvider() {
		return SessionProviderFactory.createSystemProvider();
	}*/

	static public String getCurrentUser() throws Exception {
		return Util.getPortalRequestContext().getRemoteUser();
	}

	static public String getEmailUser(String userName) throws Exception {
		OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
		User user = organizationService.getUserHandler().findUserByName(userName) ;
		String email = user.getEmail() ;
		return email;
	}

	static public String getFullName(String userName) throws Exception {
		try{
			OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
			User user = organizationService.getUserHandler().findUserByName(userName) ;
			String fullName = user.getFullName() ;
			if(isFieldEmpty(fullName)) fullName = userName;
			return fullName ;
		} catch (Exception e){
			return userName;
		}
	}

	public static boolean isFieldEmpty(String s) {
		return (s == null || s.trim().length() <= 0)?true:false;
	}

	public static boolean isValidEmailAddresses(String addressList) throws Exception {
		if (isFieldEmpty(addressList))  return true ;
		boolean isInvalid = true ;
		try {
			InternetAddress[] iAdds = InternetAddress.parse(addressList, true);
			String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}" ;
			for (int i = 0 ; i < iAdds.length; i ++) {
				if(!iAdds[i].getAddress().matches(emailRegex)) isInvalid = false;
			}
		} catch(AddressException e) {
			return false ;
		}
		return isInvalid ;
	}

	public static String getResourceBundle(String resourceBundl){
		WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
		ResourceBundle res = context.getApplicationResourceBundle() ;
		return res.getString(resourceBundl);
	}
	
	/*public static String[] getQuestionLanguages() {

		return null ;
	}*/

	@SuppressWarnings("unchecked")
	public static Map prepareMap(List inputs, Map properties) throws Exception {
		Map<String, JcrInputProperty> rawinputs = new HashMap<String, JcrInputProperty>();
		HashMap<String, JcrInputProperty> hasMap = new HashMap<String, JcrInputProperty>() ;
		for (int i = 0; i < inputs.size(); i++) {
			JcrInputProperty property = null;
			if(inputs.get(i) instanceof UIFormMultiValueInputSet) {        
				String inputName = ((UIFormMultiValueInputSet)inputs.get(i)).getName() ;        
				if(!hasMap.containsKey(inputName)) {
					List<String> values = (List<String>) ((UIFormMultiValueInputSet)inputs.get(i)).getValue() ;
					property = (JcrInputProperty) properties.get(inputName);        
					if(property != null){          
						property.setValue(values.toArray(new String[values.size()])) ;
					}
				}
				hasMap.put(inputName, property) ;
			} else {
				UIFormInputBase input = (UIFormInputBase) inputs.get(i);
				property = (JcrInputProperty) properties.get(input.getName());
				if(property != null) {
					if (input instanceof UIFormUploadInput) {
						byte[] content = ((UIFormUploadInput) input).getUploadData() ; 
						property.setValue(content);
					} else if(input instanceof UIFormDateTimeInput) {
						property.setValue(((UIFormDateTimeInput)input).getCalendar()) ;
					} else {
						property.setValue(input.getValue()) ;
					}
				}
			}
		}
		Iterator iter = properties.values().iterator() ;
		JcrInputProperty property ;
		while (iter.hasNext()) {
			property = (JcrInputProperty) iter.next() ;
			rawinputs.put(property.getJcrPath(), property) ;
		}
		return rawinputs;
	}

	public static String getSubString(String str, int max) {
		if(str.length() > max) {
			String newStr = str.substring(0, (max-3)) ;	
			return newStr.trim() + "..." ;
		}
		return str ;
	}

	public static String getTitle(String text) {
		/*int i = 0 ;
		while (i < text.length()) {
			if(text.codePointAt(i) < 10) continue;
			if (text.charAt(i) == '"'  ) text = text.replace((text.charAt(i)) + "", "&quot;") ;
			else i ++ ;
		}*/	
		return StringUtils.replace(text, "\"", "&quot;");		
	}

	public static List<String> getCategoriesIdFAQPortlet() throws Exception{
		PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
		PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
		String str = portletPref.getValue("displayCategories", "");
		List<String> list = new ArrayList<String>();
		if (!isFieldEmpty(str)) {
	    list.addAll(Arrays.asList(str.split(",")));
    }
	  return list;
  }

	public static boolean getUseAjaxFAQPortlet() {
		PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
		PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
	  return Boolean.parseBoolean(portletPref.getValue("useAjax", ""));
  }
	
	public static void saveFAQPortletPreference(List<String> list, boolean useAjax) throws Exception{
		PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
		PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
		String str = list.toString();
		str = str.replace("[", "").replace("]", "").replaceAll(" ", "");
		portletPref.setValue("displayCategories", str);
		portletPref.setValue("useAjax", String.valueOf(useAjax));
		portletPref.store();
	}
	
	public static void getPorletPreference(FAQSetting faqSetting) {
		PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
		PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
		faqSetting.setEnableViewAvatar(Boolean.parseBoolean(portletPref.getValue("enableViewAvatar", "")));
		faqSetting.setEnableAutomaticRSS(Boolean.parseBoolean(portletPref.getValue("enableAutomaticRSS", "")));
		faqSetting.setEnanbleVotesAndComments(Boolean.parseBoolean(portletPref.getValue("enanbleVotesAndComments", "")));
		faqSetting.setEnableAnonymousSubmitQuestion(Boolean.parseBoolean(portletPref.getValue("enableAnonymousSubmitQuestion", "")));
		faqSetting.setDisplayMode(portletPref.getValue("display", "")) ;
		faqSetting.setOrderBy(portletPref.getValue("orderBy", "")) ;
		faqSetting.setOrderType(portletPref.getValue("orderType", "")) ;
		faqSetting.setIsDiscussForum(Boolean.parseBoolean(portletPref.getValue("isDiscussForum", ""))) ;
		faqSetting.setIdNameCategoryForum(portletPref.getValue("idNameCategoryForum", "")) ;
		faqSetting.setEmailMoveQuestion(portletPref.getValue("emailMoveQuestion", ""));
	}

	public static void getEmailSetting(FAQSetting faqSetting, boolean isNew, boolean isSettingForm) {
		PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
		PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
		String emailContent = "";
		if(isNew){
			emailContent = portletPref.getValue("SendMailAddNewQuestion", "");
		} else {
			emailContent = portletPref.getValue("SendMailEditResponseQuestion", "");
		}
		WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
		ResourceBundle res = context.getApplicationResourceBundle() ;
		//if(!isSettingForm){
			if(emailContent == null || emailContent.trim().length() < 1){
				if(isNew){
					emailContent =  res.getString("SendEmail.AddNewQuestion.Default");
				} else {
					emailContent =  res.getString("SendEmail.EditOrResponseQuestion.Default");
				}
			}
		//}
		faqSetting.setEmailSettingSubject(res.getString("SendEmail.Default.Subject"));
		faqSetting.setEmailSettingContent(emailContent) ;
	}
	
	public static String getEmailMoveQuestion(FAQSetting faqSetting){
		PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
		PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
		String str = portletPref.getValue("emailMoveQuestion", "");
		if(isFieldEmpty(str)) {
			WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
			ResourceBundle res = context.getApplicationResourceBundle() ;
			str = res.getString("SendEmail.MoveQuetstion.Default");
		}
		faqSetting.setEmailMoveQuestion(str);
		return str;
	}

	public static void savePortletPreference(FAQSetting setting, String emailAddNewQuestion, String emailEditResponseQuestion){
		try {
			PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
			PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
			portletPref.setValue("display", setting.getDisplayMode());
			portletPref.setValue("orderBy", setting.getOrderBy());
			portletPref.setValue("orderType", setting.getOrderType());
			portletPref.setValue("isDiscussForum", String.valueOf(setting.getIsDiscussForum()));
			portletPref.setValue("idNameCategoryForum", setting.getIdNameCategoryForum());
			portletPref.setValue("enableAutomaticRSS", setting.isEnableAutomaticRSS() + "");
			portletPref.setValue("enableViewAvatar", setting.isEnableViewAvatar() + "");
			portletPref.setValue("enanbleVotesAndComments", setting.isEnanbleVotesAndComments() + "");
			portletPref.setValue("enableAnonymousSubmitQuestion", setting.isEnableAnonymousSubmitQuestion() + "");
			portletPref.setValue("SendMailAddNewQuestion", emailAddNewQuestion);
			portletPref.setValue("SendMailEditResponseQuestion", emailEditResponseQuestion);
			portletPref.setValue("emailMoveQuestion", setting.getEmailMoveQuestion());
			
			portletPref.store();
		} catch (Exception e) {
		  log.error("Fail to save portlet preferences: ", e);
		}
	}
	
	public static String getFormatDate(Date myDate) {
		if(myDate == null) return null;
		PortalRequestContext portalContext = Util.getPortalRequestContext();
		return DateFormat.getDateInstance(DateFormat.LONG, portalContext.getLocale()).format(myDate);
	}
	
	private static String getFileSource(InputStream input, String fileName, DownloadService dservice) throws Exception {
		byte[] imageBytes = null;
		if (input != null) {
			imageBytes = new byte[input.available()];
			input.read(imageBytes);
			ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
			InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image");
			dresource.setDownloadName(fileName);
			return dservice.getDownloadLink(dservice.addDownloadResource(dresource));
		}
		return null;
	}
	
	public static String getFileSource(FileAttachment attachment, DownloadService dservice){
		if(dservice == null)dservice = (DownloadService)PortalContainer.getComponent(DownloadService.class) ;
		try {
			InputStream input = attachment.getInputStream() ;
			String fileName = attachment.getName() ;
			if(fileName == null || fileName.trim().length() < 1)
				fileName = "avatar." + attachment.getMimeType();
			//String fileName = attachment.getNodeName() ;
			return getFileSource(input, fileName, dservice);
		} catch (Exception e) {			
		}
		return null;
	}
	
	public static String getLink(String link, String componentId, String componentIdhasAction, String action, String actionRep, String objectId) {
		PortalRequestContext portalContext = Util.getPortalRequestContext();
		String url = portalContext.getRequest().getRequestURL().toString();
		url = url.substring(0, url.indexOf("/", 8)) ;
		link = link.replaceFirst(componentId, componentIdhasAction).replaceFirst(action, actionRep).replaceFirst("OBJECTID", objectId).replaceAll("amp;", "");
		link = url + link;
	  return link;
  }

	public static String getLinkDiscuss(String topicId) throws Exception {
		PortalRequestContext portalContext = Util.getPortalRequestContext();
		String link = portalContext.getRequest().getRequestURL().toString();
		try {
			String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
			String portalName = "/" + Util.getUIPortal().getName() ;
			if(link.indexOf(portalName) > 0) {
	      if(link.indexOf(portalName + "/" + selectedNode) < 0){
	      	link = link.replaceFirst(portalName, portalName + "/" + selectedNode) ;
	      }                 
	    }
			link = link.substring(0, link.indexOf(selectedNode)+selectedNode.length());
			link = link.replaceAll(selectedNode, "forum") + "/" + org.exoplatform.forum.service.Utils.TOPIC + "/" + topicId;
    } catch (Exception e) {
      log.error("Fail to get link discuss: ",e);
    }
		return link;
	}
	
	public static int getLimitUploadSize(){
		PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
		PortletPreferences portletPref = pcontext.getRequest().getPreferences();
		int limitMB ;
		try {
			limitMB = Integer.parseInt(portletPref.getValue(UPLOAD_FILE_SIZE, "").trim()) ;
		}catch (Exception e) {
			limitMB = -1 ;
		}
		return limitMB ;
	}
	
}
