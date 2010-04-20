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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activity.InvalidActivityException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.dom4j.dom.DOMDocument;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.roster.Roster;
import org.jivesoftware.openfire.roster.RosterItem;
import org.jivesoftware.openfire.roster.RosterManager;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.onesocialweb.model.acl.AclAction;
import org.onesocialweb.model.acl.AclFactory;
import org.onesocialweb.model.acl.AclRule;
import org.onesocialweb.model.acl.AclSubject;
import org.onesocialweb.model.activity.ActivityActor;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.model.activity.ActivityFactory;
import org.onesocialweb.model.activity.ActivityObject;
import org.onesocialweb.model.atom.AtomReplyTo;
import org.onesocialweb.model.atom.DefaultAtomHelper;
import org.onesocialweb.openfire.OswPlugin;
import org.onesocialweb.openfire.exception.AccessDeniedException;
import org.onesocialweb.openfire.handler.activity.PEPActivityHandler;
import org.onesocialweb.openfire.model.ActivityMessage;
import org.onesocialweb.openfire.model.PersistentActivityMessage;
import org.onesocialweb.openfire.model.PersistentSubscription;
import org.onesocialweb.openfire.model.Subscription;
import org.onesocialweb.openfire.model.acl.PersistentAclFactory;
import org.onesocialweb.openfire.model.activity.PersistentActivityEntry;
import org.onesocialweb.openfire.model.activity.PersistentActivityFactory;
import org.onesocialweb.xml.dom.ActivityDomWriter;
import org.onesocialweb.xml.dom.imp.DefaultActivityDomWriter;
import org.onesocialweb.xml.namespace.Atom;
import org.w3c.dom.Element;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

/**
 * The activity manager is a singleton class taking care of all the business
 * logic related to querying, creating, updating and deleting activities.
 * 
 * @author eschenal
 * 
 */
public class ActivityManager {

	/**
	 * Singleton: keep a static reference to teh only instance
	 */
	private static ActivityManager instance;

	public static ActivityManager getInstance() {
		if (instance == null) {
			// Carefull, we are in a threaded environment !
			synchronized (ActivityManager.class) {
				instance = new ActivityManager();
			}
		}
		return instance;
	}

	/**
	 * Class dependencies 
	 * TODO Make this a true dependency injection
	 */
	private final ActivityFactory activityFactory;

	private final AclFactory aclFactory;

	/**
	 * Publish a new activity to the activity stream of the given user.
	 * activity-actor element is overwrittern using the user profile data to
	 * avoid spoofing. Notifications messages are sent to the users subscribed
	 * to this user activities.
	 * 
	 * @param user
	 *            The user who the activity belongs to
	 * @param entry
	 *            The activity entry to publish
	 * @throws UserNotFoundException
	 */
	public void publishActivity(String userJID, ActivityEntry entry) throws UserNotFoundException {
		// Overide the actor to avoid spoofing
		User user = UserManager.getInstance().getUser(new JID(userJID).getNode());
		ActivityActor actor = activityFactory.actor();
		actor.setUri(userJID);
		actor.setName(user.getName());
		actor.setEmail(user.getEmail());

		// Persist the activities
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		em.getTransaction().begin();
		entry.setId(DefaultAtomHelper.generateId());
		for (ActivityObject object : entry.getObjects()) {
			object.setId(DefaultAtomHelper.generateId());
		}
		entry.setActor(actor);
		entry.setPublished(Calendar.getInstance().getTime());
		em.persist(entry);
		em.getTransaction().commit();
		em.close();

		// Broadcast the notifications
		notify(userJID, entry);
	}

