package org.onesocialweb.openfire.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.dom.DOMDocument;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.JiveGlobals;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.model.atom.DefaultAtomHelper;
import org.onesocialweb.openfire.handler.OswPushHandler;
import org.onesocialweb.xml.dom.ActivityDomReader;
import org.onesocialweb.xml.dom.ActivityDomWriter;
import org.onesocialweb.xml.dom.imp.DefaultActivityDomReader;
import org.onesocialweb.xml.dom.imp.DefaultActivityDomWriter;
import org.onesocialweb.xml.dom4j.ElementAdapter;
import org.onesocialweb.xml.namespace.Atom;
import org.onesocialweb.xml.namespace.Onesocialweb;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import PubSubHubbub.Discovery;
import PubSubHubbub.Subscriber;
import PubSubHubbub.Web;
import Published.Publisher;

public class FeedManager {
	
	
	private static FeedManager instance;
	private static Web webserver;
	
	public static FeedManager getInstance() {
		if (instance == null) {
			// Carefull, we ae in a threaded environment !
			synchronized (FeedManager.class) {
				instance = new FeedManager();				
				int webserverPort = 8080;
				webserver = new Web(webserverPort, new OswPushHandler());

			}
		}
		return instance;
	}

	public void addToFeed(ActivityEntry entry, String username){
		String pathToFeeds=JiveGlobals.getProperty("onesocialweb.push.feeds");
		// Open the file
		if (!feedExists(pathToFeeds, username)){
			try {
				writeFeed(pathToFeeds, username);
			} 
			//swallowing exceptions suck! fix this!
			catch (FileNotFoundException e) {} 
			catch (UnsupportedEncodingException e){}
			catch (IOException e){}
			catch (UserNotFoundException e){}
		}

		//im doing this so that the feed keeps on growing in spite of the activities only retrieving 20 items...
		else  {
			String fileLoc=pathToFeeds+ "/"+username+ ".atom";		
			String feedAddress="http://"+XMPPServer.getInstance().getServerInfo().getXMPPDomain()+"/updates/"+username+".atom";

			try{
				SAXReader reader = new SAXReader();
				DOMDocument domDocument= new DOMDocument();
		        Document document = reader.read(fileLoc);
		        Element rootElement =new ElementAdapter(document.getRootElement());
		        if (rootElement==null)
		        	return;
				if (rootElement.getNodeName().equals(Atom.FEED_ELEMENT)){
				
					List<ActivityEntry> oldEntries= new ArrayList<ActivityEntry>();
					ActivityDomReader domReader= new DefaultActivityDomReader();
					NodeList entries=rootElement.getElementsByTagName(Atom.ENTRY_ELEMENT);
					for (int i=0;i<entries.getLength(); i++){
						ActivityEntry activity = domReader.readEntry((Element)entries.item(i));
						oldEntries.add(activity);
					}
			
					//prepare the feed again
					Element feedElement = (Element) domDocument.createElementNS(Atom.NAMESPACE, Atom.FEED_ELEMENT);							
					domDocument.appendChild(feedElement);
					addFeedStuff(feedElement, getHub(), feedAddress, username+"@"+XMPPServer.getInstance().getServerInfo().getXMPPDomain());
					
					
					ActivityDomWriter writer = new DefaultActivityDomWriter();
					//append the new entry
					Element entryElement = (Element) feedElement.appendChild(feedElement.getOwnerDocument().createElementNS(Atom.NAMESPACE, Atom.ENTRY_ELEMENT));
					writer.write(entry, entryElement);
					
					for (ActivityEntry oe: oldEntries){
						Element oldEntryElement = (Element) feedElement.appendChild(feedElement.getOwnerDocument().createElementNS(Atom.NAMESPACE, Atom.ENTRY_ELEMENT));
						writer.write(oe, oldEntryElement);
					}
					
				
				}
				//write to disc ..
				writeToDisc(fileLoc, domDocument);
				
				//pubsusbhubbub ping
				boolean published=publisherPing(getHub(), feedAddress);

			} catch (DocumentException e){

			}
			catch (FileNotFoundException e){

			}
			catch (UnsupportedEncodingException e){

			}
			catch (IOException e){

			}
			
			
		}
			
		
	}
	
	public boolean feedExists(String path, String username){
		//find the xrd file
		String fileLoc=path+ "/"+username+ ".atom";
		File f = new File(fileLoc);
		if (f.exists()) return true;
		else return false;
	}
	
