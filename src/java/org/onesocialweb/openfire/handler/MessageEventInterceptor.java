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
package org.onesocialweb.openfire.handler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.activity.InvalidActivityException;

import org.dom4j.Element;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.Log;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.model.relation.Relation;
import org.onesocialweb.openfire.exception.AccessDeniedException;
import org.onesocialweb.openfire.exception.InvalidRelationException;
import org.onesocialweb.openfire.handler.activity.PEPActivityHandler;
import org.onesocialweb.openfire.handler.commenting.PEPCommentingHandler;
import org.onesocialweb.openfire.manager.ActivityManager;
import org.onesocialweb.openfire.manager.RelationManager;
import org.onesocialweb.openfire.model.activity.PersistentActivityDomReader;
import org.onesocialweb.openfire.model.relation.PersistentRelationDomReader;
import org.onesocialweb.xml.dom.ActivityDomReader;
import org.onesocialweb.xml.dom.RelationDomReader;
import org.onesocialweb.xml.dom4j.ElementAdapter;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class MessageEventInterceptor implements PacketInterceptor {

	private final XMPPServer server;

	public MessageEventInterceptor() {
		server = XMPPServer.getInstance();
	}

	@SuppressWarnings( { "deprecation", "unchecked" })
	public void interceptPacket(Packet packet, Session session,
			boolean incoming, boolean processed) throws PacketRejectedException {

		// We only care for incoming Messages has not yet been processed
		if (incoming && !processed && packet instanceof Message) {

			final Message message = (Message) packet;
			final JID fromJID = message.getFrom();
			final JID toJID = message.getTo();

			// We are only interested by message to bareJID (we don't touch the one sent to fullJID)
			if (!toJID.toBareJID().equalsIgnoreCase(toJID.toString())) {
				return;
			}

			// We only care for messaes to local users
			if (!server.isLocal(toJID)
					|| !server.getUserManager().isRegisteredUser(toJID)) {
				return;
			}

			// We only bother about pubsub events
			Element eventElement = message.getChildElement("event",
					"http://jabber.org/protocol/pubsub#event");
			if (eventElement == null) {
				return;
			}

			// That contains items
			Element itemsElement = eventElement.element("items");
			if (itemsElement == null || itemsElement.attribute("node") == null) 
				return;

			// Relating to the microblogging node
			if (itemsElement.attribute("node").getValue().equals(PEPActivityHandler.NODE)) {
																
				Log.debug("Processing an activity event from " + fromJID
						+ " to " + toJID);
				final ActivityDomReader reader = new PersistentActivityDomReader();
				List<Element> items=(List<Element>) itemsElement.elements("item"); 
				if ((items!=null) && (items.size()!=0)){
					for (Element itemElement :items) {
						ActivityEntry activity = reader.readEntry(new ElementAdapter(itemElement.element("entry")));
						try {														
							ActivityManager.getInstance().handleMessage(fromJID.toBareJID(), toJID.toBareJID(),activity);																				
						} catch (InvalidActivityException e) {
							throw new PacketRejectedException();
						} catch (AccessDeniedException e) {
							throw new PacketRejectedException();
						} 
					}
				} else if (itemsElement.element("retract")!=null)
				{					
					Element retractElement = itemsElement.element("retract");
					String activityId=reader.readActivityId(new ElementAdapter(retractElement));
					ActivityManager.getInstance().deleteMessage(activityId);
				}
				Set<JID> recipientFullJIDs = getFullJIDs(toJID
						.toBareJID());
				Iterator<JID> it = recipientFullJIDs.iterator();
				Message extendedMessage = message.createCopy();
				while (it.hasNext()) {
					String fullJid = it.next().toString();
					extendedMessage.setTo(fullJid);
					server.getMessageRouter().route(extendedMessage);
				}
				throw new PacketRejectedException();
			}
			
			// or a relation event
			else if (itemsElement.attribute("node").getValue().equals(RelationManager.NODE)) {
				final RelationDomReader reader = new PersistentRelationDomReader();
				for (Element itemElement : (List<Element>) itemsElement
						.elements("item")) {
					Relation relation = reader.readElement(new ElementAdapter(
							itemElement.element("relation")));
					try {
						RelationManager.getInstance().handleMessage(
								fromJID.toBareJID(), toJID.toBareJID(),
								relation);
					} catch (InvalidRelationException e) {
						throw new PacketRejectedException();
					}
				}
			}
			//or perhaps a reply ...			
			else if (itemsElement.attribute("node").getValue().contains("urn:xmpp:microblog:0:replies")){			
			 
				final ActivityDomReader reader = new PersistentActivityDomReader();
				List<Element> items=(List<Element>) itemsElement.elements("item"); 
				if ((items!=null) && (items.size()!=0)){
					for (Element itemElement :items) {
						ActivityEntry activity = reader.readEntry(new ElementAdapter(itemElement.element("entry")));
						if (activity.getParentId()!=null)
							try{
								ActivityManager.getInstance().handleComment(fromJID.toBareJID(), toJID.toBareJID(),activity);
							} catch (UserNotFoundException e){
								throw new PacketRejectedException();
							} catch (InvalidActivityException e) {
								throw new PacketRejectedException();
							} catch (AccessDeniedException e) {
								throw new PacketRejectedException();
							} catch (UnauthorizedException e){
								throw new PacketRejectedException();
							}
					}
				}
			}						
		}
	}

	/*
	 * Returns a Set of the FullJids for all connected resources of a given
	 * BareJid
	 */
	private Set<JID> getFullJIDs(String jid) {
		JID recipientJID = new JID(jid);
		Set<JID> recipientFullJIDs = new HashSet<JID>();
		if (XMPPServer.getInstance().isLocal(recipientJID)) {
			if (recipientJID.getResource() == null) {
				for (ClientSession clientSession : SessionManager.getInstance()
						.getSessions(recipientJID.getNode())) {
					int prior = clientSession.getPresence().getPriority();
					if (prior >= 0) {
						recipientFullJIDs.add(clientSession.getAddress());
					}
				}
			}
		}

		return recipientFullJIDs;
	}
}
