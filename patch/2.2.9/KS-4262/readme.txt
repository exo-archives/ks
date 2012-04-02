Summary
Issue title High CPU usage whe opening some wiki pages
CCP Issue: N/A
Product Jira Issue: KS-4262.
Complexity: N/A
Impacted Client(s): N/A 

Proposal
 

Problem description
What is the problem to fix?

User create an wiki page: "How_to_install_stackato_VM_on_KVM"
2. Then user rename to: "How_to_install_stackato_VM_on_KVMz"
3. After that user rename back to original name : "How_to_install_stackato_VM_on_KVM
Fix description
Problem analysis
The problem that consume much memory when search in wiki caused by this case:

- User create an wiki page: "How_to_install_stackato_VM_on_KVM"
- Then user rename to: "How_to_install_stackato_VM_on_KVMz"
- After that user rename back to original name : "How_to_install_stackato_VM_on_KVM"

In JCR it will create 2 nodes:

/Groups/spaces/exo_sysadmin_corp/ApplicationData/eXoWiki/LinkRegistry/group@-spaces-exo_sysadmin_corp@How_to_install_stackato_VM_on_KVMz

/Groups/spaces/exo_sysadmin_corp/ApplicationData/eXoWiki/LinkRegistry/group@-spaces-exo_sysadmin_corp@How_to_install_stackato_VM_on_KVM

Each node have following node type:

<nodeType name="wiki:linkentry" isMixin="false" hasOrderableChildNodes="false">
   <supertypes>
     <supertype>nt:base</supertype>
     <supertype>mix:referenceable</supertype>
   </supertypes>
   <propertyDefinitions>
     <propertyDefinition name="alias" requiredType="String" autoCreated="false" mandatory="false" onParentVersion="COPY" protected="false" multiple="false">
       <valueConstraints/>
     </propertyDefinition>
     <propertyDefinition name="title" requiredType="String" autoCreated="false" mandatory="false" onParentVersion="COPY" protected="false" multiple="false">
       <valueConstraints/>
     </propertyDefinition>
     <propertyDefinition name="newlink" requiredType="Path" autoCreated="false" mandatory="false" onParentVersion="COPY" protected="false" multiple="false">
       <valueConstraints/>
     </propertyDefinition>
   </propertyDefinitions>
   <childNodeDefinitions/>
 </nodeType>
So when user do above case, newlink property on each node have following value:

Node	newlink value
group@-spaces-exo_sysadmin_corp@How_to_install_stackato_VM_on_KVMz	/Groups/spaces/exo_sysadmin_corp/ApplicationData/eXoWiki/LinkRegistry/group@-spaces-exo_sysadmin_corp@How_to_install_stackato_VM_on_KVM
group@-spaces-exo_sysadmin_corp@How_to_install_stackato_VM_on_KVM	/Groups/spaces/exo_sysadmin_corp/ApplicationData/eXoWiki/LinkRegistry/group@-spaces-exo_sysadmin_corp@How_to_install_stackato_VM_on_KVMz
When do a wiki search, following code will be executed:
org.exoplatform.wiki.webui.UIWikiAdvanceSearchResult.getPageSearchName

protected String getPageSearchName(Wiki wiki, String pageTitle) throws Exception {
   if (pageTitle.indexOf(keyword) >= 0) return "";
   if(registry == null) {
      registry = ((WikiImpl) wiki).getLinkRegistry();
   }
    Map<String, LinkEntry> linkEntries = registry.getLinkEntries();
    String titleBefore, titleAfter;
    List<LinkEntry> linkEntrys = new ArrayList<LinkEntry>();
    List<String> alias = new ArrayList<String>();
   for (LinkEntry linkEntry : linkEntries.values()) {
     if (alias.contains(linkEntry.getAlias())) continue;
     while (true) {
        alias.add(linkEntry.getAlias());
        titleAfter = linkEntry.getTitle();
        linkEntrys.add(linkEntry);
        linkEntry = linkEntry.getNewLink();
       if(linkEntry == null) break;
        titleBefore = linkEntry.getTitle();
       if(!CommonUtils.isEmpty(titleBefore) && 
            titleBefore.equals(pageTitle) && titleAfter.equals(titleBefore)) {
         for (LinkEntry entry : linkEntrys) {
           if (entry.getTitle().indexOf(keyword) >= 0) {
             return entry.getTitle();
           }
         }
         break;
       }
       if (CommonUtils.isEmpty(titleBefore) || titleAfter.equals(titleBefore)) {
          linkEntrys.clear();
         break;
       }
     }
   }
   return "";
 }
In this case, the loop will be very bad, and it loop milion times, and consume much memory.

Another bad code, It's exactly the cause of ITOP-1072.
It doesn't cause memory leak, but cause CPU peak several hours (100%)

public Page getRelatedPage(String wikiType, String wikiOwner, String pageId) throws Exception {
    Model model = getModel();
    WikiImpl wiki = (WikiImpl) getWiki(wikiType, wikiOwner, model);
    LinkRegistry linkRegistry = wiki.getLinkRegistry();
    LinkEntry oldLinkEntry = linkRegistry.getLinkEntries().get(getLinkEntryName(wikiType, wikiOwner, pageId));
    LinkEntry newLinkEntry = null;
   if (oldLinkEntry != null) {
      newLinkEntry = oldLinkEntry.getNewLink();
   }
   while (newLinkEntry != null && !newLinkEntry.equals(oldLinkEntry)) {
      oldLinkEntry = newLinkEntry;
      newLinkEntry = oldLinkEntry.getNewLink();
   }
   if (newLinkEntry == null) {
     return null;
   }
    String linkEntryAlias = newLinkEntry.getAlias();
    String[] splits = linkEntryAlias.split("@");
    String newWikiType = splits[0];
    String newWikiOwner = splits[1];
    String newPageId = linkEntryAlias.substring((newWikiType + "@" + newWikiOwner + "@").length());
   return getPageById(newWikiType, newWikiOwner, newPageId);
 }
The bad loop:

while (newLinkEntry != null && !newLinkEntry.equals(oldLinkEntry)) {
      oldLinkEntry = newLinkEntry;
      newLinkEntry = oldLinkEntry.getNewLink();
   }
How is the problem fixed?

Use RenamedMixin to get old page title in search result instead of LinkRegistry.
Check a circular rename when renaming or moving a wiki page and break it.
Use a flag (value=1000), to detect old circular data and break circular chaining list.
Patch file: KS-4262.patch
Tests to perform
Reproduction test

steps ...
Tests performed at DevLevel
cf. above
Tests performed at Support Level
cf. above
Tests performed at QA

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
Non
Changes in Selenium scripts 
Non
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:
Non

Configuration changes
Configuration changes:
* Non

Will previous configuration continue to work?
* Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: 
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?
Non


Validation (PM/Support/QA)
PM Comment
PL review: Patch validated
Support Comment
N/A
QA Feedbacks
N/A

