/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.ks.common.bbcode;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.bbcode.api.BBCodeService;
import org.exoplatform.ks.common.jcr.JCRSessionManager;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;


/**
 * Managed service implementation for {@link BBCodeService}. 
 * Stores BBCodes in JCR at {@link KSDataLocation#getBBCodesLocation()} 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
@Managed
@NameTemplate(@Property(key="service", value="bbcode"))
@ManagedDescription("BBCodes management")
public class BBCodeServiceImpl implements Startable, BBCodeService {
  
  
	private List<BBCodePlugin> defaultBBCodePlugins_;
	private KSDataLocation dataLocator;
	private JCRSessionManager sessionManager;
	
  private ManagementContext context;
	
	private static Log log = ExoLogger.getLogger(BBCodeServiceImpl.class);
	
  public BBCodeServiceImpl(KSDataLocation dataLocator) throws Exception {
    this.dataLocator = dataLocator;
    this.sessionManager = dataLocator.getSessionManager();
    defaultBBCodePlugins_ = new ArrayList<BBCodePlugin>() ;
  }
  
  public Node getBBcodeHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getBBCodesLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);  
	}
	
  /**
   * {@inheritDoc}
   */
	public void registerBBCodePlugin(BBCodePlugin plugin) throws Exception {
		defaultBBCodePlugins_.add(plugin) ;
	}
	
  /**
   * {@inheritDoc}
   */
	public void initDefaultBBCodes() throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node bbCodeHome = getBBcodeHome(sProvider);
			NodeIterator iter = bbCodeHome.getNodes();
			if(iter.getSize() <= 0){ 
				List<BBCode> bbCodes = new ArrayList<BBCode>();
		    for (BBCodePlugin pln : defaultBBCodePlugins_) {
		    	List<BBCodeData> codeDatas = pln.getBBCodeData();
		    	for (BBCodeData codeData : codeDatas) {
		        BBCode bbCode = new BBCode();
		        bbCode.setTagName(codeData.getTagName());
		        bbCode.setReplacement(codeData.getReplacement());
		        bbCode.setDescription(codeData.getDescription());
		        bbCode.setExample(codeData.getExample());
		        bbCode.setOption(Boolean.parseBoolean(codeData.getIsOption()));
		        bbCode.setActive(Boolean.parseBoolean(codeData.getIsActive()));
		        bbCodes.add(bbCode);
		        if (log.isDebugEnabled()) {
		          log.debug("Registered " + bbCode);
		        }
	        }
		    	
	        managePlugin(pln);
		    	
	      }
		    if(!bbCodes.isEmpty()){
		    	this.save(bbCodes);
		    }
		    

		    
			}
    }finally { sProvider.close() ;}	  
  }

	/**
	 * 
	 * @param pln
	 */
  private void managePlugin(BBCodePlugin pln) {
    try {
      if (context!= null) {
        context.register(pln);
      } else {
        log.warn("No Management context is available for " + getClass());
      }
    } catch (Exception e) {
      log.error("Failed to register BBCode plugin " + pln.getName(), e);
    }
  }
	
  /**
   * {@inheritDoc}
   */
	public void save(List<BBCode> bbcodes) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node bbCodeHome = getBBcodeHome(sProvider);
			Node bbcNode;
			for (BBCode bbcode : bbcodes) {
				String id = bbcode.getTagName();
				if(bbcode.isOption()) id = id + "_option";
				try {
					bbcNode = bbCodeHome.getNode(bbcode.getId());
					if(!id.equals(bbcode.getId())) {
						bbcNode.remove();
						bbcNode = bbCodeHome.addNode(id, CommonUtils.BBCODE_NODE_TYPE);
					}
	      } catch (Exception e) {
	      	bbcNode = bbCodeHome.addNode(id, CommonUtils.BBCODE_NODE_TYPE);
	      }
				bbcNode.setProperty("exo:tagName", bbcode.getTagName());
				bbcNode.setProperty("exo:replacement", bbcode.getReplacement());
				bbcNode.setProperty("exo:example", bbcode.getExample());
				bbcNode.setProperty("exo:description", bbcode.getDescription());
				bbcNode.setProperty("exo:isActive", bbcode.isActive());
				bbcNode.setProperty("exo:isOption", bbcode.isOption());
	    }
			if(bbCodeHome.isNew()){
				bbCodeHome.getSession().save();
			} else {
				bbCodeHome.save();
			}
		}catch(Exception e) {
		  log.error("Error saving BBCodes", e);
		}finally { sProvider.close() ;}		
	}
	
  /**
   * {@inheritDoc}
   */
	private BBCode getBBCodeNode(Node bbcNode) throws Exception{
		BBCode bbCode = new BBCode();
		bbCode.setId(bbcNode.getName());
    bbCode.setTagName(bbcNode.getProperty("exo:tagName").getString());
    bbCode.setReplacement(bbcNode.getProperty("exo:replacement").getString());
    bbCode.setExample(bbcNode.getProperty("exo:example").getString());
    if(bbcNode.hasProperty("exo:description"))
    	bbCode.setDescription(bbcNode.getProperty("exo:description").getString());
    bbCode.setActive(bbcNode.getProperty("exo:isActive").getBoolean());
    bbCode.setOption(bbcNode.getProperty("exo:isOption").getBoolean());
		return bbCode;
	}
	
  /**
   * {@inheritDoc}
   */
	public List<BBCode> getAll() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<BBCode> bbcodes = new ArrayList<BBCode>();
		try {
			Node bbCodeHome = getBBcodeHome(sProvider);
			NodeIterator iter = bbCodeHome.getNodes();
			while (iter.hasNext()) {
		    try{
		    	Node bbcNode = iter.nextNode();
			    bbcodes.add(getBBCodeNode(bbcNode));
		    }catch(Exception e) {
		      log.error("Error loading BBCodes", e);
		    }				
	    }			
		}finally { sProvider.close() ;}
		return bbcodes;		
	}
	
  /**
   * {@inheritDoc}
   */
	public List<String> getActive() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<String> bbcodes = new ArrayList<String>();
		try{
			Node bbCodeHome = getBBcodeHome(sProvider);
			if (bbCodeHome == null) return bbcodes ;
			QueryManager qm = bbCodeHome.getSession().getWorkspace().getQueryManager();
			StringBuilder pathQuery = new StringBuilder();
			pathQuery.append("/jcr:root").append(bbCodeHome.getPath()).append("/element(*,").append(CommonUtils.BBCODE_NODE_TYPE).append(")[@exo:isActive='true']");
			Query query = qm.createQuery(pathQuery.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			String tagName = "";
			while (iter.hasNext()) {
		    Node bbcNode = iter.nextNode();
		    tagName = bbcNode.getProperty("exo:tagName").getString();
		    if(bbcNode.getProperty("exo:isOption").getBoolean()) tagName = tagName + "=";
		    bbcodes.add(tagName);
	    }
		}finally { sProvider.close() ;}
		return bbcodes;
	}
	
  /**
   * {@inheritDoc}
   */
	public BBCode findById(String id) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		BBCode bbCode = new BBCode();
		Node bbcNode;
		try{
			Node bbCodeHome = getBBcodeHome(sProvider);
			try {
				bbcNode = bbCodeHome.getNode(id);
				bbCode.setId(bbcNode.getName());
		    bbCode.setTagName(bbcNode.getProperty("exo:tagName").getString());
		    bbCode.setReplacement(bbcNode.getProperty("exo:replacement").getString());
		    bbCode.setOption(bbcNode.getProperty("exo:isOption").getBoolean());
      } catch (Exception e) {
        log.error("Error loading BBCode" + id, e);
      }
		}finally { sProvider.close() ;}
		return bbCode ;
	}
	
  /**
   * {@inheritDoc}
   */
	public void delete(String bbcodeId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node bbCodeHome = getBBcodeHome(sProvider);
		try {
			bbCodeHome.getNode(bbcodeId).remove();
			bbCodeHome.save();
    } catch (Exception e) {
      log.error("Error removing BBCode" + bbcodeId, e);
    }finally{
    	sProvider.close();
    }
	}


  public void start() {
    try {
      initDefaultBBCodes();
    } catch (Exception e) {
      log.error("Default BBCodes failed to initialize", e);
    }
  }


  public void stop() {
  }
  
  public void setContext(ManagementContext context) {
    this.context = context;
  }
}
