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

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.Log;
import org.onesocialweb.openfire.handler.pep.PEPCommandHandler;
import org.onesocialweb.openfire.manager.ActivityManager;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

public class ActivitySubscribeHandler extends PEPCommandHandler {

	public static final String COMMAND = "subscribe";
	
	private UserManager userManager; 

	public ActivitySubscribeHandler() {
		super("OneSocialWeb - Subscribe to a user activities");
	}

	@Override
	public String getCommand() {
		return COMMAND;
	}

	@SuppressWarnings( { "deprecation" })
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {

		JID sender = packet.getFrom();
		JID recipient = packet.getTo();
		
		// Process the request inside a try/catch so that unhandled exceptions
		// (oufofbounds etc...) can trigger a server error and we can send a
		// error result packet
		try {
			
			// A valid request is an IQ of type set, for a valid and local recipient
			if (!(packet.getType().equals(IQ.Type.set) && recipient != null && recipient.getNode() != null 
					&& userManager.isRegisteredUser(recipient.getNode()))) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.bad_request);
				return result;
			}
			
			// A valid request has a (bare) JID attribute
			Element pubsubElement = packet.getChildElement();
			Element subscribeElement = pubsubElement.element("subscribe");
			Attribute jidAttribute = subscribeElement.attribute("jid");
			if (jidAttribute == null || !sender.toBareJID().equals(jidAttribute.getValue())) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.bad_request);
				return result;
			}
			
			// Add the relation to the database
			ActivityManager.getInstance().subscribe(sender.toBareJID(), recipient.toBareJID());
			
			// Return and send a result packet
			IQ result = IQ.createResultIQ(packet);
			Element resultPubsubElement = result.setChildElement("pubsub", "http://jabber.org/protocol/pubsub");
			Element resultSubscriptionElement = resultPubsubElement.addElement("subscription", "http://jabber.org/protocol/pubsub");
			resultSubscriptionElement.addAttribute("node", PEPActivityHandler.NODE);
			resultSubscriptionElement.addAttribute("jid", recipient.toBareJID());
			resultSubscriptionElement.addAttribute("subscription", "subscribed");
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
