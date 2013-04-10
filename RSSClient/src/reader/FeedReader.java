package reader;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FeedReader {

	public FeedReader() {
		
	}
	
	public void readFeed(String feedURL) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			
			Document document = documentBuilder.parse(new URL(feedURL).openStream());
			
			document.normalize();
			
			Node node = document.getFirstChild();
			
			NodeList nodeList = node.getChildNodes();
			for (int i=0; i<nodeList.getLength(); i++) {
//				Node childNode = nodeList.item(i);
				
				System.out.println(node.getNodeName() + node.getNodeType() + node.getNodeValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}