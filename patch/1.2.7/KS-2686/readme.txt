Summary

    * Status: "Click here" in a French notification mail
    * CCP Issue: CCP-575, Product Jira Issue: KS-2686.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
The French notification mail contains "click here": to be internationalized
Fix description

How is the problem fixed?

    * Correct label of UIForumAdministrationForm.label.notifyEmailContentDefault
      Old code:

      UIForumAdministrationForm.label.notifyEmailContentDefault=Hi,</br> You receive this email because you registered for eXo Forum and Topic Watching notification.<br/>We would like to inform you that there is a new $ADD_TYPE in the $OBJECT_WATCH_TYPE <b>$OBJECT_NAME</b> with the following content: <div>_______________<br/>$POST_CONTENT<br/>_______________</div><div>At $TIME on $DATE, posted by <b>$POSTER</b> .</div> Go directly to the post: $VIEWPOST_LINK <br/>Or go to reply to the post: $REPLYPOST_LINK

      is replaced by new code:

      UIForumAdministrationForm.label.notifyEmailContentDefault=Hi,</br> You receive this email because you registered for eXo Forum and Topic Watching notification.<br/>We would like to inform you that there is a new $ADD_TYPE in the $OBJECT_WATCH_TYPE <b>$OBJECT_NAME</b> with the following content: <div>_______________<br/>$POST_CONTENT<br/>_______________</div><div>At $TIME on $DATE, posted by <b>$POSTER</b> .</div><div>Go directly to the post: <a target="_blank" href="$VIEWPOST_LINK">Click here.</a> <br/>Or go to reply to the post: <a target="_blank" href="$REPLYPOST_LINK">Click here.</a></div>

Patch information:
Patch files: KS-2686.patch

Tests to perform

Reproduction test
To reproduce :

    * Login
    * Change language into French
    * Go to Forum portlet.
    * Watch a topic or a forum.
    * Add a reply in topic watching.
    * Check mailbox of email watching.
    * See content email. If the content is in French but the footer contains "click here" -> it is bug.
      If the text link is "Cliquez ici" -> OK.

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
*No

Configuration changes

Configuration changes:
*No

Will previous configuration continue to work?
*Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No change in function or ClassName.

Is there a performance risk/cost?
*No

Validation (PM/Support/QA)

PM Comment
* PM review: proposed patch approved.

Support Comment
* Support review: proposed patch validated

QA Feedbacks
*

