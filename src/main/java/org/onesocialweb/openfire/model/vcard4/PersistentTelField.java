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
import org.onesocialweb.model.vcard4.TelField;
import org.onesocialweb.openfire.model.acl.PersistentAclRule;

@Entity(name="TelField")
public class PersistentTelField extends TelField{

	@OneToMany(cascade=CascadeType.ALL, targetEntity=PersistentAclRule.class, fetch=FetchType.EAGER)
	private List<AclRule> rules = new ArrayList<AclRule>();	
	
	@Enumerated(EnumType.ORDINAL)
	private TelField.Type type = TelField.Type.VOICE;
	
	@Basic
	private String tel;
	
	@Override
	public String getNumber() {
		return tel;
	}

	@Override
	public void setNumber(String tel, Type type) {
		this.tel = tel;
		this.type=type;
	}
	
	public Type getType()
	{
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
