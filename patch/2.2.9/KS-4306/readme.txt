Summary
Can move page while user doesn't have "edit" permission on destination page

CCP Issue: N/A
Product Jira Issue: KS-4306.
Complexity: N/A

Impacted Client(s): N/A 


Proposal
 

Problem description
What is the problem to fix?

Login by "john"
Go to Wiki page
Create 2 pages: Test1 and Test2
Set permission for page Test1 that any user has only "View" permission
Login by "mary"
Go to wiki page
Select page Test1: mary cannot add/edit/move page Test1 : OK
Select page Test2 > mary can move page Test2 to Test1 : KO because mary doesn't have "Edit" permission on page Test1
Fix description
Problem analysis

The permission filter hasn't been implemented yet:
private boolean isHasCreatePagePermission(String userId, String destSpace) {
 return true;
}
How is the problem fixed?

* Add the checking permission block before performing move page action
PageImpl destPage = (PageImpl) getPageById(newLocationParams.getType(), newLocationParams.getOwner(), ewLocationParams.getPageId());
if (destPage == null || !destPage.hasPermission(PermissionType.EDITPAGE))
 return false;
* Adding new unit test for this case

Patch file: KS-4306.patch
Tests to perform
Reproduction test

cf.above
Tests performed at DevLevel

cf.above
Tests performed at Support Level

cf.above
Tests performed at QA

cf.above
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

Function or ClassName change:  No
Data (template, node type) migration/upgrade: No
Is there a performance risk/cost?

No


Validation (PM/Support/QA)
PM Comment

PL review: Patch validated
Support Comment

SL3VN review: Patch resolves this issue
QA Feedbacks

N/A
