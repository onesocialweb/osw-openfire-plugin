/*
 *  Copyright 2010 Vodafone Group Services Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *    
 */
package org.onesocialweb.openfire.model.relation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.onesocialweb.model.acl.AclRule;
import org.onesocialweb.model.relation.Relation;
import org.onesocialweb.openfire.model.acl.PersistentAclRule;

@Embeddable
@Entity(name="Relation")
public class PersistentRelation implements Relation {
	
	@Basic
	private String guid;

	@Basic
	private String comment;
	
	@Basic
	private String origin;
	
	@Basic
	private String message;
	
	@Basic
	private String nature;
	
	@Basic
	private String owner;
	
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	private Date published;
	
	@Basic
	private String status;
	
	@Basic
	private String target;
	
	@OneToMany(cascade=CascadeType.ALL, targetEntity=PersistentAclRule.class, fetch=FetchType.EAGER)
	private List<AclRule> aclRules = new ArrayList<AclRule>();
	
	@Override
	public void addAclRule(AclRule rule) {
		this.aclRules.add(rule);
	}

	@Override
	public List<AclRule> getAclRules() {
		return aclRules;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public String getFrom() {
		return origin;
	}

	@Override
	public String getId() {
		return guid;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getNature() {
		return nature;
	}

	public String getOwner() {
		return owner;
	}
	
	@Override
	public Date getPublished() {
		return published;
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public String getTo() {
		return target;
	}

	@Override
	public boolean hasAclRules() {
		return (aclRules != null && !aclRules.isEmpty());
	}

	@Override
	public boolean hasComment() {
		return (comment != null);
	}

	@Override
	public boolean hasFrom() {
		return (origin != null);
	}

	@Override
	public boolean hasId() {
		return (guid != null);
	}

	@Override
	public boolean hasMessage() {
		return (message != null);
	}

	@Override
	public boolean hasNature() {
		return (nature != null);
	}
	
	public boolean hasOwner() {
		return (owner != null);
	}

	@Override
	public boolean hasPublished() {
		return (published != null);
	}

	@Override
	public boolean hasStatus() {
		return (status != null);
	}

	@Override
	public boolean hasTo() {
		return (target != null);
	}
	
	@Override
	public void removeAclRule(AclRule rule) {
		this.aclRules.remove(rule);
	}

	@Override
	public void setAclRules(List<AclRule> rules) {
		this.aclRules = rules;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public void setFrom(String from) {
		this.origin = from;
	}

	@Override
	public void setId(String id) {
		this.guid = id;
	}

	@Override
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public void setNature(String nature) {
		this.nature = nature;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}

	@Override
	public void setPublished(Date published) {
		this.published = published;
	}

	@Override
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public void setTo(String to) {
		this.target = to;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[Relation ");
		if (guid != null) {
			buffer.append("id:" + guid + " ");
		}
		if (origin != null) {
			buffer.append("from:" + origin + " ");
		}
		if (target != null) {
			buffer.append("to:" + target + " ");
		}
		if (nature != null) {
			buffer.append("nature:" + nature + " ");
		}
		if (status != null) {
			buffer.append("status:" + status + " ");
		}
		if (message != null) {
			buffer.append("message:" + message + " ");
		}
		if (comment != null) {
			buffer.append("comment:" + comment + " ");
		}
		buffer.append("]");
		return buffer.toString();
	}

}
