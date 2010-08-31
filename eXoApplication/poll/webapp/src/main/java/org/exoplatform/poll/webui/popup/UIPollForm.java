/***************************************************************************
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.poll.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIFormMultiValueInputSet;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.poll.Utils;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.impl.PollNodeTypes;
import org.exoplatform.poll.webui.BasePollForm;
import org.exoplatform.poll.webui.UIPollManagement;
import org.exoplatform.poll.webui.UIPollPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 24 June 2010, 08:00:59
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/poll/webui/popup/UIPollForm.gtmpl",
		events = {
			@EventConfig(listeners = UIPollForm.SaveActionListener.class), 
			@EventConfig(listeners = UIPollForm.RefreshActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UIPollForm.AddGroupActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UIPollForm.CancelActionListener.class,phase = Phase.DECODE)
		}
)
public class UIPollForm extends BasePollForm implements UIPopupComponent, UISelector {
	public static final String FIELD_QUESTION_INPUT = "Question" ;
	final static public String FIELD_OPTIONS = "Option" ;
	public static final String FIELD_TIMEOUT_INPUT = "TimeOut" ;
	public static final String FIELD_GROUP_PRIVATE_INPUT = "GroupPrivate" ;
	public static final String FIELD_AGAINVOTE_CHECKBOX = "VoteAgain" ;
	public static final String FIELD_MULTIVOTE_CHECKBOX = "MultiVote" ;
	public static final String FIELD_PUBLIC_DATA_CHECKBOX = "PublicData" ;
	public static final int MAX_TITLE = 100 ;
	private UIFormMultiValueInputSet uiFormMultiValue = new UIFormMultiValueInputSet(FIELD_OPTIONS,FIELD_OPTIONS) ;
	private Poll poll = new Poll() ;
	private boolean isUpdate = false ;
	private boolean isEditPath = true;
	private boolean isPublic = true;
	
	@SuppressWarnings("unchecked")
	public UIPollForm() throws Exception {
		UIFormStringInput question = new UIFormStringInput(FIELD_QUESTION_INPUT, FIELD_QUESTION_INPUT, "");
		UIFormStringInput timeOut = new UIFormStringInput(FIELD_TIMEOUT_INPUT, FIELD_TIMEOUT_INPUT, "");
		timeOut.addValidator(PositiveNumberFormatValidator.class) ;
		UIFormCheckBoxInput VoteAgain = new UIFormCheckBoxInput<Boolean>(FIELD_AGAINVOTE_CHECKBOX, FIELD_AGAINVOTE_CHECKBOX, false) ; 
		UIFormCheckBoxInput MultiVote = new UIFormCheckBoxInput<Boolean>(FIELD_MULTIVOTE_CHECKBOX, FIELD_MULTIVOTE_CHECKBOX, false) ; 
		UIFormCheckBoxInput PublicData = new UIFormCheckBoxInput<Boolean>(FIELD_PUBLIC_DATA_CHECKBOX, FIELD_PUBLIC_DATA_CHECKBOX, true) ; 
		PublicData.setChecked(isPublic);
		UIFormStringInput GroupPrivate = new UIFormStringInput(FIELD_GROUP_PRIVATE_INPUT, FIELD_GROUP_PRIVATE_INPUT, "");
		GroupPrivate.setEditable(false);
		addUIFormInput(question) ;
		addUIFormInput(timeOut) ;
		addUIFormInput(VoteAgain);
		addUIFormInput(MultiVote);
		addUIFormInput(PublicData);
		addUIFormInput(GroupPrivate);
		setDefaulFall();
		setActions(new String[]{"Save", "Refresh", "Cancel"});
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
	
	@SuppressWarnings("unused")
	private String getDateAfter() throws Exception {
		Date date = new Date();
		if(poll != null && poll.getTimeOut() > 0) {
			date = poll.getModifiedDate() ;
		}
		String format = "MM-dd-yyyy";
		return Utils.getFormatDate(format, date);
	}
	
	@SuppressWarnings("unchecked")
	public void setUpdatePoll(Poll poll, boolean isUpdate) throws Exception {
		if(isUpdate) {
			this.poll = poll ;
			getUIStringInput(FIELD_QUESTION_INPUT).setValue(Utils.unCodeHTML(poll.getQuestion())) ;
			getUIStringInput(FIELD_TIMEOUT_INPUT).setValue(String.valueOf(poll.getTimeOut())) ;
			getUIFormCheckBoxInput(FIELD_AGAINVOTE_CHECKBOX).setChecked(poll.getIsAgainVote()) ;
			UIFormCheckBoxInput multiVoteCheckInput = getUIFormCheckBoxInput(FIELD_MULTIVOTE_CHECKBOX) ;
			multiVoteCheckInput.setChecked(poll.getIsMultiCheck());
			multiVoteCheckInput.setEnable(false);
			String group = poll.getParentPath() ;
			poll.setOldParentPath(group);
			if(group.indexOf(PollNodeTypes.APPLICATION_DATA) > 0) {
				isPublic = false;
				getUIFormCheckBoxInput(FIELD_PUBLIC_DATA_CHECKBOX).setChecked(isPublic);
				group = group.substring(group.indexOf("/", 2), group.indexOf(PollNodeTypes.APPLICATION_DATA)-1);
				getUIStringInput(FIELD_GROUP_PRIVATE_INPUT).setValue(group);
			} else if(group.indexOf(PollNodeTypes.POLLS) < 0){
				isEditPath = false;
			}
			this.isUpdate = isUpdate ;
			setDefaulFall();
		}
	}
	
	private void setDefaulFall() throws Exception {
		List<String> list = new ArrayList<String>() ;
		if(isUpdate) {
			for(String string : this.poll.getOption()) {
				list.add(Utils.unCodeHTML(string));
			}
		} else {
			list.add("");
			list.add("");
		}
		this.initMultiValuesField(list);
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	public void updateSelect(String selectField, String value) throws Exception {
		UIFormStringInput GroupPrivate = getUIStringInput(selectField);
		GroupPrivate.setValue(value);
	}
	
	static	public class SaveActionListener extends EventListener<UIPollForm> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UIPollForm> event) throws Exception {
			UIPollForm uiForm = event.getSource() ;
			UIFormStringInput questionInput = uiForm.getUIStringInput(FIELD_QUESTION_INPUT) ;
			String question = questionInput.getValue() ;
			question = Utils.enCodeHTML(question);
			String timeOutStr = uiForm.getUIStringInput(FIELD_TIMEOUT_INPUT).getValue() ;
			timeOutStr = Utils.removeZeroFirstNumber(timeOutStr) ;
			long timeOut = 0;
			if(!Utils.isEmpty(timeOutStr)){
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
				if(!Utils.isEmpty(value)){
					if(value.length() > MAX_TITLE) {
						String[] args = new String[]{uiForm.getLabel(FIELD_OPTIONS)+"("+i+")", String.valueOf(MAX_TITLE)};
						uiForm.warning("NameValidator.msg.warning-long-text", args) ;
						return ;
					}
					values_.add(Utils.enCodeHTML(value));
				} 
				++i;
			}
			String[] options = values_.toArray(new String[]{}) ;
			
			int sizeOption = values_.size();
			if(sizeOption < 2) sms = "Minimum" ;
			if(sizeOption > 10) sms = "Maximum" ;
			if(Utils.isEmpty(question)) {
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
				String[] newUser = null;
				String userName = UserHelper.getCurrentUser() ;
				String[] vote = new String[sizeOption]	;
				if(uiForm.isUpdate) {
					String[] oldVote = uiForm.poll.getVote() ;
					if(sizeOption < oldVote.length) {
						List<String> voteRemoved = new ArrayList<String>() ;
						String[] oldUserVote = uiForm.poll.getUserVote() ; 
						long temps = oldUserVote.length ;
						double rmPecent = 0;
						List<Integer>listIndexItemRemoved = uiForm.uiFormMultiValue.getListIndexItemRemoved();
						for(Integer integer : listIndexItemRemoved) {
							if(integer < oldVote.length) {
								rmPecent = rmPecent + Double.parseDouble(oldVote[integer]) ;
								voteRemoved.add(String.valueOf(integer)) ;
							}
						}
						double leftPecent = 100 - rmPecent ;
						
						int t = 0;
						for(int k = 0; k < oldVote.length; ++k) {
							if(listIndexItemRemoved.contains(k)) continue;
							double newVote = Double.parseDouble(oldVote[k]) ;
							if(leftPecent > 1){
								vote[t] = String.valueOf((newVote*100)/leftPecent) ;
							} else vote[t] = "0.0";
							++t;
						}
						// single vote
						if(!uiForm.poll.getIsMultiCheck()) {
							if(leftPecent > 1) {
								List<String> userL = new ArrayList<String>();
								newUser = new String[]{} ;
								for (String string : oldUserVote) {
									boolean check = true ; 
									for(Integer j : listIndexItemRemoved) {
										if(j < oldVote.length) {
											String x = ":" + j ;
											if(string.indexOf(x) > 0) check = false ;
										}
									}
									if(check) {
										userL.add(string);
									}
								}
								newUser = userL.toArray(new String[userL.size()]);
							} else if(voteRemoved.size() > 0 && rmPecent > 0.0){
								newUser = new String[]{};
							}
							// multi vote
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
						// add new option
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
				Poll poll = uiForm.poll ;
				poll.setQuestion(question) ;
				poll.setModifiedBy(userName) ;
				poll.setModifiedDate(new Date()) ;
				poll.setIsAgainVote(isAgainVote) ;
				poll.setIsMultiCheck(isMultiVote) ;
				poll.setOption(options) ;
				poll.setVote(vote) ;
				poll.setTimeOut(timeOut) ;
				poll.setIsClosed(uiForm.poll.getIsClosed());
				try {
					if(Utils.isEmpty(poll.getParentPath()) || poll.getParentPath().contains(PollNodeTypes.POLLS) ||
							poll.getParentPath().contains(PollNodeTypes.EXO_POLLS)) {
						boolean isPublic = uiForm.getUIFormCheckBoxInput(FIELD_PUBLIC_DATA_CHECKBOX).isChecked();
						String parentPath = "";
						// if poll of topic : parentPath = topic.getPath();
						// if poll of Group : parentPath = $GROUP/${PollNodeTypes.APPLICATION_DATA}/${PollNodeTypes.EXO_POLLS}
						// if poll of public: parentPath = $PORTAL/${PollNodeTypes.POLLS}
						if(isPublic) {
							// test for public:
							parentPath = ExoContainerContext.getCurrentContainer().getContext().getName() + "/" + PollNodeTypes.POLLS;
						} else {
							parentPath = uiForm.getUIStringInput(FIELD_GROUP_PRIVATE_INPUT).getValue();
							if(parentPath.indexOf("/") == 0) parentPath = parentPath.substring(1);
							parentPath = parentPath + "/" + PollNodeTypes.APPLICATION_DATA + "/" + PollNodeTypes.EXO_POLLS;
						}
						poll.setParentPath(parentPath);
					}
					if(uiForm.isUpdate) {
						if(newUser != null){
							poll.setUserVote(newUser) ;
						}
						uiForm.getPollService().savePoll(poll, false, false) ;
					} else {
						poll.setOwner(userName) ;
						poll.setCreatedDate(new Date());
						poll.setUserVote(new String[] {}) ;
						uiForm.getPollService().savePoll(poll, true, false) ;
					}
				} catch (Exception e) {}
				uiForm.isUpdate = false ;
      	UIPollPortlet pollPortlet = uiForm.getAncestorOfType(UIPollPortlet.class) ;
				pollPortlet.cancelAction() ;
//				pollPortlet.getChild(UIPoll.class).updateFormPoll(poll) ;
				pollPortlet.getChild(UIPollManagement.class).updateGrid() ;
      	event.getRequestContext().addUIComponentToUpdateByAjax(pollPortlet);
			}
			if(!Utils.isEmpty(sms)) {
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
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
		}
	}
	
	static	public class AddGroupActionListener extends BaseEventListener<UIPollForm> {
		public void onEvent(Event<UIPollForm> event, UIPollForm uiForm, String objctId) throws Exception {
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
			popupAction.getChild(UIPopupWindow.class).setId("UIPopupChildWindow");
			UIGroupSelector uiGroupSelector = popupAction.activate(UIGroupSelector.class, 600);
			uiGroupSelector.setId("UIGroupSelector");
			uiGroupSelector.setType(UISelectComponent.TYPE_GROUP) ;
			uiGroupSelector.setSelectedGroups(null) ;
			uiGroupSelector.setComponent(uiForm, new String[]{FIELD_GROUP_PRIVATE_INPUT}) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
		}
	}

	static	public class CancelActionListener extends EventListener<UIPollForm> {
		public void execute(Event<UIPollForm> event) throws Exception {
			UIPollForm uiForm = event.getSource() ;
			UIPollPortlet pollPortlet = uiForm.getAncestorOfType(UIPollPortlet.class) ;
			pollPortlet.cancelAction() ;
			uiForm.isUpdate = false ;
		}
	}
}