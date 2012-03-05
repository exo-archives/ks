Summary

    * Status: Displaying total topics in forum is negative in special case
    * CCP Issue: N/A, Product Jira Issue: KS-3166.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *   Displaying total topics in forum is negative in special case

Fix description

How is the problem fixed?

    *  Checking update total topics in forum. Can not update this number when remove topic is waiting or unapproved.


Patch files: KS-3166.patch

Tests to perform

Reproduction test
*
Steps to reproduce:

    * Login by administrator: define "censor keyword" and save
    * Login by normal user:
      + add about 3 topics that includes censor keyword into forum (AAA)
      -> Added is invisible with user -> OK
    * Back to administrator: censored topics are displayed
      then, delete all censored topics and back to Forum home
      -> show total topics in forum (AAA) is "-1" -> error

?
 

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No
Documentation changes

Documentation changes:
* Not
Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: Not change

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review : patch approved

Support Comment
* Support review: patch validated

QA Feedbacks
