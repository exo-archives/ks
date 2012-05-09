Summary
	* Issue title : Page Not Found when referring a space navigation node to a portal page
	* CCP Issue:  N/A
	* Product Jira Issue: KS-4365.
	* Complexity: N/A

Proposal

 
Problem description
	* Page Not Found when referring a space navigation node to a portal page

Fix description

Problem analysis
	* Wiki application specify a page based on the input URL and the current user page node. It can be see in the API below as:

		public Page resolve(String requestURI, UserNode portalUserNode) throws Exception;
	* But in fact, only requestURI is taken into account -> If user create a space tab that refers to a portal page contains a wiki, the url resolver runs improperly 
	* And in the unit test, the input portalUserNode variables are always set to null

How is the problem fixed?
	*  Push portalUserNode in to resolving process
	*  Update and add more unit tests to ensure the process is true

Tests to perform

Reproduction test
	* Go to "CCA Community Space" to add new node1 in the navigation
	* Set the page id to portal::jeeneeglobalportal::wiki
    		-> Result: this wiki of the portal jeeneeglobalportal isn't visible
    	* Go to "CCA Community Space" to add new node2 in the navigation
    	* Set the page id to group::/spaces/jeenee_intranet_space::WikiPortlet
    		-> Result: this wiki of "jeenee intranet space" isn't visible

Tests performed at DevLevel
	* cf.above

Tests performed at Support Level
	* cf.above

Tests performed at QA
	* cf.above

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

    Function or ClassName change: No
    Data (template, node type) migration/upgrade: No

Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	*
