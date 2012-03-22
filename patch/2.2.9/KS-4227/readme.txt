Automatical creation of wiki home page of non existing user/group

CCP Issue: N/A
Product Jira Issue: KS-4227.
Complexity: N/A

Impacted Client(s): N/A 

Proposal
 
Problem description

What is the problem to fix?

Step to reproduce

- Run ks standalone
- Go to wiki home page localhost:8080/ksdemo/wiki
- Type to address bar this url localhost:8080/ksdemo/wiki/user/newuser
--> the page newuser has been created -> NOK
Expected result : page not found

we have to define case : http://int.exoplatform.org/portal/intranet/wiki/group/spaces/exo_ct/KS-4227_auto_create_personal_page_of_user_is_not_in_system

public mode visit personal wiki -> page not found 
private mode ( user loged in ) visit other user page 
if page created by owner -> show page following permission 
if page did not create by owner or have no permission -> page not found 
private mode ( user loged in ) visit their page -> show page or created if did not create before

Fix description

Problem analysis

Do not check user permission before create or view the page before.

How is the problem fixed?

- Add hasPermission method to check user who can created the page or view the page

 private  boolean hasPermission(String wikiType, String owner) throws Exception {
      ConversationState conversationState = ConversationState.getCurrent();
      Identity user = null;
     if (conversationState != null) {
        user = conversationState.getIdentity();
        ExoContainer container = ExoContainerContext.getCurrentContainer();
        UserACL acl = (UserACL)container.getComponentInstanceOfType(UserACL.class);
       if(acl != null && acl.getSuperUser().equals(user.getUserId())){
         return true;
       }
     } else {
        user = new Identity(IdentityConstants.ANONIM);
     }
      List<AccessControlEntry> aces = getAccessControls(wikiType, owner);
      AccessControlList acl = new AccessControlList(owner, aces);
      String []permission = new String[]{PermissionType.ADMINSPACE.toString()};
     return Utils.hasPermission(acl, permission, user);
   }
- Before create or view a page, calling hasPermission to check the permissions of user. If user has created or viewed permissions => creates the page or views the page
- In this wikitype is group

   WikiContainer<GroupWiki> groupWikiContainer = wStore.getWikiContainer(WikiType.GROUP);
  boolean hasPermission = hasPermission(wikiType, owner);
   wiki = groupWikiContainer.getWiki(owner, hasPermission);
- In this wikitype is user

   boolean hasEditWiki = hasPermission(wikiType, owner);
    WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
    wiki = userWikiContainer.getWiki(owner, hasEditWiki);
The "wiki" will return to show for user

Patch file: KS-4227.patch

Tests to perform
Reproduction test

cf. above
Tests performed at DevLevel

cf. above
Tests performed at Support Level

cf. above
Tests performed at QA

N/A
Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
No
Changes in Selenium scripts 
N/A
Documentation changes
No
Documentation (User/Admin/Dev/Ref) changes:


Configuration changes
Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?
--> None
Function or ClassName change: None

Data (template, node type) migration/upgrade: None

Is there a performance risk/cost?
None

Validation (PM/Support/QA)
PM Comment
PL review: Patch Validated
Sl3VN Comment
Sl3VN review: Patch resolved this issue.
QA Feedbacks
N/A
