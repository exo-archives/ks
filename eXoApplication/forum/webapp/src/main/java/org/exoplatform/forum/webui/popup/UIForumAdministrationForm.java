/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.api.BBCodeService;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;
/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * May 5, 2008 - 9:01:20 AM	
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIForumAdministrationForm.gtmpl",
		events = {
			@EventConfig(listeners = UIForumAdministrationForm.SaveActionListener.class), 
			@EventConfig(listeners = UIForumAdministrationForm.AddIpActionListener.class), 
			@EventConfig(listeners = UIForumAdministrationForm.PostsActionListener.class), 
			@EventConfig(listeners = UIForumAdministrationForm.UnBanActionListener.class, confirm= "UIForumAdministrationForm.msg.confirm-delete-ipban"), 
			@EventConfig(listeners = UIForumAdministrationForm.GetDefaultMailActionListener.class), 
			@EventConfig(listeners = UIForumAdministrationForm.AddNewBBCodeActionListener.class), 
			@EventConfig(listeners = UIForumAdministrationForm.EditBBCodeActionListener.class), 
			@EventConfig(listeners = UIForumAdministrationForm.DeleteBBCodeActionListener.class), 
			@EventConfig(listeners = UIForumAdministrationForm.AddTopicTypeActionListener.class), 
			@EventConfig(listeners = UIForumAdministrationForm.EditTopicTypeActionListener.class), 
			@EventConfig(listeners = UIForumAdministrationForm.DeleteTopicTypeActionListener.class), 
			@EventConfig(listeners = UIForumAdministrationForm.CancelActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UIForumAdministrationForm.SelectTabActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UIForumAdministrationForm.PruneSettingActionListener.class),
			@EventConfig(listeners = UIForumAdministrationForm.RunPruneActionListener.class),
			@EventConfig(listeners = UIForumAdministrationForm.ActivatePruneActionListener.class)
		}
)
public class UIForumAdministrationForm extends BaseForumForm implements UIPopupComponent {
  
	private BBCodeService bbCodeService;
	
	// main bean
	private ForumAdministration administration ;

	public static final String FIELD_FORUMSORT_TAB = "forumSortTab" ;
	public static final String FIELD_CENSOREDKEYWORD_TAB = "forumCensorTab" ;
	public static final String FIELD_ACTIVETOPIC_TAB = "activeTopicTab" ;
	public static final String FIELD_NOTIFYEMAIL_TAB = "notifyEmailTab" ;
	public static final String FIELD_AUTOPRUNE_TAB = "autoPruneTab" ;
	public static final String FIELD_TOPICTYPEMANAGER_TAB = "topicTypeManagerTab" ;
	public static final String FIELD_BBCODE_TAB = "bbcodesTab" ;
	public static final String IP_BAN_TAB = "ipBanTab" ;
	
	public static final String NEW_IP_BAN_INPUT1 = "newIpBan1";
	public static final String NEW_IP_BAN_INPUT2 = "newIpBan2";
	public static final String NEW_IP_BAN_INPUT3 = "newIpBan3";
	public static final String NEW_IP_BAN_INPUT4 = "newIpBan4";
	public static final String SEARCH_IP_BAN = "searchIpBan";
	public static final String FIELD_FORUMSORTBY_INPUT = "forumSortBy" ;
	public static final String FIELD_FORUMSORTBYTYPE_INPUT = "forumSortByType" ;
	public static final String FIELD_TOPICSORTBY_INPUT = "topicSortBy" ;
	public static final String FIELD_TOPICSORTBYTYPE_INPUT = "topicSortByType" ;
	
	public static final String FIELD_CENSOREDKEYWORD_TEXTAREA = "censorKeyword" ;
	
	public static final String FIELD_ENABLEHEADERSUBJECT_CHECKBOX = "enableHeaderSubject" ;
	public static final String FIELD_HEADERSUBJECT_INPUT = "headerSubject" ;
	public static final String FIELD_NOTIFYEMAIL_TEXTAREA = "notifyEmail" ;
	public static final String FIELD_NOTIFYEMAILMOVED_TEXTAREA = "notifyEmailMoved" ;
	
	public static final String FIELD_ACTIVEABOUT_INPUT = "activeAbout" ;
	public static final String FIELD_SETACTIVE_INPUT = "setActive" ;
	public static final String BAN_IP_PAGE_ITERATOR = "IpBanPageIterator" ;
	
