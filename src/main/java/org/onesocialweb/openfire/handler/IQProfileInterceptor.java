package org.onesocialweb.openfire.handler;

import javax.persistence.EntityManager;

import org.dom4j.dom.DOMDocument;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.onesocialweb.model.cache.DomainCache;
import org.onesocialweb.model.vcard4.Profile;
import org.onesocialweb.openfire.OswPlugin;
import org.onesocialweb.openfire.manager.WebfingerManager;
import org.onesocialweb.xml.dom.VCard4DomWriter;
import org.onesocialweb.xml.dom.imp.DefaultVCard4DomWriter;
import org.onesocialweb.xml.dom4j.ElementAdapter;
import org.onesocialweb.xml.namespace.VCard4;
import org.w3c.dom.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

public class IQProfileInterceptor implements PacketInterceptor {
	
	public static final String IQ_QUERY_NAME = "query";
	
	public static final String IQ_PROFILEQUERY_NAMESPACE = "http://onesocialweb.org/spec/1.0/vcard4#query";

	@SuppressWarnings( { "deprecation", "unchecked" })
	public void interceptPacket(Packet packet, Session session,	boolean incoming, boolean processed) throws PacketRejectedException {
		//the profile that was being searched...
		
		
		if (packet instanceof IQ){
		
			IQ iq=(IQ)packet;
			JID jid=iq.getTo();
			IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
			
			if (incoming && !processed){
				
				if (iq.getType()!=IQ.Type.get)
					return;				
				Element queryElement= new ElementAdapter(iq.getChildElement());
				if (queryElement==null)
					return;			
				String nodeName=queryElement.getNodeName();
				String namespace=queryElement.getNamespaceURI();
				if ((!nodeName.equals(IQ_QUERY_NAME)) || (!namespace.equals(IQ_PROFILEQUERY_NAMESPACE)))
					return;
				
				if ((jid==null) || (XMPPServer.getInstance().getServerInfo().getXMPPDomain().equals((jid.getDomain()))))
					return;
								
				//check if the domain is in the local cache...
				final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
				DomainCache cache = WebfingerManager.getInstance().findInCache(em, jid.getDomain());
				if ((cache==null) || cache.getProtocols().equals("both")){
					//we route the packet to try to find the xmpp account							
					//but perform webfinger locally in the meantime in a different thread
					Thread webfinger=new Thread(new WebfingerTask(jid.toBareJID(), cache, packet), jid.toBareJID());
					webfinger.start();																						
					return;				

				}
				else if (cache.getProtocols().equals("ostatus")){

					Profile profile= WebfingerManager.getInstance().WebfingerLookUp(jid.toBareJID());
					if (profile!=null){
						//change the iq...
						IQ result=prepareResultIQ(packet, profile);
						iqRouter = XMPPServer.getInstance().getIQRouter();
						iqRouter.route(result);					
						throw new PacketRejectedException();									
					}
					else return;
				}
				em.close();
			}

			// We only care for IQs which were already processed
			if (!incoming && !processed) {


				//of type error
				if	(iq.getType()!=IQ.Type.error)
					return;
				PacketError error= iq.getError();		
				if (error==null)
					return;

				//the error must be a server not found code=404	or not allowed 405
				PacketError.Type errorType=error.getType();
				String code= error.getElement().attribute("code").getText();
				if (errorType!=PacketError.Type.cancel)
					return;
				if  ( (!code.equals("404")) && (!code.equals("405")))
					return;
				//must be a profile query
				Element queryElement= new ElementAdapter(iq.getChildElement());
				if (queryElement==null)
					return;			
				String nodeName=queryElement.getNodeName();
				String namespace=queryElement.getNamespaceURI();
				if ((!nodeName.equals(IQ_QUERY_NAME)) || (!namespace.equals(IQ_PROFILEQUERY_NAMESPACE)))
					return;				
				if (WebfingerManager.getInstance().iqIsAnswered(iq.getID()))
					throw new PacketRejectedException();
				else
					return;

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
