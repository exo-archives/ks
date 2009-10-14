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
import javax.jcr.PathNotFoundException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;


public class BBCodeOperator {
	private NodeHierarchyCreator nodeHierarchyCreator_;
	private List<InitBBCodePlugin> defaultBBCodePlugins_ = new ArrayList<InitBBCodePlugin>() ;
  public BBCodeOperator(NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
  	nodeHierarchyCreator_ = nodeHierarchyCreator ;
  }
  
  private Node getKSShareDateNode(SessionProvider sProvider) throws Exception {
  	Node appNode = nodeHierarchyCreator_.getPublicApplicationNode(sProvider);
		try {
			return appNode.getNode(CommonUtils.KS_SHARE_DATA);
		} catch (PathNotFoundException e) {
			return appNode.addNode(CommonUtils.KS_SHARE_DATA);			
		} catch(Exception e) {
			return null ;
		}		
	}

  public Node getBBcodeHome(SessionProvider sProvider) throws Exception {
		try {
			return getKSShareDateNode(sProvider).getNode(CommonUtils.BBCODE_HOME);
		} catch (PathNotFoundException e) {
			return getKSShareDateNode(sProvider).addNode(CommonUtils.BBCODE_HOME, CommonUtils.BBCODE_HOME_NODE_TYPE);			
		} catch(Exception e) {
			return null ;
		}		
	}
	
	public void addInitBBCodePlugin(ComponentPlugin plugin) throws Exception {
		if(plugin instanceof InitBBCodePlugin) {
			defaultBBCodePlugins_.add((InitBBCodePlugin)plugin) ;
		}
	}
	
	public void initDefaultBBCode() throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node bbCodeHome = getBBcodeHome(sProvider);
			NodeIterator iter = bbCodeHome.getNodes();
			if(iter.getSize() <= 0){ 
				List<BBCode> bbCodes = new ArrayList<BBCode>();
		    for (InitBBCodePlugin pln : defaultBBCodePlugins_) {
		    	List<BBCodeData> codeDatas = pln.getBBCodePlugin().getBbcodeDatas();
		    	for (BBCodeData codeData : codeDatas) {
		        BBCode bbCode = new BBCode();
		        bbCode.setTagName(codeData.getTagName());
		        bbCode.setReplacement(codeData.getReplacement());
		        bbCode.setDescription(codeData.getDescription());
		        bbCode.setExample(codeData.getExample());
		        bbCode.setOption(Boolean.parseBoolean(codeData.getIsOption()));
		        bbCode.setActive(Boolean.parseBoolean(codeData.getIsActive()));
		        bbCodes.add(bbCode);
	        }
	      }
		    if(!bbCodes.isEmpty()){
		    	this.saveBBCode(bbCodes);
		    }
			}
    } catch (Exception e) {
	    e.printStackTrace();
    }finally { sProvider.close() ;}	  
  }
	
	public void saveBBCode(List<BBCode> bbcodes) throws Exception{
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
			e.printStackTrace() ;
		}finally { sProvider.close() ;}		
	}
	
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
	
	public List<BBCode> getAllBBCode() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<BBCode> bbcodes = new ArrayList<BBCode>();
		try {
			Node bbCodeHome = getBBcodeHome(sProvider);
			NodeIterator iter = bbCodeHome.getNodes();
			while (iter.hasNext()) {
		    try{
		    	Node bbcNode = iter.nextNode();
			    bbcodes.add(getBBCodeNode(bbcNode));
		    }catch(Exception e) {}				
	    }			
		}finally { sProvider.close() ;}
		return bbcodes;		
	}
	
	public List<String> getActiveBBCode() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<String> bbcodes = new ArrayList<String>();
		try{
			Node bbCodeHome = getBBcodeHome(sProvider);
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
	
	public BBCode getBBcode(String id) throws Exception {
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
      }
		}finally { sProvider.close() ;}
		return bbCode ;
	}
	
	public void removeBBCode(String bbcodeId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node bbCodeHome = getBBcodeHome(sProvider);
		try {
			bbCodeHome.getNode(bbcodeId).remove();
			bbCodeHome.save();
    } catch (Exception e) {
    	e.printStackTrace() ;
    }finally{
    	sProvider.close();
    }
	}
}
