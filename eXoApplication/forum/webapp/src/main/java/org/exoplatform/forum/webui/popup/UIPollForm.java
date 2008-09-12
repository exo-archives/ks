/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIPollForm.gtmpl",
		events = {
			@EventConfig(listeners = UIPollForm.SaveActionListener.class), 
			@EventConfig(listeners = UIPollForm.RefreshActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UIPollForm.CancelActionListener.class,phase = Phase.DECODE)
		}
)
public class UIPollForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_QUESTION_INPUT = "Question" ;
	final static public String FIELD_OPTIONS = "Option" ;
	public static final String FIELD_TIMEOUT_INPUT = "TimeOut" ;
	public static final String FIELD_AGAINVOTE_CHECKBOX = "VoteAgain" ;
	public static final String FIELD_MULTIVOTE_CHECKBOX = "MultiVote" ;
	public static final int MAX_TITLE = 100 ;
	private UIFormMultiValueInputSet uiFormMultiValue = new UIFormMultiValueInputSet(FIELD_OPTIONS,FIELD_OPTIONS) ;
	private String TopicPath ;
	private Poll poll = new Poll() ;
	private boolean isUpdate = false ;
	
	@SuppressWarnings("unchecked")
	public UIPollForm() throws Exception {
		UIFormStringInput question = new UIFormStringInput(FIELD_QUESTION_INPUT, FIELD_QUESTION_INPUT, null);
		UIFormStringInput timeOut = new UIFormStringInput(FIELD_TIMEOUT_INPUT, FIELD_TIMEOUT_INPUT, null);
		timeOut.addValidator(PositiveNumberFormatValidator.class) ;
		UIFormCheckBoxInput VoteAgain = new UIFormCheckBoxInput<Boolean>(FIELD_AGAINVOTE_CHECKBOX, FIELD_AGAINVOTE_CHECKBOX, false) ; 
		UIFormCheckBoxInput MultiVote = new UIFormCheckBoxInput<Boolean>(FIELD_MULTIVOTE_CHECKBOX, FIELD_MULTIVOTE_CHECKBOX, false) ; 
		addUIFormInput(question) ;
		addUIFormInput(timeOut) ;
		addUIFormInput(VoteAgain);
		addUIFormInput(MultiVote);
	}

	private void initMultiValuesField(List<String> list) throws Exception {
		if( uiFormMultiValue != null ) removeChildById(FIELD_OPTIONS);
		uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
		uiFormMultiValue.setId(FIELD_OPTIONS) ;
		uiFormMultiValue.setName(FIELD_OPTIONS) ;
		uiFormMultiValue.setType(UIFormStringInput.class) ;
		uiFormMultiValue.setValue(list) ;
		addUIFormInput(uiFormMultiValue) ;
	}
	
	public void setTopicPath( String topicPath) {
		this.TopicPath = topicPath ;
	}
	

	@SuppressWarnings("unused")
	private String getDateAfter() throws Exception {
		String date = ForumUtils.getFormatDate("MM-dd-yyyy", new Date()) ;;
		if(poll != null && poll.getTimeOut() > 0) {
			date = ForumUtils.getFormatDate("MM-dd-yyyy", poll.getModifiedDate()) ;
		}
		return date;
	}
	
	@SuppressWarnings("unchecked")
	public void setUpdatePoll(Poll poll, boolean isUpdate) throws Exception {
		if(isUpdate) {
			this.poll = poll ;
			getUIStringInput(FIELD_QUESTION_INPUT).setValue(poll.getQuestion()) ;
			getUIStringInput(FIELD_TIMEOUT_INPUT).setValue(String.valueOf(poll.getTimeOut())) ;
			getUIFormCheckBoxInput(FIELD_AGAINVOTE_CHECKBOX).setChecked(poll.getIsAgainVote()) ;
			UIFormCheckBoxInput multiVoteCheckInput = getUIFormCheckBoxInput(FIELD_MULTIVOTE_CHECKBOX) ;
			multiVoteCheckInput.setChecked(poll.getIsMultiCheck());
			multiVoteCheckInput.setEnable(false);
			this.isUpdate = isUpdate ;
		}
	}
	
	
	public void activate() throws Exception {
		List<String> list = new ArrayList<String>() ;
		if(isUpdate) {
			for(String string : this.poll.getOption()) {
				list.add(string);
			}
		} else {
			list.add("");
			list.add("");
		}
		this.initMultiValuesField(list);
	}
	
	public void deActivate() throws Exception {
	}
	
	static	public class SaveActionListener extends EventListener<UIPollForm> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UIPollForm> event) throws Exception {
			UIPollForm uiForm = event.getSource() ;
			UIFormStringInput questionInput = uiForm.getUIStringInput(FIELD_QUESTION_INPUT) ;
			String question = questionInput.getValue() ;
			String timeOutStr = uiForm.getUIStringInput(FIELD_TIMEOUT_INPUT).getValue() ;
			timeOutStr = ForumUtils.removeZeroFirstNumber(timeOutStr) ;
			long timeOut = 0;
			if(!ForumUtils.isEmpty(timeOutStr)){
				if(timeOutStr.length() > 4){
					Object[] args = {uiForm.getLabel(FIELD_TIMEOUT_INPUT) };
					throw new MessageException(new ApplicationMessage("UIPollForm.msg.longTimeOut", args, ApplicationMessage.WARNING)) ;
				}
				timeOut = Long.parseLong(timeOutStr) ; 
			}
			boolean isAgainVote = uiForm.getUIFormCheckBoxInput(FIELD_AGAINVOTE_CHECKBOX).isChecked() ;
			boolean isMultiVote = uiForm.getUIFormCheckBoxInput(FIELD_MULTIVOTE_CHECKBOX).isChecked() ;
			String sms = "";
			List<String> values = (List<String>) uiForm.uiFormMultiValue.getValue();
			List<String> values_ = new ArrayList<String>();
			int i = 1;
			for(String value : values) {
				if(!ForumUtils.isEmpty(value)){
					if(value.length() > MAX_TITLE) {
						UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
						Object[] args = { uiForm.getLabel(FIELD_OPTIONS)+"("+i+")", String.valueOf(MAX_TITLE) };
						uiApp.addMessage(new ApplicationMessage("NameValidator.msg.warning-long-text", args, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
						return ;
					}
					values_.add(value);
				} 
				++i;
			}
			String[] options = values_.toArray(new String[]{}) ;
			
			int sizeOption = values_.size();
			if(sizeOption < 2) sms = "Minimum" ;
			if(sizeOption > 10) sms = "Maximum" ;
			if(ForumUtils.isEmpty(question)) {
				sms = "NotQuestion";
				sizeOption = 0;
			}else {
				if(question.length() > MAX_TITLE) {
					UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
					Object[] args = { uiForm.getLabel(FIELD_QUESTION_INPUT), String.valueOf(MAX_TITLE) };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.warning-long-text", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					return ;
				}
			}
			if(sizeOption >= 2 && sizeOption <= 10) {
				String[] newUser = new String[] {};
				String userName = ForumSessionUtils.getCurrentUser() ;
				String[] vote = new String[sizeOption]	;
				if(uiForm.isUpdate) {
					String[] oldVote = uiForm.poll.getVote() ;
					if(sizeOption < oldVote.length) {
						List<String> voteRemoved = new ArrayList<String>() ;
						String[] oldUserVote = uiForm.poll.getUserVote() ; 
						long temps = oldUserVote.length ;
						double rmPecent = 0;
						for(int j = sizeOption; j < oldVote.length; j++) {
							rmPecent = rmPecent + Double.parseDouble(oldVote[j]) ;
							voteRemoved.add(String.valueOf(j)) ;
						}
						rmPecent = 100 - rmPecent ;
						for(int k = 0; k < sizeOption; ++k) {
							double newVote = Double.parseDouble(oldVote[k]) ;
							vote[k] = String.valueOf((newVote*100)/rmPecent) ;
						}
						if(!uiForm.poll.getIsMultiCheck()) {
							int newSize	= (int) Math.round((temps*rmPecent)/100) ;
							newUser = new String[newSize] ;
							int l = 0 ;
							for (String string : oldUserVote) {
								boolean check = true ; 
								for(int j = sizeOption; j < oldVote.length; j++) {
									String x = ":" + j ;
									if(string.indexOf(x) > 0) check = false ;
								}
								if(check) {newUser[l] = string ; 
								++l ;}
							}
						} else {
							List<String> newUserVote = new ArrayList<String>() ;
							String userInfo = "" ;
							for(String uv : oldUserVote) {
								userInfo = "" ;
								for(String string : uv.split(":")) {
									if(!voteRemoved.contains(string)) {
										if(userInfo.length() > 0) userInfo += ":" ;
										userInfo += string ;
									}
								}
								if(userInfo.split(":").length >= 2)
									newUserVote.add(userInfo) ;
								newUser = newUserVote.toArray(new String[]{}) ;
							}
						}
					} else {
						for(int j = 0; j < sizeOption; j++) {
							if( j < oldVote.length) {
								vote[j] = oldVote[j];
							} else {
								vote[j] = "0";
							}
						}
					}
				} else {
					for (int j = 0; j < sizeOption; j++) {
						vote[j] = "0";
					}
				}
				Poll poll = new Poll() ;
				poll.setOwner(userName) ;
				poll.setQuestion(question) ;
				poll.setCreatedDate(new Date());
				poll.setModifiedBy(userName) ;
				poll.setModifiedDate(new Date()) ;
				poll.setIsAgainVote(isAgainVote) ;
				poll.setIsMultiCheck(isMultiVote) ;
				poll.setOption(options) ;
				poll.setVote(vote) ;
				poll.setTimeOut(timeOut) ;
				poll.setUserVote(new String[] {}) ;
				poll.setIsClosed(uiForm.poll.getIsClosed());
				String[] id = uiForm.TopicPath.trim().split("/") ;
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				if(uiForm.isUpdate) {
					poll.setId(uiForm.getId()) ;
					if(newUser.length > 0) poll.setUserVote(newUser) ;
					forumService.savePoll(ForumSessionUtils.getSystemProvider(), id[id.length - 3], id[id.length - 2], id[id.length - 1], poll, false, false) ;
				} else {
					forumService.savePoll(ForumSessionUtils.getSystemProvider(), id[id.length - 3], id[id.length - 2], id[id.length - 1], poll, true, false) ;
				}
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.cancelAction() ;
				uiForm.isUpdate = false ;
				UITopicDetailContainer detailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class) ;
				detailContainer.setRederPoll(true) ;
				detailContainer.getChild(UITopicPoll.class).updateFormPoll(id[id.length - 3], id[id.length - 2], id[id.length - 1]) ;
				detailContainer.getChild(UITopicDetail.class).setIsEditTopic(true) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(detailContainer);
			}
			if(!ForumUtils.isEmpty(sms)) {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UIPollForm.msg." + sms, args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static	public class RefreshActionListener extends EventListener<UIPollForm> {
		public void execute(Event<UIPollForm> event) throws Exception {
			UIPollForm uiForm = event.getSource() ;
			List<String> list = new ArrayList<String>() ;
			list.add("");
			list.add("");
			uiForm.initMultiValuesField(list);
			uiForm.getUIStringInput(FIELD_QUESTION_INPUT).setValue("") ;
			uiForm.getUIStringInput(FIELD_TIMEOUT_INPUT).setValue("0") ;
			uiForm.getUIFormCheckBoxInput(FIELD_AGAINVOTE_CHECKBOX).setChecked(false) ;
			uiForm.getUIFormCheckBoxInput(FIELD_MULTIVOTE_CHECKBOX).setChecked(false) ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIPollForm> {
		public void execute(Event<UIPollForm> event) throws Exception {
			UIPollForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
			uiForm.isUpdate = false ;
		}
	}
}