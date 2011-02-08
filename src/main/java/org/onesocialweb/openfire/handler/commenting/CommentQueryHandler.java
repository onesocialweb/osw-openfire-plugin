package org.onesocialweb.openfire.handler.commenting;

import java.util.List;

import org.dom4j.Element;
import org.dom4j.dom.DOMDocument;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.Log;
import org.onesocialweb.model.acl.AclAction;
import org.onesocialweb.model.acl.AclRule;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.openfire.handler.activity.PEPActivityHandler;
import org.onesocialweb.openfire.handler.pep.PEPCommandHandler;
import org.onesocialweb.openfire.manager.AclManager;
import org.onesocialweb.openfire.manager.RepliesManager;
import org.onesocialweb.openfire.model.activity.PersistentActivityDomReader;
import org.onesocialweb.xml.dom.ActivityDomReader;
import org.onesocialweb.xml.dom.ActivityDomWriter;
import org.onesocialweb.xml.dom.imp.DefaultActivityDomWriter;
import org.onesocialweb.xml.dom4j.ElementAdapter;
import org.onesocialweb.xml.namespace.Atom;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

public class CommentQueryHandler extends PEPCommandHandler {
	
public static final String COMMAND = "items";
		

	public CommentQueryHandler() {
		super("OneSocialWeb - Query the replies of an activity");
	}
	
	@Override
	public String getCommand() {
		return COMMAND;
	}
	
	@SuppressWarnings( { "deprecation" })
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
	
		
		// Process the request inside a try/catch so that unhandled exceptions
		// (oufofbounds etc...) can trigger a server error and we can send a
		// error result packet
		try {
			// A valid request is an IQ of type get
			if (!(packet.getType().equals(IQ.Type.get))) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.bad_request);
				return result;
			}			

			//Proccess the request here...

			// A valid submit request must contain at least one entry			
			Element pubsubElement = packet.getChildElement();
			Element itemsElement = pubsubElement.element("items");		

			// Parse the activity that we want the replies from:
			ActivityDomReader reader = new PersistentActivityDomReader();					
			String parentId= reader.readIdFromNode(new ElementAdapter(itemsElement));
			

			//The manager does the job...
			List<ActivityEntry> replies = RepliesManager.getInstance().getReplies(parentId);

			// Prepare the result packet
			ActivityDomWriter writer = new DefaultActivityDomWriter();
			DOMDocument domDocument = new DOMDocument();
			IQ result = IQ.createResultIQ(packet);
			org.dom4j.Element resultPubsubElement = result.setChildElement("pubsub", "http://jabber.org/protocol/pubsub");
			org.dom4j.Element resultItemsElement = resultPubsubElement.addElement("items");
			resultItemsElement.addAttribute("node", PEPActivityHandler.NODE);

			for (ActivityEntry reply : replies) {
				org.w3c.dom.Element entryElement = (org.w3c.dom.Element) domDocument.appendChild(domDocument.createElementNS(Atom.NAMESPACE, Atom.ENTRY_ELEMENT));
				writer.write(reply, entryElement);
				domDocument.removeChild(entryElement);
				org.dom4j.Element itemElement = resultItemsElement.addElement("item");
				itemElement.addAttribute("id", reply.getId());
				itemElement.add((org.dom4j.Element) entryElement);
			}

			// Return and send the result packet
			return result;


		} catch (Exception e) {
			Log.error(LocaleUtils.getLocalizedString("admin.error"), e);
			IQ result = IQ.createResultIQ(packet);
			result.setChildElement(packet.getChildElement().createCopy());
			result.setError(PacketError.Condition.internal_server_error);
			return result;
		}

	}
		

}
