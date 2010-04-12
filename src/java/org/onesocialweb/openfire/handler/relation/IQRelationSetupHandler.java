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
package org.onesocialweb.openfire.handler.relation;

import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.Log;
import org.onesocialweb.openfire.manager.RelationManager;
import org.onesocialweb.openfire.model.relation.PersistentRelation;
import org.onesocialweb.openfire.model.relation.PersistentRelationDomReader;
import org.onesocialweb.xml.dom.RelationDomReader;
import org.onesocialweb.xml.dom4j.ElementAdapter;
import org.onesocialweb.xml.namespace.Onesocialweb;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

public class IQRelationSetupHandler extends IQHandler {

	public static final String NAME = "setup";
	
	public static final String NAMESPACE = "http://onesocialweb.org/spec/1.0/relations#setup";
	
	private final IQHandlerInfo info = new IQHandlerInfo(NAME, NAMESPACE);

	private UserManager userManager;
	
	private RelationManager relationManager;
	
	public IQRelationSetupHandler() {
		super("OneSocialWeb - Setup relations handler");
	}

	@Override
	public IQHandlerInfo getInfo() {
		return info;
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

			// Only a local user can request to setup a relation
			if (!userManager.isRegisteredUser(sender.getNode())) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.not_authorized);
				return result;
			}

			// A valid request is an IQ of type set, sent to the bare server
			if ((!packet.getType().equals(IQ.Type.set) || (recipient != null && recipient.getNode() != null))) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.bad_request);
				return result;
			}

			// A valid submit requets must contain one relation item
			Element request = packet.getChildElement();
			Iterator<Element> i_entry = request.elementIterator(QName.get(Onesocialweb.RELATION_ELEMENT, Namespace.get(Onesocialweb.NAMESPACE)));
			if (!i_entry.hasNext()) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.bad_request);
				return result;
			}

			// Parse the relation
			RelationDomReader reader = new PersistentRelationDomReader();
			Element e_entry = i_entry.next();
			PersistentRelation relation = (PersistentRelation) reader.readElement(new ElementAdapter(e_entry));
			Log.debug("IQRelationSetup received request: " + relation);
			
			// Setup the relation (this will also trigger the notification to the user)
			relationManager.setupRelation(sender.toBareJID(), relation);

			// Send a success result
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
		relationManager = RelationManager.getInstance();
	}
	
}
