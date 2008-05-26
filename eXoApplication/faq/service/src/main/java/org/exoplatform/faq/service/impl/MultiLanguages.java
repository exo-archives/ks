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

package org.exoplatform.faq.service.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.faq.service.CategoryLanguage;
import org.exoplatform.faq.service.JcrInputProperty;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.services.jcr.impl.core.query.lucene.NodeIndexer;
import org.exoplatform.services.jcr.impl.core.value.DateValue;
import org.exoplatform.services.jcr.impl.core.value.StringValue;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */

public class MultiLanguages {
	final static public String LANGUAGES = "languages" ;
  final static public String EXO_LANGUAGE = "exo:language" ;
  final static public String COMMENTS = "comments".intern() ;
  final static public String  JCRCONTENT = "jcr:content";
  final static public String  JCRDATA = "jcr:data";
  final static public String  JCR_MIMETYPE = "jcr:mimeType";
  final static public String  NTUNSTRUCTURED = "nt:unstructured";
  final static String VOTER_PROP = "exo:voter".intern() ;  
  final static String VOTING_RATE_PROP = "exo:votingRate".intern() ;
  final static String VOTE_TOTAL_PROP = "exo:voteTotal".intern() ; 
  final static String VOTE_TOTAL_LANG_PROP = "exo:voteTotalOfLang".intern() ;
  final static String NODE = "/node/" ;
  final static String NODE_LANGUAGE = "/node/languages/" ;
  final static String CONTENT_PATH = "/node/jcr:content/" ;
  final static String TEMP_NODE = "temp" ;
  
  public MultiLanguages()throws Exception {}  

  private void setPropertyValue(String propertyName, Node node, int requiredtype, Object value, boolean isMultiple) throws Exception {
    switch (requiredtype) {
    case PropertyType.STRING:
      if (value == null) {
        node.setProperty(propertyName, "");
      } else {
        if(isMultiple) {
          if (value instanceof String) node.setProperty(propertyName, new String[] { value.toString()});
          else if(value instanceof String[]) node.setProperty(propertyName, (String[]) value);
        } else {
          if(value instanceof StringValue) {
            StringValue strValue = (StringValue) value ;
            node.setProperty(propertyName, strValue.getString());
          } else {
            node.setProperty(propertyName, value.toString());
          }
        }
      }
      break;
    case PropertyType.BINARY:
      if (value == null) node.setProperty(propertyName, "");
      else if (value instanceof byte[]) node.setProperty(propertyName, new ByteArrayInputStream((byte[]) value));
      else if (value instanceof String) node.setProperty(propertyName, new ByteArrayInputStream((value.toString()).getBytes()));
      else if (value instanceof String[]) node.setProperty(propertyName, new ByteArrayInputStream((((String[]) value)).toString().getBytes()));      
      break;
    case PropertyType.BOOLEAN:
      if (value == null) node.setProperty(propertyName, false);
      else if (value instanceof String) node.setProperty(propertyName, new Boolean(value.toString()).booleanValue());
      else if (value instanceof String[]) node.setProperty(propertyName, (String[]) value);         
      break;
    case PropertyType.LONG:
      if (value == null || "".equals(value)) node.setProperty(propertyName, 0);
      else if (value instanceof String) node.setProperty(propertyName, new Long(value.toString()).longValue());
      else if (value instanceof String[]) node.setProperty(propertyName, (String[]) value);  
      break;
    case PropertyType.DOUBLE:
      if (value == null || "".equals(value)) node.setProperty(propertyName, 0);
      else if (value instanceof String) node.setProperty(propertyName, new Double(value.toString()).doubleValue());
      else if (value instanceof String[]) node.setProperty(propertyName, (String[]) value);        
      break;
    case PropertyType.DATE:      
      if (value == null) {        
        node.setProperty(propertyName, new GregorianCalendar());
      } else {
        if(isMultiple) {
          Session session = node.getSession() ;
          if (value instanceof String) {
            Value value2add = session.getValueFactory().createValue(ISO8601.parse((String) value));
            node.setProperty(propertyName, new Value[] {value2add});
          } else if (value instanceof String[]) {
            String[] values = (String[]) value;
            Value[] convertedCalendarValues = new Value[values.length];
            int i = 0;
            for (String stringValue : values) {
              Value value2add = session.getValueFactory().createValue(ISO8601.parse(stringValue));
              convertedCalendarValues[i] = value2add;
              i++;
            }
            node.setProperty(propertyName, convertedCalendarValues);
            session.logout();
          }
        } else {
          if(value instanceof String) {
            node.setProperty(propertyName, ISO8601.parse(value.toString()));
          } else if(value instanceof GregorianCalendar) {
            node.setProperty(propertyName, (GregorianCalendar) value);
          } else if(value instanceof DateValue) {
            DateValue dateValue = (DateValue) value ;
            node.setProperty(propertyName, dateValue.getDate());
          }
        }
      }
      break ;
    case PropertyType.REFERENCE :
      if (value == null) throw new RepositoryException("null value for a reference " + requiredtype);
      if(value instanceof Value[]) 
        node.setProperty(propertyName, (Value[]) value);
        else if (value instanceof String) {
          Session session = node.getSession();
          if(session.getRootNode().hasNode((String)value)) {
            Node catNode = session.getRootNode().getNode((String)value);
            Value value2add = session.getValueFactory().createValue(catNode);
            node.setProperty(propertyName, new Value[] {value2add});          
          } else {
            node.setProperty(propertyName, (String) value);
          }
        }       
      break ;
    }
  }
  
