package org.exoplatform.wiki.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import javax.jcr.Node;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ks.common.Common;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.ModelImpl;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWiki;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.diff.DiffResult;
import org.exoplatform.wiki.service.diff.DiffService;

public class Utils {
  
  private static final Log      log_               = ExoLogger.getLogger(Utils.class);
  
  private static final String JCR_WEBDAV_SERVICE_BASE_URI = "/jcr";

  final private static String MIMETYPE_TEXTHTML = "text/html";
  
  //The path should get from NodeHierarchyCreator 
  public static String getPortalWikisPath() {    
    String path = "/exo:applications/" 
    + WikiNodeType.Definition.WIKI_APPLICATION + "/"
    + WikiNodeType.Definition.WIKIS ; 
    return path ;
  }
  /**
   * @return 
   *      <li> portal name if wiki is portal type</li>
   *      <li> groupid if wiki is group type</li>
   *      <li> userid if wiki is personal type</li>
   * @throws IllegalArgumentException if jcr path is not of a wiki page node.
   */
  public static String getSpaceIdByJcrPath(String jcrPath) throws IllegalArgumentException {
    String wikiType = getWikiType(jcrPath);
    if (PortalConfig.PORTAL_TYPE.equals(wikiType)) {
      return getPortalIdByJcrPath(jcrPath);
    } else if (PortalConfig.GROUP_TYPE.equals(wikiType)) {
      return getGroupIdByJcrPath(jcrPath);
    } else if (PortalConfig.USER_TYPE.equals(wikiType)) {
      return getUserIdByJcrPath(jcrPath);
    } else {
      throw new IllegalArgumentException(jcrPath + " is not jcr path of a wiki page node!");
    }
  }
  
  /**
   * @param jcrPath follows the format /Groups/$GROUP/ApplicationData/eXoWiki/[wikipage]
   * @return $GROUP of jcrPath
   * @throws IllegalArgumentException if jcrPath is not as expected.
   */
  public static String getGroupIdByJcrPath(String jcrPath) throws IllegalArgumentException {
    int pos1 = jcrPath.indexOf("/Groups/");
    int pos2 = jcrPath.indexOf("/ApplicationData");
    if (pos1 >= 0 && pos2 > 0) {
      return jcrPath.substring(pos1 + "/Groups/".length(), pos2);
    } else {
      throw new IllegalArgumentException(jcrPath + " is not jcr path of a group wiki page node!");
    }
  }
  
  /**
   * @param jcrPath follows the format /Users/$USERNAME/ApplicationData/eXoWiki/...
   * @return $USERNAME of jcrPath
   * @throws IllegalArgumentException if jcrPath is not as expected.
   */
  public static String getUserIdByJcrPath(String jcrPath) throws IllegalArgumentException {
    int pos1 = jcrPath.indexOf("/Users/");
    int pos2 = jcrPath.indexOf("/ApplicationData");
    if (pos1 >= 0 && pos2 > 0) {
      return jcrPath.substring(pos1 + "/Users/".length(), pos2);
    } else {
      throw new IllegalArgumentException(jcrPath + " is not jcr path of a personal wiki page node!");
    }
  }
  
  /**
   * @param jcrPath follows the format /exo:applications/eXoWiki/wikis/$PORTAL/...
   * @return $PORTAL of jcrPath
   * @throws IllegalArgumentException if jcrPath is not as expected.
   */
  public static String getPortalIdByJcrPath(String jcrPath) throws IllegalArgumentException {
    String portalPath = getPortalWikisPath();
    int pos1 = jcrPath.indexOf(portalPath);
    
    if (pos1 >= 0) {
      String restPath = jcrPath.substring(pos1 + portalPath.length() + 1);
      return restPath.substring(0, restPath.indexOf("/"));
    } else {
      throw new IllegalArgumentException(jcrPath + " is not jcr path of a portal wiki page node!");
    }
  }
  
  /**
   * @param jcrPath absolute jcr path of page node.
   * @return type of wiki page. 
   */
  public static String getWikiType(String jcrPath) throws IllegalArgumentException {
    if (jcrPath.startsWith("/exo:applications/")) {
      return PortalConfig.PORTAL_TYPE;
    } else if (jcrPath.startsWith("/Groups/")) {
      return PortalConfig.GROUP_TYPE;
    } else if (jcrPath.startsWith("/Users/")) {
      return PortalConfig.USER_TYPE;
    } else {
      throw new IllegalArgumentException(jcrPath + " is not jcr path of a wiki page node!");
    }
  }
  
