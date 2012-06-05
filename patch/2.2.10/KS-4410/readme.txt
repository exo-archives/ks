Summary
[Wiki] user can not download his own attachments

CCP Issue:EXOCONSULTING-10.
Product Jira Issue: KS-4410.
Complexity: N/A

Impacted Client(s): N/A 
Proposal
 

Problem description
What is the problem to fix?

Steps to reproduce
Login by mary or demo
Attach a file to WikiHome
-> Failed to download that file or can't see it if it's an image
Fix description
Problem analysis

Wrong process when get the permission of wiki page cause add some wrong data to the permission of attachments.
With old data, the wrong permission of attachments has been saved to database. With the new fix, we need a migration tools to upgrade the permission with old data.
How is the problem fixed?

Fix the bug at function getPermisson() of class PermissionImpl. This bug make permission data become wrong and cause NullPointerException when check permisson of attachment
Add function that grant full permission of the attachment for the owner.
Add test case to test this issue's case.
Add a function in migration tool (upgrade component) for repairing the data of intranet.
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

Function or ClassName change: 
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?

No
Validation (PM/Support/QA)
PM Comment

PL review: Patch validated
Support Comment

Support review: Patch validated
QA Feedbacks

N/A
