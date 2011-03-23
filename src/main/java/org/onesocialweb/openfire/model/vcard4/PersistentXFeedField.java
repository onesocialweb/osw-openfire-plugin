package org.onesocialweb.openfire.model.vcard4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.onesocialweb.model.acl.AclRule;
import org.onesocialweb.model.vcard4.XFeedField;
import org.onesocialweb.openfire.model.acl.PersistentAclRule;

@Entity(name="FeedField")
public class PersistentXFeedField extends XFeedField {

	@OneToMany(cascade=CascadeType.ALL, targetEntity=PersistentAclRule.class, fetch=FetchType.EAGER)
	private List<AclRule> rules = new ArrayList<AclRule>();
	
	@Basic
	private String feed;
	
	@Override
	public String getFeed() {
		return feed;
	}
	
	@Override
	public void setFeed(String feed) {
		this.feed = feed;
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