	@SuppressWarnings("unchecked")
  private JCRPageList pageList ;
	private List<String> listIpBan = new ArrayList<String>();
	private List<BBCode> listBBCode = new ArrayList<BBCode>();
	private List<TopicType> listTT = new ArrayList<TopicType>();
	List<PruneSetting> listPruneSetting = new ArrayList<PruneSetting>();
	private UIForumPageIterator pageIterator ;
	private String notifyEmail_ = "";
	private String notifyMove_ = "";
	private int id = 0 ;
	private boolean isRenderListTopic = false ;
	
	public UIForumAdministrationForm() throws Exception {
	  bbCodeService = getApplicationComponent(BBCodeService.class);
		addChild(UIListTopicOld.class, null, null) ;
		this.setActions(new String[]{"Save", "Cancel"}) ;
		pageIterator = addChild(UIForumPageIterator.class, null, BAN_IP_PAGE_ITERATOR);
	}
	
  public void setInit() throws Exception{
  	getPruneSettings();
  	
		this.administration = getForumService().getForumAdministration();
		UIFormInputWithActions forumSortTab = new UIFormInputWithActions(FIELD_FORUMSORT_TAB) ;
		UIFormInputWithActions forumCensorTab = new UIFormInputWithActions(FIELD_CENSOREDKEYWORD_TAB) ;
		UIFormInputWithActions notifyEmailTab = new UIFormInputWithActions(FIELD_NOTIFYEMAIL_TAB);
		UIFormInputWithActions ipBanTab = new UIFormInputWithActions(IP_BAN_TAB);
		UIFormInputWithActions bbcodeTab = new UIFormInputWithActions(FIELD_BBCODE_TAB);
		UIFormInputWithActions autoPruneTab = new UIFormInputWithActions(FIELD_AUTOPRUNE_TAB);
		UIFormInputWithActions topicTypeManagerTag = new UIFormInputWithActions(FIELD_TOPICTYPEMANAGER_TAB);
		

    UIFormSelectBox forumSortBy = initForumSortField();
    UIFormSelectBox forumSortByType = initForumSortDirectionField();
		UIFormSelectBox topicSortBy = initTopicSortField();		
    UIFormSelectBox topicSortByType = initTopicSortDirectionField();
		
		UIFormTextAreaInput censorKeyword = initCensoredKeywordsField();
		
		UIFormStringInput activeAbout = initActiveAboutField();
		UIFormRadioBoxInput setActive = initSetActiveField();
	  UIFormWYSIWYGInput notifyEmail = initNotifyEmailField();
    UIFormWYSIWYGInput notifyEmailMoved = initNotifyMoveField();
		
		UIFormCheckBoxInput<Boolean> enableHeaderSubject = initEnableHeaderField();
		UIFormStringInput headerSubject = initEnableHeaderSubjectField();
		
		initBBCodesFields(bbcodeTab);
		
		forumSortTab.addUIFormInput(forumSortBy) ;
		forumSortTab.addUIFormInput(forumSortByType) ;
		forumSortTab.addUIFormInput(topicSortBy) ;
		forumSortTab.addUIFormInput(topicSortByType) ;
		
		notifyEmailTab.addUIFormInput(enableHeaderSubject);
		notifyEmailTab.addUIFormInput(headerSubject);
		notifyEmailTab.addUIFormInput(notifyEmail) ;
		notifyEmailTab.addUIFormInput(notifyEmailMoved) ;
		
		forumCensorTab.addUIFormInput(censorKeyword) ;
		
		addUIFormInput(activeAbout);
		addUIFormInput(setActive);
		
		addUIFormInput(forumSortTab) ;
		addUIFormInput(forumCensorTab) ;
		addUIFormInput(notifyEmailTab) ;
		addUIFormInput(bbcodeTab) ;
		addUIFormInput(autoPruneTab) ;
		addUIFormInput(topicTypeManagerTag) ;
		if(ForumUtils.enableIPLogging()){
			ipBanTab.addUIFormInput(new UIFormStringInput(SEARCH_IP_BAN, null));
			ipBanTab.addUIFormInput((new UIFormStringInput(NEW_IP_BAN_INPUT1, null)).setMaxLength(3));
			ipBanTab.addUIFormInput((new UIFormStringInput(NEW_IP_BAN_INPUT2, null)).setMaxLength(3));
			ipBanTab.addUIFormInput((new UIFormStringInput(NEW_IP_BAN_INPUT3, null)).setMaxLength(3));
			ipBanTab.addUIFormInput((new UIFormStringInput(NEW_IP_BAN_INPUT4, null)).setMaxLength(3));
			addUIFormInput(ipBanTab);
		}
		

		initEmailField(notifyEmailTab, FIELD_NOTIFYEMAIL_TEXTAREA);		
		initEmailField(notifyEmailTab, FIELD_NOTIFYEMAILMOVED_TEXTAREA);
	}