  public static String validateWikiOwner(String wikiType, String wikiOwner){
    if(wikiType.equals(PortalConfig.GROUP_TYPE)) {
      if(wikiOwner == null || wikiOwner.length() == 0){
        return null;
      }
      if(wikiOwner.startsWith("/")){
        wikiOwner = wikiOwner.substring(1,wikiOwner.length());
      }
      if(wikiOwner.endsWith("/")){
        wikiOwner = wikiOwner.substring(0,wikiOwner.length()-1);
      }
    }
    return wikiOwner;
  }
  
  public static String getDefaultRepositoryWebDavUri() {
    StringBuilder sb = new StringBuilder();
    sb.append("/");
    sb.append(PortalContainer.getCurrentPortalContainerName());
    sb.append("/");
    sb.append(PortalContainer.getCurrentRestContextName());
    sb.append(JCR_WEBDAV_SERVICE_BASE_URI);
    sb.append("/");
    RepositoryService repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    sb.append(repositoryService.getConfig().getDefaultRepositoryName());
    sb.append("/");
    return sb.toString();
  }
  
  public static void reparePermissions(AttachmentImpl att) throws Exception {
    MOWService mowService = (MOWService) PortalContainer.getComponent(MOWService.class);
    WikiStoreImpl store = (WikiStoreImpl) mowService.getModel().getWikiStore();
    Node attNode = (Node) store.getSession().getJCRSession().getItem(att.getPath());
    ExtendedNode extNode = (ExtendedNode) attNode;
    if (extNode.canAddMixin("exo:privilegeable"))
      extNode.addMixin("exo:privilegeable");
    String[] arrayPers = { PermissionType.READ };
    extNode.setPermission("any", arrayPers);
    attNode.getSession().save();
  }
  
  public static String getDocumentURL(WikiContext wikiContext) {
    StringBuilder sb = new StringBuilder();
    sb.append(wikiContext.getPortalURI());
    sb.append(wikiContext.getPortletURI());
    sb.append("/");
    if (!PortalConfig.PORTAL_TYPE.equalsIgnoreCase(wikiContext.getType())) {
      sb.append(wikiContext.getType().toLowerCase());
      sb.append("/");
      sb.append(Utils.validateWikiOwner(wikiContext.getType(), wikiContext.getOwner()));
      sb.append("/");
    }
    sb.append(wikiContext.getPageId());
    return sb.toString();
  }
  
  public static String getCurrentUser() {
    try {
      ConversationState conversationState = ConversationState.getCurrent();
      return conversationState.getIdentity().getUserId();
    }catch(Exception e){}
    return "system" ;
  }
  
  public static Collection<Wiki> getWikisByType(WikiType wikiType) {
    MOWService mowService = (MOWService) PortalContainer.getComponent(MOWService.class);
    WikiStoreImpl store = (WikiStoreImpl) mowService.getModel().getWikiStore();
    return store.getWikiContainer(wikiType).getAllWikis();
  }
  
  public static Wiki getWiki(WikiPageParams params) {
    Collection<Wiki> wikis = getWikisByType(WikiType.valueOf(params.getType().toUpperCase()));
    for (Wiki wiki : wikis) {
      if (wiki.getOwner().equals(params.getOwner())) {
        return wiki;
      }
    }
    return null;
  }

  public static String getWikiType(Wiki wiki) {
    if (wiki instanceof PortalWiki) {
      return PortalConfig.PORTAL_TYPE;
    } else if (wiki instanceof GroupWiki) {
      return PortalConfig.GROUP_TYPE;
    } else if (wiki instanceof UserWiki) {
      return PortalConfig.USER_TYPE;
    } else {
      return null;
    }
  }
  
  public static Wiki[] getAllWikiSpace() {
    MOWService mowService = (MOWService) PortalContainer.getComponent(MOWService.class);
    WikiStoreImpl store = (WikiStoreImpl) mowService.getModel().getWikiStore();
    return store.getWikis().toArray(new Wiki[]{}) ;
  } 
  
  public static boolean isDescendantPage(PageImpl page, PageImpl parentPage) {
    Iterator<PageImpl> iter = parentPage.getChildPages().values().iterator();
    while (iter.hasNext()) {
      PageImpl childpage = (PageImpl) iter.next();
      if (childpage.equals(page))
        return true;
      if (isDescendantPage(page, childpage))
        return true;
    }
    return false;
  }

