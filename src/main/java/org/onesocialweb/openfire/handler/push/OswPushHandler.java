package org.onesocialweb.openfire.handler.push;

import java.util.ArrayList;
import java.util.List;

import org.onesocialweb.model.activity.ActivityActor;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.model.activity.ActivityFactory;
import org.onesocialweb.model.activity.ActivityObject;
import org.onesocialweb.model.activity.ActivityVerb;
import org.onesocialweb.model.atom.AtomContent;
import org.onesocialweb.model.atom.AtomFactory;
import org.onesocialweb.openfire.manager.ActivityManager;
import org.onesocialweb.openfire.model.activity.PersistentActivityFactory;
import org.onesocialweb.openfire.model.atom.PersistentAtomFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import PubSubHubbub.PuSHhandler;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndPerson;

public class OswPushHandler extends PuSHhandler{

	@Override
	protected void processFeeds(SyndFeed feed, Document doc){		
	
		ActivityFactory activityfactory= new PersistentActivityFactory();
		AtomFactory atomFactory = new PersistentAtomFactory();
		
		 SyndPerson author=(SyndPerson)feed.getAuthors().get(0);
		
		List<SyndEntry> list=feed.getEntries();
        for (SyndEntry sentry: list){
        	
        	ActivityEntry activity=activityfactory.entry();
        	activity.setTitle(sentry.getTitle());
        	activity.setPublished(sentry.getPublishedDate());
        	activity.setUpdated(sentry.getUpdatedDate());
        	
        	ActivityObject obj=activityfactory.object(ActivityObject.STATUS_UPDATE);
        	obj.setUpdated(sentry.getUpdatedDate());
        	obj.setTitle(sentry.getTitle());
        	obj.setPublished(sentry.getPublishedDate());
        	
        	List<SyndContent> contents = sentry.getContents();
        	List<AtomContent> atomContents=new ArrayList<AtomContent>();
        	for (SyndContent c: contents) {
        		activity.addContent(atomFactory.content(c.getValue(), c.getType(), null));
        		obj.addContent(atomFactory.content(c.getValue(), c.getType(), null));
        	}        	    	
        	        	
        	List<SyndLink> links= sentry.getLinks();
        	
        	for (SyndLink l: links){        		
        		if ((l.getRel().equals("alternate")) || (l.getRel().equals("self")))
        			activity.addLink(atomFactory.link(l.getHref(), l.getRel(), l.getTitle(), l.getType()));
        	}
        	        	
        	activity.addObject(obj);
        	
        	activity.addVerb(activityfactory.verb(ActivityVerb.POST));
        	
        	ActivityActor actor= activityfactory.actor();
        	String uri=author.getUri();
        	if (uri.contains("http://")){
        		uri=author.getName()+"@"+extractDomain(uri);
        	}
        	actor.setUri(uri);
        	//here set the poco Name, it is included in the feed...
        	        	        	
        	actor.setName(getPocoDisplayName(doc));
        	actor.setEmail(author.getEmail());
        	
        	activity.setActor(actor);
        	      	
        	ActivityManager.getInstance().publishOStatusActivity(activity);
        	
        }   	
	}
	
	private String extractDomain(String uri){
		uri=uri.substring(7, uri.length());
		int index=uri.indexOf("/");
		uri=uri.substring(0, index);
		return uri;
	}
	
	private String getPocoDisplayName(Document d){
		Element root=(Element)d.getFirstChild();
        NodeList authors= root.getElementsByTagName("author");
        Element a= (Element)authors.item(0);     
        NodeList children=a.getElementsByTagName("poco:displayName");
        return children.item(0).getTextContent();
	}
	
}
