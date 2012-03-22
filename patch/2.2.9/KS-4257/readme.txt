Can not use emoticons in table when switch from WYSIWYG to Markup editor

CCP Issue: N/A
Product Jira Issue: KS-4257.
Complexity: N/A

Impacted Client(s): N/A 

Proposal

Problem description

What is the problem to fix?

In markup editor:
Input an emoticon  on a table cell

Switch to WYSIWYG editor: icon isn't rendered

Fix description

Problem analysis
This problem belongs to xwiki rendering component. It' render wrong format when switch to mackup editor.

How is the problem fixed?
Extend and rewrite method onImage() of XWikiSyntaxChainingRenderer for render emoticons.

Patch file: KS-4257.patch
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
None
Changes in SNIFF/FUNC/REG tests
None
Changes in Selenium scripts 
None
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

Function or ClassName change: None
Data (template, node type) migration/upgrade: None
Is there a performance risk/cost?
No

Validation (PM/Support/QA)
PM Comment
PL review: Patch validated
Support Comment
SL3VN review: Patch resolved this issue.
QA Feedbacks
N/A
