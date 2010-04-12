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

import org.onesocialweb.model.atom.AtomCategory;
import org.onesocialweb.model.atom.AtomContent;
import org.onesocialweb.model.atom.AtomEntry;
import org.onesocialweb.model.atom.AtomFactory;
import org.onesocialweb.model.atom.AtomLink;
import org.onesocialweb.model.atom.AtomPerson;
import org.onesocialweb.model.atom.AtomReplyTo;
import org.onesocialweb.model.atom.AtomSource;

public class PersistentAtomFactory extends AtomFactory {

	@Override
	public AtomCategory category() {
		return new PersistentAtomCategory();
	}

	@Override
	public AtomContent content() {
		return new PersistentAtomContent();
	}

	@Override
	public AtomEntry entry() {
		return new PersistentAtomEntry();
	}

	@Override
	public AtomLink link() {
		return new PersistentAtomLink();
	}

	@Override
	public AtomPerson person() {
		return new PersistentAtomPerson();
	}

	@Override
	public AtomSource source() {
		return new PersistentAtomSource();
	}

	@Override
	public AtomReplyTo reply() {
		return new PersistentAtomReplyTo();
	}

}
