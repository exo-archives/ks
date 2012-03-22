
[Answer] Incorrect behavior when setting Moderator and Restricted Audience

CCP Issue: N/A
Product Jira Issue: KS-4235 
Complexity: N/A
Impacted Client(s): N/A 

Proposal

Problem description
What is the problem to fix?
Steps:

Login as john, goto Answer
2. Create Cate1, setting Moderator: john,mary
3. Create Sub_Cate belongs to Cate1
4. Login as Mary, goto Answer
5. Edit Cate1, setting Restricted Audience: demo
[Actual results]

Mary can't see Sub_Cate although she is administrator of Cate1 and Sub_Cate

Fix description
Problem analysis

 Always check limit user by Restricted Audience of category only ignore administrators not content moderators.
How is the problem fixed?

 Only check limit user by Restricted Audience for normal users,  ignore administrators and moderators.
Patch file: KS-4235.patch
Tests to perform
Reproduction test

Steps:
Login as john, goto Answer
2. Create Cate1, setting Moderator: john,mary
3. Create Sub_Cate belongs to Cate1
4. Login as Mary, goto Answer
5. Edit Cate1, setting Restricted Audience: demo
> Mary can see Sub_Cate although she is administrator of Cate1 and Sub_Cate
Tests performed at DevLevel

No
Tests performed at Support Level

 No
Tests performed at QA

No
Changes in Test Referential
Changes in SNIFF/FUNC/REG tests

Not change.
Changes in Selenium scripts 

 Not change
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

Not change
Is there a performance risk/cost?

 No
Validation (PM/Support/QA)
PM Comment
PL review: Patch validated
Support Comment
SL3VN review: Patch resolved this issue.
QA Feedbacks

