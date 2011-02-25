package org.onesocialweb.openfire.handler;

import javax.persistence.EntityManager;

import org.dom4j.dom.DOMDocument;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.onesocialweb.model.vcard4.Profile;
import org.onesocialweb.openfire.OswPlugin;
import org.onesocialweb.openfire.manager.WebfingerManager;
import org.onesocialweb.openfire.model.cache.DomainCache;
import org.onesocialweb.xml.dom.VCard4DomWriter;
import org.onesocialweb.xml.dom.imp.DefaultVCard4DomWriter;
import org.onesocialweb.xml.namespace.VCard4;
import org.w3c.dom.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

public class WebfingerTask implements Runnable{
	

	private static final String IQ_QUERY_NAME = "query";
	
	private static final String IQ_PROFILEQUERY_NAMESPACE = "http://onesocialweb.org/spec/1.0/vcard4#query";
	
	String jid;
	DomainCache cache;
	Packet packet;
	
	public WebfingerTask(String jid, DomainCache domain, Packet packet){
		this.jid=jid;
		this.cache=domain;
		this.packet=packet;
	}

	@Override
	public void run() {
		
		IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
	
		Profile profile= WebfingerManager.getInstance().WebfingerLookUp(jid);
		if (profile!=null){
			//if webfinger found, rout the reply and throw away the IQ
			IQ result=prepareResultIQ(packet, profile);					
			iqRouter.route(result);					
			WebfingerManager.getInstance().addToCache(result.getID());
			//if it was not in the cache, then add it cause we now know its an ostatus account
			if (cache==null){
				final EntityManager em = OswPlugin.getEmFactory().createEntityManager();			
				em.getTransaction().begin();
				DomainCache newDomain= new DomainCache();
				newDomain.setDomain(new JID(jid).getDomain());
				newDomain.setProtocols("ostatus");
				em.persist(newDomain);
				em.getTransaction().commit();
			}			
	}
	}
	
	private IQ prepareResultIQ(Packet packet, Profile profile){
		IQ iq=(IQ)packet;
		IQ result= IQ.createResultIQ(iq);
		
		VCard4DomWriter writer = new DefaultVCard4DomWriter();
		DOMDocument domDocument = new DOMDocument();		
		
		
		Element query = (Element) domDocument.appendChild(domDocument.createElementNS(IQ_PROFILEQUERY_NAMESPACE, IQ_QUERY_NAME));
		if (profile != null) { 
			writer.toElement(profile, query);
		} else {
			query.appendChild(domDocument.createElementNS(VCard4.NAMESPACE, VCard4.VCARD_ELEMENT));
		}
		result.setChildElement((org.dom4j.Element) query);		
		
		return result;	
	}
}
