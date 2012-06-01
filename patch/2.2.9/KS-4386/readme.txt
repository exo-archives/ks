Summary
[wiki] Performance on navigation

CCP Issue: N/A
Product Jira Issue: KS-4386.
Complexity: N/A
Summary
Proposal
Problem description
Fix description
Tests to perform
Changes in Test Referential
Documentation changes
Configuration changes
Risks and impacts
Validation (PM/Support/QA)
This page should represent the synthesis of information known about the issue fix.
This information will be used to create the general release notes file.
eXo internal information

Impacted Client(s): N/A 
Proposal
 

Problem description
What is the problem?

We don't have good performance when you want to navigate (especially on the intranet)
Improve navigation menu and child content
Improve page open (get content) and edition mode
Ask to deploy on intranet when done.
To reproduce this problem: 
This problem very easily by click to the "Home" wiki page on Engineering space. It took a lot of time to rendering the tree explorer (nearly one minute on my computer). We can improve by render just one or two level of that tree (or something else) to make it faster.

Fix description
Problem analysis

CPU leak in PageTreeMacro:
TreeNode node = TreeUtils.getDescendants(params, context);    
..
node.pushDescendants(context);
It's unnecessary to push all descendants in order to create a tree node at this stage. Because only its path is used later:

.append(node.getPath())
How is the problem fixed?

When creating new tree node for a page, don't push all level descendants into it. To do that:
Create a new function named getDescendants is used to return a tree node which already contain all descendants
Change the function getTreeNode to return tree node only
Change to use getTreeNode function in PageTreeMacro
Some minor improvements:
The page tree macro should show the first level of children by default
The loading animation of tree should be smoother by adding "loading.."
Patch file: PROD-ID.patch
Tests to perform
Reproduction test

cf.above
Tests performed at DevLevel

 cf.above
Tests performed at Support Level

 cf.above
Tests performed at QA

 cf.above
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
PL review: Patch validated
Support Comment
N/A
QA Feedbacks
N/A
