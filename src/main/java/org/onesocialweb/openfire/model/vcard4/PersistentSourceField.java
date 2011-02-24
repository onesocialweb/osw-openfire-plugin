package org.onesocialweb.openfire.model.vcard4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onesocialweb.model.acl.AclRule;
import org.onesocialweb.model.vcard4.SourceField;

public class PersistentSourceField extends SourceField{

	private List<AclRule> rules = new ArrayList<AclRule>();
	
	private String source;
	
	@Override
	public String getSource() {
		return source;
	}

	@Override
	public void setSource(String source) {
		this.source = source;
	}
	
	@Override
	public void addAclRule(AclRule rule) {
		rules.add(rule);
	}

	@Override
	public List<AclRule> getAclRules() {
		return Collections.unmodifiableList(rules);
	}

	@Override
	public void removeAclRule(AclRule rule) {
		rules.remove(rule);
	}

	@Override
	public void setAclRules(List<AclRule> rules) {
		this.rules = rules;
	}
	
	@Override
	public boolean hasAclRules() {
		if (rules != null && rules.size() > 0) {
			return true;
		}
		return false;
	}
}
