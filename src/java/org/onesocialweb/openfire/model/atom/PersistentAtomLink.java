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

import org.onesocialweb.model.atom.AtomLink;

@Entity(name="AtomLink")
public class PersistentAtomLink extends PersistentAtomCommon implements AtomLink {

	@Basic
	private String href;

	@Basic
	private String hrefLang;

	@Basic
	private String rel;

	@Basic
	private String title;

	@Basic
	private String type;

	@Basic
	private String length;
	
	@Basic
	private int counter;

	@Override
	public int getCount() {
		return counter;
	}

	@Override
	public void setCount(int count) {
		this.counter = count;
	}

	@Override
	public String getHref() {
		return href;
	}

	@Override
	public String getHreflang() {
		return hrefLang;
	}

	@Override
	public String getLength() {
		return length;
	}

	@Override
	public String getRel() {
		return rel;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setHref(final String href) {
		this.href = href;
	}

	@Override
	public void setHreflang(final String hreflang) {
		this.hrefLang = hreflang;
	}

	@Override
	public void setLength(final String length) {
		this.length = length;
	}

	@Override
	public void setRel(final String rel) {
		this.rel = rel;
	}

	@Override
	public void setTitle(final String title) {
		this.title = title;
	}

	@Override
	public void setType(final String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[AtomLink ");
		if (href != null) {
			buffer.append("href:" + href + " ");
		}
		if (hrefLang != null) {
			buffer.append("hrefLang:" + hrefLang + " ");
		}
		if (length != null) {
			buffer.append("length:" + length + " ");
		}
		if (rel != null) {
			buffer.append("rel:" + rel + " ");
		}
		if (title != null) {
			buffer.append("title:" + title + " ");
		}
		if (type != null) {
			buffer.append("type:" + type + " ");
		}
		buffer.append("]");
		return buffer.toString();
	}

	@Override
	public boolean hasHref() {
		return href != null;
	}

	@Override
	public boolean hasHreflang() {
		return hrefLang != null;
	}

	@Override
	public boolean hasLength() {
		return length != null;
	}

	@Override
	public boolean hasRel() {
		return rel != null;
	}

	@Override
	public boolean hasTitle() {
		return title != null;
	}

	@Override
	public boolean hasType() {
		return type != null;
	}
	
	@Override
	public boolean hasCount() {
		return counter != 0;
	}

}