  public static Object getObject(String path, String type) throws Exception {
    WikiService wservice = (WikiService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
    return wservice.findByPath(path, type) ;
  }
  
  public static Object getObjectFromParams(WikiPageParams param) throws Exception {
    WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer()
                                                               .getComponentInstanceOfType(WikiService.class);
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer()
                                                            .getComponentInstanceOfType(MOWService.class);
    WikiStoreImpl store = (WikiStoreImpl) mowService.getModel().getWikiStore();
    String wikiType = param.getType();
    String wikiOwner = param.getOwner();
    String wikiPageId = param.getPageId();

    if (wikiOwner != null && wikiPageId != null) {
      if (!wikiPageId.equals(WikiNodeType.Definition.WIKI_HOME_NAME)) {
        // Object is a page
        Page expandPage = (Page) wikiService.getPageById(wikiType, wikiOwner, wikiPageId);
        return expandPage;
      } else {
        // Object is a wiki home page
        Wiki wiki = store.getWikiContainer(WikiType.valueOf(wikiType.toUpperCase()))
                         .getWiki(wikiOwner);
        WikiHome wikiHome = (WikiHome) wiki.getWikiHome();
        return wikiHome;
      }
    } else if (wikiOwner != null) {
      // Object is a wiki
      Wiki wiki = store.getWikiContainer(WikiType.valueOf(wikiType.toUpperCase()))
                       .getWiki(wikiOwner);
      return wiki;
    } else if (wikiType != null) {
      // Object is a space
      return wikiType;
    } else {
      return null;
    }
  }
  
  public static Stack<WikiPageParams> getStackParams(PageImpl page) throws Exception {
    Stack<WikiPageParams> stack = new Stack<WikiPageParams>();
    Wiki wiki = page.getWiki();
    while (page != null) {
      stack.push(new WikiPageParams(Utils.getWikiType(wiki), wiki.getOwner(), page.getName()));
      page = page.getParentPage();
    }
    return stack;
  }
  
  
  public static WikiPageParams getWikiPageParams(Page page) {
    Wiki wiki = ((PageImpl) page).getWiki();
    String wikiType = getWikiType(wiki);
    WikiPageParams params = new WikiPageParams(wikiType, wiki.getOwner(), page.getName());
    return params;
  }
  
  public static void sendMailOnChangeContent(ContentImpl content) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    DiffService diffService = (DiffService) container.getComponentInstanceOfType(DiffService.class);
    Common common = new Common();
    Message message = new Message();
    ConversationState conversationState = ConversationState.getCurrent();
    // Get author
    String author = conversationState.getIdentity().getUserId();

    // Get watchers' mails
    PageImpl page = content.getParent();
    List<String> list = page.getWatchedMixin().getWatchers();
    List<String> emailList = new ArrayList<String>();
    for (int i = 0; i < list.size(); i++) {
      emailList.add(UserHelper.getEmailUser(list.get(i)));
    }   
    
    // Get differences
    String pageTitle = content.getTitle();
    String currentVersionContent = content.getText();
    NTVersion previousVersion = page.getVersionableMixin().getBaseVersion();
    String previousVersionContent = ((ContentImpl) previousVersion.getNTFrozenNode()
                                                                  .getChildren()
                                                                  .get(WikiNodeType.Definition.CONTENT)).getText();
    DiffResult diffResult = diffService.getDifferencesAsHTML(previousVersionContent,
                                                             currentVersionContent,
                                                             false);
    if (diffResult.getChanges() == 0) {
      diffResult.setDiffHTML("No changes, new revision is created.");
    }    
    StringBuilder sb = new StringBuilder();
    sb.append("The page \"" + pageTitle + "\" has been modifed by <b>" + author + "</b>")
      .append("<br/><br/>")
      .append("Changes are:")
      .append("<br/><br/>")
      .append(insertStyle(diffResult.getDiffHTML()));
    
    // Create message
    message.setFrom(makeNotificationSender(author));    
    message.setSubject("\"" + pageTitle + "\" page was modified");
    message.setMimeType(MIMETYPE_TEXTHTML);
    message.setBody(sb.toString());
    try {
      JobSchedulerService schedulerService = (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
      if (schedulerService != null)
        common.sendEmailNotification(emailList, message, "KnowledgeSuite");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

    }
  }
  

  
  public static boolean isWikiAvailable(String wikiType, String wikiOwner) {
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer()
                                                            .getComponentInstanceOfType(MOWService.class);
    ModelImpl model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<Wiki> container = wStore.getWikiContainer(WikiType.valueOf(wikiType.toUpperCase()));
    return (container.contains(wikiOwner) != null);
  }
  
  public static HashMap<String, String[]> getACLForAdmins() {
    HashMap<String, String[]> permissionMap = new HashMap<String, String[]>();
    UserACL userACL = (UserACL) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);
    permissionMap.put(userACL.getSuperUser(), org.exoplatform.services.jcr.access.PermissionType.ALL);
    permissionMap.put(userACL.getAdminGroups(), org.exoplatform.services.jcr.access.PermissionType.ALL);
    for (String group : userACL.getPortalCreatorGroups()) {
      permissionMap.put(group, org.exoplatform.services.jcr.access.PermissionType.ALL);
    }
    return permissionMap;
  }
  
