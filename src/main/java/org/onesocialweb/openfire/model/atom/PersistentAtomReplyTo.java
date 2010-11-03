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

import org.onesocialweb.model.atom.AtomReplyTo;
@Entity(name="AtomReplyTo")
public class PersistentAtomReplyTo extends PersistentAtomCommon implements AtomReplyTo {

	@Basic
	private String ref;
	
	@Basic
	private String href;
	
	@Basic
	private String type;
	
	@Basic
	private String source;
	
	@Override
	public String getHref() {
		return href;
	}

	@Override
	public String getRef() {
		return ref;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public boolean hasHref() {
		return href != null;
	}

	@Override
	public boolean hasRef() {
		return ref != null;
	}

	@Override
	public boolean hasSource() {
		return source != null;
	}

	@Override
	public boolean hasType() {
		return type != null;
	}

	@Override
	public void setHref(String href) {
		this.href = href;
	}

	@Override
	public void setRef(String ref) {
		this.ref = ref;
	}

	@Override
	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}


	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[AtomLink ");
		if (href != null) {
			buffer.append("href:" + href + " ");
		}
		if (ref != null) {
			buffer.append("ref:" + ref + " ");
		}
		if (type != null) {
			buffer.append("type:" + type + " ");
		}
		if (source != null) {
			buffer.append("source:" + source + " ");
		}		
		buffer.append("]");
		return buffer.toString();
	}

}
