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

import javax.persistence.Basic;
import javax.persistence.Entity;

import org.onesocialweb.model.acl.AclAction;

@Entity(name="AclAction")
public class PersistentAclAction implements AclAction {

	@Basic
	private String name;
	
	@Basic
	private String permission;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPermission() {
		return permission;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setPermission(String permission) {
		this.permission = permission;
	}

	@Override
	public boolean hasName() {
		return (name != null && !name.isEmpty());
	}

	@Override
	public boolean hasPermission() {
		return (permission != null && !permission.isEmpty());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AclAction) {
			AclAction other = (AclAction) obj;
			return (other.getPermission().equals(permission) && other.getName()
					.equals(name));
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[AclAction ");
		if (name != null) {
			buffer.append("name:" + name + " ");
		}
		if (permission != null) {
			buffer.append("permission:" + permission + " ");
		}
		buffer.append("]");
		return buffer.toString();
	}

}
