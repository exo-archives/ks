Summary

    * Status: English labels are not translated in French
    * CCP Issue: CCP-470, Product Jira Issue: KS-3001.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
In Submit question form in French: 
- "Sauver" should be changed to "Enregistrer"
- Values of Language field should be translated into French: English -> anglais; French -> français; default -> par défaut.

Fix description

How is the problem fixed?

    * Set current Locale for select box when change language.
    * Display language by current locale in select box.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:
There are currently no attachments on this page.
Tests to perform

Reproduction test
   1. Login
   2. Change language in French
   3. Go to Answers portlet
   4. Submit a question
      "Sauver" should be changed to "Enregistrer"
      Values of Language field should be translated into French: English -> anglais; French -> français; default -> par défaut

Tests performed at DevLevel
* 

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
* No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PM review: patch approved

Support Comment
* Support review :patch validated

QA Feedbacks
*

