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
package org.exoplatform.poll.webui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.portlet.PortletPreferences;

import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.poll.Utils;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.PollService;
import org.exoplatform.poll.webui.popup.UIPollForm;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormRadioBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * June 26, 2010 9:48:18 AM 
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/poll/webui/UIPoll.gtmpl", 
		events = {
			@EventConfig(listeners = UIPoll.VoteActionListener.class),	
			@EventConfig(listeners = UIPoll.EditPollActionListener.class) ,
			@EventConfig(listeners = UIPoll.RemovePollActionListener.class, confirm="UITopicPoll.msg.confirm-RemovePoll"),
			@EventConfig(listeners = UIPoll.ClosedPollActionListener.class),
			@EventConfig(listeners = UIPoll.VoteAgainPollActionListener.class)
		}
)
public class UIPoll extends BasePollForm	{
	private Poll poll_ ;
	private String pollId , userId;
	private boolean isAgainVote = false ;
	private boolean isEditPoll = false ;
	private boolean canViewEditMenu = false ;
	
	public UIPoll() throws Exception {
		userId = UserHelper.getCurrentUser() ;
	}

	public void setPollId() throws Exception {
		if(Utils.isEmpty(pollId)) {
				PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
		    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
		    pollId = portletPref.getValue("pollIdShow", "");
		    if(Utils.isEmpty(pollId)) {
		    	List<String> list = getPollService().getListPollId();
		    	if(!list.isEmpty()){
		    		pollId = list.get(0);
		    	}
		    }
				this.isEditPoll = true ;
		}
	}
	
	public void updateFormPoll(Poll poll) throws Exception {
		poll_ = poll;
		this.isEditPoll = false ;
	}

	public void updatePollById(String pollId) throws Exception {
		this.pollId = pollId;
		this.isEditPoll = true ;
	}
	
	@SuppressWarnings("unchecked")
	private void init() throws Exception {
		if(this.hasChildren()){
			this.removeChild(UIFormRadioBoxInput.class) ;
			int i=0;
			while(i<10) {
				if(this.hasChildren())this.removeChild(UIForumCheckBoxInput.class) ;
				else break ; ++i;
			}
		}
		if(poll_ != null) {
			if(!poll_.getIsMultiCheck()) {
				List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
				for (String s : poll_.getOption()) {
					options.add( new SelectItemOption<String>(s, s) ) ;
				}
				UIFormRadioBoxInput input = new UIFormRadioBoxInput("vote", "vote", options);
				input.setAlign(1) ;
				addUIFormInput(input);
			} else {
				for(String s : poll_.getOption()) {
					addUIFormInput(new UIForumCheckBoxInput(s,s, false) );
				}
			}
		}
	}
	
	private Poll getPoll() throws Exception {
		setPollId();
		if(isEditPoll && pollId != null && pollId.length() > 0) {
			poll_ = getPollService().getPoll(pollId) ; 
		}
		this.init() ;
		return poll_ ;
	}
	
	@SuppressWarnings("unused")
	private boolean getIsEditPoll() {
		return isEditPoll ;
	}
	
	public void setEditPoll(boolean isEditPoll) {
	  this.isEditPoll = isEditPoll;
  }
	
	@SuppressWarnings("unused")
	private boolean getCanViewEditMenu(){
		return this.canViewEditMenu ;
	}
	
	@SuppressWarnings("unused")
	private boolean isGuestPermission() throws Exception {
		if(poll_.getIsClosed()) return true ;
		if(Utils.isEmpty(userId)) return true ;
		if(poll_.getTimeOut() > 0) {
			Date today = new Date() ;
			if((today.getTime() - this.poll_.getCreatedDate().getTime()) >= poll_.getTimeOut()*86400000) return true ;
		}
		if(this.isAgainVote) {
			return false ;
		}
		String[] userVotes = poll_.getUserVote() ;
		for (String string : userVotes) {
			string = string.substring(0, string.indexOf(":")) ;
			if(string.equalsIgnoreCase(userId)) return true ;
		}
		return false ;
	}
	
	@SuppressWarnings("unused")
	private String[] getInfoVote() throws Exception {
		Poll poll = poll_ ;
		String[] voteNumber = poll.getVote() ;
		String[] userVotes = poll.getUserVote();
		long size = 0 , temp = 1;
		if(!poll.getIsMultiCheck()) {
			size = userVotes.length	;
		} else {
			for (int i = 0; i < userVotes.length; i++) {
				size += userVotes[i].split(":").length -1 ;
      }
		}
		temp = size;
		if(size == 0) size = 1;
		int l = voteNumber.length;
		String[] infoVote = new String[(l + 1)] ;
		for (int j = 0; j < l; j++) {
			String string = voteNumber[j];
			double tmp = Double.parseDouble(string) ;
			double k = (tmp*size)/100 ;
			int t = (int)Math.round(k) ;
			string = "" + (double) t*100/size ;
			infoVote[j] = string + ":" + t ;
    }
		infoVote[l] = "" + temp ;
		if(poll.getIsMultiCheck()) {
			infoVote[l] = String.valueOf(userVotes.length) ;
		}
		return infoVote ;
	}
	
