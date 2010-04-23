package org.onesocialweb.openfire.model.vcard4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.onesocialweb.model.acl.AclRule;
import org.onesocialweb.model.vcard4.EmailField;
import org.onesocialweb.openfire.model.acl.PersistentAclRule;

@Entity(name="EmailField")
public class PersistentEmailField extends EmailField {
	
	@OneToMany(cascade=CascadeType.ALL, targetEntity=PersistentAclRule.class, fetch=FetchType.EAGER)
	private List<AclRule> rules = new ArrayList<AclRule>();
	
	@Enumerated(EnumType.ORDINAL)
	private EmailField.Type type = EmailField.Type.Home;
	
	@Basic
	private String email;
	
	@Override
	public String getEmail() {
		return this.email;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	public void setEmail(String email, Type type) {
		this.email = email;
		this.type= type;
	}
	
	@Override
	public Type getType(){
		return this.type;
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
