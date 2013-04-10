package importer;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import entities.Folder;
import entities.Subscription;

public class Importer {

	private static Importer instance = null;
	
	private Importer() {
		
	}
	
	public static Importer getInstance() {
		if (instance == null) {
			instance = new Importer();
		}
		return instance;
	}
	
	public Collection<Folder> getSubscriptions() {
		Collection<Folder> result = new LinkedList<Folder>();
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(new File("/home/avatar/Escritorio/desarrollo/java/workspace/RSSClient/assets/googleTakeout/batliber@gmail.com-takeout/Google Reader/subscriptions.xml"));
			
			document.normalize();
			
			Node node = document.getElementsByTagName("body").item(0);
			
			Folder folder = new Folder();
			folder.setName("Root");
			
			Collection<Subscription> subscriptions = new LinkedList<Subscription>();
			
			NodeList nodeList = node.getChildNodes();
			
			for (int i=0; i<nodeList.getLength(); i++) {
				Node nodeSubscription = nodeList.item(i);
				
				if (nodeSubscription.getNodeType() == Node.ELEMENT_NODE) {
					NamedNodeMap namedNodeMap = nodeSubscription.getAttributes();
					
					if (namedNodeMap.getNamedItem("xmlUrl") != null) {
						Subscription subscription = new Subscription();
						subscription.setFeedURL(namedNodeMap.getNamedItem("xmlUrl").getNodeValue());
						subscription.setSiteURL(namedNodeMap.getNamedItem("htmlUrl").getNodeValue());
						subscription.setTitle(namedNodeMap.getNamedItem("title").getNodeValue());
						subscription.setFolder(folder);
						
						subscriptions.add(subscription);
					} else {
						folder.setSubscriptions(subscriptions);
						
						result.add(folder);
						
						break;
					}
				}
			}
			
			for (int i=0; i<nodeList.getLength(); i++) {
				Node nodeSubscription = nodeList.item(i);
				
				if (nodeSubscription.getNodeType() == Node.ELEMENT_NODE) {
					NamedNodeMap namedNodeMap = nodeSubscription.getAttributes();
					
					if (namedNodeMap.getNamedItem("xmlUrl") == null) {
						folder = new Folder();
						folder.setName(namedNodeMap.getNamedItem("title").getNodeValue());
						
						subscriptions = new LinkedList<Subscription>();
						
						NodeList nodeListSubNode = nodeSubscription.getChildNodes();
						for (int j=0; j<nodeListSubNode.getLength(); j++) {
							Node nodeSubNode = nodeListSubNode.item(j);
							
							if (nodeSubNode.getNodeType() == Node.ELEMENT_NODE) {
								NamedNodeMap namedNodeMapSubNode = nodeSubNode.getAttributes();
								
								Subscription subscription = new Subscription();
								subscription.setFeedURL(namedNodeMapSubNode.getNamedItem("xmlUrl").getNodeValue());
								subscription.setSiteURL(namedNodeMapSubNode.getNamedItem("htmlUrl").getNodeValue());
								subscription.setTitle(namedNodeMapSubNode.getNamedItem("title").getNodeValue());
								subscription.setFolder(folder);
								
								subscriptions.add(subscription);
							}
						}
						
						folder.setSubscriptions(subscriptions);
						
						result.add(folder);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
}