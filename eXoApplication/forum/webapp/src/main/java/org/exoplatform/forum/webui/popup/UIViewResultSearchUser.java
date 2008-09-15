package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai Van
 *					ha.mai@exoplatform.com
 * Sebt 09, 2008 11:29:18 AM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIViewResultSearchUser.gtmpl",
		events = {
			@EventConfig(listeners = UIViewResultSearchUser.ViewUserActionListener.class),
			@EventConfig(listeners = UIViewResultSearchUser.CloseActionListener.class,phase = Phase.DECODE)
		}
)

public class UIViewResultSearchUser extends UIForm implements UIPopupComponent {
	
	private List<UserProfile> userProfiles = new ArrayList<UserProfile>();
  private JCRPageList pageList ;
	private static String FORUM_PAGE_ITERATOR="ForumUserPageIterator";
	private String[] permissionUser = null;
	
	public UIViewResultSearchUser() throws Exception {
		addChild(UIForumPageIterator.class, null, FORUM_PAGE_ITERATOR) ;
		WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    ResourceBundle res = context.getApplicationResourceBundle() ;
		permissionUser = new String[]{res.getString("UIForumPortlet.label.PermissionAdmin").toLowerCase(), 
																	res.getString("UIForumPortlet.label.PermissionModerator").toLowerCase(),
																	res.getString("UIForumPortlet.label.PermissionUser").toLowerCase(),
																	res.getString("UIForumPortlet.label.PermissionGuest").toLowerCase()};
		this.setActions(new String[]{"Close"});
	}
	
	public void setPageListSearch(JCRPageList pageList){
		this.pageList = pageList;
		this.pageList.setPageSize(5);
		this.getChild(UIForumPageIterator.class).updatePageList(this.pageList) ;
	}
	
	@SuppressWarnings("unused")
  private List<UserProfile> getListUserProfile(){
		long page = this.getChild(UIForumPageIterator.class).getPageSelected() ;
  	long maxPage = this.pageList.getAvailablePage() ;
  	List<UserProfile> listUserProfile = new ArrayList<UserProfile>();
    try {
	    listUserProfile = this.pageList.getPage(page);
    } catch (Exception e) {
	    e.printStackTrace();
    }
  	this.userProfiles = new ArrayList<UserProfile>();
  	int i =0, j = 0;
  	for (UserProfile userProfile : listUserProfile) {
  		this.userProfiles.add(userProfile);
    }
  	return this.userProfiles;
	}
	
	public void activate() throws Exception { }

	public void deActivate() throws Exception { }
	
	static  public class ViewUserActionListener extends EventListener<UIViewResultSearchUser> {
		public void execute(Event<UIViewResultSearchUser> event) throws Exception {
			UIViewResultSearchUser uiForm = event.getSource() ;
			String userId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIModeratorManagementForm moderatorManagementForm = popupContainer.getChild(UIModeratorManagementForm.class);
			moderatorManagementForm.setValueSearch(userId) ;
			popupContainer.getChild(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static  public class CloseActionListener extends EventListener<UIViewResultSearchUser> {
    public void execute(Event<UIViewResultSearchUser> event) throws Exception {
    	UIViewResultSearchUser uiForm = event.getSource() ;
    	UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      popupContainer.getChild(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
}
