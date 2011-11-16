/**
 * 
 */
package org.exoplatform.poll.service.ws;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.exoplatform.poll.service.PollNodeTypes;
import org.exoplatform.poll.service.PollService;
import org.exoplatform.poll.service.PollSummary;
import org.exoplatform.poll.service.Utils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.MembershipEntry;

/**
 * @author Vu Duy Tu
 * 
 */
@Path("ks/poll")
public class PollWebservice implements ResourceContainer {
  private static final Log   log      = ExoLogger.getLogger(PollWebservice.class);

  final public static String APP_TYPE = "poll".intern();

  public PollWebservice() {
  }

  @GET
  @Path("/viewpoll/{resourceid}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response viewPoll(@PathParam("resourceid") String pollId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    PollService pollService = (PollService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
    if (!Utils.isEmpty(pollId)) {
      try {
        Poll poll = pollService.getPoll(pollId);
        if (poll != null) {
          // poll.setIsAdmin(String.valueOf(hasGroupAdminOfGatein()));
          poll.setIsAdmin("true");
          String group = poll.getParentPath();
          boolean hasPerminsion = false;
          if (group.indexOf(PollNodeTypes.APPLICATION_DATA) > 0 && poll.getIsAdmin().equals("false")) {
            group = group.substring(group.indexOf(PollNodeTypes.GROUPS + "/") + PollNodeTypes.GROUPS.length(), group.indexOf("/" + PollNodeTypes.APPLICATION_DATA));
            for (String group_ : getGroupsOfUser()) {
              if (group_.indexOf(group) >= 0) {
                hasPerminsion = true;
                break;
              }
            }
          } else if (group.indexOf(PollNodeTypes.POLLS) < 0) {
            hasPerminsion = pollService.hasPermissionInForum(group + "/" + poll.getId(), getAllGroupAndMembershipOfUser());
          } else {
            hasPerminsion = true;
          }
          if (!hasPerminsion) {
            poll = new Poll();
            poll.setId("DoNotPermission");
            return Response.ok(poll, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
          }
          poll.setVotes();
          poll.setInfoVote();
          poll.setShowVote(isGuestPermission(poll));
          return Response.ok(poll, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
        }
      } catch (Exception e) {
        log.error("Can not get poll by id: " + pollId, e);
      }
    }
    PollSummary pollSummary = new PollSummary();
    /*
    if (hasGroupAdminOfGatein()) {
      pollSummary = pollService.getPollSummary();
      pollSummary.setIsAdmin("true");
    } else {
      pollSummary.setId("DoNotPermission");
    }
    */
    pollSummary = pollService.getPollSummary(getAllGroupAndMembershipOfUser());
    pollSummary.setIsAdmin("true");
    return Response.ok(pollSummary, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  @GET
  @Path("/votepoll/{pollId}/{indexVote}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response votePoll(@PathParam("pollId") String pollId, @PathParam("indexVote") String indexVote) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    if (!Utils.isEmpty(pollId) && !Utils.isEmpty(indexVote)) {
      try {
        PollService pollService = (PollService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
        Poll poll = pollService.getPoll(pollId.trim());
        String username = getUserId();
        if (poll != null && !IdentityConstants.ANONIM.equals(username) && 
            validateIndexVote(indexVote, poll.getOption().length)) {
          poll = Utils.calculateVote(poll, username, indexVote);
          pollService.savePoll(poll, false, true);
          poll.setVotes();
          poll.setInfoVote();
          poll.setShowVote(isGuestPermission(poll));
          poll.setIsAdmin(String.valueOf(hasGroupAdminOfGatein()));
          return Response.ok(poll, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
        }
      } catch (Exception e) {
        log.debug("Failed to vote poll.", e);
      }
      return Response.ok("You do not have permission to vote this poll; or some options have been removed from the poll.", MediaType.TEXT_PLAIN).cacheControl(cacheControl).build();
    }
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  private boolean validateIndexVote(String indexVote, int max) {
    String[] ivArr = indexVote.split(Utils.COLON);
    for (int i = 1; i < ivArr.length; i++) {
      try {
        int t = Integer.parseInt(ivArr[i]);
        if (t >= max) {
          return false;
        }
      } catch (Exception e) {
        return false;
      }
    }
    return true;
  }

  private boolean isGuestPermission(Poll poll_) throws Exception {
    if (poll_.getIsClosed())
      return true;
    if (poll_.getTimeOut() > 0) {
      Date today = Utils.getGreenwichMeanTime().getTime();
      if ((today.getTime() - poll_.getCreatedDate().getTime()) >= poll_.getTimeOut() * 86400000)
        return true;
    }
    String username = getUserId();
    if (Utils.isEmpty(username) || IdentityConstants.ANONIM.equals(username))
      return true;
    String[] userVotes = poll_.getUserVote();
    for (String string : userVotes) {
      string = string.substring(0, string.indexOf(":"));
      if (string.equalsIgnoreCase(username))
        return true;
    }
    return false;
  }

  private boolean hasGroupAdminOfGatein() {
    try {
      UserACL userACL = (UserACL) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);
      List<String> list = new ArrayList<String>();
      Identity identity = ConversationState.getCurrent().getIdentity();
      list.add(identity.getUserId());
      list.addAll(identity.getGroups());
      // for (MembershipEntry membership : identity.getMemberships()) {}
      // userACL.getAdminMSType();
      for (String str : list) {
        if (str.equals(userACL.getSuperUser()) || str.equals(userACL.getAdminGroups()))
          return true;
      }
    } catch (Exception e) {
      log.debug("Failed to check has group admin of gatein.", e);
    }
    return false;
  }

  private String getUserId() {
    String username = "";
    try {
      username = ConversationState.getCurrent().getIdentity().getUserId();
    } catch (Exception e) {
    }
    return username;
  }

  private List<String> getAllGroupAndMembershipOfUser() {
    List<String> listOfUser = new ArrayList<String>();
    try {
      Identity identity = ConversationState.getCurrent().getIdentity();
      listOfUser.add(identity.getUserId());
      Set<String> list = new HashSet<String>();
      list.addAll(identity.getGroups());
      for (MembershipEntry membership : identity.getMemberships()) {
        String value = membership.getGroup();
        list.add(value); // its groups
        value = membership.getMembershipType() + ":" + value;
        list.add(value);
      }
      listOfUser.addAll(list);
    } catch (Exception e) {
      log.warn("Failed to add all info of user.");
    }
    return listOfUser;
  }

  private List<String> getGroupsOfUser() {
    try {
      return new ArrayList<String>(ConversationState.getCurrent().getIdentity().getGroups());
    } catch (Exception e) {
      log.warn("Failed to add group of user.");
      return new ArrayList<String>();
    }
  }
}
