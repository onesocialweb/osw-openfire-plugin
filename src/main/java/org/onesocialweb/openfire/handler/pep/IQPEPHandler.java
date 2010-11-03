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
package org.onesocialweb.openfire.handler.pep;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.onesocialweb.openfire.handler.commenting.PEPCommentingHandler;
import org.xmpp.packet.IQ;

public class IQPEPHandler extends IQHandler {
	
	private XMPPServer server;
	
	private Map<String, PEPNodeHandler> handlers = new ConcurrentHashMap<String, PEPNodeHandler>();
	
	public IQPEPHandler() {
		super("Intercept PEP request and divert to another handler based on node URI");
	}
	
	@Override
	public void initialize(XMPPServer server) {
		super.initialize(server);
		this.server = server;
	}

	@Override
	public IQHandlerInfo getInfo() {
		return new IQHandlerInfo("pubsub", "http://jabber.org/protocol/pubsub");
	}

	@SuppressWarnings("unchecked")
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
        final Element childElement = packet.getChildElement();
        final List<Element> pubsubElements = childElement.elements();	

        if (pubsubElements != null && pubsubElements.size() > 0) {
        	Element actionElement = pubsubElements.get(0);
        	Attribute node = actionElement.attribute("node");
        	PEPNodeHandler handler = getHandler(node.getValue());
        	if (handler != null) {
        		return handler.handleIQ(packet);
        	}
        }
        
       	return XMPPServer.getInstance().getIQPEPHandler().handleIQ(packet);
	}
	
	public void addHandler(PEPNodeHandler handler) {
		handler.initialize(server);
		handlers.put(handler.getNode(), handler);
	}

	public PEPNodeHandler getHandler(String node) {
		if  ((handlers.get(node) ==null) && (node.contains("replies:item=")))
			addHandler(new PEPCommentingHandler(node));		
		return handlers.get(node);
	}

}
