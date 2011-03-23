package org.onesocialweb.openfire.manager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.onesocialweb.model.vcard4.DefaultVCard4Factory;
import org.onesocialweb.model.vcard4.Profile;
import org.onesocialweb.model.vcard4.VCard4Factory;
import org.onesocialweb.model.vcard4.XFeedField;
import org.onesocialweb.model.xml.hcard.HCardReader;
import org.onesocialweb.model.xml.hcard.XMLHelper;
import org.onesocialweb.openfire.model.cache.DomainCache;
import org.onesocialweb.xml.dom4j.ElementAdapter;
import org.onesocialweb.xml.namespace.OStatus;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmpp.packet.JID;


public class WebfingerManager {
	
	private static WebfingerManager instance;
	private static Map<String, Object> data = new HashMap<String, Object>();
	private static List<String> answeredIQsCache= new ArrayList<String>();
		
	
	public Profile WebfingerLookUp(String id){
		//set the hcard info and the feed url in the cache...		
		try{

			Element rootXrd=findXRD(id);		
			if (rootXrd==null)
				return null;
			Profile hcardProfile= getOStatusProfile(getRelAttribute("href", OStatus.HCARD_NAMESPACE, rootXrd));
			String feedLink= getRelAttribute("href", OStatus.ATOM_UPDATES, rootXrd);		
			data.put(id+"-profile", hcardProfile);
			data.put(id+"-link", feedLink);
			
			//set the feed url for the OStatus profile...
			VCard4Factory factory = new DefaultVCard4Factory();
	        XFeedField feedField=factory.feed();
	        feedField.setFeed(feedLink);
	        hcardProfile.addField(feedField);
	        
			return hcardProfile;
		}
		catch (Exception e){
			// do some proper exception handling here please....
			return null;
		}
	}
	
	private Profile getOStatusProfile(String hcardLink) throws DocumentException, IOException{
		//get the hcard document and parse it to a Profile object?...
		
		if (hcardLink.contains("google.com")){
			hcardLink=hcardLink.replaceFirst("http", "https");		
		}
 		URL url = new URL(hcardLink);
 		InputStream is=null;
 		if (hcardLink.contains("https")){
 			installTrustingStore();
 			HttpsURLConnection https=(HttpsURLConnection)url.openConnection ();
 			is= (InputStream)https.getContent();
 		} else {
 			URLConnection conn = url.openConnection ();
 			is= (InputStream)conn.getContent();
 		}
	
		
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

		if (is==null)
			return null;
	
		SAXReader reader = new SAXReader();
        org.dom4j.Document doc = reader.read(is);
        if (doc==null)
        	return null;
        org.w3c.dom.Element root =new ElementAdapter(doc.getRootElement());
        if (root==null)
        	return null;
        
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
	
	public void addToCache(String iqId){
		answeredIQsCache.add(iqId);
	}
	
	public boolean iqIsAnswered(String iqId){
		if (answeredIQsCache.contains(iqId))
			return true;
		else 
			return false;
	}
	
	private void installTrustingStore(){
		
		TrustManager[] trustAllCerts = new TrustManager[]{
			    new X509TrustManager() {
			        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			            return null;
			        }
			        public void checkClientTrusted(
			            java.security.cert.X509Certificate[] certs, String authType) {
			        }
			        public void checkServerTrusted(
			            java.security.cert.X509Certificate[] certs, String authType) {
			        }
			    }
			};

			// Install the all-trusting trust manager
			try {
			    SSLContext sc = SSLContext.getInstance("SSL");
			    sc.init(null, trustAllCerts, new java.security.SecureRandom());
			    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			} catch (Exception e) {
			
			}
	}

	public DomainCache findInCache(EntityManager em, String domain){		
		
		Query query = em.createQuery("SELECT x FROM DomainCache x WHERE x.domain = ?1");
		query.setParameter(1, domain);		
		List<DomainCache> entries = query.getResultList();
		if ((entries!=null) && (entries.size()>0))
			return entries.get(0);

		return null;
	}
}
