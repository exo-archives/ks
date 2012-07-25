Summary
Cannot select group that doesnot have sub-group when add permission for Poll/Forum/Topic 
CCP Issue: N/A
Product Jira Issue: KS-4540.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?

Go to Poll application
Click on More action ==> Edit poll
Uncheck on Public data check-box
Click on select group icon
Result: User cannot select groups that don't have sub-group like: Customers, Sandbox, Partners ..
Note:
The same problem when select permission for Forum, Topic ..
In Wiki, when the group does not have sub-group, there is a link allows user to select the group. See attached files
Fix description
Problem analysis

Group that doesn't have sub-group isn't displayed a link to select itself.
How is the problem fixed?

Add a link to select group that doesn't have sub-group like in Wiki product.
Git Pull Request: https://github.com/exoplatform/ks/pull/92
Tests to perform
Reproduction test

Go to Poll application
Click on More action ==> Edit poll
Uncheck on Public data check-box
Click on select group icon
Result: User cannot select groups that don't have sub-group like: Customers, Sandbox, Partners ..
Tests performed at DevLevel

Go to Poll application
Click on More action ==> Edit poll
Uncheck on Public data check-box
Click on select group icon
Result: User can select groups that don't have sub-group like: Customers, Sandbox, Partners ..
Tests performed at Support Level
N/A
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
Support review: PR validated
QA Feedbacks
N/A
