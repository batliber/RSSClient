package scheduler;

import it.sauronsoftware.feed4j.FeedIOException;
import it.sauronsoftware.feed4j.FeedParser;
import it.sauronsoftware.feed4j.FeedXMLParseException;
import it.sauronsoftware.feed4j.UnsupportedFeedException;
import it.sauronsoftware.feed4j.bean.Feed;
import it.sauronsoftware.feed4j.bean.FeedItem;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import dao.FolderDAO;
import dao.ItemDAO;
import entities.Folder;
import entities.Item;
import entities.Subscription;

public class FetchJob implements Job {

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Map<String, Folder> folders = FolderDAO.getInstance().listFolders();
		
		for (String folderName : folders.keySet()) {
			Folder folder = folders.get(folderName);
			
			for (Subscription subscription : folder.getSubscriptions()) {
				try {	
					Feed feed = FeedParser.parse(new URL(subscription.getFeedURL()));
					
					for (int i=0; i<feed.getItemCount(); i++) {
						FeedItem feedItem = feed.getItem(i);
						
						Item item = new Item();
						item.setGUID(feedItem.getGUID());
						item.setHTML(feedItem.getDescriptionAsHTML());
						item.setLink(feedItem.getLink());
						item.setFetchDate(GregorianCalendar.getInstance().getTime());
						item.setPubDate(feedItem.getPubDate());
						item.setSubscription(subscription);
						item.setText(feedItem.getDescriptionAsText());
						item.setTitle(feedItem.getTitle());
						
						ItemDAO.getInstance().insert(item, subscription, folder);
					}
				} catch (FeedIOException e) {
					e.printStackTrace();
				} catch (FeedXMLParseException e) {
					e.printStackTrace();
				} catch (UnsupportedFeedException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}