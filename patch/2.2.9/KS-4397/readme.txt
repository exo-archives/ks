Summary
[wiki] Performace with cache markup during navigation

CCP Issue: N/A
Product Jira Issue: KS-4397.
Complexity: N/A

Impacted Client(s): N/A 
Proposal
 

Problem description
What is the problem to fix?

Use exocache on some markup.
Usecase:
Open a page
Does this pages exist in the cache ?
Yes = reuse this one.
No = Render the page and add to the cache
Edit a page = invalidate the cache page.
Push to make some bench when done.

Fix description
Problem analysis

The loading time of page need to improve
How is the problem fixed?

 Implementation a HTML markup cache:
Get the content of a page 
Render the wiki markup at the first time to a HTML markup
Put it in the cache with the corresponding key is the composition of
Wiki markup
Source syntax
Target syntax
Is support section editing
Create  configurations for cache

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

No
Changes in Selenium scripts 

No
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:

Need to update in Admin guide/Developer/Ref guide
Configuration changes
Configuration changes:

Yes, adding of cache-configurations.xml and modifications in configuration.properties
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
N/A
QA Feedbacks
N/A
