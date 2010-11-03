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

import org.onesocialweb.model.atom.AtomCategory;

@Entity(name="AtomCategory")
public class PersistentAtomCategory extends PersistentAtomCommon implements AtomCategory {

	@Basic
	private String label;

	@Basic
	private String scheme;

	@Basic
	private String term;

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public String getTerm() {
		return term;
	}

	@Override
	public boolean hasLabel() {
		return (label != null);
	}

	@Override
	public boolean hasScheme() {
		return scheme != null;
	}

	@Override
	public boolean hasTerm() {
		return term != null;
	}

	@Override
	public void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public void setScheme(final String scheme) {
		this.scheme = scheme;
	}

	@Override
	public void setTerm(final String term) {
		this.term = term;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[AtomCategory ");
		if (label != null) {
			buffer.append("label:" + label + " ");
		}
		if (term != null) {
			buffer.append("term:" + term + " ");
		}
		if (scheme != null) {
			buffer.append("scheme:" + scheme + " ");
		}
		buffer.append("]");
		return buffer.toString();
	}
		
}
