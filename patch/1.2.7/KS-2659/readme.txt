Summary

    * Status: Deleted user still appears in User Management in KS
    * CCP Issue: N/A, Product Jira Issue: KS-2659.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* Deleted user still appears in User Management in KS
         1. Login by root
         2. Register new user AAA
         3. Go to Forum page
         4. Click icon to view User Management form (in Forum)
         5. Click icon to edit info of user AAA -> Edit user AAA info is displayed below the Users list -> OK
         6. Login by john (in another window)
         7. Go to Community Management page
         8. In User Management --> delete user AAA
         9. Back to root
        10. Click Save at the Edit user AAA info form --> Save successfully & AAA still exist in User Management of Forum although (s)he was deleted.

Fix description

How is the problem fixed?
    * For now in the 1.2.x version when removing a portal user, KS still keeps the data about user profile to let us reference to all posts or topics of this user.
    * So when saving Edit form, we check if the user is removed from portal, we show a notification message.

Patch file: KS-2659.patch

Tests to perform

Tests performed at DevLevel
1. Login by admin of Forum
   a. Open User manager.
   b. Edit one user in forum
2. Login by admin of portal by other browser.
   a. Open Community management
   b. Click to remove user.
3. Back to the admin of forum.
   a. Click Save button in the form, message will be shown "This user has been deleted".

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
* PM review: patch approved

Support Comment
* Patch validated by the Support team

QA Feedbacks
*

