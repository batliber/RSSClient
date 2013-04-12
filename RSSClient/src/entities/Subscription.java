package entities;

import java.util.Collection;

import org.bson.types.ObjectId;

public class Subscription {

	private ObjectId id;
	private String title;
	private String feedURL;
	private String siteURL;
	private Long unread;
	
	private Collection<Folder> folders;

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
	
	public Long getUnread() {
		return unread;
	}

	public void setUnread(Long unread) {
		this.unread = unread;
	}

	public Collection<Folder> getFolders() {
		return folders;
	}

	public void setFolders(Collection<Folder> folders) {
		this.folders = folders;
	}

}