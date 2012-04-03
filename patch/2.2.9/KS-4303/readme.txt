Summary
Cannot save page permission for /platform/administrators

CCP Issue: N/A
Product Jira Issue: KS-4303.
Complexity: N/A
Impacted Client(s): N/A 

Proposal
Problem description
What is the problem to fix?

Go to Add Page → Blank Page (or From Template)
Add values in required fields
Click Save
Add permission for group /platform/administrators, Save
Click Page permission again
> Issue: do not see /platform/administrators in the list.
Other group is added and listed in the permission list normally.
Fix description
Problem analysis

For each created wiki, it has some privilege permission which is granted implicitly for administrators:
Super user
Admin groups
Portal creator groups
And when each page on that wiki is created will inherit all of its permission. But the problem is, it's still visible on wiki permission list but hidden in page permission list.
-> This is a conflict behavior
That's why when user try to add one of preceding administrators, it's filtered and not showed.
How is the problem fixed?

Show all permission involve normal and privilege entries in page permission list by removing:
HashMap<String, String[]> adminsACLMap = org.exoplatform.wiki.utils.Utils.getACLForAdmins();
// Filter out ACL for administrators
for(String id: adminsACLMap.keySet()){
permissionMap.remove(id);
}
Fix duplicated entry bug when adding to privilege permission group
if (!permissions.contains(portalEditClause)) {
     permissions.add(portalEditClause);
}
Prevent user from editing/removing that entries(make them immutable)
if (!uiPermissionEntry.isImmutable()) {
// Perform to edit or remove
}
Patch file: KS-4303.patch
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

Update test case about immutable entries
Changes in Selenium scripts 

change as above
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:

Update new screen and description
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
SL3VN review: Patch resolves this issue.
QA Feedbacks
N/A
