Summary
Answer is not counted after moving question to another category 
CCP Issue: N/A
Product Jira Issue: KS-4514.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?

Login as administrator
Create category: "test1" and "test2"
Create "question1" in category "test1"
Open this question and answer
Click on question link: Display answer counter increase 1
Right click on this question and select move
Select destination category "test2" and move "question1" into it
Open "question1" and answer
Click on question link again
Result: Answer counter is not increased even though new answer is added successfully
Fix description
Problem analysis

The root cause of this issue is after moving question to another category, the new question node is not added a QuestionNodeListener to listen for any changes in the question node.
How is the problem fixed?

Add a QuestionNodeListener to the question node after moving.
Git Pull Request: https://github.com/exoplatform/ks/pull/79
Tests to perform
Reproduction test

Login as administrator
Create category: "test1" and "test2"
Create "question1" in category "test1"
Open this question and answer
Click on question link: Display answer counter increase 1
Right click on this question and select move
Select destination category "test2" and move "question1" into it
Open "question1" and answer
Click on question link again
Result: Answer counter is not increased even though new answer is added successfully
Tests performed at DevLevel

Login as administrator
Create category: "test1" and "test2"
Create "question1" in category "test1"
Open this question and answer
Click on question link: Display answer counter increase 1
Right click on this question and select move
Select destination category "test2" and move "question1" into it
Open "question1" and answer
Click on question link again
Result: Answer counter is increased normally
Tests performed at Support Level

...
Tests performed at QA

...
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
