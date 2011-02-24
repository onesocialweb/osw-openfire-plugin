package org.onesocialweb.openfire.handler;

import org.dom4j.dom.DOMDocument;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.onesocialweb.model.vcard4.Profile;
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

public class IQErrorInterceptor implements PacketInterceptor {
	
	public static final String IQ_QUERY_NAME = "query";
	
	public static final String IQ_PROFILEQUERY_NAMESPACE = "http://onesocialweb.org/spec/1.0/vcard4#query";

	@SuppressWarnings( { "deprecation", "unchecked" })
	public void interceptPacket(Packet packet, Session session,	boolean incoming, boolean processed) throws PacketRejectedException {
		
		// We only care for IQs which were already processed
		if (!incoming && !processed && packet instanceof IQ) {
			
			IQ iq=(IQ)packet;
			//of type error
			if	(iq.getType()!=IQ.Type.error)
			  return;
			PacketError error= iq.getError();		
			if (error==null)
			return;
			
			//the error must be a server not found code=404			
			PacketError.Type errorType=error.getType();
			String code= error.getElement().attribute("code").getText();
			if ((errorType!=PacketError.Type.cancel) || (!code.equals("404"))) 
				return;
			//must be a profile query
			Element queryElement= new ElementAdapter(iq.getChildElement());
			if (queryElement==null)
				return;			
			String nodeName=queryElement.getNodeName();
			String namespace=queryElement.getNamespaceURI();
			if ((!nodeName.equals(IQ_QUERY_NAME)) || (!namespace.equals(IQ_PROFILEQUERY_NAMESPACE)))
				return;
			
			//the profile that was being searched...
			JID jid=iq.getFrom();
			Profile profile= WebfingerManager.getInstance().WebfingerLookUp(jid.toBareJID());
			if (profile!=null){
				//change the iq...
				iq=prepareIQ(packet, profile);
				packet=iq.createCopy();
				
			}
			
		}
	}
	
	private IQ prepareIQ(Packet packet, Profile profile){
		VCard4DomWriter writer = new DefaultVCard4DomWriter();
		DOMDocument domDocument = new DOMDocument();
		
		IQ result = new IQ(IQ.Type.result, packet.getID());
		result.setFrom(packet.getFrom());
		result.setTo(packet.getTo());
		
		
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
