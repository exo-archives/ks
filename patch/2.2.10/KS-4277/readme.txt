Summary
[Answers] Import Category is failed

CCP Issue: N/A
Product Jira Issue: KS-4277.
Complexity: N/A
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
Proposal
 

Problem description
What is the problem to fix?

Steps to reproduce on PLF 3.5.2 snapshot:
Add Answer portlet
Login as John
Go to ACME site
Go to Edit > Page > Layout
Add Answer portlet
Create some categories and Questions
2. Export:
Right click on 1 category and select Export
Put file's name and Export
The Export is done successfully
3. Import
Right click on 1 category and select Import
Choose the file
The import is failed: any cat and questions are imported and an error is occured in the console.
WARNING: Failed to calculate imported root category
javax.jcr.PathNotFoundException: Node not found /exo:applications/faqApp/categories/Categoryf125d1a67f00010100b1d26971c8678c/categories
at org.exoplatform.services.jcr.impl.core.NodeImpl.getNode(NodeImpl.java:1025)
at org.exoplatform.faq.service.impl.JCRDataStorage.calculateImportRootCategory(JCRDataStorage.java:2901)
...
Fix description
Problem analysis

When import category in Answer portlet, it will get the category home node "categories". If the imported category not contain the category home node, will throw an exception with message "Failed to calculate imported root category" and the stack trace. 
How is the problem fixed?

Check the imported category contains the category home node or not. If not, do nothing.
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
