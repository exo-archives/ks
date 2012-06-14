Summary
[Forum] NPE when add a private group for poll

CCP Issue: N/A
Product Jira Issue: KS-4308.
Complexity: N/A

Impacted Client(s): N/A 
Proposal
 

Problem description
What is the problem to fix?

Create new page and add poll portlet into this page
Edit this portlet and create new poll with public data
Submit poll and save
Save the page
Select new created page
Click on More action ==> Edit
Uncheck on Public Data check-box
Click on icon to select Group Private ==> NPE
Mar 23, 2012 10:43:18 AM org.exoplatform.webui.application.portlet.PortletApplicationController processAction
SEVERE: Error while processing action in the porlet
java.lang.NullPointerException
at org.exoplatform.poll.webui.popup.UIPollForm$AddGroupActionListener.onEvent(UIPollForm.java:431)
at org.exoplatform.poll.webui.popup.UIPollForm$AddGroupActionListener.onEvent(UIPollForm.java:428)
at org.exoplatform.ks.common.webui.BaseEventListener.execute(BaseEventListener.java:44)
at org.exoplatform.webui.event.Event.broadcast(Event.java:89)
at org.exoplatform.webui.core.lifecycle.UIFormLifecycle.processDecode(UIFormLifecycle.java:59)
at org.exoplatform.webui.core.lifecycle.UIFormLifecycle.processDecode(UIFormLifecycle.java:40)
at org.exoplatform.webui.core.UIComponent.processDecode(UIComponent.java:124)
at org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle.processDecode(UIApplicationLifecycle.java:46)
at org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle.processDecode(UIApplicationLifecycle.java:31)
at org.exoplatform.webui.core.UIComponent.processDecode(UIComponent.java:124)
at org.exoplatform.webui.application.portlet.PortletApplication.processAction(PortletApplication.java:1
Fix description
Problem analysis
Use UIPollForm event but this UI form doesn't exist. So, the system will throws NullPointerException when trying to get infomation from UIPollForm.

How is the problem fixed?

 Add new logic: Check if the UIPollForm doesn't exist, add this UI form before using it.

      UIPollForm pollForm = popupContainer.getChild(UIPollForm.class);
     if (pollForm == null)
        pollForm = popupContainer.addChild(UIPollForm.class, null, null);
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

No
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:

No
Configuration changes
Configuration changes:

No
Will previous configuration continue to work?

Yes
Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: No
Data (template, node type) migration/upgrade: No
Is there a performance risk/cost?

No
Validation (PM/Support/QA)
PM Comment

PL review: Patch validated
Support Comment

Support review: Patch validated
QA Feedbacks

N/A
