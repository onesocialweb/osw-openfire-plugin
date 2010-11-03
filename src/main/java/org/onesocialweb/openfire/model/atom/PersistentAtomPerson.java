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
package org.onesocialweb.openfire.model.atom;

import javax.persistence.Basic;
import javax.persistence.Entity;

import org.onesocialweb.model.atom.AtomPerson;

@Entity(name="AtomPerson")
public class PersistentAtomPerson extends PersistentAtomCommon implements AtomPerson {

	@Basic
	private String email;
	
	@Basic
	private String name;
	
	@Basic
	private String uri;

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[AtomPerson ");
		if (name != null) {
			buffer.append("name:" + name + " ");
		}
		if (email != null) {
			buffer.append("email:" + email + " ");
		}
		if (uri != null) {
			buffer.append("uri:" + uri + " ");
		}
		buffer.append("]");
		return buffer.toString();
	}

	@Override
	public boolean hasEmail() {
		return email != null;
	}

	@Override
	public boolean hasName() {
		return name != null;
	}

	@Override
	public boolean hasUri() {
		return uri != null;
	}

}