  private void initBBCodesFields(UIFormInputWithActions bbcodeTab) throws Exception {
    loadBBCodes();
    
		for (BBCode bbc : listBBCode) {
			UIFormCheckBoxInput<Boolean>isActiveBBcode = new UIFormCheckBoxInput<Boolean>(bbc.getId(), bbc.getId(), false);
    	isActiveBBcode.setChecked(bbc.isActive());
      bbcodeTab.addChild(isActiveBBcode);
    }
  }

  private void initEmailField(UIFormInputWithActions notifyEmailTab, String param) throws Exception {
    List<ActionData> actions = new ArrayList<ActionData>() ;
		ActionData ad = new ActionData() ;
		ad.setActionListener("GetDefaultMail") ;
		ad.setActionParameter(param) ;
		ad.setCssIconClass("Refresh") ;
		ad.setActionName("TitleResetMail");
		actions.add(ad) ;
		notifyEmailTab.setActionField(param, actions);
  }

  private UIFormStringInput initEnableHeaderSubjectField() {
    UIFormStringInput headerSubject = new UIFormStringInput(FIELD_HEADERSUBJECT_INPUT, FIELD_HEADERSUBJECT_INPUT, null);
		String headerSubject_ = administration.getHeaderSubject(); 
		if(ForumUtils.isEmpty(headerSubject_)) headerSubject_ = this.getLabel("notifyEmailHeaderSubjectDefault");
		headerSubject.setValue(headerSubject_);
    return headerSubject;
  }

  private UIFormCheckBoxInput<Boolean> initEnableHeaderField() {
    UIFormCheckBoxInput<Boolean> enableHeaderSubject = new UIFormCheckBoxInput<Boolean>(FIELD_ENABLEHEADERSUBJECT_CHECKBOX, FIELD_ENABLEHEADERSUBJECT_CHECKBOX, false);
		enableHeaderSubject.setChecked(administration.getEnableHeaderSubject());
    return enableHeaderSubject;
  }

  private UIFormTextAreaInput initCensoredKeywordsField() {
    UIFormTextAreaInput censorKeyword = new UIFormTextAreaInput(FIELD_CENSOREDKEYWORD_TEXTAREA, FIELD_CENSOREDKEYWORD_TEXTAREA, null);
		censorKeyword.setValue(administration.getCensoredKeyword()) ;
    return censorKeyword;
  }

  private UIFormStringInput initActiveAboutField() throws Exception {
    UIFormStringInput activeAbout = new UIFormStringInput(FIELD_ACTIVEABOUT_INPUT, FIELD_ACTIVEABOUT_INPUT, null);
		activeAbout.setValue("0");
		activeAbout.addValidator(PositiveNumberFormatValidator.class);
    return activeAbout;
  }

  private UIFormWYSIWYGInput initNotifyMoveField() {
    String value;
		value = administration.getNotifyEmailMoved();
		if(ForumUtils.isEmpty(value)) value = this.getLabel("EmailToAuthorMoved");
		UIFormWYSIWYGInput notifyEmailMoved = new UIFormWYSIWYGInput(FIELD_NOTIFYEMAILMOVED_TEXTAREA, FIELD_NOTIFYEMAILMOVED_TEXTAREA, "");
		notifyEmailMoved.setToolBarName("Basic");
		notifyEmailMoved.setValue(value); 
		this.notifyMove_ = value;
    return notifyEmailMoved;
  }

