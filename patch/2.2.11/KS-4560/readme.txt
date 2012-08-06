Summary
Proposal
Problem description
Fix description
Tests to perform
Changes in Test Referential
Documentation changes
Configuration changes
Risks and impacts
Validation (PM/Support/QA)
This page should represent the synthesis of information known about the issue fix.
This information will be used to create the general release notes file.
eXo internal information

Impacted Client(s): N/A 
Summary
[Forum] Users in membership *: of "Restrict who can view in this topic" of "Viewers" field cannot see topics 
CCP Issue:  N/A
Product Jira Issue: KS-4560.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?
CASE 1: List topic empty with normal users when setting permission for Restrict who can view in this topic

Login administrators/moderators of Forum
Create category/forum/topic and restrict who can view the topics is *:/platform/users
Logout and login other users has not administrators/moderators of forum
Click on the created forum => No topic is listed. (see attached image)
Note: Still see the created topic when click direct to the link topic.
CASE 2: Cannot list the created topic with contains the word when searching.

The same steps as above
Go to Search: Enter the word that contains in the message of topic.
Result: cannot show the topic as result.
Fix description
Problem analysis

 The xPath of query missing case for comparing with membership membershipType:/groupName and *:/groupName
How is the problem fixed?

 Change the build xPath of query: Add new case for comparing with membership membershipType:/groupName and *:/groupName
     if (ForumServiceUtils.isGroupExpression(str)) {
        query.append(" or @").append(property).append(" = '*:").append(str).append("'");
      } else if(ForumServiceUtils.isMembershipExpression(str)){
        str = str.substring(str.indexOf(":")+1);
        query.append(" or @").append(property).append(" = '*:").append(str).append("'");
      }
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

PL review: PR validated
Support Comment

Support review: PR validated
QA Feedbacks

N/A
