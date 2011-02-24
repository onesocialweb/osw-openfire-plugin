package org.onesocialweb.openfire.manager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.onesocialweb.model.vcard4.DefaultProfile;
import org.onesocialweb.model.vcard4.Profile;
import org.onesocialweb.model.xml.hcard.HCardReader;
import org.onesocialweb.model.xml.hcard.XMLHelper;
import org.onesocialweb.xml.dom4j.ElementAdapter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmpp.packet.JID;

public class WebfingerManager {
	
	private static WebfingerManager instance;
	private static Map<String, Object> data = new HashMap<String, Object>();
		
	private String HCARD_NS="http://microformats.org/profile/hcard";
	private String UPDATES_FROM_NS="http://schemas.google.com/g/2010#updates-from";
	
	public Profile WebfingerLookUp(String id){
		//set the hcard info and the feed url in the cache...		
		try{

			Element rootXrd=findXRD(id);		
			Profile hcardProfile= getOStatusProfile(getRelAttribute("href", HCARD_NS, rootXrd));
			String feedLink= getRelAttribute("href", UPDATES_FROM_NS, rootXrd);		
			data.put(id+"-profile", hcardProfile);
			data.put(id+"-link", feedLink);
			return hcardProfile;
		}
		catch (Exception e){
			// do some proper exception handling here please....
			return null;
		}
	}
	
	private Profile getOStatusProfile(String hcardLink) throws DocumentException, IOException{
		//get the hcard document and parse it to a Profile object?...

 		URL url = new URL(hcardLink);
		URLConnection conn = url.openConnection ();
			
		InputStream is= (InputStream)conn.getContent();;
        Node rootNode =XMLHelper.clearSoup(is);
		
        HCardReader hCardReader= new HCardReader(hcardLink);
        return hCardReader.readProfile(rootNode);
		
	}
	
	private Element findXRD(String identifier) throws  IOException, DocumentException {
		
		if ((identifier==null) || (identifier.length()==0))
				return null;
				
		
		String template =null;
		String host = new JID(identifier).getDomain();		
		String hostMetaAddress="http://"+host+"/.well-known/host-meta";
		
		URL url = new URL(hostMetaAddress);
		URLConnection conn = url.openConnection ();
			
		// Get the content , this should be the host meta file ...
		InputStream is= (InputStream)conn.getContent();

	
		SAXReader reader = new SAXReader();
        org.dom4j.Document doc = reader.read(is);
        org.w3c.dom.Element root =new ElementAdapter(doc.getRootElement());
	
        NodeList nodelist =root.getElementsByTagNameNS("http://host-meta.net/xrd/1.0", "Host");
        
		Element hostElement = (Element)nodelist.item(0);
		String foundHost = hostElement.getNodeValue();
		if (!foundHost.equals(host))
			return null;
		//Now we find the first Link element with rel attribute with a value of ‘lrdd’
		template= getRelAttribute("template", "lrdd", root);
		
		if (template==null)
			return null;
		identifier = "acct:"+identifier;
		template=replaceUri(template, identifier);
		
		url= new URL(template);
		conn=url.openConnection();
		is = (InputStream)conn.getContent();
	//	String xrdFile = convertStreamToString(is);
		
		doc = reader.read(is);
        root =new ElementAdapter(doc.getRootElement());
		
		return root;
	}

	private String replaceUri(String template, String identifier){
		// replace the value of the uri with the identifier ( http://identi.ca/main/xrd?uri={uri} )
		int index= template.indexOf("{");
		template=template.substring(0, index);
		template += identifier;
		return template;
	}



	private String getRelAttribute(String name, String relValue, Element rootXrd){
		String attr= null;
		NodeList linksList= rootXrd.getElementsByTagName("Link");
		for (int i=0; i<linksList.getLength(); i++){
			Element link=(Element)linksList.item(i);
			if ((link.hasAttribute("rel")) && (link.getAttribute("rel").equalsIgnoreCase(relValue))){
				attr = link.getAttribute(name);
				break;
			}			
		}
		return attr;
	}
	
	public static WebfingerManager getInstance() {
		if (instance == null) {
			instance = new WebfingerManager();
		}
		return instance;
	}
	
	private WebfingerManager(){
		
	}

}
