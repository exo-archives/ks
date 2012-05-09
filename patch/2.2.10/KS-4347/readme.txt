Summary
[KS-Wiki] Page not found when rename page

Product Jira Issue: KS-4347.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?

Go to wiki page
Create new page
Open this page
Double-click on page name in toolbar to rename for page
Put new page's name which has space character
Press Enter:  show message: Page not found
Fix description
Problem analysis

The problem's cause by some redundant javascript codes, it's create some redundant request to server.
How is the problem fixed?

Remove the redundant javascript codes
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

Support review: Patch vaidated
QA Feedbacks

N/A
