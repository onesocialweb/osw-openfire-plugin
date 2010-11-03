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
package org.onesocialweb.openfire.model.acl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.onesocialweb.model.acl.AclAction;
import org.onesocialweb.model.acl.AclRule;
import org.onesocialweb.model.acl.AclSubject;

@Entity(name="AclRule")
public class PersistentAclRule implements AclRule {
	
	@OneToMany(cascade=CascadeType.ALL, targetEntity=org.onesocialweb.openfire.model.acl.PersistentAclSubject.class, fetch=FetchType.EAGER)
	private List<AclSubject> subjects = new ArrayList<AclSubject>();

	@OneToMany(cascade=CascadeType.ALL, targetEntity=org.onesocialweb.openfire.model.acl.PersistentAclAction.class, fetch=FetchType.EAGER)
	private List<AclAction> actions = new ArrayList<AclAction>();

	@Override
	public List<AclAction> getActions() {
		return actions;
	}

	@Override
	public void setActions(List<AclAction> actions) {
		this.actions = actions;
	}

	@Override
	public List<AclSubject> getSubjects() {
		return subjects;
	}

	@Override
	public void setSubjects(List<AclSubject> subjects) {
		this.subjects = subjects;
	}
	
	@Override
	public void addAction(AclAction action) {
		this.actions.add(action);
	}

	@Override
	public void addSubject(AclSubject subject) {
		this.subjects.add(subject);
	}

	@Override
	public void removeAction(AclAction action) {
		this.actions.remove(action);
	}

	@Override
	public void removeSubject(AclSubject subject) {
		this.subjects.remove(subject);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AclRule) {
			AclRule rule = (AclRule) obj;
			return actions.equals(rule.getActions())
					&& subjects.equals(rule.getSubjects());
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[AclRule ");
		for (AclAction action : actions) {
			buffer.append(action);
		}
		for (AclSubject subject : subjects) {
			buffer.append(subject);
		}
		buffer.append("]");
		return buffer.toString();
	}

	@Override
	public boolean hasAction(AclAction action) {
		if (actions == null || action == null) return false;
		
		for (AclAction target : actions) {
			if (target.equals(action)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean hasSubject(AclSubject subject) {
		if (subjects == null || subject == null) return false;
		
		for (AclSubject target : subjects) {
			if (target.equals(subject)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean hasActions() {
		return (actions != null && !actions.isEmpty());
	}

	@Override
	public boolean hasSubjects() {
		return (subjects != null && !subjects.isEmpty());
	}

	@Override
	public List<AclAction> getActions(String name, String permission) {
		List<AclAction> result = new ArrayList<AclAction>();
		
		if (actions == null) return result;
		
		for (AclAction target : actions) {
			if (target.getName().equals(name) && target.getPermission().equals(permission)) {
				result.add(target);
			}
		}
		
		return result;
	}

	@Override
	public List<AclSubject> getSubjects(String type) {
		List<AclSubject> result = new ArrayList<AclSubject>();
		
		if (subjects == null || type == null) return result;
		
		for (AclSubject aclSubject : result) {
			if (aclSubject.getType().equals(type)) {
				result.add(aclSubject);
			}
		}
		
		return result;
	}
	
}
