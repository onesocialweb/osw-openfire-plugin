package org.onesocialweb.openfire.handler.cache;

import java.util.List;

import javax.persistence.EntityManager;

import org.dom4j.dom.DOMDocument;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.Log;
import org.onesocialweb.model.cache.DomainCache;
import org.onesocialweb.openfire.OswPlugin;
import org.onesocialweb.openfire.manager.WebfingerManager;
import org.onesocialweb.openfire.model.cache.PersistentDomainCache;
import org.onesocialweb.xml.dom.CacheDomWriter;
import org.onesocialweb.xml.namespace.Atom;
import org.w3c.dom.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

public class IQCacheQueryHandler extends IQHandler {

	public static final String NAME = "query";
	
	public static final String NAMESPACE = "http://onesocialweb.org/extensions/1.0/ostatus#cache";
											
	
	private final IQHandlerInfo info = new IQHandlerInfo(NAME, NAMESPACE);
	
	private UserManager userManager;
	
	public IQCacheQueryHandler(){
		super("OneSocialWeb - Query the domains cache");
	}
	
	
	@Override
	public IQHandlerInfo getInfo() {
		return info;
	}
	

	
	@SuppressWarnings( { "deprecation" })
	@Override
	public IQ handleIQ(IQ packet) {
		
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

		
							
			final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
			List<DomainCache> cache= WebfingerManager.getInstance().getCache(em);

			// Prepare the result packet
			CacheDomWriter writer = new CacheDomWriter();
			DOMDocument domDocument = new DOMDocument();
			IQ result = IQ.createResultIQ(packet);
			org.dom4j.Element query = (org.dom4j.Element) result.setChildElement(NAME, NAMESPACE);
			for (DomainCache c: cache){
				Element domainElement = (Element)domDocument.appendChild(domDocument.createElementNS(NAMESPACE, "domain"));
				writer.toElement(c, domainElement);
				domDocument.removeChild(domainElement);
				query.add((org.dom4j.Element)domainElement);
			}
			result.setChildElement((org.dom4j.Element) query);
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
