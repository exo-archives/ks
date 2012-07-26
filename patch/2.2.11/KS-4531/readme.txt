Summary
Throw ArrayIndexOutOfBoundsException when move topic in category in special case 
CCP Issue: N/A
Product Jira Issue: KS-4531.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?

Start a clean package
Login as administrator
Go to Forum page
Create category "cat1" with 2 forum "forum1" and "forum2"
In each forum, create "topic1" and "topic2"
Open cat1> forum1
Click Administration > Banned IPs > Add IP
Login as other user using banned ip
Go to cat 1> forum1 > topic1 > Cannot add post-OK
Back to administrator
Open cat1 > forum1 > topic1
Click More action, select "Move" and select destination forum is "forum2"
Result: Moved topic successfully but throw exception on terminal
Jul 19, 2012 5:16:35 PM org.exoplatform.forum.service.impl.JCRDataStorage calculateLastRead
SEVERE: Failed to calculate last read
java.lang.ArrayIndexOutOfBoundsException: 2
at org.exoplatform.forum.service.impl.JCRDataStorage.calculateLastRead(JCRDataStorage.java:2709)
at org.exoplatform.forum.service.impl.JCRDataStorage.moveTopic(JCRDataStorage.java:2652)
at org.exoplatform.forum.service.cache.CachedDataStorage.moveTopic(CachedDataStorage.java:611)
at org.exoplatform.forum.service.impl.ForumServiceImpl.moveTopic(ForumServiceImpl.java:478)
at org.exoplatform.forum.webui.popup.UIMoveTopicForm$SaveActionListener.onEvent(UIMoveTopicForm.java:105)
at org.exoplatform.forum.webui.popup.UIMoveTopicForm$SaveActionListener.onEvent(UIMoveTopicForm.java:96)
at org.exoplatform.ks.common.webui.BaseEventListener.execute(BaseEventListener.java:44)
at org.exoplatform.webui.event.Event.broadcast(Event.java:89)
at org.exoplatform.webui.core.lifecycle.UIFormLifecycle.processAction(UIFormLifecycle.java:103)
at org.exoplatform.webui.core.lifecycle.UIFormLifecycle.processAction(UIFormLifecycle.java:40)
at org.exoplatform.webui.core.UIComponent.processAction(UIComponent.java:135)
at 
Fix description
Problem analysis

We have tow problem in code:
+ Used property EXO_LAST_READ_POST_OF_TOPIC in calculateLastRead of class JCRDataStorage, it must is EXO_READ_TOPIC
+ Used two times to update last post read of topic in class UITopicDetail.
How is the problem fixed?

Replaced property EXO_LAST_READ_POST_OF_TOPIC by EXO_READ_TOPIC to updated information last read topic of user.
Remove code duplicate update last post read of topic in class  UITopicDetail.
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

PL review: PR validated
Support Comment

Support review: PR vaidated
QA Feedbacks

N/A
