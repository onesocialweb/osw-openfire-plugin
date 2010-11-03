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

import org.onesocialweb.model.atom.AtomContent;

@Entity(name="AtomContent")
public class PersistentAtomContent extends PersistentAtomCommon implements AtomContent {

	@Basic
	private String src;

	@Basic
	private String type;

	@Basic
	private String value;

	@Override
	public String getSrc() {
		return src;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setSrc(final String src) {
		this.src = src;
	}

	@Override
	public void setType(final String type) {
		this.type = type;
	}

	@Override
	public void setValue(final String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[AtomContent ");
		if (type != null) {
			buffer.append("type:" + type + " ");
		}
		if (src != null) {
			buffer.append("src:" + src + " ");
		}
		if (value != null) {
			buffer.append("value:" + value + " ");
		}
		buffer.append("]");
		
		return buffer.toString();
	}

	@Override
	public boolean hasSrc() {
		return (src != null);
	}

	@Override
	public boolean hasType() {
		return (type != null);
	}

	@Override
	public boolean hasValue() {
		return (value != null);
	}

}