	static public class VoteActionListener extends EventListener<UIPoll> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UIPoll> event) throws Exception {
			UIPoll topicPoll = event.getSource() ;
			Poll poll = topicPoll.poll_ ; 
			String[] votes ;
			String[] setUserVote ;
			String userVote = topicPoll.userId ;
			List<UIComponent> children = topicPoll.getChildren() ;
			
			//User vote and vote number
			String[] temporary = topicPoll.poll_.getUserVote() ;
			int size = 0 ;
			if(temporary != null && temporary.length > 0) {
				size = temporary.length ;
			}
			
			if(!poll.getIsMultiCheck()) {
				UIFormRadioBoxInput radioInput = null ;
				for(UIComponent child : children) {
					if(child instanceof UIFormRadioBoxInput) {
						radioInput = (UIFormRadioBoxInput) child ;
					}
				}
				if(radioInput.getValue().equalsIgnoreCase("vote")) {
					topicPoll.warning("UITopicPoll.msg.notCheck") ;
				} else {
					// order number
					List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
					options = radioInput.getOptions() ;
					int i = 0, j = 0 ;
					for (SelectItemOption<String> option : options) {
						if(option.getValue().equalsIgnoreCase(radioInput.getValue())){ j = i ; break ;}
						i = i + 1;
					}
					int index = 0 ;
					if(topicPoll.isAgainVote) {
						setUserVote = new String[size] ;
						for (int t = 0; t < size; t++) {
							String string = temporary[t].substring(0, temporary[t].length() - 2) ;
							if(string.equalsIgnoreCase(userVote)) {
								setUserVote[t] = userVote + ":" + j;
								index = t;
							} else {
								setUserVote[t] = temporary[t];
							}
						}
					} else {
						setUserVote = new String[(size+1)] ;
						for (int t = 0; t < size; t++) {
							setUserVote[t] = temporary[t];
						}
						setUserVote[size] = userVote + ":" + j;
						size = size + 1 ;
					}
					votes = topicPoll.poll_.getVote() ;
					double onePercent = (double)100/size;
					if(topicPoll.isAgainVote) {
						char tmp = temporary[index].charAt((temporary[index].length() - 1));
						int k = Integer.valueOf(tmp) - 48;
						if( k < votes.length) votes[k] = String.valueOf((Double.parseDouble(votes[k]) - onePercent)) ;
						votes[j] = String.valueOf((Double.parseDouble(votes[j]) + onePercent)) ;
					} else {
						i = 0;
						for(String vote : votes) {
							double a	= Double.parseDouble(vote) ;
							if(i == j) votes[i] = "" + ((a - a/size)+ onePercent) ;
							else votes[i] = "" + (a - a/size) ;
							i = i + 1;
						}
					}
					//save Poll
					poll.setVote(votes) ;
					poll.setUserVote(setUserVote) ;
				}
			// multichoice when vote 
			} else {
				UIForumCheckBoxInput forumCheckBox = null ;
				List<String> listValue = new ArrayList<String>() ;
				for(UIComponent child : children) {
					if(child instanceof UIForumCheckBoxInput){
						forumCheckBox = ((UIForumCheckBoxInput)child) ;
						if(forumCheckBox.isChecked()) {
							listValue.add(forumCheckBox.getName()) ;
						}
					}
				}
				if(!listValue.isEmpty()) {
					votes = topicPoll.poll_.getVote() ;
					double totalVote = 0 ;
					double doubleVote[] = new double[votes.length] ;
					String[] listUserVoted = topicPoll.poll_.getUserVote() ;
					if(listUserVoted.length > 0) {
						for(String us : listUserVoted) {
							totalVote += us.split(":").length - 1 ;
						}
					}
					int i = 0 ;
					int pos = 0 ;
					if( votes!= null && votes.length > 0) {
						for(String v : votes) {
							doubleVote[i++] = Double.parseDouble(v) ;
						}
					}
					if(totalVote > 0) {
						for( i = 0 ; i < doubleVote.length ; i ++) {
							doubleVote[i] = (doubleVote[i]*totalVote)/100 ;
						}
					}
					if(!topicPoll.isAgainVote) {
						i = 0 ;
						pos = size ;
						setUserVote = new String[size	+ 1] ;
						for(String userHaveVoted : poll.getUserVote())
							setUserVote[i++] = userHaveVoted ;
						setUserVote[i] = userVote ;
					} else {
						setUserVote = poll.getUserVote() ;
						for( i = 0 ; i < setUserVote.length ; i ++) {
							if(setUserVote[i].split(":")[0].equals(userVote)) {
								pos = i ;
								break ;
							}
						}
						String[] posHaveVoted = (setUserVote[pos].substring(setUserVote[pos].indexOf(":"))).split(":") ;
						setUserVote[pos] = setUserVote[pos].substring(0, setUserVote[pos].indexOf(":")) ;
						for(String posVoted : posHaveVoted) {
							if(!Utils.isEmpty(posVoted)) {
								doubleVote[Integer.parseInt(posVoted)] -= 1 ;
								totalVote -= 1 ;
							}
						}
					}
					i = 0 ;
					for(String option : topicPoll.poll_.getOption()) {
						if(listValue.contains(option)) {
							doubleVote[i] += 1 ;
							totalVote += 1 ;
							setUserVote[pos] += ":" + i ;
						}
						i ++ ;
					}
					i = 0 ;
					for(double dv : doubleVote) {
						if(totalVote > 0)
							votes[i] = ((dv/totalVote)*100) + "" ;
						else
							votes[i] = "0" ;
						i ++ ;
					}
					// save votes:
					poll.setUserVote(setUserVote) ;
					poll.setVote(votes) ;
				} else {
					topicPoll.warning("UITopicPoll.msg.notCheck") ;
				}
			}
			topicPoll.getPollService().savePoll(poll, false, true) ;
			topicPoll.isAgainVote = false ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll.getParent()) ;
		}
	}
	
	static public class EditPollActionListener extends BaseEventListener<UIPoll> {
		public void onEvent(Event<UIPoll> event, UIPoll topicPoll, final String objectId) throws Exception {
			UIPopupAction popupAction ;
			try {
				UIPollPortlet forumPortlet = topicPoll.getAncestorOfType(UIPollPortlet.class) ;
				popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      } catch (Exception e) {
      	UIPollPortlet forumPollPortlet = topicPoll.getAncestorOfType(UIPollPortlet.class) ;
      	popupAction = forumPollPortlet.getChild(UIPopupAction.class) ;
      }
			UIPollForm	pollForm = popupAction.createUIComponent(UIPollForm.class, null, null) ;
			topicPoll.poll_ = topicPoll.getPoll();
			pollForm.setUpdatePoll(topicPoll.poll_, true) ;
			popupAction.activate(pollForm, 662, 466) ;
			topicPoll.isEditPoll = true ;
		}
	}

	static public class RemovePollActionListener extends EventListener<UIPoll> {
		public void execute(Event<UIPoll> event) throws Exception {
			UIPoll topicPoll = event.getSource() ;
			try {
				topicPoll.getPollService().removePoll(topicPoll.pollId) ;
      } catch (Exception e) {
      }
			if(topicPoll.poll_.getIsMultiCheck()) {
				List<UIComponent> children = topicPoll.getChildren() ;
				for (int i = 0; i < children.size(); i++) {
					topicPoll.removeChild(UIForumCheckBoxInput.class) ;
				}
			} else {
				topicPoll.removeChild(UIFormRadioBoxInput.class) ;
			}
    	UIPollPortlet pollPortlet = topicPoll.getAncestorOfType(UIPollPortlet.class) ;
    	topicPoll.setRendered(false);
    	event.getRequestContext().addUIComponentToUpdateByAjax(pollPortlet) ;
		}
	}

	static public class VoteAgainPollActionListener extends EventListener<UIPoll> {
		public void execute(Event<UIPoll> event) throws Exception {
			UIPoll topicPoll = event.getSource() ;
			topicPoll.isAgainVote = true ;
			topicPoll.poll_= topicPoll.getPoll();
			topicPoll.init() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll) ;
		}
	}

	static public class ClosedPollActionListener extends BaseEventListener<UIPoll> {
		public void onEvent(Event<UIPoll> event, UIPoll topicPoll, final String id) throws Exception {
			if(id.equals("true")) {
				topicPoll.poll_.setIsClosed(false) ;
				topicPoll.poll_.setTimeOut(0);
			} else {
				topicPoll.poll_.setIsClosed(!topicPoll.poll_.getIsClosed()) ;
			}
			try {
				topicPoll.getPollService().setClosedPoll(topicPoll.poll_) ;
      } catch (Exception e) {
      }
			topicPoll.isAgainVote = false ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll) ;
		}
	}
}
