package org.exoplatform.forum.service.conf;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;

public class RoleRulesPlugin extends BaseComponentPlugin {

  private Map<String, List<String>> rules_ = new LinkedHashMap<String, List<String>>();

  public RoleRulesPlugin(InitParams params) throws Exception {
  	ValueParam vlParam = params.getValueParam("role") ;
  	ValuesParam vlsParam = params.getValuesParam("rules") ;     
    rules_.put(vlParam.getValue(), vlsParam.getValues());
  }

  public List<String> getRules(String role) {
    return rules_.get(role);
  }
}
