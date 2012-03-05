Summary

    * Status: Error when log in to Forum the first time
    * CCP Issue: N/A, Product Jira Issue: KS-3136.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Error in the server console when the first user opens Forum portlet.
      ?
      SEVERE: Invalid <CSS FILE> configuration, please check the @import url(SyntaxHighlighter.css) in /forum/skin/DefaultSkin/webui/Stylesheet.css , SkinService could not load the skin /ksResources/syntaxhighlighter/Styles/SyntaxHighlighter.css

Fix description

How is the problem fixed?

    * Remove this wrong import.

Patch files: KS-3136.patch

Tests to perform

Reproduction test

   1. Start PLF server
   2. Login
   3. Open Forum portlet
      The error appears in the server console.

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
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

    * Function or ClassName change: no

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*