	public void writeFeed(String pathToWrite, String username) throws FileNotFoundException, UnsupportedEncodingException, IOException, UserNotFoundException{
		
		String hub=getHub();
		
		//request the inbox -- last 20 items
		String jid=username +"@" +XMPPServer.getInstance().getServerInfo().getXMPPDomain();
		List<ActivityEntry> activities = ActivityManager.getInstance().getActivities(jid, jid);
		
		
		org.dom4j.Document domDocument = new DOMDocument();
		Element feedElement = (Element) ((DOMDocument)domDocument).createElementNS(Atom.NAMESPACE, Atom.FEED_ELEMENT);		
		((DOMDocument)domDocument).appendChild(feedElement);
		
		String feedAddress="http://"+XMPPServer.getInstance().getServerInfo().getXMPPDomain()+"/updates/"+username+".atom";
		addFeedStuff(feedElement, hub, feedAddress, jid);
		
		
		ActivityDomWriter writer = new DefaultActivityDomWriter();
		
		for (ActivityEntry activity : activities) {
			//remove the ACL rules ...
			
			//write the activities in the feed
			Element entryElement = (Element) feedElement.appendChild(feedElement.getOwnerDocument().createElementNS(Atom.NAMESPACE, Atom.ENTRY_ELEMENT));
			writer.write(activity, entryElement);			
			NodeList rules= entryElement.getElementsByTagName(Onesocialweb.ACL_RULE_ELEMENT);
			for (int i=0; i<rules.getLength(); i++){
				Node node=rules.item(i);
				entryElement.removeChild(node);
			}
			feedElement.appendChild(entryElement);
		}
		//write to disc
		String fileName=pathToWrite+"/" +username+".atom";
		writeToDisc(fileName, domDocument);
	}
	
	private void writeToDisc(String fileName, org.dom4j.Document domDocument) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(fos, format);
		writer.write(domDocument);		
		writer.flush();
	}
	
	//the hub where the OSW account publishes updates..If declared as a property in the console we read and use it, otherwise we take the google one...
	private String getHub(){
		String hub = JiveGlobals.getProperty("onesocialweb.push.hub");
		if (hub== null){
			hub="http://pubsubhubbub.appspot.com/";
		}
		return hub;
	}
	
	private boolean publisherPing(String hub, String feed){
		Publisher publisher = new Publisher();		

		try{
			int status = publisher.execute(hub,	feed);

			if (status == 200)
				return true;
			else 
				return false;
		} catch (Exception e){
			
			return false;
		}
	}
	
	public int subscribeToFeed(String topic) throws Exception {
		
			
			   Discovery disc= new Discovery();
			   String hub= disc.getHub(topic);
			   Subscriber sbcbr = new Subscriber(webserver);				
				
			   String hostname = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
			   //String hostname = "vodafonernd.com";
				//the callback endpoint is actually running on 8080, but since that port is not open 
				//we set apache to redirect to it, and specify here just port 80
				hostname = "http://" + hostname;
			   int statusCode = sbcbr.subscribe(hub, topic, hostname, null, null);
			   
			   return statusCode;
		
	}
	
	private void addFeedStuff(Element feedElement, String hub, String feedAddress, String jid){
		//add feed stuff, hub and the rest...
		
		Element hubElement=feedElement.getOwnerDocument().createElementNS(Atom.NAMESPACE, Atom.LINK_ELEMENT);
		hubElement.setAttribute(Atom.REL_ATTRIBUTE, "hub");
		hubElement.setAttribute(Atom.HREF_ATTRIBUTE, hub);
		feedElement.appendChild(hubElement);
		
		Element selfLink=feedElement.getOwnerDocument().createElementNS(Atom.NAMESPACE, Atom.LINK_ELEMENT);		
		selfLink.setAttribute(Atom.REL_ATTRIBUTE, "self");
		selfLink.setAttribute(Atom.HREF_ATTRIBUTE, feedAddress);
		selfLink.setAttribute(Atom.TYPE_ATTRIBUTE, "application/atom+xml");
		feedElement.appendChild(selfLink);
		
		Element title=feedElement.getOwnerDocument().createElementNS(Atom.NAMESPACE, Atom.TITLE_ELEMENT);
		title.appendChild(feedElement.getOwnerDocument().createTextNode("OSW Public Feed for: "+jid));
		feedElement.appendChild(title);
		
		Element updated=feedElement.getOwnerDocument().createElementNS(Atom.NAMESPACE, Atom.UPDATED_ELEMENT);
		updated.appendChild(feedElement.getOwnerDocument().createTextNode(DefaultAtomHelper.format(Calendar.getInstance().getTime())));
		feedElement.appendChild(updated);
		
		Element id=feedElement.getOwnerDocument().createElementNS(Atom.NAMESPACE, Atom.ID_ELEMENT);
		id.appendChild(feedElement.getOwnerDocument().createTextNode(feedAddress));
		feedElement.appendChild(id);
		
	}
	

}
