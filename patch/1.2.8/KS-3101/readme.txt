Summary

    * Status: Buttons hidden when start a topic in the Forum portlet
    * CCP Issue: CCP-813, Product Jira Issue: KS-3101
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
The window of "start topic" in the Forum portlet is a bit small. So, the buttons of "Preview", "Submit" and "Cancel" are not displayed correctly when setting full screen.

Fix description

How is the problem fixed?

    * Edit some style css for UITopicForm and UIPostForm.
    * Set new height when open popup UITopicForm and UIPostForm.

Patch file: KS-3101.patch

Tests to perform

Reproduction test

    * Open Forum portlet.
    * Open the web browser with full screen
    * Add new or edit topic in forum default data. In the form, all buttons "preview", "submit" and "cancel" are not visible, there is a vertical scroll bar.
    * Same problem with Add/Edit post.

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No
Documentation changes

Documentation changes:

    * Update screen shots for new form start topic, add new post.

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * None

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment

    * Patch validated

QA Feedbacks
*
