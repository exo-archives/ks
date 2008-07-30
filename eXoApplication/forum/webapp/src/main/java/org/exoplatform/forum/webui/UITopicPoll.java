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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.popup.UIPollForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormRadioBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * Octo 26, 2007 9:48:18 AM 
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/forum/webui/UITopicPoll.gtmpl", 
		events = {
			@EventConfig(listeners = UITopicPoll.VoteActionListener.class),	
			@EventConfig(listeners = UITopicPoll.EditPollActionListener.class) ,
			@EventConfig(listeners = UITopicPoll.RemovePollActionListener.class, confirm="UITopicPoll.msg.confirm-RemovePoll"),
			@EventConfig(listeners = UITopicPoll.ClosedPollActionListener.class),
			@EventConfig(listeners = UITopicPoll.VoteAgainPollActionListener.class)
		}
)
public class UITopicPoll extends UIForm	{
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private Poll poll_ ;
	private String categoryId, forumId, topicId ;
	private boolean isAgainVote = false ;
	private boolean isEditPoll = false ;
  private boolean canViewEditMenu = false ;
	private Topic topic ;
	private Forum forum ;
	
	public UITopicPoll() throws Exception {
	}

	@SuppressWarnings("unused")
  private UserProfile getOption() {
		return this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
	
	public void setForum(Forum forum) {
	  this.forum = forum ;
  }
	public void updatePoll(String categoryId, String forumId, Topic topic) throws Exception {
		this.categoryId = categoryId; 
		this.forumId = forumId; 
		this.topicId = topic.getId();
		this.topic = topic ;
	}

	public void updateFormPoll(String categoryId, String forumId, String topicId) throws Exception {
		this.categoryId = categoryId; 
		this.forumId = forumId; 
		this.topicId = topicId;
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
	
	@SuppressWarnings("unused")
	private Poll getPoll() throws Exception {
		if(!ForumUtils.isEmpty(categoryId)) {
			if(this.isEditPoll) {
				this.topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topicId, "guest") ;
			}
			UserProfile userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
			String userId = userProfile.getUserId() ;
			long userRole = userProfile.getUserRole() ;
			if(userRole == 0 || ForumServiceUtils.hasPermission(this.forum.getModerators(), userId)) this.canViewEditMenu = true ;
			else this.canViewEditMenu = false ;
			if(this.topic.getIsPoll()) {
				Poll poll = forumService.getPoll(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topicId) ; 
				poll_ = poll ;
				this.init() ;
				return poll ;
			}
		}
		return null ;
	}
  
  @SuppressWarnings("unused")
  private boolean getIsEditPoll() {
    return this.isEditPoll ;
  }
  
  @SuppressWarnings("unused")
  private boolean getCanViewEditMenu(){
    return this.canViewEditMenu ;
  }
	
	@SuppressWarnings("unused")
	private boolean getIsVoted() throws Exception {
		if(poll_.getIsClosed()) return true ;
		String userVote = ForumSessionUtils.getCurrentUser() ;
		if(ForumUtils.isEmpty(userVote)) return true ;
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
			if(string.equalsIgnoreCase(userVote)) return true ;
		}
		return false ;
	}
	
	@SuppressWarnings("unused")
	private String[] getInfoVote() throws Exception {
		Poll poll = poll_ ;
		String[] voteNumber = poll.getVote() ;
		long size = 0 ;
    if(!poll.getIsMultiCheck()) {
      size = poll.getUserVote().length	;
    } else {
      for(String user : poll.getUserVote()) {
        size += user.split(":").length -1 ;
      }
    }
		if(size == 0) size = 1;
		String[] infoVote = new String[(voteNumber.length + 1)] ;
		int i = 0;
		for (String string : voteNumber) {
			double tmp = Double.parseDouble(string) ;
			double k = (tmp*size)/100 ;
			int t = (int)Math.round(k) ;
			string = "" + (double) t*100/size ;
			infoVote[i] = string + ":" + t ;
			i = i + 1 ;
		}
		infoVote[i] = "" + size ;
		return infoVote ;
	}
	
	static public class VoteActionListener extends EventListener<UITopicPoll> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicPoll> event) throws Exception {
			UITopicPoll topicPoll = event.getSource() ;
      Poll poll = topicPoll.poll_ ; 
      String[] votes ;
      String[] setUserVote ;
      String userVote = ForumSessionUtils.getCurrentUser() ;
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
          Object[] args = { };
          throw new MessageException(new ApplicationMessage("UITopicPoll.msg.notCheck", args, ApplicationMessage.WARNING)) ;
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
  					int k = (new Integer(tmp)).intValue() - 48;
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
            setUserVote = new String[size  + 1] ;
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
              if(!ForumUtils.isEmpty(posVoted)) {
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
          Object[] args = { };
          throw new MessageException(new ApplicationMessage("UITopicPoll.msg.notCheck", args, ApplicationMessage.WARNING)) ;
        }
      }
			topicPoll.forumService.savePoll(ForumSessionUtils.getSystemProvider(), topicPoll.categoryId, topicPoll.forumId, topicPoll.topicId, poll, false, true) ;
			topicPoll.isAgainVote = false ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll.getParent()) ;
		}
	}
	
	static public class EditPollActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
			UITopicPoll topicPoll = event.getSource() ;
			UIForumPortlet forumPortlet = topicPoll.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPollForm	pollForm = popupAction.createUIComponent(UIPollForm.class, null, null) ;
			String path = topicPoll.categoryId + "/" + topicPoll.forumId + "/" + topicPoll.topicId;
			pollForm.setTopicPath(path) ;
			topicPoll.poll_ = topicPoll.getPoll();
			pollForm.setUpdatePoll(topicPoll.poll_, true) ;
			popupAction.activate(pollForm, 662, 466) ;
			topicPoll.isEditPoll = true ;
		}
	}

	static public class RemovePollActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
			UITopicPoll topicPoll = event.getSource() ;
			topicPoll.forumService.removePoll(ForumSessionUtils.getSystemProvider(), topicPoll.categoryId, topicPoll.forumId, topicPoll.topicId) ;
			if(topicPoll.poll_.getIsMultiCheck()) {
				List<UIComponent> children = topicPoll.getChildren() ;
				for (int i = 0; i < children.size(); i++) {
					topicPoll.removeChild(UIForumCheckBoxInput.class) ;
        }
			} else {
        topicPoll.removeChild(UIFormRadioBoxInput.class) ;
			}
			UITopicDetailContainer topicDetailContainer = (UITopicDetailContainer)topicPoll.getParent() ;
			topicDetailContainer.getChild(UITopicDetail.class).setIsEditTopic(true) ;
			topicPoll.isEditPoll = false ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetailContainer) ;
		}
	}

	static public class VoteAgainPollActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
			UITopicPoll topicPoll = event.getSource() ;
			topicPoll.isAgainVote = true ;
			topicPoll.poll_= topicPoll.getPoll();
			topicPoll.init() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll) ;
		}
	}

	static public class ClosedPollActionListener extends EventListener<UITopicPoll> {
		public void execute(Event<UITopicPoll> event) throws Exception {
			UITopicPoll topicPoll = event.getSource() ;
			Poll poll = topicPoll.poll_ ;
			poll.setIsClosed(!poll.getIsClosed()) ;
			topicPoll.forumService.setClosedPoll(ForumSessionUtils.getSystemProvider(), topicPoll.categoryId, topicPoll.forumId, topicPoll.topicId, poll) ;
			topicPoll.isAgainVote = false ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll) ;
		}
	}
}
