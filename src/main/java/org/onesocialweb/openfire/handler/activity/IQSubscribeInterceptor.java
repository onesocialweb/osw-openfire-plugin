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

import javax.persistence.EntityManager;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.onesocialweb.model.cache.DomainCache;
import org.onesocialweb.openfire.OswPlugin;
import org.onesocialweb.openfire.manager.ActivityManager;
import org.onesocialweb.openfire.manager.FeedManager;
import org.onesocialweb.openfire.manager.WebfingerManager;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

public class IQSubscribeInterceptor implements PacketInterceptor {

	private final XMPPServer server;

	public IQSubscribeInterceptor() {
		server = XMPPServer.getInstance();
	}

	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException {

		// We only care for incoming IQ that have not yet been processed
		if (incoming && !processed && packet instanceof IQ) {

			final IQ  iq		= (IQ) packet;
			final JID fromJID = iq.getFrom();
			final JID toJID 	= iq.getTo();

			// Must be iq of type set and sent to remote users
			if (!iq.getType().equals(IQ.Type.set) || server.isLocal(toJID)) {
				return;
			}

			// With a pubsub requests
			Element requestElement = iq.getChildElement();
			if (!requestElement.getNamespace().equals(Namespace.get("http://jabber.org/protocol/pubsub"))) {
				return;
			}

			// With a subscibe or unsubscribe command
			Element commandElement = requestElement.element("subscribe");
			if (commandElement == null) {
				commandElement = requestElement.element("unsubscribe");
				if (commandElement == null) {
					return;
				}
			}
			
			// Relating to the microblogging node
			Attribute nodeAttribute = commandElement.attribute("node");
			if (nodeAttribute==null)
				return;
			if (!nodeAttribute.getValue().equals(PEPActivityHandler.NODE)) {
				//if it's not a subscription to the microblogging node, then we check if its an ostatus account, 
				// and try to subscribe to it...
				String topic=nodeAttribute.getValue();

				final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
				DomainCache cache = WebfingerManager.getInstance().findInCache(em, toJID.getDomain());

				if (cache.getProtocols().equals("ostatus")){
					IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();	
					int status;
					try {
						if (commandElement.getName().equals("subscribe")) {
							status=FeedManager.getInstance().subscribeToFeed(topic);
						}
						else {
							status=FeedManager.getInstance().unsubscribeToFeed(topic);
						}

						if (status==204){
							if (commandElement.getName().equals("subscribe")) {
								ActivityManager.getInstance().subscribe(fromJID.toBareJID(), toJID.toBareJID());
							}
							else {
								ActivityManager.getInstance().unsubscribe(fromJID.toBareJID(), toJID.toBareJID());
							}

							IQ result = IQ.createResultIQ(iq);							
							if (commandElement.getName().equals("subscribe")) {
								Element resultPubsubElement = result.setChildElement("pubsub", "http://jabber.org/protocol/pubsub");
								Element resultSubscriptionElement = resultPubsubElement.addElement("subscription", "http://jabber.org/protocol/pubsub");
								resultSubscriptionElement.addAttribute("node", topic);
								resultSubscriptionElement.addAttribute("jid", toJID.getDomain());
								resultSubscriptionElement.addAttribute("subscription", "subscribed");
							}

							iqRouter.route(result);					
							throw new PacketRejectedException();
						}
						if (status==202){
							IQ result = IQ.createResultIQ(iq);
							if (commandElement.getName().equals("subscribe")) {
								Element resultPubsubElement = result.setChildElement("pubsub", "http://jabber.org/protocol/pubsub");
								Element resultSubscriptionElement = resultPubsubElement.addElement("subscription", "http://jabber.org/protocol/pubsub");
								resultSubscriptionElement.addAttribute("node", topic);
								resultSubscriptionElement.addAttribute("jid", toJID.getDomain());
								resultSubscriptionElement.addAttribute("subscription", "not-verified");					
							}
							iqRouter.route(result);					
							throw new PacketRejectedException();
						}
						else {
							IQ result = IQ.createResultIQ(iq);
							result.setChildElement(iq.getChildElement().createCopy());
							result.setError(PacketError.Condition.internal_server_error);

							iqRouter.route(result);					
							throw new PacketRejectedException();
						}
						
					} 
					catch (Exception e){
						IQ result = IQ.createResultIQ(iq);
						result.setChildElement(iq.getChildElement().createCopy());
						result.setError(PacketError.Condition.internal_server_error);
														
						iqRouter.route(result);					
						throw new PacketRejectedException();
					}
				}
			}
			else {

				// Then we keep track of the subscribe/unsubscribe request
				if (commandElement.getName().equals("subscribe")) {
					ActivityManager.getInstance().subscribe(fromJID.toBareJID(), toJID.toBareJID());
				} else {
					ActivityManager.getInstance().unsubscribe(fromJID.toBareJID(), toJID.toBareJID());
				}
			}

		}
	}
}