	/**
	 * Retrieve the last activities of the target user, taking into account the
	 * access rights of the requesting user.
	 * 
	 * @param requestorJID
	 *            the user requesting the activities
	 * @param targetJID
	 *            the user whose activities are requested
	 * @return an immutable list of the last activities of the target entity that can be seen by the
	 *         requesting entity
	 * @throws UserNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public List<ActivityEntry> getActivities(String requestorJID, String targetJID) throws UserNotFoundException {
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		Query query = em.createQuery("SELECT DISTINCT entry FROM ActivityEntry entry" + "             JOIN entry.rules rule "
				+ "             JOIN rule.actions action " + "             JOIN rule.subjects subject "
				+ "             WHERE entry.actor.uri = :target " + "             AND action.name = :view "
				+ "             AND action.permission = :grant " + "             AND (subject.type = :everyone "
				+ "                  OR (subject.type = :group_type " + "                     AND subject.name IN (:groups)) "
				+ "                  OR (subject.type = :person " + "                      AND subject.name = :jid)) ORDER BY entry.published DESC");

		// Parametrize the query
		query.setParameter("target", targetJID);
		query.setParameter("view", AclAction.ACTION_VIEW);
		query.setParameter("grant", AclAction.PERMISSION_GRANT);
		query.setParameter("everyone", AclSubject.EVERYONE);
		query.setParameter("group_type", AclSubject.GROUP);
		query.setParameter("groups", getGroups(targetJID, requestorJID));
		query.setParameter("person", AclSubject.PERSON);
		query.setParameter("jid", requestorJID);
		query.setMaxResults(20);
		List<ActivityEntry> result = query.getResultList();
		em.close();

		return Collections.unmodifiableList(result);
	}

	/**
	 * Handle an activity pubsub event. Such a message is usually
	 * received by a user in these conditions: - the local user has subscribed
	 * to the remote user activities - the local user is "mentionned" in this
	 * activity - this activity relates to another activity of the local user
	 * 
	 * @param remoteJID
	 *            the entity sending the message
	 * @param localJID
	 *            the entity having received the message
	 * @param activity
	 *            the activity contained in the message
	 * @throws InvalidActivityException
	 * @throws AccessDeniedException
	 */
	public synchronized void handleMessage(String remoteJID, String localJID, ActivityEntry activity) throws InvalidActivityException, AccessDeniedException {

		// Validate the activity
		if (activity == null || !activity.hasId()) {
			throw new InvalidActivityException();
		}
		
		// Create a message for the recipient
		ActivityMessage message = new PersistentActivityMessage();
		message.setSender(remoteJID);
		message.setRecipient(localJID);
		message.setReceived(Calendar.getInstance().getTime());

		// Search if the activity exists in the database
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		PersistentActivityEntry previousActivity = em.find(PersistentActivityEntry.class, activity.getId());

		// Assign the activity to the existing one if it exists
		if (previousActivity != null) {
			message.setActivity(previousActivity);
		} else {
			message.setActivity(activity);
		}

		// We go ahead and post the message to the recipient mailbox
		em.getTransaction().begin();
		em.persist(message);
		em.getTransaction().commit();
		em.close();
				
	}
	
	
	
	/**
	 * Subscribe an entity to another entity activities.
	 * 
	 * @param from the subscriber
	 * @param to entity being subscribed to
	 * @throws AlreadySubscribed
	 */
	@SuppressWarnings("unchecked")
	public synchronized void subscribe(String from, String to) {
		
		// Check if it already exist
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		Query query = em.createQuery("SELECT x FROM Subscriptions x WHERE x.subscriber = ?1 AND x.target = ?2");
		query.setParameter(1, from);
		query.setParameter(2, to);
		List<Subscription> subscriptions = query.getResultList();
		
		// If already exist, we don't have anything left to do
		if (subscriptions != null && subscriptions.size() > 0) {
			em.close();
			return;
		}
		
		// Add the subscription
		Subscription subscription = new PersistentSubscription();
		subscription.setSubscriber(from);
		subscription.setTarget(to);
		subscription.setCreated(Calendar.getInstance().getTime());
		
		// Store
		em.getTransaction().begin();
		em.persist(subscription);
		em.getTransaction().commit();
		em.close();
	}
	

	/**
	 * Delete a subscription.
	 * 
	 * @param from the entity requesting to unsubscribe
	 * @param to the subscription target
	 * @throws SubscriptionNotFound
	 */
	@SuppressWarnings("unchecked")
	public synchronized void unsubscribe(String from, String to) {
		EntityManager em  = OswPlugin.getEmFactory().createEntityManager();
		
		// Check if it already exist
		Query query = em.createQuery("SELECT x FROM Subscriptions x WHERE x.subscriber = ?1 AND x.target = ?2");
		query.setParameter(1, from);
		query.setParameter(2, to);
		List<Subscription> subscriptions = query.getResultList();
		
		// If it does not exist, we don't have anything left to do 
		if (subscriptions == null || subscriptions.size()== 0) {
			em.close();
			return;
		}
		
		// Remove the subscriptions
		// There should never be more than one.. but better safe than sorry
		em.getTransaction().begin();
		for (Subscription activitySubscription : subscriptions) {
			em.remove(activitySubscription);
		}
		em.getTransaction().commit();
		em.close();
	}
	
	@SuppressWarnings("unchecked")
	public List<Subscription> getSubscribers(String targetJID) {
		// Get a list of people who are interested by this stuff
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		Query query = em.createQuery("SELECT x FROM Subscriptions x WHERE x.target = ?1");
		query.setParameter(1, targetJID);
		List<Subscription> subscriptions = query.getResultList();
		em.close();
		return subscriptions;
	}
	
	@SuppressWarnings("unchecked")
	public List<Subscription> getSubscriptions(String subscriberJID) {
		// Get a list of people who are interested by this stuff
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		Query query = em.createQuery("SELECT x FROM Subscriptions x WHERE x.subscriber = ?1");
		query.setParameter(1, subscriberJID);
		List<Subscription> subscriptions = query.getResultList();
		em.close();
		return subscriptions;
	}

