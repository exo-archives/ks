Summary
	* Issue Title: Forum migration tool from 2.1.x to 2.2.x does not work 
	* CCP Issue: N/A
	* Product Jira Issue: KS-4472.
	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* Forum migration tool from 2.1.x to 2.2.x does not work 

Fix description

Problem analysis
	* The PLF-3.0.x has oldVersion="0" but forum migration is only activated if OldVersion is greater than "2.1"
	* The fn:name() function which returns categoryID in query to get node containing categories of space, can only be used in conjunction with an equals operator.

How is the problem fixed?
	* Add new property *UNKNOWN_VERSION* when migration PLF-3.0.x to PLF-3.5.x
	* Get category by exo:id parameter which allows using not equal operator.


Tests to perform

Reproduction test
	1) Start PLF-3.0.8 (use ks-2.1.8). Login as john, create space "test", go to forum of space "test" and create some topic
	2) Migrate PLF-3.0.8 to PLF-3.5.3 (use ks 2.2.9)
	3) After migration, login as john, go to space "test"/click to tab "Foum"

     The Forum portlet does not display anything.

Tests performed at DevLevel
	* n/a

Tests performed at Support Level
	* n/a

Tests performed at QA
	* n/a

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	* No

Changes in Selenium scripts 
	* No

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:
	* No

Configuration changes

Configuration changes:
	* No

Will previous configuration continue to work?
	* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
	* Function or ClassName change: N/a
	* Data (template, node type) migration/upgrade: N/a 

Is there a performance risk/cost?
	* n/a

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	* 
