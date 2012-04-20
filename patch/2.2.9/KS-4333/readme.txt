Summary

[Answer] Error when move category in Answer portlet 

    * CCP Issue:N/A
    * Product Jira Issue: KS-4333.
    * Complexity: N/A

    * Summary
    * Proposal
          o Problem description
          o Fix description
          o Tests to perform
          o Changes in Test Referential
          o Documentation changes
          o Configuration changes
          o Risks and impacts
    * Validation (PM/Support/QA)

This page should represent the synthesis of information known about the issue fix.
This information will be used to create the general release notes file.

eXo internal information

    * Impacted Client(s): N/A 

Proposal

 
Problem description

What is the problem to fix?

    * Login as John
    * Create new page with answer portlet
    * Create 2 category
    * Move one category to another
      Result: Move successfully but display error pop up
      The same issue when delete a category

Fix description

Problem analysis

    * When working on KS-4114, we removed some table/td/div and CSS classes. So, the structure of DOM HTML which was applied to run javascript's functions has been broken.
    * In code javascript: Not check null before using this element. So, the error occurs when running javascript's functions.

  // do not check null
   uiNav.scrollMgr[scrollname].mainContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "td", "ControlButtonContainer");

How is the problem fixed?

    *  Revert code to revision committed 80406 in file UIBreadcumbs.gtmpl, do not optimization this file. 
    *  Change logic javascript: Check element not null before use it.

   var controlButtonContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "td", "ControlButtonContainer");
  if (container && controlButtonContainer) {
    //.... do something
   }Problem analysis

When working on KS-4114, we removed some table/td/div and CSS classes. So, the structure of DOM HTML which was applied to run javascript's functions has been broken.
In code javascript: Not check null before using this element. So, the error occurs when running javascript's functions.
  // do not check null
   uiNav.scrollMgr[scrollname].mainContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "td", "ControlButtonContainer");
The old template/css before fix KS-4114 is error when the categories's breadcrums is too long. 
How is the problem fixed?

 Revert code to revision committed 80406 in file UIBreadcumbs.gtmpl, do not optimization this file. 
 Change logic javascript: Check element not null before use it.
   var controlButtonContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "td", "ControlButtonContainer");
  if (container && controlButtonContainer) {
    //.... do something
   }
 Change some css style and template for fix case the categories's breadcrums is too long. 

Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel

    * No

Tests performed at Support Level

    * No

Tests performed at QA

    * No

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

    * Function or ClassName change: None
    * Data (template, node type) migration/upgrade: None

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * PL review: Patch validated

Support Comment

    * SL3VN review: Patch validated

QA Feedbacks

    * N/A


