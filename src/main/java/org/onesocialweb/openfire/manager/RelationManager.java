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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.dom4j.dom.DOMDocument;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.onesocialweb.model.atom.DefaultAtomHelper;
import org.onesocialweb.model.relation.Relation;
import org.onesocialweb.openfire.OswPlugin;
import org.onesocialweb.openfire.exception.InvalidRelationException;
import org.onesocialweb.openfire.model.relation.PersistentRelation;
import org.onesocialweb.xml.dom.RelationDomWriter;
import org.onesocialweb.xml.dom.imp.DefaultRelationDomWriter;
import org.onesocialweb.xml.namespace.Onesocialweb;
import org.w3c.dom.Element;
import org.xmpp.packet.Message;

/**
 * The relation manager is a singleton class taking care of all the business
 * logic related to querying, creating, updating and deleting relations.
 * 
 * @author eschenal
 * 
 */
public class RelationManager {

	public static final String NODE = "http://onesocialweb.org/spec/1.0/relations";
	
	/**
	 * Singleton: keep a static reference to teh only instance
	 */
	private static RelationManager instance;

	public static RelationManager getInstance() {
		if (instance == null) {
			// Carefull, we are in a threaded environment !
			synchronized (RelationManager.class) {
				instance = new RelationManager();
			}
		}
		return instance;
	}

	/**
	 * Retrieves the relation of the target user as can be seen by the requesting user.
	 * 
	 * TODO ACL is not yet implemented. All relations are returned at this stage.
	 * 
	 * @param requestorJID the entity requesting the relations.
	 * @param targetJID the entity whose relations are requested.
	 * @return the list of relations of the target user, as can be seen by the requestor 
	 * @throws UserNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public List<Relation> getRelations(String requestorJID, String targetJID) throws UserNotFoundException {
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		Query query = em.createQuery("SELECT DISTINCT relation FROM Relation relation WHERE relation.owner = :target");

		// Parametrize the query
		query.setParameter("target", targetJID);
		List<Relation> result = query.getResultList();
		em.close();

		return result;
	}

	/**
	 * Setup a new relation.
	 * 
	 * @param userJID the user seting up the new relation
	 * @param relation the relation to setup
	 * @throws InvalidRelationException
	 */
	public void setupRelation(String userJID, PersistentRelation relation) throws InvalidRelationException {
		// Validate the relation request
		// TODO More should be validated here
		if (!relation.hasFrom() || !relation.hasTo() || !relation.hasNature()) {
			throw new InvalidRelationException("Relation is missing required elements");
		}

		// Verify that the from or to is the user making the request
		if (!(relation.getFrom().equals(userJID) || relation.getTo().equals(userJID))) {
			throw new InvalidRelationException("Must be part of the relation to create it");
		}

		// Assign a unique ID to this new relation
		relation.setId(DefaultAtomHelper.generateId());

		// Set the request status
		relation.setStatus(Relation.Status.REQUEST);

		// We store the relation for requestor
		relation.setOwner(userJID);

		// Persist the relation
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		em.getTransaction().begin();
		em.persist(relation);
		em.getTransaction().commit();
		em.close();

		// We cleanup and notifiy the relation for the recipient
		relation.setAclRules(null);
		relation.setComment(null);
		notify(userJID, relation);
	}

	/**
	 * Update an existing relation.
	 * 
	 * @param userJID the user seting up the new relation
	 * @param relation the relation to setup
	 * @throws InvalidRelationException
	 */	
	@SuppressWarnings("unchecked")
	public void updateRelation(String userJID, PersistentRelation relation) throws InvalidRelationException {
		// Validate the relation request
		// TODO More should be validated here
		if (!relation.hasId() || !relation.hasStatus()) {
			throw new InvalidRelationException("Relation is missing required elements");
		}

		// Search for an existing relation with the given ID
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		Query query = em.createQuery("SELECT x FROM Relation x WHERE x.owner = ?1 AND x.guid = ?2");
		query.setParameter(1, userJID);
		query.setParameter(2, relation.getId());
		List<PersistentRelation> relations = query.getResultList();

		// If no match, or more than one, we have an issue
		if (relations.size() != 1) {
			throw new InvalidRelationException("Could not find relationship with id " + relation.getId());
		}

		// We update the persisted relation
		em.getTransaction().begin();
		PersistentRelation storedRelation = relations.get(0);
		storedRelation.setStatus(relation.getStatus());
		em.getTransaction().commit();
		em.close();

		// We cleanup and notifiy the relation for the recipient
		storedRelation.setAclRules(null);
		storedRelation.setComment(null);
		notify(userJID, storedRelation);
	}

