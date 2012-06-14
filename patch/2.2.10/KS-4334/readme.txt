Summary
Number of rate of answer is reset to 0 after add new answer for question 

Impacted Client(s): N/A 
Proposal
 

Problem description
What is the problem to fix?

Login as John
Creat new question
Login as Mary on other browser
Add answer for this question
John refresh browser, click open question and rate the answer (+1 for example)
Mary refresh browser, open the question and add another answer for this question
  Result: Number of rate of the previous answer is reset to 0-NOK
Fix description
Problem analysis
 Get the incorrect nodetype exo:markVote, the nodetype doesn't exits in answer node (Node of JCR). ==> Error.
   answer.setMarkVotes(reader.l(EXO_MARK_VOTE, 0));
How is the problem fixed?

Get the correct nodetype exo:MarkVotes.
   answer.setMarkVotes(reader.l(EXO_MARK_VOTES));
Other change files: It is improvement code for better (performance, skin). 
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

Yes
Validation (PM/Support/QA)
PM Comment

PL review: PR validated
Support Comment

Support review: PR validated
QA Feedbacks

N/A
