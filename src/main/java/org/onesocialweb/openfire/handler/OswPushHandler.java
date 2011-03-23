package org.onesocialweb.openfire.handler;

import java.util.ArrayList;
import java.util.List;

import org.onesocialweb.model.activity.ActivityActor;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.model.activity.ActivityFactory;
import org.onesocialweb.model.activity.ActivityObject;
import org.onesocialweb.model.activity.ActivityVerb;
import org.onesocialweb.model.activity.DefaultActivityFactory;
import org.onesocialweb.model.atom.AtomContent;
import org.onesocialweb.model.atom.AtomFactory;
import org.onesocialweb.model.atom.DefaultAtomFactory;
import org.onesocialweb.model.atom.DefaultAtomHelper;
import org.onesocialweb.openfire.manager.ActivityManager;
import org.onesocialweb.openfire.model.Subscription;

import PubSubHubbub.PuSHhandler;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndPerson;

public class OswPushHandler extends PuSHhandler{

	@Override
	protected void processFeeds(SyndFeed feed){		
	
		ActivityFactory activityfactory= new DefaultActivityFactory();
		AtomFactory atomFactory = new DefaultAtomFactory();
		
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
        	actor.setUri(author.getUri());
        	actor.setName(author.getName());
        	actor.setEmail(author.getEmail());
        	
        	activity.setActor(actor);
        	      	
        	ActivityManager.getInstance().publishOStatusActivity(activity);
        	
        }   	
	}
	
}
