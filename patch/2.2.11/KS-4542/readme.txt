Summary
[KS] Should add * in mandatory fields in Add New Poll form 
CCP Issue: N/A
Product Jira Issue: KS-4542.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?

Go to Poll application
Open form to add new poll
Expected result:  Should add '*' in mandatory fields
Fix description
Problem analysis

Mandatory fields in add new poll form still isn't added Mandatory validator yet.
How is the problem fixed?

Add Mandatory validator to mandatory fields in add new poll form (Poll question and poll options).
Git Pull Request: https://github.com/exoplatform/ks/pull/96
Tests to perform
Reproduction test

Go to Poll application
Open form to add new poll
Mandatory fields (Poll question and poll options) aren't marked with asterik '*' character
Tests performed at DevLevel

Go to Poll application
Open form to add new poll
Mandatory fields (Poll question and poll options) are marked with asterik '*' character
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
PL review: PR validated
Support Comment
Support review: PR validated
QA Feedbacks
N/A
