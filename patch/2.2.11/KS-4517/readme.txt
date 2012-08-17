Summary
Watch category/forum/topic works wrong 
CCP Issue:  N/A
Product Jira Issue: KS-4517.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?

Login as adminstrator (John for ex)
Go to forum
Create category
Select a category and click on "watch" icon : display message "You are now watching this item"-OK
Click on Settings, select my subscription tab : display default email of John-OK
Input valid email (thaopth@exoplatform.com for example) and click "Update" : Click "Save"
Click "Manage Category", select "Watches" : No email is displayed-NOK
Open Settings/ My subscriptions again : Email subscription is still default email-NOK
Close Settings form, click on "Unwatch" icon, display message "You are no longer watching this item." -OK
After "Unwatch" the first time, this button still displayed as "Unwatch"-NOK
Open Settings/My subscription/subscription email is the mail input at step 6-NOK
"Unwatch" the second times : successfully, no email is displayed
Expected result:
- Subscriptions mail must be updated
- Unwatch the first times must be successful
- Click Manage category > Watches > Subcription emails must be displayed
Note: The same issue when watch forum and topic

Fix description
Problem analysis

The watching data cache is not cleared when user watches or unwatches a category | forum | topic.
How is the problem fixed?

Clear the watching data cache when user watches or unwatches a category | forum | topic.
Git Pull Request: https://github.com/exoplatform/ks/pull/119
Tests to perform
Reproduction test

Login as adminstrator (John for ex)
Go to forum
Create category
Select a category and click on "watch" icon : display message "You are now watching this item"
Click on Settings, select my subscription tab : display default email of John
Input valid email (thaopth@exoplatform.com for example) and click "Update" : Click "Save"
Click "Manage Category", select "Watches" : No email is displayed
Open Settings/ My subscriptions again : Email subscription is still default email
Close Settings form, click on "Unwatch" icon, display message "You are no longer watching this item."
After "Unwatch" the first time, this button still displayed as "Unwatch"
Open Settings/My subscription/subscription email is the mail input at step 6
"Unwatch" the second times : successfully, no email is displayed
Tests performed at DevLevel

Login as adminstrator (John for ex)
Go to forum
Create category
Select a category and click on "watch" icon : display message "You are now watching this item"
Click on Settings, select my subscription tab : display default email of John
Input valid email (thaopth@exoplatform.com for example) and click "Update" : Click "Save"
Click "Manage Category", select "Watches" : The email input in previous step is displayed
Open Settings/ My subscriptions again : Email subscription is the input email in step 6
Close Settings form, click on "Unwatch" icon, display message "You are no longer watching this item."
After "Unwatch" the first time, this button displays as "Watch"
Open Settings/My subscription/subscription No email is displayed
Tests performed at Support Level
cf. above
Tests performed at QA
N/A
Changes in Test Referential
Changes in SNIFF/FUNC/REG tests

No
Changes in Selenium scripts 

No
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:

No
Configuration changes
Configuration changes:

No
Will previous configuration continue to work?

Yes
Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: No
Data (template, node type) migration/upgrade: No
Is there a performance risk/cost?

No
Validation (PM/Support/QA)
PM Comment
PL review: PR validated
Support Comment
Support review: PR validated
QA Feedbacks
N/A
