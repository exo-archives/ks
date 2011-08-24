Summary

    * Status: Popup Resizing problem
    * CCP Issue: CCP-835, Product Jira Issue: KS-3137.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
To reproduce the problem:

    * Go to forum.
    * Click-on settings.
    * try to resize the pop up.
      ==>All the texts on the page are selected

Fix description

How is the problem fixed?

    * Set style css for KSMaskLayer of forum & answer portlet:
      ?
      user-select:none;
      -moz-user-select:none;
      -khtml-user-select:none;
      -webkit-user-select:none;
      -o-user-select:none;
    * Add attribute in div KSMaskLayer
      ?
      <div id="KSMaskLayer" class="KSMaskLayer" onselectstart="return false;" ondragstart="return false;" unselectable="on"><span></span></div>

Patch files:KS-3137.patch 

Tests to perform

Reproduction test

    * Go to forum.
    * Click-on settings.
    * try to resize the pop up only in forum portlet - not over forum portlet.
      ==>All the texts on the page are not select.

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

    * Function or ClassName change: None

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* Patch is approved

Support Comment
* Support review: patch validated

QA Feedbacks
*

