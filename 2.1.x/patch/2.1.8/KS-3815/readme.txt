Summary

Status: Manager of a space is also moderator of other space's forum
CCP Issue: CCP-1131, Product Jira Issue: KS-3815.
Complexity: N/A
The Proposal

Problem description
What is the problem to fix?

How to reproduce
1)login as root and create 2 users test1 and test2
2)logout and login as test1
3)create a new open space "test1space"
4)logout and login as test2
5)create a new space "test2space"
6)join "test1space"=>test2 is manager of "test2space" and member of "test1space"
7)Go to "test1space">forums

Expected result: only test1 is moderator of test1space>forum
Actual Result:test1 and test2 are both moderators even though test1 is just a regular plain old member of test2space

Fix description
How is the problem fixed?

- Change logic for get users have moderators of forum via user/group/membership
- Case: We have one user (example user demo)has membership type is manager in other group, and has member ship type is member:/space/space_test in group /space/space_test (is member of space "space test").
- Before apply patch:  When use function getUserPermission with membership type is manager:/space/space_test it will return value container the user demo ==> error.
- After apply patch: When use function getUserPermission with membership type is manager:/space/space_test it will return only user is manager of group /space/space_test, not container the user demo .

Patch files:KS-3815.patch

Tests to perform
Reproduction test
* 1)login as root and create 2 users test1 and test2
2)logout and login as test1
3)create a new open space "test1space"
4)logout and login as test2
5)create a new space "test2space"
6)join "test1space"=>test2 is manager of "test2space" and member of "test1space"
7)Go to "test1space">forums Expected result: only test1 is moderator of test1space>forum

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

None
Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

Labels:

