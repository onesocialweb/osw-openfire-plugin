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

import org.onesocialweb.model.activity.ActivityActor;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.model.activity.ActivityFactory;
import org.onesocialweb.model.activity.ActivityObject;
import org.onesocialweb.model.activity.ActivityVerb;

public class PersistentActivityFactory extends ActivityFactory {

	@Override
	public ActivityActor actor() {
		return new PersistentActivityActor();
	}

	@Override
	public ActivityEntry entry() {
		return new PersistentActivityEntry();
	}

	@Override
	public ActivityObject object() {
		return new PersistentActivityObject();
	}

	@Override
	public ActivityVerb verb() {
		return new PersistentActivityVerb();
	}

}
