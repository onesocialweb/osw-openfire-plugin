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

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.Log;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.openfire.handler.pep.PEPCommandHandler;
import org.onesocialweb.openfire.manager.ActivityManager;
import org.onesocialweb.openfire.model.activity.PersistentActivityDomReader;
import org.onesocialweb.xml.dom.ActivityDomReader;
import org.onesocialweb.xml.dom4j.ElementAdapter;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

public class ActivityPublishHandler extends PEPCommandHandler {

	public static final String COMMAND = "publish";
	
	private UserManager userManager;

	private ActivityManager activityManager;
	
	public ActivityPublishHandler() {
		super("OneSocialWeb - Publish activity handler");
	}
	
	@Override
	public String getCommand() {
		return COMMAND;
	}

	@SuppressWarnings( { "deprecation", "unchecked" })
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		final JID sender = packet.getFrom();
		final JID recipient = packet.getTo();

		// Process the request inside a try/catch so that unhandled exceptions
		// (oufofbounds etc...) can trigger a server error and we can send a
		// error result packet
		try {

			// A valid request is an IQ of type set, 
			if (!packet.getType().equals(IQ.Type.set)) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.bad_request);
				return result;
			}
			
			// If a recipient is specified, it must be equal to the sender
			// bareJID
			if (recipient != null && !recipient.toString().equals(sender.toBareJID())) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.not_authorized);
				return result;
			}
			
			// Only a local user can publish an activity to his stream
			if (!userManager.isRegisteredUser(sender.getNode())) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.not_authorized);
				return result;
			}

			// A valid submit request must contain at least one entry
			Element pubsubElement = packet.getChildElement();
			Element publishElement = pubsubElement.element("publish");
			List<Element> items = publishElement.elements("item");
			if (items == null || items.size() == 0) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.bad_request);
				return result;
			}

			// Parse the activities
			ActivityDomReader reader = new PersistentActivityDomReader();
			List<String> itemIds = new ArrayList<String>(items.size());
			for (Element item : items) {
				Element entry = item.element("entry");
				if (entry != null) {
					ActivityEntry activity = reader.readEntry(new ElementAdapter(entry));
					Log.debug("ActivityPublishHandler received activity: " + activity);
					try {
						if ((activity.getId()!=null) && (activity.getId().length()!=0))
							activityManager.updateActivity(sender.toBareJID(), activity);					
						else{							
							activityManager.publishActivity(sender.toBareJID(), activity);
							itemIds.add(activity.getId());
							}														
					} catch (UserNotFoundException e) {}
				}
			}

			// Send a success result
			IQ result = IQ.createResultIQ(packet);
			Element resultPubsubElement = result.setChildElement("pubsub", "http://jabber.org/protocol/pubsub");
			Element resultPublishElement = resultPubsubElement.addElement("publish", "http://jabber.org/protocol/pubsub");
			resultPublishElement.addAttribute("node", PEPActivityHandler.NODE);
			for (String itemId : itemIds) {
				Element itemElement = resultPublishElement.addElement("item");
				itemElement.addAttribute("id", itemId);
			}
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
		activityManager = ActivityManager.getInstance();
	}
	
}
