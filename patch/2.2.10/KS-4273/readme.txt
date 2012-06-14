Summary
[Forum] Wrong notification subject when move topic (null prefix)

CCP Issue: N/A
Product Jira Issue: KS-4273.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?

When move topic, we receive a email has subject content text "null",
ex: nullChat expired popupLost of links in forum notification mail
Expected result :
Remove "null" in subject email.
Fix description
Problem analysis
Problem because can not set category-name in header subject of email notification.

// The object name is empty ( CommonUtils.EMPTY_STR )
 MessageBuilder messageBuilder = getInfoMessageMove(sProvider, mailContent, CommonUtils.EMPTY_STR, true);
How is the problem fixed?
Set category-name in header subject of email notification.

   String objectName = new StringBuilder("[").append(destForumNode.getParent().getProperty(EXO_NAME).getString())
                           .append("][").append(destForumNode.getProperty(EXO_NAME).getString()).append("] ").toString();
   MessageBuilder messageBuilder = getInfoMessageMove(sProvider, mailContent, objectName, true);
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
* PL review: Patch validated

Support Comment
* Support review: Patch validated
QA Feedbacks
N/A
