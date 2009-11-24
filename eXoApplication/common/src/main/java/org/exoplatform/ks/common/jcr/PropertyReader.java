package org.exoplatform.ks.common.jcr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Value;

/**
 * A simple util wrapper to read JCR Nodes properties easily.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 *
 */
public class PropertyReader {

  Node node = null;
	public PropertyReader(Node node) {
    this.node = node;
	}
	
	public Double d(String name) {
		try {
			return node.getProperty(name).getDouble();
		}
		catch (Exception e) {
			return 0d;
		}
	}

	public long l(String name) {
		try {
			return node.getProperty(name).getLong();
		}
		catch (Exception e) {e.printStackTrace();
			return 0;
		}
	}

	public String string(String name, String defaultValue) {
		try {
			return node.getProperty(name).getString();
		}
		catch (Exception e) {
			return defaultValue;
		}
	}
	
	public String string(String name) {
		return string(name, null);
	}
	
	public Date date(String name) {
		try {
			return node.getProperty(name).getDate().getTime();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public Boolean bool(String name) {
	 return bool(name, false);
	}
	
	public Boolean bool(String name, boolean defaultValue) {
		try {
			return node.getProperty(name).getBoolean();
		}
		catch (Exception e) {
			return defaultValue;
		}
	}
	
	public String[] strings(String name) {
		return strings(name,null);
	}
	
	public String[] strings(String name, String [] defaultValue) {
		try {
			return valuesToArray(node.getProperty(name).getValues());
		}
		catch (Exception e) {
			return defaultValue;
		}
	}
	
	
	public List<String> list(String name) {
		return list(name, null);
	}
	
	public List<String> list(String name, List<String>defaultValue) {
		try {
			return valuesToList(node.getProperty(name).getValues());
		}
		catch (Exception e) {
			return null;
		}
	}
	
	
	 String[] valuesToArray(Value[] Val) throws Exception {
	    if (Val.length < 1)
	      return new String[] {};
	    if (Val.length == 1)
	      return new String[] { Val[0].getString() };
	    String[] Str = new String[Val.length];
	    for (int i = 0; i < Val.length; ++i) {
	      Str[i] = Val[i].getString();
	    }
	    return Str;
	  }

	  List<String> valuesToList(Value[] values) throws Exception {
	    List<String> list = new ArrayList<String>();
	    if (values.length < 1)
	      return list;
	    if (values.length == 1) {
	      list.add(values[0].getString());
	      return list;
	    }
	    for (int i = 0; i < values.length; ++i) {
	      list.add(values[i].getString());
	    }
	    return list;
	  }
	
}