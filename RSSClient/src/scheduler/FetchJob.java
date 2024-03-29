package scheduler;

import it.sauronsoftware.feed4j.FeedParser;
import it.sauronsoftware.feed4j.bean.Feed;
import it.sauronsoftware.feed4j.bean.FeedItem;

import java.net.URL;
import java.util.GregorianCalendar;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import dao.ItemDAO;
import dao.SubscriptionDAO;
import entities.Item;
import entities.Subscription;

public class FetchJob implements Job {

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		for (Subscription subscription : SubscriptionDAO.getInstance().listSubscriptions()) {
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
					
					ItemDAO.getInstance().insert(item, subscription, null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}