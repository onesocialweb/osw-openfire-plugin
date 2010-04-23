package org.onesocialweb.openfire.model.vcard4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.onesocialweb.model.acl.AclRule;
import org.onesocialweb.model.vcard4.TimeZoneField;
import org.onesocialweb.openfire.model.acl.PersistentAclRule;

@Entity(name="TimeZoneField")
public class PersistentTimeZoneField extends TimeZoneField
{
	
	@OneToMany(cascade=CascadeType.ALL, targetEntity=PersistentAclRule.class, fetch=FetchType.EAGER)
	private List<AclRule> rules = new ArrayList<AclRule>();	
	
	@Enumerated(EnumType.ORDINAL)
	private TimeZoneField.Type type = TimeZoneField.Type.TEXT;	

	@Basic
	private TimeZone timezone;
	
	
	@Override
	public String getTimeZone() {
		if (timezone != null) {
			return timezone.getID();
		} else {
			return null;
		}
	}
	
	@Override
	public TimeZone getJavaTimeZone() {
		return timezone;
	}
	
	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public void setTimeZone(String value, Type type) {
		TimeZone tz=TimeZone.getTimeZone(value);
		if (!tz.getID().equalsIgnoreCase(value))
			tz=TimeZone.getDefault();		
		setJavaTimeZone(tz);		
		this.type = type;
	}
	
	@Override
	public void setJavaTimeZone(TimeZone tz) {
		this.timezone = tz;		
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