	/**
	 * Handles a relation notification message.
	 * 
	 * @param remoteJID the entity sending the message
	 * @param localJID the entity having received the message
	 * @param relation the relation being notified
	 * @throws InvalidRelationException
	 */
	public void handleMessage(String remoteJID, String localJID, Relation relation) throws InvalidRelationException {
		// We need at least a status field
		if (!relation.hasStatus()) {
			throw new InvalidRelationException("Relation is missing a status field");
		}

		// Is this a new request ?
		if (relation.getStatus().equals(Relation.Status.REQUEST)) {
			handleRequestMessage(remoteJID, localJID, relation);
		} else {
			handleUpdateMessage(remoteJID, localJID, relation);
		}
	}

	@SuppressWarnings("unchecked")
	private void handleRequestMessage(String remoteJID, String localJID, Relation relation) throws InvalidRelationException {
		// Are required fields for a new relation setup present ?
		if (!relation.hasNature() || !relation.hasStatus() || !relation.hasFrom() || !relation.hasTo() || !relation.hasId()) {
			throw new InvalidRelationException("Relation is missing required elements");
		}

		// The relation should be between the sender and the receiver
		if (getDirection(relation, remoteJID, localJID) == 0) {
			throw new InvalidRelationException("Relation from/to do not match message from/to");
		}

		// Cannot add a relation to yourself
		if (relation.getFrom().equals(relation.getTo())) {
			throw new InvalidRelationException("Cannot add relation to yourself");
		}

		// Verify that this relation is not already here
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		Query query = em.createQuery("SELECT x FROM Relation x WHERE x.owner = ?1 AND x.guid = ?2");
		query.setParameter(1, localJID);
		query.setParameter(2, relation.getId());
		List<PersistentRelation> relations = query.getResultList();

		// If there is a match, we give up
		// TODO Not that fast. The other end may jut have not received any
		// answer and wants to try again
		// we should deal with all these recovery features.
		if (relations.size() > 0) {
			throw new InvalidRelationException("This relation has already been requested");
		}

		// Save the relation
		PersistentRelation persistentRelation = (PersistentRelation) relation;
		persistentRelation.setOwner(localJID);

		em.getTransaction().begin();
		em.persist(persistentRelation);
		em.getTransaction().commit();
		em.close();
	}

	@SuppressWarnings("unchecked")
	private void handleUpdateMessage(String remoteJID, String localJID, Relation relation) throws InvalidRelationException {
		// Search for the stored relation
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		Query query = em.createQuery("SELECT x FROM Relation x WHERE x.owner = ?1 AND x.guid = ?2");
		query.setParameter(1, localJID);
		query.setParameter(2, relation.getId());
		List<PersistentRelation> relations = query.getResultList();

		// If no match, or more than one, we have an issue
		if (relations.size() != 1) {
			throw new InvalidRelationException("Could not find matching relationship");
		}

		// We update the persisted relation
		em.getTransaction().begin();
		PersistentRelation previous = relations.get(0);
		previous.setStatus(relation.getStatus());
		em.getTransaction().commit();
		em.close();
	}

	private void notify(String localJID, Relation relation) {
		final DOMDocument domDocument = new DOMDocument();
		final Element entryElement = (Element) domDocument.appendChild(domDocument.createElementNS(Onesocialweb.NAMESPACE, Onesocialweb.RELATION_ELEMENT));
		final RelationDomWriter writer = new DefaultRelationDomWriter();
		writer.write(relation, entryElement);
		domDocument.removeChild(entryElement);

		final Message message = new Message();
		message.setFrom(localJID);
		message.setType(Message.Type.headline);
		org.dom4j.Element eventElement = message.addChildElement("event", "http://jabber.org/protocol/pubsub#event");
		org.dom4j.Element itemsElement = eventElement.addElement("items");
		itemsElement.addAttribute("node", NODE);
		org.dom4j.Element itemElement = itemsElement.addElement("item");
		itemElement.addAttribute("id", relation.getId());
		itemElement.add((org.dom4j.Element) entryElement);

		// Send to this user
		message.setTo(getOtherEnd(relation, localJID));
		server.getMessageRouter().route(message);
	}

	private String getOtherEnd(Relation relation, String userJID) {
		if (!relation.hasFrom() || !relation.hasTo()) {
			return null;
		}

		if (relation.getFrom().equals(userJID)) {
			return relation.getTo();
		}

		if (relation.getTo().equals(userJID)) {
			return relation.getFrom();
		}

		// The given JID is no part of this relation
		return null;
	}

	private int getDirection(Relation relation, String fromJID, String toJID) {
		if (!relation.hasFrom() || !relation.hasTo()) {
			return 0;
		}

		if (relation.getFrom().equals(fromJID) && relation.getTo().equals(toJID)) {
			return 1;
		}

		if (relation.getFrom().equals(toJID) && relation.getTo().equals(fromJID)) {
			return -1;
		}

		// If we are here, the relation does not concern from & to
		return 0;
	}

	/**
	 * Class dependencies (should be true dependency injection someday)
	 */

	private final XMPPServer server;

	/**
	 * Private constructor to enforce the singleton
	 */
	private RelationManager() {
		server = XMPPServer.getInstance();
	}

}
