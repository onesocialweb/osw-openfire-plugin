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

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.Log;
import org.onesocialweb.model.atom.DefaultAtomHelper;
import org.onesocialweb.openfire.handler.pep.PEPCommandHandler;
import org.onesocialweb.openfire.manager.ActivityManager;
import org.onesocialweb.openfire.model.Subscription;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

public class ActivitySubscriptionsHandler extends PEPCommandHandler {

	public static final String COMMAND = "subscriptions";
	
	private UserManager userManager;

	private ActivityManager activityManager;
	
	public ActivitySubscriptionsHandler() {
		super("OneSocialWeb - Retrieves a user subscriptions");
	}
	
	@Override
	public String getCommand() {
		return COMMAND;
	}

	@SuppressWarnings( { "deprecation"})
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		JID sender = packet.getFrom();
		JID recipient = packet.getTo();

		// Process the request inside a try/catch so that unhandled exceptions
		// (oufofbounds etc...) can trigger a server error and we can send a
		// error result packet
		try {

			// If no recipient, we assume the recipient is the sender
			if (recipient == null) {
				recipient = sender;
			}
			
			// A valid request is an IQ of type get, for a valid and local recipient
			if (!(packet.getType().equals(IQ.Type.get) && recipient != null && recipient.getNode() != null 
					&& userManager.isRegisteredUser(recipient.getNode()))) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.bad_request);
				return result;
			}

			// Fetch the subscriptions
			List<Subscription> subscriptions = activityManager.getSubscriptions(recipient.toBareJID());

			// Send a success result
			IQ result = IQ.createResultIQ(packet);
			Element resultPubsubElement = result.setChildElement("pubsub", "http://jabber.org/protocol/pubsub");
			Element resultPublishElement = resultPubsubElement.addElement("subscriptions", "http://jabber.org/protocol/pubsub");
			resultPublishElement.addAttribute("node", PEPActivityHandler.NODE);
			for (Subscription sub : subscriptions) {
				Element subElement = resultPublishElement.addElement("subscription");
				subElement.addAttribute("node", PEPActivityHandler.NODE);
				subElement.addAttribute("jid", sub.getTarget());
				subElement.addAttribute("subscription", "subscribed");
				subElement.addAttribute("created", DefaultAtomHelper.format(sub.getCreated()));
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
