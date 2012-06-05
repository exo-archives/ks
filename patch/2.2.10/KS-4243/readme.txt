Summary
Export File is empty

CCP Issue: N/A
Product Jira Issue: KS-4243.
Complexity: N/A

Impacted Client(s): N/A 
Proposal
 

Problem description
What is the problem to fix?

Steps to reproduce:
Add a Forum Portlet in a page
Add some Categories
Go to Administration/Export
Actual result:
The size of .zip file is 0Kb.
 Impossible to open the .zip file

Expected result:
User can export to a zip file and import this zip file successfully.
Exception on console:
WARNING: Parameters: Invalid chunk '=false' ignored.
java.io.IOException: Bad file descriptor
at java.io.FileInputStream.readBytes(Native Method)
at java.io.FileInputStream.read(FileInputStream.java:177)
at org.exoplatform.web.handler.DownloadHandler.optimalRead(DownloadHandler.java:108)
at org.exoplatform.web.handler.DownloadHandler.execute(DownloadHandler.java:89)
at org.exoplatform.web.handler.DownloadHandler.execute(DownloadHandler.java:54)
...
Fix description
Problem analysis

 The JCR session is closed before export the data from node of JCR.
The file inputStream is closed before return to web-browser.
How is the problem fixed?

 Before fix: Old code has logic close JCR session . And the inputStream is close.
   categoryHome.getSession().logout();
 .....
  if (inputStream != null) {
          inputStream.close();
   }
After fix: Remove logic close JCR session (The JCR session automatically close by SessionProviderService ). Remove logic close inputStream (This inputStream  automatically close by DownloadService - service of gatein)
-        categoryHome.getSession().logout();
....

-        if (inputStream != null) {
-          inputStream.close();
-        }
-        if (dresource != null && dresource.getInputStream() != null) {
-          dresource.getInputStream().close();
-        }
Tests to perform
Reproduction test

cf. above
Tests performed at DevLevel

cf. above
Tests performed at Support Level

cf. above
Tests performed at QA

None
Changes in Test Referential
Changes in SNIFF/FUNC/REG tests

None
Changes in Selenium scripts 

None
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:
None

Configuration changes
Configuration changes:
*None

Will previous configuration continue to work?

Yes
Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: None
Data (template, node type) migration/upgrade: None
Is there a performance risk/cost?

None
Validation (PM/Support/QA)
PM Comment

PL review: Patch validated
Support Comment

Support review: Patch validated
QA Feedbacks

None
