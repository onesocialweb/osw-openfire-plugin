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

package org.onesocialweb.openfire.handler.activity;

import java.util.List;

import org.dom4j.dom.DOMDocument;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.Log;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.openfire.handler.pep.PEPCommandHandler;
import org.onesocialweb.openfire.manager.ActivityManager;
import org.onesocialweb.xml.dom.ActivityDomWriter;
import org.onesocialweb.xml.dom.imp.DefaultActivityDomWriter;
import org.onesocialweb.xml.namespace.Atom;
import org.w3c.dom.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

public class ActivityQueryHandler extends PEPCommandHandler {

	public static final String COMMAND = "items";
	
	private UserManager userManager; 

	public ActivityQueryHandler() {
		super("OneSocialWeb - Query a user activities");
	}

	@Override
	public String getCommand() {
		return COMMAND;
	}
	
	@SuppressWarnings( { "deprecation" })
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		
		JID sender = packet.getFrom();
		JID target = packet.getTo();
		
		// Process the request inside a try/catch so that unhandled exceptions
		// (oufofbounds etc...) can trigger a server error and we can send a
		// error result packet
		try {
			// If no recipient, then the target is the sender
			if (target == null || target.getNode() == null) {
				target = packet.getFrom();
			}
			
			// A valid request is an IQ of type get, for a valid and local recipient
			if (!(packet.getType().equals(IQ.Type.get) && target != null && target.getNode() != null 
					&& userManager.isRegisteredUser(target.getNode()))) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.bad_request);
				return result;
			}
			
			// We fetch the activities of the target user
			List<ActivityEntry> activities = ActivityManager.getInstance().getActivities(sender.toBareJID(), target.toBareJID());
			
			// Prepare the result packet
			ActivityDomWriter writer = new DefaultActivityDomWriter();
			DOMDocument domDocument = new DOMDocument();
			IQ result = IQ.createResultIQ(packet);
			org.dom4j.Element pubsubElement = result.setChildElement("pubsub", "http://jabber.org/protocol/pubsub");
			org.dom4j.Element itemsElement = pubsubElement.addElement("items");
			itemsElement.addAttribute("node", PEPActivityHandler.NODE);

			for (ActivityEntry entry : activities) {
				Element entryElement = (Element) domDocument.appendChild(domDocument.createElementNS(Atom.NAMESPACE, Atom.ENTRY_ELEMENT));
				writer.write(entry, entryElement);
				domDocument.removeChild(entryElement);
				org.dom4j.Element itemElement = itemsElement.addElement("item");
				itemElement.addAttribute("id", entry.getId());
				itemElement.add((org.dom4j.Element) entryElement);
			}
			
			// Return and send the result packet
			return result;
			
		} catch (Exception e) {
			Log.error(LocaleUtils.getLocalizedString("admin.error"), e);
			IQ result = IQ.createResultIQ(packet);
			result.setChildElement(packet.getChildElement().createCopy());
			result.setError(PacketError.Condition.internal_server_error);
			return result;
		}
	}

	@Override
	public void initialize(XMPPServer server) {
		super.initialize(server);
		userManager = server.getUserManager();
	}
}
