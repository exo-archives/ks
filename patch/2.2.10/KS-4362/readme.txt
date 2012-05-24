Summary

[Wiki Injector] Simplify page naming

    * CCP Issue: N/A
    * Product Jira Issue: KS-4362.
    * Complexity: low

Proposal

 
Problem description

What is the problem to fix?
TQA use case:
* KS-4362:
Note about (wiki page) naming pattern that the injector is using: prefix_name-of-mother-page_index-number

so if we have 3 level with prefix lv1, lv2, lv3, the lv3 page will be named like that :
"lv3 lv2 lv1 Wiki Home 1 1 1" and its url : wiki/lv3_lv2_lv1_Wiki_Home_1_1_1
(to easily imagine out, see attached image)

The uncomfortable fact is that a child page's name always repeats its mother's name.
Expectation: use the simple naming: prefix_index

* KS-4398: when applying the initial patch for KS-4362
** First injection: create Administration and 5 child pages (subAdmin 1, 2, ..., 5)
** Second injection: create 2 child pages of Administration (Finance 1, 2), under each Finance page, create 3 child pages (subFin)
*** Output: there are also 3 child pages subFin under each subAdmin page:

Fix description

Problem analysis

    * Name of ancestors is added to a page. 
    * There is no filter for page creations with given prefixes while transversing page tree -> It should be implemented to prevent injector from creating redundant page in the whole page tree wide.

How is the problem fixed?

    * Remove parent title from page title.
    * Add new function getPagesByPrefix
    * Add unit tests for inject and reject data to simulate this case in detail.

Tests to perform

Reproduction test

    * cf.above

Tests performed at DevLevel

    * Unit test

Tests performed at Support Level

    * cf.above

Tests performed at QA

    * cf.abode

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests

    * No

Changes in Selenium scripts 

    * No

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:

    * No

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: new private function' added
    * Data (template, node type) migration/upgrade: No

Is there a performance risk/cost?

    * No.

Validation (PM/Support/QA)

PM Comment

    * Patch validated

Support Comment

    * Patch validated

QA Feedbacks

    * ...
