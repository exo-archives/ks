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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
			@EventConfig(listeners = UIPollForm.RefreshActionListener.class),
			@EventConfig(listeners = UIPollForm.CancelActionListener.class,phase = Phase.DECODE)
		}
)
public class UIPollForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_QUESTION_INPUT = "Question" ;
	final static public String FIELD_OPTIONS = "Option" ;
	public static final String FIELD_TIMEOUT_INPUT = "TimeOut" ;
	public static final String FIELD_AGAINVOTE_CHECKBOX = "VoteAgain" ;
	public static final String FIELD_MULTIVOTE_CHECKBOX = "MultiVote" ;
	private UIFormMultiValueInputSet uiFormMultiValue = new UIFormMultiValueInputSet(FIELD_OPTIONS,FIELD_OPTIONS) ;
	private String TopicPath ;
	private Poll poll ;
	private boolean isUpdate = false ;
	
	@SuppressWarnings("unchecked")
	public UIPollForm() throws Exception {
		UIFormStringInput question = new UIFormStringInput(FIELD_QUESTION_INPUT, FIELD_QUESTION_INPUT, null);
		UIFormStringInput timeOut = new UIFormStringInput(FIELD_TIMEOUT_INPUT, FIELD_TIMEOUT_INPUT, null);
		UIFormCheckBoxInput VoteAgain = new UIFormCheckBoxInput<Boolean>(FIELD_AGAINVOTE_CHECKBOX, FIELD_AGAINVOTE_CHECKBOX, false) ; 
		UIFormCheckBoxInput MultiVote = new UIFormCheckBoxInput<Boolean>(FIELD_MULTIVOTE_CHECKBOX, FIELD_MULTIVOTE_CHECKBOX, false) ; 
		timeOut.addValidator(PositiveNumberFormatValidator.class) ;
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
	
	public void setUpdatePoll(Poll poll, boolean isUpdate) throws Exception {
		if(isUpdate) {
			this.poll = poll ;
			getUIStringInput(FIELD_QUESTION_INPUT).setValue(poll.getQuestion()) ;
			getUIStringInput(FIELD_TIMEOUT_INPUT).setValue(String.valueOf(poll.getTimeOut())) ;
			getUIFormCheckBoxInput(FIELD_AGAINVOTE_CHECKBOX).setChecked(poll.getIsAgainVote()) ;
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
			long timeOut = 0;
			if(timeOutStr != null && timeOutStr.length() > 0) timeOut = Long.parseLong(timeOutStr) ; 
			boolean isAgainVote = uiForm.getUIFormCheckBoxInput(FIELD_AGAINVOTE_CHECKBOX).isChecked() ;
			String sms = "";
			int i = 0 ; 
			List<String> values = (List<String>) uiForm.uiFormMultiValue.getValue();
			String temp = "" ;
			String[] options = new String[values.size()] ;	
			if(values.size() > 0) {
				for(String value : values) {
					temp = value ;
					if(temp != null && temp.length() > 0){
						options[i] = temp;
					} 
					++i;
				}
			}
			int sizeOption = options.length;
			if(sizeOption < 2) sms = "Minimum" ;
			if(sizeOption > 10) sms = "Maximum" ;
			if(question == null || question.length() == 0) {
				sms = "NotQuestion";
				sizeOption = 0;
			}
			if(sizeOption >= 2 && sizeOption <= 10) {
				String[] newUser = new String[] {};
				String userName = ForumSessionUtils.getCurrentUser() ;
				String[] vote = new String[sizeOption]	;
				if(uiForm.isUpdate) {
					String[] oldVote = uiForm.poll.getVote() ;
					if(sizeOption < oldVote.length) {
						String[] oldUserVote = uiForm.poll.getUserVote() ; 
						long temps = oldUserVote.length ;
						double rmPecent = 0;
						for(int j = sizeOption; j < oldVote.length; j++) {
							rmPecent = rmPecent + Double.parseDouble(oldVote[j]) ;
						}
						rmPecent = 100 - rmPecent ;
						for(int k = 0; k < sizeOption; ++k) {
							double newVote = Double.parseDouble(oldVote[k]) ;
							vote[k] = String.valueOf((newVote*100)/rmPecent) ;
						}
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
				poll.setOption(options) ;
				poll.setVote(vote) ;
				poll.setTimeOut(timeOut) ;
				poll.setUserVote(new String[] {}) ;
				poll.setIsClosed(false);
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
			if(sms != null && sms.length() > 0) {
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