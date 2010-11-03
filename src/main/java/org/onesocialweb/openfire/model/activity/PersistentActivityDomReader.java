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
package org.onesocialweb.openfire.model.activity;

import java.util.Date;

import org.onesocialweb.model.activity.ActivityFactory;
import org.onesocialweb.model.atom.DefaultAtomHelper;
import org.onesocialweb.openfire.model.acl.PersistentAclDomReader;
import org.onesocialweb.openfire.model.atom.PersistentAtomDomReader;
import org.onesocialweb.xml.dom.AclDomReader;
import org.onesocialweb.xml.dom.ActivityDomReader;
import org.onesocialweb.xml.dom.AtomDomReader;

public class PersistentActivityDomReader extends ActivityDomReader {

	@Override
	protected AclDomReader getAclDomReader() {
		return new PersistentAclDomReader();
	}

	@Override
	protected ActivityFactory getActivityFactory() {
		return new PersistentActivityFactory();
	}

	@Override
	protected AtomDomReader getAtomDomReader() {
		return new PersistentAtomDomReader();
	}

	@Override
	protected Date parseDate(String atomDate) {
		return DefaultAtomHelper.parseDate(atomDate);
	}

}
