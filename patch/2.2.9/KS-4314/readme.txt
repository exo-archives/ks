Summary
get polls service returns wrong results in platform jboss

CCP Issue: N/A
Product Jira Issue: KS-4314.
Complexity: N/A


Impacted Client(s): N/A 
Proposal
 

Problem description
What is the problem to fix?

In platform jboss:
Login with any user except mary
Create new space
Add new forum under the space
Create new topic and add a poll to this topic
Login by mary
Go to Dashboard page
Drag & drop Feature Poll
--> The poll newly added is visible although mary is not in the space

Polls gadget calls service /rest/ks/poll/viewpoll/pollid that uses userId of current user to get polls of that user, but it gets wrong userId of current user so mary still see the polls created by user logged in before.

Fix description
Problem analysis

* The PollWebService wrong when get current userId via ConversationState.getCurrent().getIdentity()
* The PollService error when check permission of user in poll of topic.

How is the problem fixed?

* Use SecurityContext and UriInfo replace for ConversationState.getCurrent().getIdentity() to get current userId
* Change logic checking permission of user in poll of topic.
 - Before change: Check Restricted Audience (userPrivate) same check viewer of category parent ==> Error

//brefore
 viewers.addAll(reader.set("exo:userPrivate", new HashSet<String>()));  
 - After fix: Check Restricted Audience before check viewer of category parent ==> Error


//fix
 List<String> privates = new ArrayList<String>(reader.set("exo:userPrivate", new HashSet<String>()));
Patch file: KS-4314.patch
Tests to perform
Reproduction test

steps ...
Tests performed at DevLevel

No
Tests performed at Support Level

No
Tests performed at QA

No
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
 - Not change.

Data (template, node type) migration/upgrade: 
 - Not change
Is there a performance risk/cost?
 No
Validation (PM/Support/QA)
PM Comment
PL review: Patch validated
Support Comment
SL3VN review: Patch resolves this issue.
QA Feedbacks
N/A
