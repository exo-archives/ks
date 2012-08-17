Summary
[KS][Forum] IE7 Alert popup are shown when return to a forum 
CCP Issue:
Product Jira Issue: KS-4251,  KS-4252 .
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?
CASE 1: KS-4251

Steps to reproduce in IE7:
Add a tag (PLF) to the Topic
Select a second Topic
Add a Tag (PLF) to the second Topic
Actual result:
The existed tag is not proposed when we put the first letter in the field
Please see the screenshot tag ff.jpg to see the behavior in the FF.
CASE 2: KS-4252
Steps to reproduce on IE7
Go to topic "Topic1" in forum "Forum1"
Add Tag: PLF
Return to Forum1.
Alert messages appeared
Fix description
Problem analysis

 Javascript's event cannot be called on IE7 
    itemNode.onmouseover = eXo.forum.webservice.SearchTagName.mouseEvent(this, true);
    itemNode.onfocus = eXo.forum.webservice.SearchTagName.mouseEvent(this, true);
    itemNode.onmouseout = eXo.forum.webservice.SearchTagName.mouseEvent(this, false);
    itemNode.onblur = eXo.forum.webservice.SearchTagName.mouseEvent(this, false);
- This block of code only run OK with Firefox and Chrome.

How is the problem fixed?

Change the logic to set javascript's event
    itemNode.onmouseover = SearchTagName.onmouseOver;
    itemNode.onfocus = SearchTagName.onmouseOver;
    itemNode.onmouseout = SearchTagName.onmouseOut;
    itemNode.onblur = SearchTagName.onmouseOut;
- This logic run OK with all browsers.

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
