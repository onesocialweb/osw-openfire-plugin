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
package org.onesocialweb.openfire.handler.profile;

import org.dom4j.Element;
import org.dom4j.QName;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.Log;
import org.onesocialweb.model.vcard4.Profile;
import org.onesocialweb.openfire.manager.ProfileManager;
import org.onesocialweb.openfire.model.vcard4.PersistentVCard4DomReader;
import org.onesocialweb.xml.dom.VCard4DomReader;
import org.onesocialweb.xml.dom4j.ElementAdapter;
import org.onesocialweb.xml.namespace.VCard4;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

public class IQProfilePublishHandler extends IQHandler {

	private final IQHandlerInfo info = new IQHandlerInfo("publish", "http://onesocialweb.org/spec/1.0/vcard4#publish");

	private UserManager userManager;

	public IQProfilePublishHandler() {
		super("OneSocialWeb - Publish profile handler");
	}

	@Override
	public IQHandlerInfo getInfo() {
		return info;
	}

	@SuppressWarnings("deprecation")
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
			
			// Only a local user can publish its profile
			if (!userManager.isRegisteredUser(sender.getNode())) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.not_authorized);
				return result;
			}

			// A valid submit requets must contain a vcard4 entry
			Element request = packet.getChildElement();
			Element e_profile = request.element(QName.get(VCard4.VCARD_ELEMENT, VCard4.NAMESPACE));
			if (e_profile == null) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.bad_request);
				return result;
			}

			// Parse the profile
			VCard4DomReader reader = new PersistentVCard4DomReader();
			Profile profile = reader.readProfile(new ElementAdapter(e_profile));
			
			// Commit the profile (this will also trigger the messages)
			try {
				ProfileManager.getInstance().publishProfile(sender.toBareJID(), profile);
			} catch (UserNotFoundException e) {
				// We know this cannot happen
			}

			// Send a success result
			// TODO should this contain more, like the ID of the new activities ?
			IQ result = IQ.createResultIQ(packet);
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
