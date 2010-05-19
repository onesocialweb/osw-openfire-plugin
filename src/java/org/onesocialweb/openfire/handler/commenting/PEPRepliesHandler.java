package org.onesocialweb.openfire.handler.commenting;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.onesocialweb.openfire.handler.pep.PEPCommandHandler;
import org.onesocialweb.openfire.handler.pep.PEPNodeHandler;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

public class PEPRepliesHandler extends PEPNodeHandler {

	public static String NODE = "http://onesocialweb.org/spec/1.0/replies";
	
	private XMPPServer server;
	

	
	private Map<String, PEPCommandHandler> handlers = new ConcurrentHashMap<String, PEPCommandHandler>();

	public PEPRepliesHandler() {
		super("Handler for Onesocialweb replies PEP node");
	}
	
	@Override
	public String getNode() {
		return NODE;
	}

	@Override
	public void initialize(XMPPServer server) {
		super.initialize(server);
		this.server = server;
		addHandler(new RepliesQueryHandler());
	}

	@SuppressWarnings("unchecked")
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		// We search for a handler based on the element name
		// and process the packet with the handler if found.
        final Element childElement = packet.getChildElement();
        final List<Element> pubsubElements = childElement.elements();	

        if (pubsubElements != null && pubsubElements.size() > 0) {
        	Element actionElement = pubsubElements.get(0);
        	PEPCommandHandler handler = getHandler(actionElement.getName());
			if (handler != null) {
				return handler.handleIQ(packet);
			}
		}

		// No valid hanlder found. Return a feature not implemented
		// error.
		IQ result = IQ.createResultIQ(packet);
		result.setChildElement(packet.getChildElement().createCopy());
		result.setError(PacketError.Condition.feature_not_implemented);
		return result;
	}

	public void addHandler(PEPCommandHandler handler) {
		handler.initialize(server);
		handlers.put(handler.getCommand(), handler);
	}

	public PEPCommandHandler getHandler(String name) {
		return handlers.get(name);
	}
	


}
