Summary
[Answer] Cannot select group/role for category after selecting user

CCP Issue:  N/A
Product Jira Issue: KS-4313.
Complexity: N/A
Impacted Client(s): N/A 

Proposal

Problem description
What is the problem to fix?

Go to Answer page
Open form to add category
In Restricted Audience or Moderator field, select 1 user ==> OK
After that, open form to add a group/role for these fields
Click icon to expand groups ==> sub-group is not shown ==> KO
Click save the category ==> Alert pop-up javascript error ==> KO.
Fix description
Problem analysis

 In form UIUserSelecter display when add users, content child from UIGroupSelector. So When we open UIGroupSelector  after open UIUserSelecter the template of form UIGroupSelector some time will cache ==> it has duplicate ==> Update template error.
 When click button save, we have reload all component of Answer portlet ==> javascript will update again ==> error.
How is the problem fixed?

When we open UIGroupSelector, if UIUserSelecter not null, we will close and remove it by function closePopupWindow.
     if (uiUserSelect != null) {
        UIPopupWindow popupWindow = uiUserSelect.getParent();
        closePopupWindow(popupWindow);
      }
 Only reload UIAnswersContainer and UIPopupAction when save  UICategoryForm.
answerPortlet.cancelAction();
event.getRequestContext().addUIComponentToUpdateByAjax(answerPortlet.getChild(UIAnswersContainer.class));
Patch file: KS-4313.patch 
Tests to perform
Reproduction test

steps ...
Tests performed at DevLevel

No
Tests performed at Support Level

No
Tests performed at QA

No
Changes in Test Referential
Changes in SNIFF/FUNC/REG tests

 Not change
Changes in Selenium scripts 

Not change
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:

Not change
Configuration changes
Configuration changes:

Not change
Will previous configuration continue to work?

Yes
Risks and impacts
Can this bug fix have any side effects on current client projects?

Not change 
Data (template, node type) migration/upgrade: 
 No
Is there a performance risk/cost?
No
Validation (PM/Support/QA)
PM Comment
PL review: Patch validated
Support Comment
SL3VN review: Patch resolves this issue
QA Feedbacks
N/A