	private void notify(String fromJID, ActivityEntry entry) throws UserNotFoundException {

		// TODO We may want to do some cleaning of activities before
		// forwarding them (e.g. remove the acl, it is no one business)
		final ActivityDomWriter writer = new DefaultActivityDomWriter();
		final XMPPServer server = XMPPServer.getInstance();
		final List<Subscription> subscriptions = getSubscribers(fromJID);
		final Roster roster = XMPPServer.getInstance().getRosterManager().getRoster(new JID(fromJID).getNode());
		final DOMDocument domDocument = new DOMDocument();

		// Prepare the message
		final Element entryElement = (Element) domDocument.appendChild(domDocument.createElementNS(Atom.NAMESPACE, Atom.ENTRY_ELEMENT));
		writer.write(entry, entryElement);
		domDocument.removeChild(entryElement);

		final Message message = new Message();
		message.setFrom(fromJID);
		message.setBody("New activity: " + entry.getTitle());
		message.setType(Message.Type.headline);
		org.dom4j.Element eventElement = message.addChildElement("event", "http://jabber.org/protocol/pubsub#event");
		org.dom4j.Element itemsElement = eventElement.addElement("items");
		itemsElement.addAttribute("node", PEPActivityHandler.NODE);
		org.dom4j.Element itemElement = itemsElement.addElement("item");
		itemElement.addAttribute("id", entry.getId());
		itemElement.add((org.dom4j.Element) entryElement);

		// Keep a list of people we sent it to avoid duplicates
		List<String> alreadySent = new ArrayList<String>();
		
		// Send to this user
		alreadySent.add(fromJID);
		message.setTo(fromJID);
		server.getMessageRouter().route(message);	
						
		// Send to all subscribers
		for (Subscription activitySubscription : subscriptions) {
			String recipientJID = activitySubscription.getSubscriber();
			if (!canSee(entry, roster, recipientJID)) {
				continue;
			}
			alreadySent.add(recipientJID);						
			message.setTo(recipientJID);
			server.getMessageRouter().route(message);	
		}

		// Send to recipients, if they can see it and have not already received it
		if (entry.hasRecipients()) {
			for (AtomReplyTo recipient : entry.getRecipients()) {
				//TODO This is dirty, the recipient should be an IRI etc...
				String recipientJID = recipient.getHref();  
				if (!alreadySent.contains(recipientJID) && canSee(entry, roster, recipientJID)) {
					alreadySent.add(fromJID);
					
					message.setTo(recipientJID);
					server.getMessageRouter().route(message);												
				}
			}
		}			
	}
	

	private List<String> getGroups(String ownerJID, String userJID) {
		RosterManager rosterManager = XMPPServer.getInstance().getRosterManager();
		Roster roster;
		try {
			roster = rosterManager.getRoster(new JID(ownerJID).getNode());
			RosterItem rosterItem = roster.getRosterItem(new JID(userJID));
			if (rosterItem != null) {
				return rosterItem.getGroups();
			}
		} catch (UserNotFoundException e) {
		}

		return new ArrayList<String>();
	}

	private boolean canSee(ActivityEntry entry, Roster roster, String viewer) {
		// Get a view action
		final AclAction viewAction = aclFactory.aclAction(AclAction.ACTION_VIEW, AclAction.PERMISSION_GRANT);
		AclRule rule = null;
		for (AclRule aclRule : entry.getAclRules()) {
			if (aclRule.hasAction(viewAction)) {
				rule = aclRule;
				break;
			}
		}

		// If no view action was found, we consider it is denied
		if (rule == null)
			return false;

		// Get the subjects, if none then access denied
		final List<AclSubject> subjects = rule.getSubjects();
		if (subjects == null)
			return false;

		// Get the roster entry that match the viewer, this is only
		// used for the groups based matches
		RosterItem rosterItem = null;
		try {
			rosterItem = roster.getRosterItem(new JID(viewer));
		} catch (UserNotFoundException e) {
		}

		// Iterate through the subjects and hope for the best
		for (AclSubject aclSubject : subjects) {
			if (aclSubject.getType().equals(AclSubject.EVERYONE)) {
				return true;
			} else if (aclSubject.getType().equals(AclSubject.GROUP)) {
				if (rosterItem != null && rosterItem.getGroups().contains(aclSubject.getName())) {
					return true;
				}
			} else if (aclSubject.getType().equals(AclSubject.PERSON)) {
				if (viewer.equals(aclSubject.getName())) {
					return true;
				}
			}
		}

		// Still here ? Then we did not find a match and it is a deny
		return false;
	}

	/**
	 * Private constructor to enforce the singleton
	 */
	private ActivityManager() {
		activityFactory = new PersistentActivityFactory();
		aclFactory = new PersistentAclFactory();
	}
}
