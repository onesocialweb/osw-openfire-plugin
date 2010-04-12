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

import java.util.List;

import javax.activity.InvalidActivityException;

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.Log;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.model.relation.Relation;
import org.onesocialweb.openfire.exception.AccessDeniedException;
import org.onesocialweb.openfire.exception.InvalidRelationException;
import org.onesocialweb.openfire.handler.activity.PEPActivityHandler;
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
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException {

		// We only care for incoming Messages has not yet been processed
		if (incoming && !processed && packet instanceof Message) {

			final Message message = (Message) packet;
			final JID fromJID = message.getFrom();
			final JID toJID = message.getTo();

			// We only care for messaes to local users
			if (!server.isLocal(toJID) || !server.getUserManager().isRegisteredUser(toJID)) {
				return;
			}

			// We only bother about pubsub events
			Element eventElement = message.getChildElement("event", "http://jabber.org/protocol/pubsub#event");
			if (eventElement == null) {
				return;
			}

			// That contains items
			Element itemsElement = eventElement.element("items");
			if (itemsElement == null || itemsElement.attribute("node") == null) {
				return;
			}

			// Relating to the microblogging node
			if (itemsElement.attribute("node").getValue().equals(PEPActivityHandler.NODE)) {
				Log.debug("Processing an activity event from " + fromJID + " to " + toJID);
				final ActivityDomReader reader = new PersistentActivityDomReader();
				for (Element itemElement : (List<Element>) itemsElement.elements("item")) {
					ActivityEntry activity = reader.readEntry(new ElementAdapter(itemElement.element("entry")));
					try {
						ActivityManager.getInstance().handleMessage(fromJID.toBareJID(), toJID.toBareJID(), activity);
					} catch (InvalidActivityException e) {
						throw new PacketRejectedException();
					} catch (AccessDeniedException e) {
						throw new PacketRejectedException();
					}
				}
			}
			// or a relation event
			else if (itemsElement.attribute("node").getValue().equals(RelationManager.NODE)) {
				final RelationDomReader reader = new PersistentRelationDomReader();
				for (Element itemElement : (List<Element>) itemsElement.elements("item")) {
					Relation relation = reader.readElement(new ElementAdapter(itemElement.element("relation")));
					try {
						RelationManager.getInstance().handleMessage(fromJID.toBareJID(), toJID.toBareJID(), relation);
					} catch (InvalidRelationException e) {
						throw new PacketRejectedException();
					}
				}
			}

		}
	}
}
