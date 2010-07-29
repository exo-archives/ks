/**
 * 
 */
package org.exoplatform.poll.service.ws;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.PollService;
import org.exoplatform.poll.service.PollSummary;
import org.exoplatform.poll.service.impl.JCRDataStorage;
import org.exoplatform.poll.service.impl.PollNodeTypes;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;




/**
 * @author Uoc Nguyen
 * 
 */
@Path("private/ks/poll")
public class PollWebservice implements ResourceContainer {

  final public static String APP_TYPE = "poll".intern();
  public PollWebservice() {}

  @GET
  @Path("/viewpoll/{resourceid}")//
  @Produces(MediaType.APPLICATION_JSON)
  public Response viewPoll(@PathParam("resourceid") String pollId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    PollService pollService = (PollService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
    if(!JCRDataStorage.isEmpty(pollId)) {
	    try {
	    	Poll poll = pollService.getPoll(pollId);
	    	if(poll != null) {
	    		poll.setIsAdmin(String.valueOf(hasGroupAdminOfGatein()));
	    		String group = poll.getParentPath();
	    		if(group.indexOf(PollNodeTypes.APPLICATION_DATA) > 0 && poll.getIsAdmin().equals("false")) {
	    			group = group.substring(group.indexOf("/", 2), group.indexOf(PollNodeTypes.APPLICATION_DATA)-1);
	    			UserACL userACL = (UserACL)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);
	    			if(userACL.isUserInGroup(group)) {
	    				poll = new Poll();
	    				poll.setId("DoNotPermission");
	    				return Response.ok(poll, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
	    			}
	    		}
	    		poll.setVotes();
	    		poll.setInfoVote();
	    		poll.setShowVote(isGuestPermission(poll));
		      return Response.ok(poll, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
	    	}
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
    }
    PollSummary pollSummary = new PollSummary();
    if(hasGroupAdminOfGatein()) {
    	pollSummary = pollService.getPollSummary();
    	pollSummary.setIsAdmin("true");
    } else {
    	pollSummary.setId("DoNotPermission");
    }
    return Response.ok(pollSummary, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  @GET
  @Path("/votepoll/{pollId}/{indexVote}")//
  @Produces(MediaType.APPLICATION_JSON)
  public Response votePoll(@PathParam("pollId") String pollId, @PathParam("indexVote") String indexVote) throws Exception {
  	CacheControl cacheControl = new CacheControl();
  	cacheControl.setNoCache(true);
  	cacheControl.setNoStore(true);
  	if(!JCRDataStorage.isEmpty(pollId) && !JCRDataStorage.isEmpty(indexVote)) {
  		try {
  			PollService pollService = (PollService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
  			Poll poll = pollService.getPoll(pollId.trim());
  			if(poll != null) {
  				poll = calculateVote(poll, getUserId(), indexVote) ;
  				pollService.savePoll(poll, false, true) ;
  				poll.setVotes();
	    		poll.setInfoVote();
  				poll.setShowVote(isGuestPermission(poll));
  				poll.setIsAdmin(String.valueOf(hasGroupAdminOfGatein()));
  				return Response.ok(poll, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  			}
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
  	}
  	return Response.status(Status.INTERNAL_SERVER_ERROR).build() ;
  }
  
  private Poll calculateVote(Poll poll, String userVote, String optionVote) throws Exception {
  	String[] votes ;
		String[] setUserVote ;
	//User vote and vote number
		String[] temporary = poll.getUserVote() ;
		int size = 0 ;
		if(temporary != null && temporary.length > 0) {
			size = temporary.length ;
		}
  	if(!poll.getIsMultiCheck()) {
				// order number
				int j = Integer.valueOf(optionVote);
				
				setUserVote = new String[(size+1)] ;
				for (int t = 0; t < size; t++) {
					setUserVote[t] = temporary[t];
				}
				setUserVote[size] = userVote + ":" + j;
				size = size + 1 ;
					
				votes = poll.getVote() ;
				double onePercent = (double)100/size;
				int i = 0;
				for(String vote : votes) {
					double a	= Double.parseDouble(vote) ;
					if(i == j) votes[i] = "" + ((a - a/size)+ onePercent) ;
					else votes[i] = "" + (a - a/size) ;
					i = i + 1;
				}
				//save Poll
				poll.setVote(votes) ;
				poll.setUserVote(setUserVote) ;
		// multichoice when vote 
		} else {
			List<String> listValue = new ArrayList<String>() ;// list index checked
			
			votes = poll.getVote() ;
			double totalVote = 0 ;
			double doubleVote[] = new double[votes.length] ;
			String[] listUserVoted = poll.getUserVote() ;
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
				if(JCRDataStorage.isEmpty(posVoted)) {
					doubleVote[Integer.parseInt(posVoted)] -= 1 ;
					totalVote -= 1 ;
				}
			}
			i = 0 ;
			for(String option : poll.getOption()) {
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
		}
  	
  	return poll;
  }
  
	private boolean isGuestPermission(Poll poll_) throws Exception {
		if(poll_.getIsClosed()) return true ;
		if(poll_.getTimeOut() > 0) {
			Date today = JCRDataStorage.getGreenwichMeanTime().getTime() ;
			if((today.getTime() - poll_.getCreatedDate().getTime()) >= poll_.getTimeOut()*86400000) return true ;
		}
		String username = getUserId() ;
		if(JCRDataStorage.isEmpty(username)) return true ;
		String[] userVotes = poll_.getUserVote() ;
		for (String string : userVotes) {
			string = string.substring(0, string.indexOf(":")) ;
			if(string.equalsIgnoreCase(username)) return true ;
		}
		return false ;
	}
	
	private boolean hasGroupAdminOfGatein() {
		try {
			UserACL userACL = (UserACL)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);
			List<String> list = new ArrayList<String>();
			Identity identity = ConversationState.getCurrent().getIdentity();
			list.add(identity.getUserId());
			list.addAll(identity.getGroups());
//			for (MembershipEntry membership : identity.getMemberships()) {}
//			userACL.getAdminMSType();
			for (String str : list) {
				if(str.equals(userACL.getSuperUser()) || str.equals(userACL.getAdminGroups())) return true;
			}
		} catch (Exception e) {e.printStackTrace();}
		return false;
	}

  private String getUserId() {
		String username = "";
		try {
			username = ConversationState.getCurrent().getIdentity().getUserId();
		} catch (Exception e) {}
		return username;
  }
}
