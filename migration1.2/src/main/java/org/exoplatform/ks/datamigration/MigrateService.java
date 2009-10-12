package org.exoplatform.ks.datamigration;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class MigrateService implements Startable{
	
	private ConfigurationManager configManager_ ;
	private ExtendedNodeTypeManager ntManager_ ;
	private NodeHierarchyCreator nodeHierarchy_ ;
	private InitParams params_ ;
	private static Log log = ExoLogger.getLogger(MigrateService.class) ;
	public MigrateService(ConfigurationManager configManager, NodeHierarchyCreator nodeHierarchy, InitParams params) throws Exception {
		configManager_ = configManager ;
		nodeHierarchy_ = nodeHierarchy ;
		params_ = params ;
	}
	
	public void start() {
		
		try{			
			//FAQ migration
			ExoContainer container = ExoContainerContext.getCurrentContainer();
			RepositoryService repoService = (RepositoryService)container.getComponentInstance(RepositoryService.class) ;
			ntManager_ = repoService.getCurrentRepository().getNodeTypeManager() ;
			NodeTypeIterator ntIter = ntManager_.getAllNodeTypes() ;
			boolean hasAnswerNT = false ;
			boolean hasFAQHomeNT = false ;
			while(ntIter.hasNext()) {
				NodeType nt = ntIter.nextNodeType() ;
				if(nt.getName().equals("exo:answer")) hasAnswerNT = true ;
				else if(nt.getName().equals("exo:faqHome")) hasFAQHomeNT = true ;
			}
			
			if(hasAnswerNT && !hasFAQHomeNT) {
				//Export data from FAQ 1.1
				exportFAQData1_1() ;
				removeFAQNodetypes1_1() ;
			}else {
				if(!hasAnswerNT){
					//Import & migrate data from 1.1 -> 1.2
					registerFAQNodetypeForMigration() ;
					faqMigration() ;
					removeFAQMigragionNodetypes() ;
				}else {
					// Import 1.2 data
					importNewFAQData() ;
				}								
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}
		
		//Forum migration
		try{
			ExoContainer container = ExoContainerContext.getCurrentContainer();
			RepositoryService repoService = (RepositoryService)container.getComponentInstance(RepositoryService.class) ;
			ntManager_ = repoService.getCurrentRepository().getNodeTypeManager() ;
			NodeTypeIterator ntIter = ntManager_.getAllNodeTypes() ;
			boolean hasForumDataNT = false ;
			boolean hasForumServiceNT = false ;
			while(ntIter.hasNext()) {
				NodeType nt = ntIter.nextNodeType() ;
				if(nt.getName().equals("exo:forumData")) hasForumDataNT = true ;
				else if(nt.getName().equals("exo:forumHome")) hasForumServiceNT = true ;
			}
			
			if(!hasForumDataNT && hasForumServiceNT) { // running on version 1.1
				//Export data from Form 1.1
				exportForumData1_1() ;
				//remove data of nodetypes 1.1 in JCR
				removeForumNodetypes1_1() ;
			}else {
				if(!hasForumServiceNT){ //Import & migrate data from 1.1 -> 1.2
					//register nodetypes for migration
					registerForumNodetypeForMigration() ;
					//import and migrate data to 1.2
					forumMigration() ;
					//remove data of nodetypes for migration in JCR
					removeForumMigragionNodetypes() ;
				}else { 
					// Import 1.2 data
					importNewForumData() ;
				}								
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}				
	}
	
	public void stop() {}
	
	private void exportFAQData1_1() {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node faqHome = getFAQHome(sProvider) ;
			log.info("\n\nExporting FAQ data version 1.1 ...") ;
			String file = params_.getValueParam("faqData1.1").getValue() ;
			ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
			faqHome.getSession().exportSystemView(faqHome.getPath(), bos, false, false) ;
			saveToFile(bos.toByteArray(), file) ;			
			log.info("\nFAQ data has exported to " + file + "\n\n") ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;} 
		
	}
	
	private void exportForumData1_1() {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node forumHome = getForumHome(sProvider) ;
			log.info("\n\nExporting Forum data version 1.1 ...") ;
			String file = params_.getValueParam("forumData1.1").getValue() ;
			ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
			forumHome.getSession().exportSystemView(forumHome.getPath(), bos, false, false) ;
			saveToFile(bos.toByteArray(), file) ;
			log.info("\nForum data has exported to " + file + "\n") ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;}
	}
	
	private void registerFAQNodetypeForMigration () throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			//remove data
			/*Node faqHome = getFAQHome(sProvider) ;
			Node parent = faqHome.getParent() ;
			faqHome.remove() ;
			parent.save() ;*/
			//remove FAQ 1.2 nodetypes			
			/*Node nodeTypes = (Node)faqHome.getSession().getItem("/jcr:system/jcr:nodetypes") ;
			String nt_12_File = params_.getValueParam("faqNodetypes1.2").getValue() ;
			InputStream in_12 = configManager_.getInputStream(nt_12_File) ;
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    	Document doc = docBuilder.parse(in_12);
    	NodeList list = doc.getElementsByTagName("nodeType") ;
    	for(int i = 0; i < list.getLength(); i ++) {
    		try{    			
    			System.out.println("====>" + list.item(i).getAttributes().getNamedItem("name").getNodeValue());
      		Node NT = nodeTypes.getNode(list.item(i).getAttributes().getNamedItem("name").getNodeValue()) ;
  				NT.remove() ;
    		}catch(Exception e) {
    			e.printStackTrace() ;
    		}
    	}*/
    	//nodeTypes.getSession().save() ;
			
			//Register migrate data nodetypes
			log.info("Register FAQ nodetypes for migrate data\n") ;
			String ntFile = params_.getValueParam("migrationFAQNodetypes").getValue() ;
			InputStream in = configManager_.getInputStream(ntFile) ;
			ntManager_.registerNodeTypes(in, ExtendedNodeTypeManager.IGNORE_IF_EXISTS) ;
			log.info("FAQ nodetypes for migrate data registered \n") ;
		}catch(Exception e){
			e.printStackTrace() ;
		}finally{sProvider.close() ;}
		
	}
	
	private void registerForumNodetypeForMigration () throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			//Register migrate data nodetypes
			log.info("Register Forum nodetypes for migrate data") ;
			String ntFile = params_.getValueParam("migrationForumNodetypes").getValue() ;
			InputStream in = configManager_.getInputStream(ntFile) ;
			ntManager_.registerNodeTypes(in, ExtendedNodeTypeManager.IGNORE_IF_EXISTS) ;
			log.info("Forum nodetypes for migrate data registered \n") ;
		}catch(Exception e){
			e.printStackTrace() ;
		}finally{sProvider.close() ;}		
	}
	
	private void removeFAQNodetypes1_1() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node faqHome = getFAQHome(sProvider) ;
			//remove FAQ 1.1 nodetypes			
			log.info("Removing FAQ 1.1 nodetypes in JCR") ;
			Node nodeTypes = (Node)faqHome.getSession().getItem("/jcr:system/jcr:nodetypes") ;
			String nt_migrate_File = params_.getValueParam("faqNodetypes1.1").getValue() ;
			InputStream in_migrate = configManager_.getInputStream(nt_migrate_File) ;
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    	Document doc = docBuilder.parse(in_migrate);
    	NodeList list = doc.getElementsByTagName("nodeType") ;
    	for(int i = 0; i < list.getLength(); i ++) {
    		try{
    			log.info("====>" + list.item(i).getAttributes().getNamedItem("name").getNodeValue()) ;
      		Node NT = nodeTypes.getNode(list.item(i).getAttributes().getNamedItem("name").getNodeValue()) ;  				
  				NT.remove() ;
    		}catch(Exception e) {
    			e.printStackTrace() ;
    		}
    	}    				
    	faqHome.remove() ;
    	nodeTypes.getSession().save() ;
    	log.info("Nodetypes removed ") ;
		}catch(Exception e){
			e.printStackTrace() ;
		}finally{sProvider.close() ;}
	}
	
	private void removeForumNodetypes1_1() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node forumHome = getForumHome(sProvider) ;
			//remove FAQ 1.1 nodetypes			
			Node nodeTypes = (Node)forumHome.getSession().getItem("/jcr:system/jcr:nodetypes") ;
			String nt_11_file = params_.getValueParam("forumNodetypes1.1").getValue() ;
			InputStream in_migrate = configManager_.getInputStream(nt_11_file) ;
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    	Document doc = docBuilder.parse(in_migrate);
    	NodeList list = doc.getElementsByTagName("nodeType") ;
    	log.info("Removing forum 1.1 nodetypes in JCR") ;
    	for(int i = 0; i < list.getLength(); i ++) {
    		try{
    			log.info("====>" + list.item(i).getAttributes().getNamedItem("name").getNodeValue()) ;
      		Node NT = nodeTypes.getNode(list.item(i).getAttributes().getNamedItem("name").getNodeValue()) ;  				
  				NT.remove() ;
    		}catch(Exception e) {
    			e.printStackTrace() ;
    		}
    	}    				
    	forumHome.remove() ;
    	nodeTypes.getSession().save() ;
    	log.info("Nodetypes removed") ;			
		}catch(Exception e){
			e.printStackTrace() ;
		}finally{sProvider.close() ;}
	}
	
	private void removeFAQMigragionNodetypes () throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node faqHome = getFAQHome(sProvider) ;
			//remove FAQ 1.2 nodetypes			
			Node nodeTypes = (Node)faqHome.getSession().getItem("/jcr:system/jcr:nodetypes") ;
			//List<String> nts = params_.getValuesParam("removeFAQNodeTypes1.2").getValues();
			String nt_migrate_File = params_.getValueParam("migrationFAQNodetypes").getValue() ;
			InputStream in_migrate = configManager_.getInputStream(nt_migrate_File) ;
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    	Document doc = docBuilder.parse(in_migrate);
    	NodeList list = doc.getElementsByTagName("nodeType") ;
    	
    	log.info("Removing FAQ migrate data nodetypes");
    	for(int i = 0; i < list.getLength(); i ++) {
    		try{
    			log.info("====>" + list.item(i).getAttributes().getNamedItem("name").getNodeValue());
      		Node NT = nodeTypes.getNode(list.item(i).getAttributes().getNamedItem("name").getNodeValue()) ;  				
  				NT.remove() ;
    		}catch(Exception e) {
    			e.printStackTrace() ;
    		}
    	}
    	log.info("FAQ Nodetypes removed");			
    	//faqHome.remove() ;
    	nodeTypes.getSession().save() ;
		}finally{sProvider.close() ;}
		
	}
	
	private void removeForumMigragionNodetypes () throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node forumHome = getForumHome(sProvider) ;
			//remove migration nodetypes in JCR			
			Node nodeTypes = (Node)forumHome.getSession().getItem("/jcr:system/jcr:nodetypes") ;
			String nt_migrate_File = params_.getValueParam("migrationForumNodetypes").getValue() ;
			InputStream in_migrate = configManager_.getInputStream(nt_migrate_File) ;
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    	Document doc = docBuilder.parse(in_migrate);
    	NodeList list = doc.getElementsByTagName("nodeType") ;
    	
    	log.info("Removing forum migrate data nodetypes");
    	for(int i = 0; i < list.getLength(); i ++) {
    		try{
    			log.info("====>" + list.item(i).getAttributes().getNamedItem("name").getNodeValue());
      		Node NT = nodeTypes.getNode(list.item(i).getAttributes().getNamedItem("name").getNodeValue()) ;  				
  				NT.remove() ;
    		}catch(Exception e) {
    			e.printStackTrace() ;
    		}
    	}
    	log.info("Forum Nodetypes removed");			
    	//faqHome.remove() ;
    	nodeTypes.getSession().save() ;
		}finally{sProvider.close() ;}
		
	}
	
	private void importNewFAQData() {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			String faqData12 = params_.getValueParam("faqData1.2").getValue() ;
			InputStream in = configManager_.getInputStream(faqData12) ;
			Node faqHome = getFAQHome(sProvider) ;
			Node parent = faqHome.getParent() ;
			//if(faqHome.hasNodes()) return ;
			faqHome.remove() ;
			parent.save() ;
			log.info("\n >>>>>> Knowledge Suite: FAQ's data version 1.2 is importing.... \n");
			parent.getSession().importXML(parent.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
			parent.getSession().save() ;			
			log.info("\n >>>>>> Knowledge Suite: FAQ's data version 1.2 is imported succesful !\n");			
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;}
	}
	
	private void faqMigration() {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node tempNode = getFAQTemp(sProvider) ;
			if (tempNode.hasNodes()) return ;
			log.info("\n\n Knowledge Suite 1.2: Migrating FAQ data from 1.1 to 1.2 ....");
			String faqData11 = params_.getValueParam("faqData1.1").getValue() ;
			log.info("faqData11===>" + faqData11);
			InputStream in = configManager_.getInputStream(faqData11) ;
			tempNode.getSession().importXML(tempNode.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
			tempNode.getSession().save() ;
			log.info("\n >>>>>> Data version 1.1 has imported \n");
			migrateFAQData() ;			
			Node faqHome = getFAQHome(sProvider) ;
			ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
			faqHome.getSession().exportSystemView(faqHome.getPath(), bos, false, false) ;
			String faqData12 = params_.getValueParam("faqData1.2").getValue() ;
			saveToFile(bos.toByteArray(), faqData12) ;
			Node parent = faqHome.getParent() ;
			faqHome.remove() ;
			//tempNode.remove() ;
			parent.getSession().save() ;
			log.info("\n >>>>>> Data version 1.2 has exported to file " + faqData12 );
			log.info("\n Knowledge Suite: Migrate FAQ data 1.1 to 1.2 is finished .... \n\n");
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;}
		
	}
	
	private void migrateFAQData() {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node tempData = getFAQTemp(sProvider) ;
			if(!tempData.hasNodes()) {
				log.info(">>>>>> There is no FAQ's data for migrating!") ;
				return ;
			}			
			Node faqTempHome = tempData.getNode("faqApp") ;			
			//Migrate Category
			if(faqTempHome.hasNode("catetories")) {
				migrateFAQCategory(faqTempHome.getNode("catetories")) ;
			}
			Node parent = tempData.getParent() ;
			tempData.remove() ;			
			parent.save() ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;}
	}
	
	private Node getFAQTemp(SessionProvider sProvider) throws Exception {
		Node userApp = nodeHierarchy_.getPublicApplicationNode(sProvider)	;
		try {
			return	userApp.getNode("FAQ_TEMP") ;
		} catch (PathNotFoundException ex) {
			userApp.addNode("FAQ_TEMP", "nt:unstructured") ;
			userApp.getSession().save() ;
			return userApp.getNode("FAQ_TEMP") ;
		}		
	}
	
	private Node getFAQHome(SessionProvider sProvider) throws Exception {
		Node userApp = nodeHierarchy_.getPublicApplicationNode(sProvider)	;
		try {
			return	userApp.getNode("faqApp") ;
		} catch (PathNotFoundException ex) {
			userApp.addNode("faqApp", "exo:faqHome") ;
			userApp.getSession().save() ;
			return userApp.getNode("faqApp") ;
		}		
	}
	
	private Node getFAQCategoryHome(SessionProvider sProvider) throws Exception {
		try {
			return	getFAQHome(sProvider).getNode("categories") ;
		} catch (PathNotFoundException ex) {
			Node catHome = getFAQHome(sProvider).addNode(org.exoplatform.faq.service.Utils.CATEGORY_HOME, "exo:faqCategory") ;
			catHome.setProperty("exo:name", "Root") ;
			catHome.addMixin("mix:faqSubCategory") ;
			catHome.setProperty("exo:isView", true) ;
			//catHome.addNode(org.exoplatform.faq.service.Utils.QUESTION_HOME, "exo:faqQuestionHome") ;
			catHome.getSession().save() ;
			return catHome ;
		}		
	}
	
	private void migrateFAQCategory(Node tempcatHome) {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node categoryHome = getFAQCategoryHome(sProvider) ;
			NodeIterator iter = tempcatHome.getNodes() ;
			log.info(" >>>> Migrating FAQ categories ");
			while(iter.hasNext()) {
				Node node = iter.nextNode() ;
				categoryHome.getSession().getWorkspace().move(node.getPath(), categoryHome.getPath() + "/" + node.getName()) ;
			}
			categoryHome.getSession().save() ;
			repairQuestion() ;
			migrateCategoryChildren() ;			
			
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
	}
	
	private void repairQuestion() {
		//repairing question that is created in root category
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node tempNode = getFAQTemp(sProvider) ;
			QueryManager qm = tempNode.getSession().getWorkspace().getQueryManager();
			StringBuffer queryBuffer = new StringBuffer();
			queryBuffer.append("/jcr:root").append(tempNode.getPath()).append("//element(*,exo:faqQuestion)[@exo:categoryId='null']") ;
			Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			while(iter.hasNext()) {
				Node question = iter.nextNode() ;
				question.setProperty("exo:categoryId", org.exoplatform.faq.service.Utils.CATEGORY_HOME) ;
				question.save() ;				
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}		
	}
	
	private void migrateCategoryChildren() {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node tempNode = getFAQTemp(sProvider) ;
			Node categoryHome = getFAQHome(sProvider) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryBuffer = new StringBuffer();
			queryBuffer.append("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:faqCategory)") ;
			Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			while(iter.hasNext()) {
				Node category = iter.nextNode() ;
				if(category.canAddMixin("mix:faqSubCategory")) {
					category.addMixin("mix:faqSubCategory") ;					
				}
				category.setProperty("exo:isView", true) ;
				Node questionHome = category.addNode(org.exoplatform.faq.service.Utils.QUESTION_HOME, "exo:faqQuestionHome") ;
				category.save() ;
				migrateQuestion(category.getName(), questionHome, tempNode) ;
			}			
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}		
	}
	
	private void migrateQuestion(String categoryId, Node questionHome, Node tempData) {
		log.info("Migrating questions .... \n") ;
		try{
			QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryBuffer = new StringBuffer();
			queryBuffer.append("/jcr:root").append(tempData.getPath()).append("//element(*,exo:faqQuestion)") ;
			queryBuffer.append("[@exo:categoryId='" + categoryId + "']") ;
			Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			while(iter.hasNext()){
				Node question = iter.nextNode() ;
				log.info("question ===> "+ question.getPath()) ;
				try{					
					migrateAnswer(question) ;
					migrateComment(question) ;
					migrateLanguage(question) ;
					migrateAttachment(question) ;
					//question.save() ;
					questionHome.getSession().getWorkspace().move(question.getPath(), questionHome.getPath() + "/" + question.getName()) ;
					questionHome.save() ;
				}catch(Exception e) {
					e.printStackTrace() ;
				}				
			}			
		}catch(Exception e) {
			e.printStackTrace() ;
		}
		log.info("End migrate questions \n") ;
	}
	
	private void migrateAttachment(Node question) {
		log.info("===> Migrating attachments...");
		try{
			NodeIterator iter  = question.getNodes() ;
			log.info("attsize ===>" + iter.getSize());
			while(iter.hasNext()) {
				Node att = iter.nextNode() ;
				if(att.isNodeType("nt:file") || att.isNodeType("exo:faqAttachment")) {
					Node attTemp = att ;
					Node nodeFile = question.addNode("file" + IdGenerator.generate(), "exo:faqAttachment");
					FAQServiceUtils.reparePermissions(nodeFile, "any");
					Node nodeContent = nodeFile.addNode("jcr:content", "exo:faqResource") ;
					nodeContent.setProperty("exo:fileName", attTemp.getProperty("exo:fileName").getValue()) ;
					nodeContent.setProperty("exo:categoryId", question.getProperty("exo:categoryId").getValue()) ;
					nodeContent.setProperty("jcr:mimeType", attTemp.getNode("jcr:content").getProperty("jcr:mimeType").getValue());
					nodeContent.setProperty("jcr:data", attTemp.getNode("jcr:content").getProperty("jcr:data").getValue());
					nodeContent.setProperty("jcr:lastModified", attTemp.getNode("jcr:content").getProperty("jcr:lastModified").getValue());				
					log.info("fileName ===>" + nodeContent.getProperty("exo:fileName").getString());
					att.remove() ;
				}
			}
			question.save() ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}
	}
	
	private void migrateAnswer(Node question) {
		log.info("===> Migrating anwsers...");
		try{
			if(question.hasNode(org.exoplatform.faq.service.Utils.ANSWER_HOME)) {
				question.getSession().getWorkspace().move(question.getPath()+"/" + org.exoplatform.faq.service.Utils.ANSWER_HOME, question.getPath()+"/answerTemp") ;
				question.save() ;
				Node temp = question.getNode("answerTemp") ;
				Node answerHome = question.addNode(org.exoplatform.faq.service.Utils.ANSWER_HOME, "exo:answerHome") ;
				question.save() ;
				NodeIterator iter = temp.getNodes() ;
				while(iter.hasNext()) {
					Node ans = iter.nextNode() ;
					ans.setProperty("exo:questionId", question.getName()) ;
					ans.setProperty("exo:categoryId", question.getProperty("exo:categoryId").getString()) ;
					ans.setProperty("exo:responseLanguage", question.getProperty("exo:language").getString()) ;
					ans.setProperty("exo:fullName", ans.getProperty("exo:responseBy").getString()) ;
					ans.save() ;
					answerHome.getSession().getWorkspace().move(ans.getPath(), answerHome.getPath()+"/" + ans.getName()) ;
				}
				temp.remove() ;
				question.save() ;
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}
	}
	
	private void migrateComment(Node question) {
		log.info("===> Migrating comments...");
		try{
			if(question.hasNode(org.exoplatform.faq.service.Utils.COMMENT_HOME)) {
				question.getSession().getWorkspace().move(question.getPath()+"/" + org.exoplatform.faq.service.Utils.COMMENT_HOME, question.getPath()+"/commentTemp") ;
				question.save() ;
				Node temp = question.getNode("commentTemp") ;
				Node commentHome = question.addNode(org.exoplatform.faq.service.Utils.COMMENT_HOME, "exo:commentHome") ;
				question.save() ;
				NodeIterator iter = temp.getNodes() ;
				while(iter.hasNext()) {
					Node comment = iter.nextNode() ;
					comment.setProperty("exo:fullName", comment.getProperty("exo:commentBy").getString()) ;
					comment.save() ;
					commentHome.getSession().getWorkspace().move(comment.getPath(), commentHome.getPath()+"/" + comment.getName()) ;
				}
				temp.remove() ;
				question.save();
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}
	}
	
	private void migrateLanguage(Node question) {
		log.info("===> Migrating other question languages ...");
		try{
			if(question.hasNode(org.exoplatform.faq.service.Utils.LANGUAGE_HOME)) {
				question.getSession().getWorkspace().move(question.getPath()+"/" + org.exoplatform.faq.service.Utils.LANGUAGE_HOME, question.getPath()+"/languageTemp") ;
				question.save() ;
				Node temp = question.getNode("languageTemp") ;
				Node languageHome = question.addNode(org.exoplatform.faq.service.Utils.LANGUAGE_HOME, "exo:questionLanguageHome") ;
				NodeIterator iter = temp.getNodes() ;
				while(iter.hasNext()) {
					Node language = iter.nextNode() ;
					Node newLang = languageHome.addNode(language.getName(), "exo:faqLanguage") ;
					newLang.setProperty("exo:language", language.getProperty("exo:language").getString()) ;
					newLang.setProperty("exo:name", language.getProperty("exo:name").getString()) ;
					newLang.setProperty("exo:title", language.getProperty("exo:title").getString()) ;
					newLang.setProperty("exo:questionId", question.getName()) ;
					newLang.setProperty("exo:categoryId", question.getProperty("exo:categoryId").getString()) ;
					Node ansHome = newLang.addNode(org.exoplatform.faq.service.Utils.ANSWER_HOME, "exo:answerHome") ;
					Node commentHome = newLang.addNode(org.exoplatform.faq.service.Utils.COMMENT_HOME, "exo:commentHome") ;
					question.save() ;
					
					//migrate answer for multi languages
					if(language.hasNode(org.exoplatform.faq.service.Utils.ANSWER_HOME)) {
						NodeIterator ansIter = language.getNode(org.exoplatform.faq.service.Utils.ANSWER_HOME).getNodes() ;
						while(ansIter.hasNext()) {
							Node ans = ansIter.nextNode() ;
							ansHome.getSession().getWorkspace().move(ans.getPath(), ansHome.getPath()+"/" + ans.getName()) ;
						}
						ansHome.getSession().save() ;
					}					
					
					//migrate comment for multi languages
					if(language.hasNode(org.exoplatform.faq.service.Utils.COMMENT_HOME)) {
						NodeIterator commentIter = language.getNode(org.exoplatform.faq.service.Utils.COMMENT_HOME).getNodes() ;
						while(commentIter.hasNext()) {
							Node comment = commentIter.nextNode() ;
							commentHome.getSession().getWorkspace().move(comment.getPath(), commentHome.getPath()+"/" + comment.getName()) ;
						}
						commentHome.getSession().save() ;
					}					
				}
				temp.remove() ;
				languageHome.getSession().save() ;
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}
	}	
	
	// Migrate data for Forum
	private void forumMigration() {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node tempNode = getForumTemp(sProvider) ;
			if (tempNode.hasNodes()) return ;
			log.info("\n\n Knowledge Suite 1.2: Migrating Forum data from 1.1 to 1.2 ....");
			String forumData11 = params_.getValueParam("forumData1.1").getValue() ;
			log.info("forumData11===>" + forumData11);
			InputStream in = configManager_.getInputStream(forumData11) ;
			tempNode.getSession().importXML(tempNode.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
			tempNode.getSession().save() ;
			log.info("\n >>>>>> Data version 1.1 has imported \n");
			migrateForumData() ;			
			Node forumHome = getForumHome(sProvider) ;
			
			//remove old data
			NodeIterator iter = forumHome.getNodes() ;
			while(iter.hasNext()) {
				Node data = iter.nextNode() ;
				if(!data.getName().equals(Utils.FORUM_SYSTEM) && !data.getName().equals(Utils.FORUM_DATA)) {
					log.info(" Removed ==> " + data.getPath());
					data.remove() ;					
				}				
			}
			forumHome.getSession().save() ;			
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
			forumHome.getSession().exportSystemView(forumHome.getPath(), bos, false, false) ;
			String forumData12 = params_.getValueParam("forumData1.2").getValue() ;
			saveToFile(bos.toByteArray(), forumData12) ;
			log.info("\n >>>>>> Data version 1.2 has exported to file " + forumData12 + "\n");
			log.info("\n\n Knowledge Suite: Migrate Forum data 1.1 to 1.2 is finished .... \n\n");
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;}
	}
	
	private void importNewForumData() {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			String forumData12 = params_.getValueParam("forumData1.2").getValue() ;
			InputStream in = configManager_.getInputStream(forumData12) ;
			Node forumHome = getForumHome(sProvider) ;
			Node parent = forumHome.getParent() ;
			//if(forumHome.hasNodes()) return ;
			forumHome.remove() ;
			parent.save() ;
			log.info("\n >>>>>> Knowledge Suite: Forum's data version 1.2 is importing.... \n");
			parent.getSession().importXML(parent.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
			parent.getSession().save() ;			
			log.info("\n >>>>>> Knowledge Suite: Forum's data version 1.2 is imported succesful !\n");			
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;}
	}
	
	private void saveToFile(byte[] data, String path) throws Exception {
		FileOutputStream file = new FileOutputStream(path.replaceAll("file:", "")) ;
		file.write(data) ;
		file.flush() ;
		file.close() ;
	}
	
	private Node getForumTemp(SessionProvider sProvider) throws Exception {
		Node userApp = nodeHierarchy_.getPublicApplicationNode(sProvider)	;
		try {
			return	userApp.getNode("FORUM_TEMP") ;
		} catch (PathNotFoundException ex) {
			userApp.addNode("FORUM_TEMP") ;
			userApp.getSession().save() ;
			return userApp.getNode("FORUM_TEMP") ;
		}		
	}
	
	private Node getForumHome(SessionProvider sProvider) throws Exception {
		Node userApp = nodeHierarchy_.getPublicApplicationNode(sProvider)	;
		try {
			return	userApp.getNode(Utils.FORUM_SERVICE) ;
		} catch (PathNotFoundException ex) {
			userApp.addNode(Utils.FORUM_SERVICE, "exo:forumHome") ;
			userApp.getSession().save() ;
			return userApp.getNode(Utils.FORUM_SERVICE) ;
		}		
	}
	
	private Node getForumSystem(SessionProvider sProvider) throws Exception {
		Node forumHome = getForumHome(sProvider)	;
		try {
			return	forumHome.getNode(Utils.FORUM_SYSTEM) ;
		} catch (PathNotFoundException ex) {
			forumHome.addNode(Utils.FORUM_SYSTEM, "exo:forumSystem") ;
			forumHome.getSession().save() ;
			return forumHome.getNode(Utils.FORUM_SYSTEM) ;
		}		
	}
	
	private Node getForumData(SessionProvider sProvider) throws Exception {
		Node forumHome = getForumHome(sProvider)	;
		try {
			return	forumHome.getNode(Utils.FORUM_DATA) ;
		} catch (PathNotFoundException ex) {
			forumHome.addNode(Utils.FORUM_DATA, "exo:forumData") ;
			forumHome.getSession().save() ;
			return forumHome.getNode(Utils.FORUM_DATA) ;
		}		
	}
	
	private void migrateForumData() throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node tempData = getForumTemp(sProvider) ;
			if(!tempData.hasNodes()) {
				log.info(">>>>>> There is no Forum's data for migrating!") ;
				return ;
			}
			Node forumHome = tempData.getNode(Utils.FORUM_SERVICE) ;
			if(forumHome.hasNode(Utils.FORUM_BAN_IP)) {
				migrateForumBanIP(forumHome.getNode(Utils.FORUM_BAN_IP)) ;
			}
			if(forumHome.hasNode("UserAdministration")) {
				migrateForumUserProfiles(forumHome.getNode("UserAdministration")) ;
			}
			if(forumHome.hasNode(Utils.FORUMADMINISTRATION)) {
				migrateForumAdministration(forumHome.getNode(Utils.FORUMADMINISTRATION)) ;
			}
			if(forumHome.hasNode("forumStatisticId")) {
				migrateForumStatistic(forumHome.getNode("forumStatisticId")) ;
			}
			NodeIterator iter = forumHome.getNodes() ;
			while(iter.hasNext()) {
				Node node = iter.nextNode() ;
				if(node.isNodeType("exo:forumTag")) {
					migrateForumTag(node) ;
				}
				if(node.isNodeType("exo:forumCategory")) {
					migrateForumCategory(node) ;
				}
			}
			//updateForumTopicData() ;			
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;}
	}
	
	private void migrateForumBanIP(Node banIP) {
		log.info("===> migrating Forum BanIP");
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node banHome ;
		try{
			try{
				banHome = getForumSystem(sProvider).getNode(Utils.BANIP_HOME) ;
			}catch(PathNotFoundException e) {
				banHome = getForumSystem(sProvider).addNode(Utils.BANIP_HOME, "exo:banIPHome") ;
				banHome.getSession().save() ;
			}			
			log.info("===>" + banHome.getPath() + "/" + banIP.getName());
			banHome.getSession().getWorkspace().move(banIP.getPath(), banHome.getPath() + "/" + banIP.getName()) ;
			banHome.getSession().save() ;
		}catch(Exception e) {
			e.printStackTrace() ;
		} finally{ sProvider.close() ;}
	} 
	
	private void migrateForumUserProfiles(Node userProfileHome) {
		log.info("===> migrating User Profiles");
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node profileHome ;
		try{
			try{
				profileHome = getForumSystem(sProvider).getNode(Utils.USER_PROFILE_HOME) ;
			}catch(PathNotFoundException e) {
				profileHome = getForumSystem(sProvider).addNode(Utils.USER_PROFILE_HOME, "exo:userProfileHome") ;
				profileHome.getSession().save() ;
			}
			NodeIterator iter = userProfileHome.getNode("UserProfile").getNodes() ;
			while(iter.hasNext()) {
				try{
					Node profile = iter.nextNode() ;
					profileHome.getSession().getWorkspace().move(profile.getPath(), profileHome.getPath() + "/" + profile.getName()) ;
					profileHome.save() ;
					log.info("===>" + profileHome.getPath() + "/" + profile.getName());
				}catch(Exception e) {
					e.printStackTrace() ;
				}				
			}			
			profileHome.getSession().save() ;
		}catch(Exception e) {
			e.printStackTrace() ;
		} finally{ sProvider.close() ;}
	}
	
	private void migrateForumAdministration(Node administration) {
		log.info("===> migrating Administration");
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node adminHome ;
		try{
			try{
				adminHome = getForumSystem(sProvider).getNode(Utils.ADMINISTRATION_HOME) ;
			}catch(PathNotFoundException e) {
				adminHome = getForumSystem(sProvider).addNode(Utils.ADMINISTRATION_HOME, "exo:administrationHome") ;
				adminHome.getSession().save() ;
			}			
			log.info("===>" + adminHome.getPath() + "/" + administration.getName());
			adminHome.getSession().getWorkspace().move(administration.getPath(), adminHome.getPath() + "/" + administration.getName()) ;
			adminHome.getSession().save() ;
		}catch(Exception e) {
			e.printStackTrace() ;
		} finally{ sProvider.close() ;}
	}
	
	private void migrateForumStatistic(Node statistic) {
		log.info("===> migrating Statistic");
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{			
			Node statisticHome ;
			Node systemHome ;
			systemHome = getForumSystem(sProvider);
			if(systemHome.hasNode(Utils.STATISTIC_HOME)) statisticHome = systemHome.getNode(Utils.STATISTIC_HOME) ;
			else {
				statisticHome = systemHome.addNode(Utils.STATISTIC_HOME, "exo:statisticHome") ;
				systemHome.save() ;
			}
			if(statisticHome.hasNode(Utils.FORUM_STATISTIC)) {
				statisticHome.getNode(Utils.FORUM_STATISTIC).remove() ;
				statisticHome.save() ;
			}
			systemHome.getSession().getWorkspace().move(statistic.getPath(), statisticHome.getPath() + "/" + Utils.FORUM_STATISTIC) ;
			statisticHome.getSession().save() ;
			log.info("===>" + statisticHome.getPath() + "/" + Utils.FORUM_STATISTIC);
		}catch(Exception e) {
			e.printStackTrace() ;
		} finally{ sProvider.close() ;}
	}
	
	private void migrateForumTag(Node tag) {
		log.info("===> migrating Forum Tags");
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node tagHome ;
		try{
			try{
				tagHome = getForumData(sProvider).getNode(Utils.TAG_HOME) ;
			}catch(PathNotFoundException e) {
				tagHome = getForumData(sProvider).addNode(Utils.TAG_HOME, "exo:tagHome") ;
				tagHome.getSession().save() ;
			}
			log.info("===>" + tagHome.getPath() + "/" + tag.getName());
		}catch(Exception e) {
			e.printStackTrace() ;
		} finally{ sProvider.close() ;}
	}

	private void migrateForumCategory(Node category) {
		log.info("===> migrating Category");
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node categoryHome ;
		try{
			try{
				categoryHome = getForumData(sProvider).getNode(Utils.CATEGORY_HOME) ;
			}catch(PathNotFoundException e) {
				categoryHome = getForumData(sProvider).addNode(Utils.CATEGORY_HOME, "exo:categoryHome") ;
				categoryHome.getSession().save() ;
			}
			// Migrate attachments
			migratePostAttachment(category) ;
			
			log.info("===>" + categoryHome.getPath() + "/" + category.getName());
			categoryHome.getSession().getWorkspace().move(category.getPath(), categoryHome.getPath() + "/" + category.getName()) ;
			categoryHome.getSession().save() ;
			
			//rename exo:KSRSS type to exo:forumRSS type
			renameRSSType(categoryHome) ;
		}catch(Exception e) {
			e.printStackTrace() ;
		} finally{ sProvider.close() ;}
	}
	
	private void migratePostAttachment(Node category) throws Exception {
		QueryManager qm = category.getSession().getWorkspace().getQueryManager();
		StringBuilder strQuery = new StringBuilder();
		strQuery.append("/jcr:root" + category.getPath()).append("//element(*,exo:forumAttachment)");
		//System.out.println("\n\n---------> strQuery:" + strQuery.toString());
		
		Query query = qm.createQuery(strQuery.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();	
		try{
			log.info("attsize ===>" + iter.getSize());
			while(iter.hasNext()) {
				Node att = iter.nextNode() ;
				if(att.isNodeType("nt:file") || att.isNodeType("exo:faqAttachment")) {
					Node post = att.getParent() ;
					Node attTemp = att ;
					Node nodeFile = post.addNode("file" + IdGenerator.generate(), "exo:forumAttachment");
					ForumServiceUtils.reparePermissions(nodeFile, "any");
					Node nodeContent = nodeFile.addNode("jcr:content", "exo:forumResource") ;																															
					nodeContent.setProperty("exo:fileName", attTemp.getProperty("exo:fileName").getValue()) ;
					nodeContent.setProperty("jcr:mimeType", attTemp.getNode("jcr:content").getProperty("jcr:mimeType").getValue());
					nodeContent.setProperty("jcr:data", attTemp.getNode("jcr:content").getProperty("jcr:data").getValue());
					nodeContent.setProperty("jcr:lastModified", attTemp.getNode("jcr:content").getProperty("jcr:lastModified").getValue());				
					log.info("fileName ===>" + nodeContent.getProperty("exo:fileName").getString());
					att.remove() ;
				}
			}
			category.save() ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}
	}
	
	private void renameRSSType(Node categoryHome) throws Exception {
		log.info("===> migrating rename Rss Type");
		QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
		StringBuffer queryBuffer = new StringBuffer();
		queryBuffer.append("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:KSRSS)") ;
		Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		while(iter.hasNext()) {
			try{
				Node ksRssNode = iter.nextNode() ;
				Node parentNode = ksRssNode.getParent() ;
				Node forumRssNode = parentNode.addNode("ks.rss", "exo:forumRSS") ;
				forumRssNode.setProperty("exo:content", ksRssNode.getProperty("exo:content").getValue()) ;
				ksRssNode.remove() ;
				parentNode.save() ;
			}catch(Exception e) {
				e.printStackTrace() ;
			}
		}
	}	
}