  private UIFormWYSIWYGInput initNotifyEmailField() {
    String value = administration.getNotifyEmailContent();
		if(ForumUtils.isEmpty(value)) value = this.getLabel("notifyEmailContentDefault");
		UIFormWYSIWYGInput notifyEmail = new UIFormWYSIWYGInput(FIELD_NOTIFYEMAIL_TEXTAREA, FIELD_NOTIFYEMAIL_TEXTAREA, "");
		notifyEmail.setToolBarName("Basic");
		notifyEmail.setValue(value); 
		this.notifyEmail_ = value;
    return notifyEmail;
  }

  private UIFormRadioBoxInput initSetActiveField() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
		options.add( new SelectItemOption<String>("true", "true") ) ;
		options.add( new SelectItemOption<String>("false", "false") ) ;
		UIFormRadioBoxInput setActive = new UIFormRadioBoxInput(FIELD_SETACTIVE_INPUT, FIELD_SETACTIVE_INPUT, options);
		setActive.setValue("false") ;
    return setActive;
  }

  private UIFormSelectBox initTopicSortDirectionField() {
    List<SelectItemOption<String>> ls;
		ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>(this.getLabel("ascending"), "ascending")) ;
		ls.add(new SelectItemOption<String>(this.getLabel("descending"), "descending")) ;
		UIFormSelectBox topicSortByType = new UIFormSelectBox(FIELD_TOPICSORTBYTYPE_INPUT, FIELD_TOPICSORTBYTYPE_INPUT, ls);
		topicSortByType.setValue(administration.getTopicSortByType()) ;
    return topicSortByType;
  }

  private UIFormSelectBox initTopicSortField() {
    String[] idLables;
    List<SelectItemOption<String>> ls;
    idLables = new String[]{"isLock", "createdDate", "modifiedDate", 
				"lastPostDate", "postCount", "viewCount", "numberAttachments"}; 
		ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>(this.getLabel("threadName"), "name")) ;
		for (String string : idLables) {
			ls.add(new SelectItemOption<String>(this.getLabel(string), string)) ;
		}
		
		UIFormSelectBox topicSortBy = new UIFormSelectBox(FIELD_TOPICSORTBY_INPUT, FIELD_TOPICSORTBY_INPUT, ls);
		topicSortBy.setValue(administration.getTopicSortBy()) ;
    return topicSortBy;
  }

  private UIFormSelectBox initForumSortDirectionField() {
 
    List<SelectItemOption<String>> ls;
		ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>(this.getLabel("ascending"), "ascending")) ;
		ls.add(new SelectItemOption<String>(this.getLabel("descending"), "descending")) ;
		UIFormSelectBox forumSortByType = new UIFormSelectBox(FIELD_FORUMSORTBYTYPE_INPUT, FIELD_FORUMSORTBYTYPE_INPUT, ls);
		forumSortByType.setValue(administration.getForumSortByType()) ;
    return forumSortByType;
  }

  private UIFormSelectBox initForumSortField() {
    String []idLables = new String[]{"forumOrder", "isLock", "createdDate",
																"modifiedDate",	"topicCount", "postCount"}; 
		List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>(this.getLabel("forumName"), "name")) ;
		for (String string : idLables) {
			ls.add(new SelectItemOption<String>(this.getLabel(string), string)) ;
		}
		UIFormSelectBox forumSortBy = new UIFormSelectBox(FIELD_FORUMSORTBY_INPUT, FIELD_FORUMSORTBY_INPUT, ls);
    forumSortBy.setValue(administration.getForumSortBy()) ;
    return forumSortBy;
  }
	
	public void loadBBCodes() throws Exception {
		listBBCode = new ArrayList<BBCode>();
		try {
			listBBCode.addAll(bbCodeService.getAll());
    } catch (Exception e) {
	    log.error("failed to set BBCode List", e);
    }
	}
	
	private List<PruneSetting> getPruneSettings() throws Exception {
		listPruneSetting = new ArrayList<PruneSetting>();
		try {
			listPruneSetting.addAll(getForumService().getAllPruneSetting());
    } catch (Exception e) {
      log.error("failed to get prune settings", e);
    }
		return listPruneSetting;
	}
	
	private PruneSetting getPruneSetting(String pruneId) throws Exception {
		for (PruneSetting prune : listPruneSetting) {
	    if(prune.getId().equals(pruneId)) return prune;
    }
		return new PruneSetting();
	}
	
	@SuppressWarnings("unused")
  private List<TopicType> getTopicTypes() throws Exception {
		listTT = new ArrayList<TopicType>();
		listTT.addAll(getForumService().getTopicTypes());
		return listTT;
	}
	
	private TopicType getTopicType(String topicTId) throws Exception {
		for (TopicType topicT : listTT) {
	    if(topicT.getId().equals(topicTId)) return topicT;
    }
		return new TopicType();
	}
	
	@SuppressWarnings("unused")
  private List<BBCode> getListBBcode() throws Exception{
		return listBBCode;
	}
	
	private BBCode getBBCode(String bbcId) {
		for (BBCode bbCode : listBBCode) {
	    if(bbCode.getId().equals(bbcId)) return bbCode;
    }
		return new BBCode();
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	private List<String> getListIpBan() throws Exception{
		listIpBan = new ArrayList<String>();
		try {
			listIpBan.addAll(getForumService().getBanList());
		} catch (Exception e) {
      log.error("failed to get ban list", e);
		}
		pageList = new ForumPageList(8, listIpBan.size());
		pageList.setPageSize(8);
		pageIterator = this.getChild(UIForumPageIterator.class);
		pageIterator.updatePageList(pageList);
		List<String>list = new ArrayList<String>();
		list.addAll(this.pageList.getPageList(pageIterator.getPageSelected(), listIpBan)) ;
		pageIterator.setSelectPage(pageList.getCurrentPage());
		try {
			if(pageList.getAvailablePage() <= 1) pageIterator.setRendered(false);
			else  pageIterator.setRendered(true);
		} catch (Exception e) {
      log.error("failed to init page iterator", e);
		}
		return list;
	}
	
	public boolean isRenderListTopic() {
		return isRenderListTopic;
	}

	public void setRenderListTopic(boolean isRenderListTopic) {
		this.isRenderListTopic = isRenderListTopic;
	}
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	@SuppressWarnings("unused")
	private boolean getIsSelected(int id) {
		if(this.id == id) return true ;
		return false ;
	}
	
	private String checkIpAddress(String[] ipAdd){
		String ip = "";
		try{
			int[] ips = new int[4];
			for(int t = 0; t < ipAdd.length; t ++){
				if(t>0) ip += ".";
				ip += ipAdd[t];
				ips[t] = Integer.parseInt(ipAdd[t]);
			}
			for(int i = 0; i < 4; i ++){
				if(ips[i] < 0 || ips[i] > 255) return null;
			}
			if(ips[0] == 255 && ips[1] == 255 && ips[2] == 255 && ips[3] == 255) return null;
			if(ips[0] == 0 && ips[1] == 0 && ips[2] == 0 && ips[3] == 0) return null;
			return ip;
		} catch (Exception e){
      log.error("failed to check IP address, Ip is not format number.");
			return null;
		}
	}
	
	static	public class SaveActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String objectId) throws Exception {
			UIFormInputWithActions forumSortTab = getChildById(FIELD_FORUMSORT_TAB) ;
			UIFormInputWithActions forumCensor = getChildById(FIELD_CENSOREDKEYWORD_TAB) ;
			UIFormInputWithActions notifyEmailTab = getChildById(FIELD_NOTIFYEMAIL_TAB) ;
			String forumSortBy = forumSortTab.getUIFormSelectBox(FIELD_FORUMSORTBY_INPUT).getValue() ;
			String forumSortByType = forumSortTab.getUIFormSelectBox(FIELD_FORUMSORTBYTYPE_INPUT).getValue() ;
			String topicSortBy = forumSortTab.getUIFormSelectBox(FIELD_TOPICSORTBY_INPUT).getValue() ;
			String topicSortByType = forumSortTab.getUIFormSelectBox(FIELD_TOPICSORTBYTYPE_INPUT).getValue() ;
			String censoredKeyword = forumCensor.getUIFormTextAreaInput(FIELD_CENSOREDKEYWORD_TEXTAREA).getValue() ;
			censoredKeyword = ForumUtils.removeSpaceInString(censoredKeyword);
			if(!ForumUtils.isEmpty(censoredKeyword)) {
				censoredKeyword = censoredKeyword.toLowerCase();
			}
			boolean enableHeaderSubject = (Boolean)notifyEmailTab.getUIFormCheckBoxInput(FIELD_ENABLEHEADERSUBJECT_CHECKBOX).getValue();
			String headerSubject = notifyEmailTab.getUIStringInput(FIELD_HEADERSUBJECT_INPUT).getValue();
			String notifyEmail = ((UIFormWYSIWYGInput)notifyEmailTab.getChildById(FIELD_NOTIFYEMAIL_TEXTAREA)).getValue() ;
			String notifyEmailMoved = ((UIFormWYSIWYGInput)notifyEmailTab.getChildById(FIELD_NOTIFYEMAILMOVED_TEXTAREA)).getValue() ;
			UIForumPortlet forumPortlet = administrationForm.getAncestorOfType(UIForumPortlet.class) ;
			if(notifyEmail == null || notifyEmail.replaceAll("<p>", "").replaceAll("</p>", "").replaceAll("&nbsp;", "").trim().length() < 1){
				warning("UIForumAdministrationForm.msg.mailContentInvalid", getLabel(FIELD_NOTIFYEMAIL_TEXTAREA));
				return;
			}
			if(notifyEmailMoved == null || notifyEmailMoved.replaceAll("<p>", "").replaceAll("</p>", "").replaceAll("&nbsp;", "").trim().length() < 1){
			  warning("UIForumAdministrationForm.msg.mailContentInvalid", getLabel(FIELD_NOTIFYEMAILMOVED_TEXTAREA));
				return;
			}
			ForumAdministration forumAdministration = new ForumAdministration() ;
			forumAdministration.setForumSortBy(forumSortBy) ;
			forumAdministration.setForumSortByType(forumSortByType) ;
			forumAdministration.setTopicSortBy(topicSortBy) ;
			forumAdministration.setTopicSortByType(topicSortByType) ;
			forumAdministration.setCensoredKeyword(censoredKeyword) ;
			forumAdministration.setEnableHeaderSubject(enableHeaderSubject) ;
			forumAdministration.setHeaderSubject(headerSubject);
			forumAdministration.setNotifyEmailContent(notifyEmail) ;
			forumAdministration.setNotifyEmailMoved(notifyEmailMoved);
			try {
				administrationForm.getForumService().saveForumAdministration(forumAdministration) ;
				if(!forumSortBy.equals(administrationForm.administration.getForumSortBy()) || !forumSortByType.equals(administrationForm.administration.getForumSortByType())){
					forumPortlet.findFirstComponentOfType(UICategory.class).setIsEditForum(true);
				}
			} catch (Exception e) {
				administrationForm.log.error("failed to save forum administration", e);
			}
			UIFormInputWithActions bbcodeTab = getChildById(FIELD_BBCODE_TAB) ;
//			
			List<BBCode> bbCodes = new ArrayList<BBCode>();
			boolean inactiveAll = true;
			for (BBCode bbc : administrationForm.listBBCode) {
				boolean isActive = true;
				try {
					isActive = (Boolean)bbcodeTab.getUIFormCheckBoxInput(bbc.getId()).getValue();
        } catch (Exception e) {
        }
				if(bbc.isActive() != isActive){
					bbc.setActive(isActive);
					bbCodes.add(bbc);
				}
				if(isActive) inactiveAll = false;
      }
			if(administrationForm.listBBCode.size() > 0 && inactiveAll){
			  warning("UIForumAdministrationForm.msg.inactiveAllBBCode", getLabel(FIELD_NOTIFYEMAILMOVED_TEXTAREA));
				return;
			}
			if(!bbCodes.isEmpty()){
				try {
					administrationForm.bbCodeService.save(bbCodes);
	      } catch (Exception e) {
	      	administrationForm.log.error("failed to save bbcodes", e);
	      }
			}
			forumPortlet.cancelAction() ;
	    event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
  static public class RunPruneActionListener extends BaseEventListener<UIForumAdministrationForm> {
    public void onEvent(Event<UIForumAdministrationForm> event,
                        UIForumAdministrationForm administrationForm, final String pruneId) throws Exception {
      PruneSetting pruneSetting = administrationForm.getPruneSetting(pruneId);
      if (pruneSetting.getInActiveDay() == 0) {
        warning("UIForumAdministrationForm.sms.not-set-activeDay");
        return;
      } else {
      	UIPopupContainer popupContainer = administrationForm.getAncestorOfType(UIPopupContainer.class) ;
        UIRunPruneForm pruneForm = administrationForm.openPopup(popupContainer, UIRunPruneForm.class, 200, 0);
        pruneForm.setPruneSetting(pruneSetting);
      }
    }
  }
	
	static	public class GetDefaultMailActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String objectId) throws Exception {
			if(objectId.equals(FIELD_NOTIFYEMAIL_TEXTAREA)) {
				UIFormWYSIWYGInput areaInput = ((UIFormInputWithActions)getChildById(FIELD_NOTIFYEMAIL_TAB)).
																																					getChildById(FIELD_NOTIFYEMAIL_TEXTAREA);
				areaInput.setValue(getLabel("notifyEmailContentDefault"));
			} else {
				UIFormWYSIWYGInput areaInput = ((UIFormInputWithActions)getChildById(FIELD_NOTIFYEMAIL_TAB)).
				getChildById(FIELD_NOTIFYEMAILMOVED_TEXTAREA);
				areaInput.setValue(getLabel("EmailToAuthorMoved"));
			}
			refresh();
		}
	}
	
	static	public class SelectTabActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String objectId) throws Exception {
			UIForumAdministrationForm uiForm = event.getSource();
			int temp = uiForm.id;
			uiForm.id = Integer.parseInt(objectId);
			UIFormInputWithActions notifyEmailTab = uiForm.getChildById(FIELD_NOTIFYEMAIL_TAB) ;
			UIFormWYSIWYGInput notifyEmailForm = notifyEmailTab.getChildById(FIELD_NOTIFYEMAIL_TEXTAREA) ;
			UIFormWYSIWYGInput notifyMoveForm = notifyEmailTab.getChildById(FIELD_NOTIFYEMAILMOVED_TEXTAREA);
			if(uiForm.id == 2) {
				notifyEmailForm.setValue(uiForm.notifyEmail_);
				notifyMoveForm.setValue(uiForm.notifyMove_);
			} else if(temp == 2){
				uiForm.notifyEmail_ = notifyEmailForm.getValue();
				uiForm.notifyMove_ = notifyMoveForm.getValue();
			}
			if(uiForm.id == 3){
				UIPopupWindow popupWindow = uiForm.getAncestorOfType(UIPopupWindow.class);
	      popupWindow.setWindowSize(650, 450) ;
	      event.getRequestContext().addUIComponentToUpdateByAjax(popupWindow) ;
			} else {
			  refresh();
			}
		}
	}

	static	public class AddNewBBCodeActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String objectId) throws Exception {
			 UIPopupContainer popupContainer = administrationForm.getAncestorOfType(UIPopupContainer.class) ;
		   administrationForm.openPopup(popupContainer, UIAddBBCodeForm.class, 670, 0);
		}
	}

	static	public class EditBBCodeActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String bbcodeId) throws Exception {
			UIPopupContainer popupContainer = administrationForm.getAncestorOfType(UIPopupContainer.class) ;
			BBCode bbCode = administrationForm.getBBCode(bbcodeId);
			UIAddBBCodeForm bbcForm = administrationForm.openPopup(popupContainer, UIAddBBCodeForm.class, "EditBBCodeForm", 670, 0);
	    bbcForm.setEditBBcode(bbCode);
		}
	}
	
	static	public class DeleteBBCodeActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String objectId) throws Exception {
		  administrationForm.bbCodeService.delete(objectId);
		  administrationForm.loadBBCodes();
			refresh();
		}
	}

	static	public class AddTopicTypeActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String objectId) throws Exception {
			UIPopupContainer popupContainer = administrationForm.getAncestorOfType(UIPopupContainer.class) ;
			administrationForm.openPopup(popupContainer, UIAddTopicTypeForm.class, 700, 0);
		}
	}
	
	static	public class EditTopicTypeActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String topicTId) throws Exception {
			TopicType topicType = administrationForm.getTopicType(topicTId);
			UIPopupContainer popupContainer = administrationForm.getAncestorOfType(UIPopupContainer.class) ;
			UIAddTopicTypeForm topicTypeForm = administrationForm.openPopup(popupContainer, UIAddTopicTypeForm.class,"EditTopicTypeForm", 700, 0);
			topicTypeForm.setTopicType(topicType);
		}
	}
	
	static	public class DeleteTopicTypeActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String topicTypeId) throws Exception {
		  administrationForm.getForumService().removeTopicType(topicTypeId);
			UIForumPortlet forumPortlet = administrationForm.getAncestorOfType(UIForumPortlet.class);
			UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
			topicContainer.setTopicType(topicTypeId);
			if(forumPortlet.getChild(UIForumContainer.class).isRendered() && !forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class).isRendered()){
				event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer) ;
			}
			refresh();
		}
	}
	
	static	public class CancelActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String objectId) throws Exception {
			event.getSource().getAncestorOfType(UIForumPortlet.class).cancelAction() ;
		}
	}

	static	public class ActivatePruneActionListener extends BaseEventListener<UIForumAdministrationForm> {
	  public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String pruneId) throws Exception {
			PruneSetting pruneSetting = administrationForm.getPruneSetting(pruneId);
			if(pruneSetting.getInActiveDay() == 0) {
				UIPopupContainer popupContainer = administrationForm.getAncestorOfType(UIPopupContainer.class) ;
				UIAutoPruneSettingForm pruneSettingForm = administrationForm.openPopup(popupContainer, UIAutoPruneSettingForm.class, 525, 0) ;
				pruneSettingForm.setPruneSetting(pruneSetting);
				pruneSettingForm.setActivate(true);
			} else {
				pruneSetting.setActive(!pruneSetting.isActive());
				administrationForm.getForumService().savePruneSetting(pruneSetting);
				refresh();
			}
		}
	}
	
	static	public class PruneSettingActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String pruneId) throws Exception {
			UIPopupContainer popupContainer = administrationForm.getAncestorOfType(UIPopupContainer.class) ;
			UIAutoPruneSettingForm pruneSettingForm = administrationForm.openPopup(popupContainer, UIAutoPruneSettingForm.class, 525, 0) ;
			PruneSetting pruneSetting = administrationForm.getPruneSetting(pruneId);
			pruneSettingForm.setPruneSetting(pruneSetting);
		}
	}
	
	private String getValueIp(UIFormInputWithActions inputWithActions, String inputId) throws Exception {
		UIFormStringInput stringInput = inputWithActions.getUIStringInput(inputId) ;  
		String vl = stringInput.getValue();
		stringInput.setValue("");
		return ForumUtils.isEmpty(vl)?"0":vl;
	}
	
	static	public class AddIpActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String objectId) throws Exception {
			UIFormInputWithActions inputWithActions = getChildById(IP_BAN_TAB);
			String[] ip = new String[]{ administrationForm.getValueIp(inputWithActions, NEW_IP_BAN_INPUT1),
																	administrationForm.getValueIp(inputWithActions, NEW_IP_BAN_INPUT2),
																	administrationForm.getValueIp(inputWithActions, NEW_IP_BAN_INPUT3),
																	administrationForm.getValueIp(inputWithActions, NEW_IP_BAN_INPUT4)
																};
			String ipAdd = administrationForm.checkIpAddress(ip);
			if(ipAdd == null){
				warning("UIForumAdministrationForm.sms.ipInvalid");
				return ;
			} 
			
			if(!administrationForm.getForumService().addBanIP(ipAdd)){
			  warning("UIForumAdministrationForm.sms.ipBanFalse", ipAdd);
				return;
			}
			refresh();
		}
	}
	
	static	public class PostsActionListener extends BaseEventListener<UIForumAdministrationForm> {
	  public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String ip) throws Exception {
	  	UIPopupContainer popupContainer = administrationForm.getAncestorOfType(UIPopupContainer.class) ;
	    UIPageListPostByIP viewPostedByUser = administrationForm.openPopup(popupContainer, UIPageListPostByIP.class, 650, 0);
			viewPostedByUser.setIp(ip);
		}
	}
	
	static	public class UnBanActionListener extends BaseEventListener<UIForumAdministrationForm> {
		public void onEvent(Event<UIForumAdministrationForm> event, UIForumAdministrationForm administrationForm, final String ip) throws Exception {
			administrationForm.getForumService().removeBan(ip) ;
			refresh();
		}
	}
}
