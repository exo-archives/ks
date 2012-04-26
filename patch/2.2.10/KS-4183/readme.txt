Description
	* Issue title: Users on different tenants appear online in forum in cloud workspace
	* CCP Issue: N/A
	* Product Jira Issue: KS-4183.
	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* Register to http://tenant1.wks.exoplatform.org tenant with tenant1 username.
	* Register to http://exoplatform.wks.exoplatform.org tenant with tenant2 username.
	* tenant1 can see tenant2 online in the exoplatform tenant even though tenant2 am not registered

Fix description

Problem analysis
	*The user online storage in on Collection Set for all tenant. So, all users online will display on anything of tenant.

How is the problem fixed?
	* Storage the users online in Map<String, List<String>>  onlineUserMap ; with key is tenant-name and Value is list of user online in this tenant.


Tests to perform

Reproduction test
	* Register to http://tenant1.wks.exoplatform.org tenant with tenant1 username.
	* Register to http://exoplatform.wks.exoplatform.org tenant with tenant2 username.
	* tenant1 can see tenant2 online in the exoplatform tenant even though tenant2 am not registered

Tests performed at DevLevel
	* n/a

Tests performed at Support Level
	* n/a

Tests performed at QA
	* n/a

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	* n/a

Changes in Selenium scripts 
	* n/a

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:


Configuration changes

Configuration changes:
	* n/a

Will previous configuration continue to work?
	* n/a

Risks and impacts

Can this bug fix have any side effects on current client projects?

    Function or ClassName change: 
    Data (template, node type) migration/upgrade: 

Is there a performance risk/cost?
	* n/a

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated in PLF because of no regression

QA Feedbacks
	* 
