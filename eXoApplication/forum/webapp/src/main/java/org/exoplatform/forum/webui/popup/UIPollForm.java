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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.UIForumPollPortlet;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIFormMultiValueInputSet;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.PollService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
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
public class UIPollForm extends BaseForumForm implements UIPopupComponent {
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
		setDefaulFall();
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
		Date date = new Date();
		if(poll != null && poll.getTimeOut() > 0) {
			date = poll.getModifiedDate() ;
		}
		String format = "MM-dd-yyyy";
		try {
	    format = this.getAncestorOfType(UIForumPortlet.class).getUserProfile().getShortDateFormat();
    } catch (NullPointerException e) {
	    format = getForumService().getDefaultUserProfile(UserHelper.getCurrentUser(), null).getShortDateFormat();
    } catch (Exception e) {}
		return ForumUtils.getFormatDate(format, date);
	}
	
	@SuppressWarnings("unchecked")
	public void setUpdatePoll(Poll poll, boolean isUpdate) throws Exception {
		if(isUpdate) {
			this.poll = poll ;
			getUIStringInput(FIELD_QUESTION_INPUT).setValue(ForumTransformHTML.unCodeHTML(poll.getQuestion())) ;
			getUIStringInput(FIELD_TIMEOUT_INPUT).setValue(String.valueOf(poll.getTimeOut())) ;
			getUIFormCheckBoxInput(FIELD_AGAINVOTE_CHECKBOX).setChecked(poll.getIsAgainVote()) ;
			UIFormCheckBoxInput multiVoteCheckInput = getUIFormCheckBoxInput(FIELD_MULTIVOTE_CHECKBOX) ;
			multiVoteCheckInput.setChecked(poll.getIsMultiCheck());
			multiVoteCheckInput.setEnable(false);
			this.isUpdate = isUpdate ;
			setDefaulFall();
		}
	}
	
	private void setDefaulFall() throws Exception {
		List<String> list = new ArrayList<String>() ;
		if(isUpdate) {
			for(String string : this.poll.getOption()) {
				list.add(ForumTransformHTML.unCodeHTML(string));
			}
		} else {
			list.add("");
			list.add("");
		}
		this.initMultiValuesField(list);
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	static	public class SaveActionListener extends EventListener<UIPollForm> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UIPollForm> event) throws Exception {
			UIPollForm uiForm = event.getSource() ;
			UIFormStringInput questionInput = uiForm.getUIStringInput(FIELD_QUESTION_INPUT) ;
			String question = questionInput.getValue() ;
			question = ForumTransformHTML.enCodeHTML(question);
			String timeOutStr = uiForm.getUIStringInput(FIELD_TIMEOUT_INPUT).getValue() ;
			timeOutStr = ForumUtils.removeZeroFirstNumber(timeOutStr) ;
			long timeOut = 0;
			if(!ForumUtils.isEmpty(timeOutStr)){
				if(timeOutStr.length() > 4){
					uiForm.warning("UIPollForm.msg.longTimeOut", new String[]{uiForm.getLabel(FIELD_TIMEOUT_INPUT)}) ;
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
						String[] args = new String[]{uiForm.getLabel(FIELD_OPTIONS)+"("+i+")", String.valueOf(MAX_TITLE)};
						uiForm.warning("NameValidator.msg.warning-long-text", args) ;
						return ;
					}
					values_.add(ForumTransformHTML.enCodeHTML(value));
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
					String[] args = { uiForm.getLabel(FIELD_QUESTION_INPUT), String.valueOf(MAX_TITLE) };
					uiForm.warning("NameValidator.msg.warning-long-text", args) ;
					return ;
				}
			}
			if(sizeOption >= 2 && sizeOption <= 10) {
				String[] newUser = new String[] {};
				String userName = UserHelper.getCurrentUser() ;
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
							
							
							for(String uv : oldUserVote) {
							  StringBuffer sbUserInfo = new StringBuffer();
								for(String string : uv.split(":")) {
									if(!voteRemoved.contains(string)) {
										if(sbUserInfo.length() > 0) sbUserInfo.append(":");
										sbUserInfo.append(string) ;
									}
								}
								String userInfo = sbUserInfo.toString() ;
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
				uiForm.poll.setOwner(userName) ;
				uiForm.poll.setQuestion(question) ;
				uiForm.poll.setModifiedBy(userName) ;
				uiForm.poll.setIsAgainVote(isAgainVote) ;
				uiForm.poll.setIsMultiCheck(isMultiVote) ;
				uiForm.poll.setOption(options) ;
				uiForm.poll.setVote(vote) ;
				uiForm.poll.setTimeOut(timeOut) ;
				uiForm.poll.setUserVote(new String[] {}) ;
				uiForm.poll.setIsClosed(uiForm.poll.getIsClosed());
				String[] id = uiForm.TopicPath.trim().split("/") ;
				try {
					PollService pollSv = (PollService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
					if(uiForm.isUpdate) {
						if(newUser.length > 0)uiForm.poll.setUserVote(newUser) ;
						pollSv.savePoll(uiForm.poll, false, false);
					} else {
						uiForm.poll.setId(id[id.length - 1].replace(Utils.TOPIC, Utils.POLL));
						uiForm.poll.setParentPath(uiForm.TopicPath.trim());
						pollSv.savePoll(uiForm.poll, true, false);
					}
				} catch (Exception e) {}
				uiForm.isUpdate = false ;
				try {
					UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
					forumPortlet.cancelAction() ;
					UITopicDetailContainer detailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class) ;
					detailContainer.setRederPoll(true) ;
					detailContainer.getChild(UITopicPoll.class).updateFormPoll(id[id.length - 3], id[id.length - 2], id[id.length - 1]) ;
					detailContainer.getChild(UITopicDetail.class).hasPoll(true);
					event.getRequestContext().addUIComponentToUpdateByAjax(detailContainer);
        } catch (Exception e) {
        	UIForumPollPortlet forumPollPortlet = uiForm.getAncestorOfType(UIForumPollPortlet.class) ;
					forumPollPortlet.cancelAction() ;
					forumPollPortlet.getChild(UITopicPoll.class).updateFormPoll(id[id.length - 3], id[id.length - 2], id[id.length - 1]);
        	event.getRequestContext().addUIComponentToUpdateByAjax(forumPollPortlet);
        }
			}
			if(!ForumUtils.isEmpty(sms)) {
				uiForm.warning("UIPollForm.msg." + sms) ;
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
			try {
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.cancelAction() ;
      } catch (Exception e) {
      	UIForumPollPortlet forumPollPortlet = uiForm.getAncestorOfType(UIForumPollPortlet.class) ;
				forumPollPortlet.cancelAction() ;
      }
			uiForm.isUpdate = false ;
		}
	}
}