  /**
   * Has permission.
   * 
   * @param acl
   *          access control list
   * @param permission
   *          permissions array
   * @param user
   *          user Identity
   * @return boolean
   */
  public static boolean hasPermission(AccessControlList acl, String[] permission, Identity user) {

    String userId = user.getUserId();
    if (userId.equals(SystemIdentity.SYSTEM)) {
      // SYSTEM has permission everywhere
      return true;
    } else if (userId.equals(acl.getOwner())) {
      // Current user is owner of node so has all privileges
      return true;
    } else if (userId.equals(SystemIdentity.ANONIM)) {
      List<String> anyPermissions = acl.getPermissions(SystemIdentity.ANY);

      if (anyPermissions.size() < permission.length)
        return false;

      for (int i = 0; i < permission.length; i++) {
        if (!anyPermissions.contains(permission[i]))
          return false;
      }
      return true;
    } else {
      if (acl.getPermissionsSize() > 0 && permission.length > 0) {
        // check permission to perform all of the listed actions
        for (int i = 0; i < permission.length; i++) {
          // check specific actions
          if (!isPermissionMatch(acl.getPermissionEntries(), permission[i], user))
            return false;
        }
        return true;
      }
      return false;
    }
  }
  
  private static boolean isPermissionMatch(List<AccessControlEntry> existedPermission, String testPermission, Identity user) {
    for (int i = 0, length = existedPermission.size(); i < length; i++) {
      AccessControlEntry ace = existedPermission.get(i);
      // match action
      if (ace.getPermission().equals(testPermission)) {
        // match any
        if (ace.getIdentity().equals(SystemIdentity.ANY))
          return true;
        else if (ace.getIdentity().indexOf(":") == -1) {
          // just user
          if (ace.getIdentity().equals(user.getUserId()))
            return true;

        } else if (user.isMemberOf(ace.getMembershipEntry()))
          return true;
      }
    }
    return false;
  }

  private static String insertStyle(String rawHTML) {
    String result = rawHTML;
    result = result.replaceAll("class=\"diffaddword\"", "style=\"background: #b5ffbf;\"");
    result = result.replaceAll("<span class=\"diffremoveword\">",
                               "<span style=\" background: #ffd8da;text-decoration: line-through;\">");
    result = result.replaceAll("<pre class=\"diffremoveword\">",
                               "<pre style=\" background: #ffd8da;\">");
    return result;
  }
  
  private static String makeNotificationSender(String from) {
    InternetAddress addr = null;
    if (from == null) return null;
    try {
      addr = new InternetAddress(from);
    } catch (AddressException e) {
      if (log_.isDebugEnabled()) { log_.debug("value of 'from' field in message made by forum notification feature is not in format of mail address", e); }
      return null;
    }
    Properties props = new Properties(System.getProperties());
    String mailAddr = props.getProperty("gatein.email.smtp.from");
    if (mailAddr == null || mailAddr.length() == 0) mailAddr = props.getProperty("mail.from");
    if (mailAddr != null) {
      try {
        InternetAddress serMailAddr = new InternetAddress(mailAddr);
        addr.setAddress(serMailAddr.getAddress());
        return addr.toUnicodeString();
      } catch (AddressException e) {
        if (log_.isDebugEnabled()) { log_.debug("value of 'gatein.email.smtp.from' or 'mail.from' in configuration file is not in format of mail address", e); }
        return null;
      }
    } else {
      return null;
    }
  }
}
