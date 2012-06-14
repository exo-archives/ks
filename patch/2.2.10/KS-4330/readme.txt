Summary
KS - linking questions - non-member groups appear

CCP Issue:  N/A
Product Jira Issue: KS-4330.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?
When linking a question in the answers portlet, ALL groups appear, even the ones the user is not a member of. This is very confusing and unuserfriendly.
To reproduce:
1- Try to create space with a moderator (john for exemple) and invite specific group or members( mary for exemple).
2- create another space with another moderator and other group or members ( for exemple (root and james)
3- in answers portlet try to submit questions and response for each space.
4- when you submit a question with the moderator of one group and you try to link a question (the + button), all groups appear and their questions even ones the user is not member of and he can reponse on their questions.
Expected behaviour : only groups of which the user is part of AND only these groups that contain questions are presented to the user.

Fix description
Problem analysis

Problem:
Do not check the permission of group's space before displaying: list all categories on UI form. ==> Error
How is the problem fixed?
Main code changed:

In class UIAddRelationForm change logic display categories.
Problem: list all categories.
To fix:  Check user's permission before display category's list.
In class UIForumPortlet and UIAnswerPortlet
Get the groupId by spaceId when user open the portlet in space.
+ In class UIGroupSelector
Check permission user in space, display only the group/membership of current space, when user open the portlet in the space.
+ In class UIUserSelect
Display only users space's group when opening the space's portlet.
+ Other change: Optimization code:
Move class and template of UIGroupSelector to common
Remove class and template of UIGroupSelector on the products.
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
