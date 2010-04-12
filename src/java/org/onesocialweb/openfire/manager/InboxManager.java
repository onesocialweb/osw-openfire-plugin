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
package org.onesocialweb.openfire.manager;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.onesocialweb.openfire.OswPlugin;
import org.onesocialweb.openfire.model.ActivityMessage;

/**
 * The inbox manager is a singleton class taking care of all the business
 * logic related to querying, creating, updating and deleting offline messages. 
 * 
 * @author eschenal
 * 
 */
public class InboxManager {

	/**
	 * Singleton: keep a static reference to teh only instance
	 */
	private static InboxManager instance;

	public static InboxManager getInstance() {
		if (instance == null) {
			// Carefull, we are in a threaded environment !
			synchronized (InboxManager.class) {
				instance = new InboxManager();
			}
		}
		return instance;
	}
	
	/**
	 * Retrieves the last 20 message (which are in fact received activity entries) from
	 * a user inbox as a immutable list.
	 *  
	 * @param userJID the user requesting his inbox
	 * @return the last 20 messages from the user inbox
	 */
	@SuppressWarnings("unchecked")
	public List<ActivityMessage> getMessages(String userJID) {
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		Query query = em.createQuery("SELECT x FROM Messages x WHERE x.recipient = ?1 ORDER BY x.received DESC");
		query.setParameter(1, userJID);
		query.setMaxResults(20);
		List<ActivityMessage> messages = query.getResultList();
		em.close();
		return Collections.unmodifiableList(messages);
	}

}
