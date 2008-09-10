package org.exoplatform.sample.service.impl;

import java.util.ArrayList;
import java.util.List;

public class TestConfig {
	private List<ObjectParam> objectParams = new ArrayList<ObjectParam>() ;
  
	public List<ObjectParam> getObjectParams() { return this.objectParams ; }	
	public void setObjectParams(List<ObjectParam> op) { this.objectParams = op ;}
	
	static public class ObjectParam {
		
		private String value ;
		private String name ;
		
		public String getValue() { return this.value ; }
		public void setValue(String vl) { this.value = vl ;}
		
		public String getName() {return this.name ; }
		public void setName(String name) { this.name = name ; }
	}
}
