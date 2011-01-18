Summary

    * Status: Format of mini calendar is in English when changing language into French
    * CCP Issue: CCP-470, Product Jira Issue: KS-3000.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
1. Login
2. Go to Forum
3. Click on Advanced search icon
4. Change language in French, mini calendar is in English format

Fix description

How is the problem fixed?
    * Modify UIFormDateTimePicker.java and add locale. 
    * Modify UIDateTimePicker.js, add some new fields (weekdays, tooltip and lang) for label localization.
    * Add new folder lang which contains some file defining months and days (EN.js, fr.js, vn.js, de.js)

Patch file: KS-3000.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
    * N/A

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PL review: patch approved

Support Comment
* Support review: patch validated.

QA Feedbacks
*