  public void addLanguage(Node questionNode, QuestionLanguage language) throws Exception {
  	if(!questionNode.isNodeType("mix:faqi18n")) {
  		questionNode.addMixin("mix:faqi18n") ;
  	}
  	Node languagesNode = null ;
    try{
    	languagesNode = questionNode.getNode(LANGUAGES) ;
    }catch(Exception e) {
    	languagesNode = questionNode.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    }
    Node langNode = null ;
    try{
    	langNode = languagesNode.getNode(language.getLanguage()) ;
    }catch(Exception e) {
    	langNode = languagesNode.addNode(language.getLanguage(), NTUNSTRUCTURED) ;
    }
    
    langNode.setProperty("exo:name", language.getQuestion()) ;
    langNode.setProperty("exo:responses", language.getResponse()) ;   
    questionNode.save() ;
  }
  
  public void removeLanguage(Node questionNode, List<String> listLanguage) {
    try {
      if(!questionNode.hasNode(LANGUAGES)) return ;
      Node languageNode = questionNode.getNode(LANGUAGES) ;
      NodeIterator nodeIterator = languageNode.getNodes();
      Node node = null ;
      while(nodeIterator.hasNext()) {
        node = nodeIterator.nextNode() ;
        if(!listLanguage.contains(node.getName())) {
          node.remove() ;
        }
      }
      questionNode.getSession().save() ;
    } catch (PathNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public void addLanguage(Node categoryNode, CategoryLanguage language) throws Exception {
  	if(!categoryNode.isNodeType("mix:faqi18n")) {
  		categoryNode.addMixin("mix:faqi18n") ;
  	}
  	Node languagesNode = null ;
    try{
    	languagesNode = categoryNode.getNode(LANGUAGES) ;
    }catch(Exception e) {
    	languagesNode = categoryNode.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    }
    Node langNode = null ;
    try{
    	langNode = languagesNode.getNode(language.getLanguage()) ;
    }catch(Exception e) {
    	langNode = languagesNode.addNode(language.getLanguage(), NTUNSTRUCTURED) ;
    }    
    langNode.setProperty("exo:name", language.getName()) ;
    categoryNode.save() ;
  }
  
  private void setMixin(Node node, Node newLang) throws Exception {
    NodeType[] mixins = node.getMixinNodeTypes() ;
    for(NodeType mixin:mixins) {
      if(!mixin.getName().equals("exo:actionable")) {
        if(newLang.canAddMixin(mixin.getName())) {
          newLang.addMixin(mixin.getName()) ;
          for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
            if(!def.isProtected()) {
              String propName = def.getName() ;
              if(def.isMandatory() && !def.isAutoCreated()) {
                if(def.isMultiple()) {
                  newLang.setProperty(propName,node.getProperty(propName).getValues()) ;
                } else {
                  newLang.setProperty(propName,node.getProperty(propName).getValue()) ; 
                }
              }        
            }
          }
        }
      }
    }
  }

  public void addLanguage(Node node, Map inputs, String language, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    String defaultLanguage = getDefault(node) ;
    if(!node.isNodeType("mix:faqi18n")) node.addMixin("mix:faqi18n") ;
    if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
    else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) {
          newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        } else {
          newLanguageNode = languagesNode.addNode(defaultLanguage) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin:mixins) {
            if(!mixin.getName().equals("exo:actionable")) {
              if(newLanguageNode.canAddMixin(mixin.getName())) newLanguageNode.addMixin(mixin.getName()) ;            
            }
          }
        }
      } else {
        if(languagesNode.hasNode(language)) {
          newLanguageNode = languagesNode.getNode(language) ;
        } else {
          newLanguageNode = languagesNode.addNode(language) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin : mixins) {
            if(!mixin.getName().equals("exo:actionable")) {
              if(newLanguageNode.canAddMixin(mixin.getName())) newLanguageNode.addMixin(mixin.getName()) ;
              for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
                if(!def.isProtected()) {
                  String propName = def.getName() ;
                  if(def.isMandatory() && !def.isAutoCreated()) {
                    if(def.isMultiple()) {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValues()) ;
                    } else {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValue()) ; 
                    }
                  }        
                }
              }
            }
          }
          newLanguageNode.setProperty(EXO_LANGUAGE, language) ;
        }
      }
    }   
    PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
    for(PropertyDefinition pro : properties){
      
      if(!pro.isProtected()) {
        String propertyName = pro.getName() ;
        JcrInputProperty property = (JcrInputProperty)inputs.get(NODE + propertyName) ;
        System.out.println("propertyName ====>"+ propertyName + "===>"+ property);
        
        if(defaultLanguage.equals(language) && property != null) {
          setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
        } else {          
          if(isDefault) {            
            if(node.hasProperty(propertyName)) {
              Object value = null ;
              int requiredType = node.getProperty(propertyName).getDefinition().getRequiredType() ;
              boolean isMultiple = node.getProperty(propertyName).getDefinition().isMultiple() ;
              if(isMultiple) value = node.getProperty(propertyName).getValues() ;
              else value = node.getProperty(propertyName).getValue() ;
              setPropertyValue(propertyName, newLanguageNode, requiredType, value, isMultiple) ;
            }
            if(property != null) {
              setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          } else {
            if(property != null) {
              setPropertyValue(propertyName, newLanguageNode, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          }
        }               
      }
    }
    if(!defaultLanguage.equals(language) && isDefault){
      Node selectedLangNode = null ;
      if(languagesNode.hasNode(language)) selectedLangNode = languagesNode.getNode(language) ;
      setVoteProperty(newLanguageNode, node, selectedLangNode) ;
      setCommentNode(node, newLanguageNode, selectedLangNode) ;
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    if(isDefault && languagesNode.hasNode(language)) languagesNode.getNode(language).remove() ;
    node.save() ;
    node.getSession().save() ;
    
    System.out.println("\n\n\n\n>>>>addLanguage() --> size of questionNodeChildren: " + node.getNodes().getSize());
  }
  
  public void addLanguage(Node node, Map inputs, String language, boolean isDefault, String nodeType) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    String defaultLanguage = getDefault(node) ;
    Workspace ws = node.getSession().getWorkspace() ;
    if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
    else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) {
          newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        } else {
          newLanguageNode = languagesNode.addNode(defaultLanguage) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin:mixins) {
            if(!mixin.getName().equals("exo:actionable")) {
              if(newLanguageNode.canAddMixin(mixin.getName())) newLanguageNode.addMixin(mixin.getName()) ;            
            }
          }
        }
      } else {
        if(languagesNode.hasNode(language)) {
          newLanguageNode = languagesNode.getNode(language) ;
        } else {
          newLanguageNode = languagesNode.addNode(language) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin : mixins) {
            if(!mixin.getName().equals("exo:actionable")) {
              if(newLanguageNode.canAddMixin(mixin.getName())) newLanguageNode.addMixin(mixin.getName()) ;
              for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
                if(!def.isProtected()) {
                  String propName = def.getName() ;
                  if(def.isMandatory() && !def.isAutoCreated()) {
                    if(def.isMultiple()) {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValues()) ;
                    } else {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValue()) ; 
                    }
                  }        
                }
              }
            }
          }
          newLanguageNode.setProperty(EXO_LANGUAGE, language) ;
        }
      }
      Node jcrContent = node.getNode(nodeType) ;
      node.save() ;
      if(!newLanguageNode.hasNode(nodeType)) {
        ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
      }
      Node newContentNode = newLanguageNode.getNode(nodeType) ;
      PropertyIterator props = newContentNode.getProperties() ;
      while(props.hasNext()) {
        Property prop = props.nextProperty() ;
        if(inputs.containsKey(NODE + nodeType + "/" + prop.getName())) {
          JcrInputProperty inputVariable = (JcrInputProperty) inputs.get(NODE + nodeType + "/" + prop.getName()) ;
          boolean isMultiple = prop.getDefinition().isMultiple() ;
          setPropertyValue(prop.getName(), newContentNode, prop.getType(), inputVariable.getValue(), isMultiple) ;
        }
      }
      if(isDefault) {
        Node tempNode = node.addNode(TEMP_NODE, "nt:unstructured") ;
        node.getSession().move(node.getNode(nodeType).getPath(), tempNode.getPath() + "/" + nodeType) ;
        node.getSession().move(newLanguageNode.getNode(nodeType).getPath(), node.getPath() + "/" + nodeType) ;
        node.getSession().move(tempNode.getNode(nodeType).getPath(), languagesNode.getPath() + "/" + defaultLanguage + "/" + nodeType) ;
        tempNode.remove() ;
      }      
    } else {
      JcrInputProperty inputVariable = (JcrInputProperty) inputs.get(NODE + nodeType + "/" + JCRDATA) ;
      setPropertyValue(JCRDATA, node.getNode(nodeType), inputVariable.getType(), inputVariable.getValue(), false) ;
    }
    PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
    for(PropertyDefinition pro : properties){
      if(!pro.isProtected()) {
        String propertyName = pro.getName() ;
        JcrInputProperty property = (JcrInputProperty)inputs.get(NODE + propertyName) ;
        if(defaultLanguage.equals(language) && property != null) {
          setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
        } else {          
          if(isDefault) {            
            if(node.hasProperty(propertyName)) {
              Object value = null ;
              int requiredType = node.getProperty(propertyName).getDefinition().getRequiredType() ;
              boolean isMultiple = node.getProperty(propertyName).getDefinition().isMultiple() ;
              if(isMultiple) value = node.getProperty(propertyName).getValues() ;
              else value = node.getProperty(propertyName).getValue() ;
              setPropertyValue(propertyName, newLanguageNode, requiredType, value, isMultiple) ;
            }
            if(property != null) {
              setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          } else {
            if(property != null) {
              setPropertyValue(propertyName, newLanguageNode, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          }
        }               
      }
    }
    if(!defaultLanguage.equals(language) && isDefault){
      Node selectedLangNode = null ;
      if(languagesNode.hasNode(language)) selectedLangNode = languagesNode.getNode(language) ;
      setVoteProperty(newLanguageNode, node, selectedLangNode) ;
      setCommentNode(node, newLanguageNode, selectedLangNode) ;
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    if(isDefault && languagesNode.hasNode(language)) languagesNode.getNode(language).remove() ;
    node.save() ;
    node.getSession().save() ;    
  }
  
  public void addFileLanguage(Node node, Value value, String mimeType, String language, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    Workspace ws = node.getSession().getWorkspace() ;
    String defaultLanguage = getDefault(node) ;
    if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
    else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        else newLanguageNode = languagesNode.addNode(defaultLanguage) ;
        Node jcrContent = node.getNode(JCRCONTENT) ;
        node.save() ;
        ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
        jcrContent.setProperty(JCR_MIMETYPE, mimeType) ;
        jcrContent.setProperty(JCRDATA, value) ;
      } else {
        if(languagesNode.hasNode(language)) newLanguageNode = languagesNode.getNode(language) ;
        else newLanguageNode = languagesNode.addNode(language) ;
        Node jcrContent = node.getNode(JCRCONTENT) ;
        node.save() ;
        ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
        newLanguageNode.getNode(JCRCONTENT).setProperty(JCR_MIMETYPE, mimeType) ;
        newLanguageNode.getNode(JCRCONTENT).setProperty(JCRDATA, value) ;        
      }
      // add mixin type for node
      setMixin(node, newLanguageNode) ;
    } else {
      node.getNode(JCRCONTENT).setProperty(JCRDATA, value) ;   
    }
    if(!defaultLanguage.equals(language) && isDefault){
      Node selectedLangNode = null ;
      if(languagesNode.hasNode(language)) selectedLangNode = languagesNode.getNode(language) ;
      setVoteProperty(newLanguageNode, node, selectedLangNode) ;
      setCommentNode(node, newLanguageNode, selectedLangNode) ;
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    node.save() ;
    node.getSession().save() ;    
  }
  
  public void addFileLanguage(Node node, String language, Map mappings, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    Workspace ws = node.getSession().getWorkspace() ;
    String defaultLanguage = getDefault(node) ;
    if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
    else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        else newLanguageNode = languagesNode.addNode(defaultLanguage) ;
      } else {
        if(languagesNode.hasNode(language)) newLanguageNode = languagesNode.getNode(language) ;
        else newLanguageNode = languagesNode.addNode(language) ;
      }
      Node jcrContent = node.getNode(JCRCONTENT) ;
      node.save() ;
      if(!newLanguageNode.hasNode(JCRCONTENT)) {
        ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
      }
      Node newContentNode = newLanguageNode.getNode(JCRCONTENT) ;
      PropertyIterator props = newContentNode.getProperties() ;
      while(props.hasNext()) {
        Property prop = props.nextProperty() ;
        if(mappings.containsKey(CONTENT_PATH + prop.getName())) {
          JcrInputProperty inputVariable = (JcrInputProperty) mappings.get(CONTENT_PATH + prop.getName()) ;
          boolean isMultiple = prop.getDefinition().isMultiple() ;
          setPropertyValue(prop.getName(), newContentNode, prop.getType(), inputVariable.getValue(), isMultiple) ;
        }
      }
      if(isDefault) {
        Node tempNode = node.addNode(TEMP_NODE, "nt:unstructured") ;
        node.getSession().move(node.getNode(JCRCONTENT).getPath(), tempNode.getPath() + "/" + JCRCONTENT) ;
        node.getSession().move(newLanguageNode.getNode(JCRCONTENT).getPath(), node.getPath() + "/" + JCRCONTENT) ;
        node.getSession().move(tempNode.getNode(JCRCONTENT).getPath(), languagesNode.getPath() + "/" + defaultLanguage + "/" + JCRCONTENT) ;
        tempNode.remove() ;
      }
      // add mixin type for node
      setMixin(node, newLanguageNode) ;
    } else {
      JcrInputProperty inputVariable = (JcrInputProperty) mappings.get(CONTENT_PATH + JCRDATA) ;
      setPropertyValue(JCRDATA, node.getNode(JCRCONTENT), inputVariable.getType(), inputVariable.getValue(), false) ;
    }
    PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
    for(PropertyDefinition pro : properties){
      if(!pro.isProtected()) {
        String propertyName = pro.getName() ;
        JcrInputProperty property = (JcrInputProperty)mappings.get(NODE + propertyName) ;
        if(defaultLanguage.equals(language) && property != null) {
          setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
        } else {          
          if(isDefault) {            
            if(node.hasProperty(propertyName)) {
              Object value = null ;
              int requiredType = node.getProperty(propertyName).getDefinition().getRequiredType() ;
              boolean isMultiple = node.getProperty(propertyName).getDefinition().isMultiple() ;
              if(isMultiple) value = node.getProperty(propertyName).getValues() ;
              else value = node.getProperty(propertyName).getValue() ;
              setPropertyValue(propertyName, newLanguageNode, requiredType, value, isMultiple) ;
            }
            if(property != null) {
              setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          } else {
            if(property != null) {
              setPropertyValue(propertyName, newLanguageNode, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          }
        }               
      }
    }    
    if(!defaultLanguage.equals(language) && isDefault) {
      Node selectedLangNode = null ;
      if(languagesNode.hasNode(language)) selectedLangNode = languagesNode.getNode(language) ;
      setVoteProperty(newLanguageNode, node, selectedLangNode) ;
      setCommentNode(node, newLanguageNode, selectedLangNode) ;
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    node.save() ;
    node.getSession().save() ;    
  }
  
  public String getDefault(Node node) throws Exception {
    if(node.hasProperty(EXO_LANGUAGE)) return node.getProperty(EXO_LANGUAGE).getString() ;
    return null ;
  }

  public List<String> getSupportedLanguages(Node node) throws Exception {
    List<String> languages = new ArrayList<String>();
    String defaultLang = getDefault(node) ;
    if(defaultLang != null) languages.add(defaultLang) ;
    if(node.hasNode(LANGUAGES)){
      Node languageNode = node.getNode(LANGUAGES) ;
      NodeIterator iter  = languageNode.getNodes() ;      
      while(iter.hasNext()) {
        languages.add(iter.nextNode().getName());
      }
    } 
    return languages;
  }

  private void setVoteProperty(Node newLang, Node node, Node selectedLangNode) throws Exception {
    if(hasMixin(newLang, "mix:votable")) {
      newLang.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ; 
      newLang.setProperty(VOTE_TOTAL_LANG_PROP, node.getProperty(VOTE_TOTAL_LANG_PROP).getLong()) ;
      newLang.setProperty(VOTING_RATE_PROP, node.getProperty(VOTING_RATE_PROP).getLong()) ;
      if(node.hasProperty(VOTER_PROP)) {
        newLang.setProperty(VOTER_PROP, node.getProperty(VOTER_PROP).getValues()) ;
      }
      if(selectedLangNode != null) {
        node.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ; 
        if(selectedLangNode.hasProperty(VOTE_TOTAL_LANG_PROP)) {
          node.setProperty(VOTE_TOTAL_LANG_PROP, selectedLangNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong()) ;
        } else {
          node.setProperty(VOTE_TOTAL_LANG_PROP, 0) ;
        }
        if(selectedLangNode.hasProperty(VOTING_RATE_PROP)) {
          node.setProperty(VOTING_RATE_PROP, selectedLangNode.getProperty(VOTING_RATE_PROP).getLong()) ;
        } else {
          node.setProperty(VOTING_RATE_PROP, 0) ;
        }
        if(selectedLangNode.hasProperty(VOTER_PROP)) {
          node.setProperty(VOTER_PROP, selectedLangNode.getProperty(VOTER_PROP).getValues()) ;
        }
      } else {
        node.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ;
        node.setProperty(VOTE_TOTAL_LANG_PROP, 0) ;
        node.setProperty(VOTING_RATE_PROP, 0) ;
      }
    }
  }
  
  private void setCommentNode(Node node, Node newLang, Node selectedLangNode) throws Exception {
    if(node.hasNode(COMMENTS)) {
      node.getSession().move(node.getPath() + "/" + COMMENTS, newLang.getPath() + "/" + COMMENTS) ;
    }
    if(selectedLangNode != null && selectedLangNode.hasNode(COMMENTS)) {
      node.getSession().move(selectedLangNode.getPath() + "/" + COMMENTS, node.getPath() + "/" + COMMENTS) ;
    }
  }
  
  public long getVoteTotal(Node node) throws Exception {
    long voteTotal = 0;
    if(!node.hasNode(LANGUAGES) && node.hasProperty(VOTE_TOTAL_PROP)) {
      return node.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
    }
    Node multiLanguages = node.getNode(LANGUAGES) ;
    voteTotal = node.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
    NodeIterator nodeIter = multiLanguages.getNodes() ;
    String defaultLang = getDefault(node) ;
    while(nodeIter.hasNext()) {
      Node languageNode = nodeIter.nextNode() ;
      if(!languageNode.getName().equals(defaultLang) && languageNode.hasProperty(VOTE_TOTAL_LANG_PROP)) {
        voteTotal = voteTotal + languageNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
      }
    }
    return voteTotal ;
  }
  
  private boolean hasMixin(Node node, String nodeTypeName) throws Exception {
    NodeType[] mixinTypes = node.getMixinNodeTypes() ; 
    for(NodeType nodeType : mixinTypes) {
      if(nodeType.getName().equals(nodeTypeName)) return true ;
    }
    return false ;
  }
  
  public void setDefault(Node node, String language) throws Exception {
    String defaultLanguage = getDefault(node) ;
    if(!defaultLanguage.equals(language)){
      Node languagesNode = null ;
      if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
      else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
      Node selectedLangNode = languagesNode.getNode(language) ;
      Node newLang = languagesNode.addNode(defaultLanguage) ;
      PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
      for(PropertyDefinition pro : properties){
        if(!pro.isProtected()){
          String propertyName = pro.getName() ;
          if(node.hasProperty(propertyName)) {
            if(node.getProperty(propertyName).getDefinition().isMultiple()) {
              Value[] values = node.getProperty(propertyName).getValues() ;
              newLang.setProperty(propertyName, values) ;
            } else {
              newLang.setProperty(propertyName, node.getProperty(propertyName).getValue()) ;
            }
          }
          if(selectedLangNode.hasProperty(propertyName)) {
            if(selectedLangNode.getProperty(propertyName).getDefinition().isMultiple()) {
              Value[] values = selectedLangNode.getProperty(propertyName).getValues() ;
              node.setProperty(propertyName, values) ;
            } else {
              node.setProperty(propertyName, selectedLangNode.getProperty(propertyName).getValue()) ;
            }
          }
        }
      }
      if(hasNodeTypeNTResource(node)) {
        processWithDataChildNode(node, selectedLangNode, languagesNode, defaultLanguage, getChildNodeType(node)) ;
      }
      setMixin(node, newLang) ;
      setVoteProperty(newLang, node, selectedLangNode) ;
      node.setProperty(EXO_LANGUAGE, language) ;
      setCommentNode(node, newLang, selectedLangNode) ;
      selectedLangNode.remove() ;
      node.save() ;
      node.getSession().save() ;
    }
  }
  
  private void processWithDataChildNode(Node node, Node selectedLangNode, Node languagesNode, 
      String defaultLanguage, String nodeType) throws Exception {
    Node tempNode = node.addNode(TEMP_NODE, "nt:unstructured") ;
    node.getSession().move(node.getNode(nodeType).getPath(), tempNode.getPath() + "/" + nodeType) ;
    node.getSession().move(selectedLangNode.getNode(nodeType).getPath(), node.getPath() + "/" + nodeType) ;
    node.getSession().move(tempNode.getNode(nodeType).getPath(), languagesNode.getPath() + "/" + defaultLanguage + "/" + nodeType) ;
    tempNode.remove() ;
  }
  
  private boolean hasNodeTypeNTResource(Node node) throws Exception {
    if(node.hasNodes()) {
      NodeIterator nodeIter = node.getNodes() ;
      while(nodeIter.hasNext()) {
        Node childNode = nodeIter.nextNode() ;
        if(childNode.getPrimaryNodeType().getName().equals("nt:resource")) return true ;
      }
    }
    return false ;
  }
  
  private String getChildNodeType(Node node) throws Exception {
    if(node.hasNodes()) {
      NodeIterator nodeIter = node.getNodes() ;
      while(nodeIter.hasNext()) {
        Node childNode = nodeIter.nextNode() ;
        if(childNode.getPrimaryNodeType().getName().equals("nt:resource")) return childNode.getName() ;
      }
    }
    return null ;
  }

  public Node getLanguage(Node node, String language) throws Exception {
  	if(node.hasNode(LANGUAGES + "/"+ language)) return node.getNode(LANGUAGES + "/"+ language) ;
    return null;
  }
  

}
