package test;

import java.util.Date;

import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import scheduler.FetchJob;


public class TestFeedReader {

	public TestFeedReader() {
		FetchJob fetchJob = new FetchJob();
		try {
			fetchJob.execute(new JobExecutionContext() {
				
				@Override
				public void setResult(Object arg0) {
					
				}
				
				@Override
				public void put(Object arg0, Object arg1) {
					
				}
				
				@Override
				public boolean isRecovering() {
					return false;
				}
				
				@Override
				public Trigger getTrigger() {
					return null;
				}
				
				@Override
				public Scheduler getScheduler() {
					return null;
				}
				
				@Override
				public Date getScheduledFireTime() {
					return null;
				}
				
				@Override
				public Object getResult() {
					return null;
				}
				
				@Override
				public int getRefireCount() {
					return 0;
				}
				
				@Override
				public Date getPreviousFireTime() {
					return null;
				}
				
				@Override
				public Date getNextFireTime() {
					return null;
				}
				
				@Override
				public JobDataMap getMergedJobDataMap() {
					return null;
				}
				
				@Override
				public long getJobRunTime() {
					return 0;
				}
				
				@Override
				public Job getJobInstance() {
					return null;
				}
				
				@Override
				public JobDetail getJobDetail() {
					return null;
				}
				
				@Override
				public Date getFireTime() {
					return null;
				}
				
				@Override
				public String getFireInstanceId() {
					return null;
				}
				
				@Override
				public Calendar getCalendar() {
					return null;
				}
				
				@Override
				public Object get(Object arg0) {
					return null;
				}
			});
		} catch (JobExecutionException e1) {
			e1.printStackTrace();
		}
		
//		ItemDAO.getInstance().purgeItems();
		
//		FolderDAO.getInstance().getById(new ObjectId("514b8d5844ae6a72a111754b"));
//		Map<String, Folder> folders = FolderDAO.getInstance().listFolders();
//		
//		for (String folderName : folders.keySet()) {
//			Folder folder = folders.get(folderName);
//			for (Subscription subscription : folder.getSubscriptions()) {
//				Item item = new Item();
//				item.setGUID("Dummy");
//				item.setHTML("Dummy Updated");
//				try {
//					item.setLink(new URL("http://www.google.com"));
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//				}
//				item.setPubDate(new Date());
//				item.setSubscription(subscription);
//				item.setText("Dummy");
//				item.setTitle("Dummy");
//				
//				ItemDAO.getInstance().insert(item, subscription, folder);
				
//				FeedDAO.getInstance().consolidateItems(subscription);
				
//				ItemDAO.getInstance().purgeItems();
						
//				Collection<Item> items = ItemDAO.getInstance().listItemsByFolder(folders.get("Noticias"), false);
				
//				if (folderName.equals("Noticias")) {
//					System.out.println(subscription.getTitle());
//					
//					Collection<Item> items = ItemDAO.getInstance().listItemsBySubscription(subscription, true);
//					for (Item item : items) {
//						System.out.println(item.getSubscription().getTitle() + " - " + item.getTitle());
//					}
//				}
//				break;
//			}
//			
//			break;
//		}
		
//		FolderDAO.getInstance().updateFolderSubscriptions();
		
//		Collection<Folder> folders = Importer.getInstance().getSubscriptions();
//		
//		FolderDAO.getInstance().createFolders(folders);
		
//		for (Folder folder : folders) {
//			SubscriptionDAO.getInstance().createSubscriptions(folder, folder.getSubscriptions());
//		}
//		
//		for (String folderName : subscriptions.keySet()) {
//			for (Subscription subscription : subscriptions.get(folderName)) {
//				try {
//					Feed feed = FeedParser.parse(new URL("http://www.180.com.uy/feed.php"));
//					
//					for (int i=0; i<feed.getItemCount(); i++) {
//						FeedItem feedItem = feed.getItem(i);
//						
//						System.out.println(feedItem.getPubDate());
//						
//						FeedDAO.getInstance().insert(feedItem, subscription.getTitle(), folders.get(folderName));
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		try {
//			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
//			
//			JobDetail job = JobBuilder.newJob(FetchJob.class)
//					.storeDurably(true)
//					.withIdentity("fetchJob", "yarssc")
//					.build();
//
//			CronTrigger trigger = TriggerBuilder.newTrigger()
//				    .withIdentity("tr-" + "fetchJob", "yarssc")
//				    .withSchedule(CronScheduleBuilder.cronSchedule("0 0,15,30,45 * * * ? *"))
//				    .build();
//			
//			scheduler.scheduleJob(job, trigger);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public static void main(String[] args) {
		new TestFeedReader();
	}
}
