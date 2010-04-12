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

import javax.persistence.EntityManager;

import org.jivesoftware.openfire.user.UserNotFoundException;
import org.onesocialweb.model.vcard4.Profile;
import org.onesocialweb.openfire.OswPlugin;
import org.onesocialweb.openfire.model.vcard4.PersistentProfile;

/**
 * The profile manager is a singleton class taking care of all the business
 * logic related to querying, creating, and updating a user profile.
 * 
 * @author eschenal
 * 
 */
public class ProfileManager {

	/**
	 * Singleton: keep a static reference to teh only instance
	 */
	private static ProfileManager instance;

	public static ProfileManager getInstance() {
		if (instance == null) {
			instance = new ProfileManager();
		}
		return instance;
	}

	/**
	 * Retrieves the profile of the target entity has can be seen by the
	 * requesting entity.
	 * 
	 * TODO ACL is not yet implemented. All fields are returned at this stage.
	 * 
	 * @param requestorJID
	 *            the entity requesting the profile
	 * @param targetJID
	 *            the entity whose profile is requested
	 * @return the profile of the target entity as can be seen by the requesting
	 *         entity.
	 * @throws UserNotFoundException
	 */
	public Profile getProfile(String requestorJID, String targetJID) throws UserNotFoundException {
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		PersistentProfile profile = em.find(PersistentProfile.class, targetJID);
		em.close();
		if (profile != null) {
			if (requestorJID.equals(targetJID)) {
				return profile;
			} else {
				// TODO We should filter all fields that the requestor is not
				// supposed to see and strip all data related to ACLs.
				return profile;
			}
		} else {
			return null;
		}
	}

	/**
	 * Create or update the profile of a user.
	 * 
	 * If the user already has a profile defined, that profile will first be deleted and 
	 * replaced by the new profile.
	 * 
	 * @param userJID the user whose profile is to be changed
	 * @param profile the new profile
	 * @throws UserNotFoundException
	 */
	public void publishProfile(String userJID, Profile profile) throws UserNotFoundException {
		// open a transaction since we want delete and update to be atomical
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		em.getTransaction().begin();

		// Overide the user to avoid spoofing
		profile.setUserId(userJID);

		// Remove an old profile
		PersistentProfile oldProfile = em.find(PersistentProfile.class, userJID);
		if (oldProfile != null) {
			em.remove(oldProfile);
		}

		// Persist the profile
		em.persist(profile);

		// Safe to commit here
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * Private constructor to enforce the singleton
	 */
	private ProfileManager() {
		//
	}
}
