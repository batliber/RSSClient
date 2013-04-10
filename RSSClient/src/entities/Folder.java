package entities;

import java.util.Collection;

import org.bson.types.ObjectId;

public class Folder {

	private ObjectId id;
	private String name;
	private Long unread;
	
	private Collection<Subscription> subscriptions;
	
	public Folder() {
		
	}
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Collection<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}
	
	public Long getUnread() {
		return unread;
	}

	public void setUnread(Long unread) {
		this.unread = unread;
	}
}