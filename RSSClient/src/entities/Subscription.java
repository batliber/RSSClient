package entities;

import org.bson.types.ObjectId;

public class Subscription {

	private ObjectId id;
	private String title;
	private String feedURL;
	private String siteURL;
	private Long unread;
	
	private Folder folder;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFeedURL() {
		return feedURL;
	}

	public void setFeedURL(String feedURL) {
		this.feedURL = feedURL;
	}

	public String getSiteURL() {
		return siteURL;
	}

	public void setSiteURL(String siteURL) {
		this.siteURL = siteURL;
	}
	
	public Folder getFolder() {
		return folder;
	}

	public void setFolder(Folder folder) {
		this.folder = folder;
	}
	
	public Long getUnread() {
		return unread;
	}

	public void setUnread(Long unread) {
		this.unread = unread;
	}
}