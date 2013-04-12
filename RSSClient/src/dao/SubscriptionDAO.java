package dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.WriteResult;

import entities.Folder;
import entities.Subscription;

public class SubscriptionDAO extends AbstractDAO {

	private static SubscriptionDAO instance = null;
	
	private SubscriptionDAO() {
		this.collectionName = "Subscriptions";
	}
	
	public static SubscriptionDAO getInstance() {
		if (instance == null) {
			instance = new SubscriptionDAO();
		}
		return instance;
	}
	
	public Collection<Subscription> listSubscriptions() {
		Collection<Subscription> result = new LinkedList<Subscription>();
		
		try {
			this.connect();
			
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			DBCursor dbCursor = this.dbCollection.find();
			
			if (dbCursor != null) {
				while (dbCursor.hasNext()) {
					DBObject dbObject = dbCursor.next();
					
					Subscription subscription = this.pojoFromBSON(dbObject, false);
					
					result.add(subscription);
				}
			}
			
			this.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public Subscription getById(ObjectId id) {
		Subscription result = null;
		
		try {
			this.connect();
			
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			DBObject dbObject = new BasicDBObject();
			dbObject.put("_id", id);
			
			DBCursor dbCursor = this.dbCollection.find(dbObject);
			
			if (dbCursor != null && dbCursor.hasNext()) {
				dbObject = dbCursor.next();
				
				result = this.pojoFromBSON(dbObject, true);
			}
			
			this.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public Subscription pojoFromBSON(DBObject dbObject, boolean shallow) {
		Subscription result = new Subscription();
		
		result.setFeedURL(dbObject.get("feedURL").toString());
		result.setId(new ObjectId(dbObject.get("_id").toString()));
		result.setSiteURL(dbObject.get("siteURL").toString());
		result.setTitle(dbObject.get("title").toString());
		
//		result.setUnread(ItemDAO.getInstance().countUnreadBySubscription(result));
		result.setUnread(new Long(0));
		
		if (!shallow) {
			Collection<Folder> folders = new LinkedList<Folder>();
			
			BasicDBList basicDBList = (BasicDBList) dbObject.get("folders");
			if (basicDBList != null) {
				for (Iterator<Object> iterator = basicDBList.iterator(); iterator.hasNext();) {
					DBRef dbRef = (DBRef) iterator.next();
					DBObject dbObjectFolder = dbRef.fetch();
					
					Folder folder = FolderDAO.getInstance().pojoFromBSON(dbObjectFolder, true);
					
					folders.add(folder);
				}
			}
			
			result.setFolders(folders);
		}
		
		return result;
	}
	
	public DBObject bsonFromPOJO(Subscription subscription) {
		DBObject result = new BasicDBObject();
		
		result.put("feedURL", subscription.getFeedURL());
		if (subscription.getId() != null) {
			result.put("_id", subscription.getId());
		}
		result.put("siteURL", subscription.getSiteURL());
		result.put("title", subscription.getTitle());
		
		Collection<DBRef> subscriptionFoldersRefs = new LinkedList<DBRef>();
		for (Folder folder : subscription.getFolders()) {
			DBRef dbRefFolder = new DBRef(this.db, FolderDAO.getInstance().getCollectionName(), folder.getId());
			
			subscriptionFoldersRefs.add(dbRefFolder);
		}
		result.put("folders", subscriptionFoldersRefs);
		
		return result;
	}

	public void createSubscriptions(Folder folder, Collection<Subscription> subscriptions) {
		try {
			this.connect();
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			for (Subscription subscription : subscriptions) {
				BasicDBObject basicDBObject = new BasicDBObject();
				
				basicDBObject.put("feedURL", subscription.getFeedURL());
				basicDBObject.put("siteURL", subscription.getSiteURL());
				basicDBObject.put("title", subscription.getTitle());
				
				DBRef dbRef = new DBRef(this.db, FolderDAO.getInstance().getCollectionName(), folder.getId());
				
				basicDBObject.put("folder", dbRef);
				
				this.db.requestStart();
				WriteResult result = this.dbCollection.insert(basicDBObject);
				result.getLastError().throwOnError();
				this.db.requestDone();
			}
			
			this.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateSubscriptionsFolders() {
		try {
			this.connect();
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			DBCursor dbCursor = this.dbCollection.find();
			
			Map<String, Collection<Folder>> folders = new HashMap<String, Collection<Folder>>();
			Map<String, Subscription> subscriptions = new HashMap<String, Subscription>();
			
			if (dbCursor != null) {
				while (dbCursor.hasNext()) {
					DBObject dbObject = dbCursor.next();
					
					Subscription subscription = new Subscription();
					
					subscription.setFeedURL(dbObject.get("feedURL").toString());
					subscription.setId(new ObjectId(dbObject.get("_id").toString()));
					subscription.setSiteURL(dbObject.get("siteURL").toString());
					subscription.setTitle(dbObject.get("title").toString());
					
					Folder folder = new Folder();
					
					DBObject dbObjectFolder = (DBObject) ((DBRef) dbObject.get("folder")).fetch();
					
					folder.setId((ObjectId) dbObjectFolder.get("_id"));
					
//					subscription.setFolder(folder);
//					
//					subscriptions.put(subscription.getTitle(), subscription);
//					
//					if (folders.containsKey(subscription.getTitle())) {
//						if (!folders.get(subscription.getTitle()).contains(subscription.getFolder())) {
//							folders.get(subscription.getTitle()).add(subscription.getFolder());
//						}
//					} else {
//						Collection<Folder> subscriptionFolders = new LinkedList<Folder>();
//						
//						subscriptionFolders.add(subscription.getFolder());
//						
//						folders.put(subscription.getTitle(), subscriptionFolders);
//					}
				}
				dbCursor.close();
			}
			
//			this.db.requestStart();
//			WriteResult result = this.dbCollection.remove(new BasicDBObject());
//			result.getLastError().throwOnError();
//			this.db.requestDone();
			
			for (Subscription subscription : subscriptions.values()) {
				subscription.setFolders(folders.get(subscription.getTitle()));
				
				DBObject dbObjectSubscription = new BasicDBObject();
				dbObjectSubscription.put("feedURL", subscription.getFeedURL());
				dbObjectSubscription.put("siteURL", subscription.getSiteURL());
				dbObjectSubscription.put("title", subscription.getTitle());
				
				Collection<DBRef> subscriptionFoldersRefs = new LinkedList<DBRef>();
				for (Folder folder : folders.get(subscription.getTitle())) {
					DBRef dbRefFolder = new DBRef(this.db, FolderDAO.getInstance().getCollectionName(), folder.getId());
					
					subscriptionFoldersRefs.add(dbRefFolder);
				}
				dbObjectSubscription.put("folders", subscriptionFoldersRefs);
				
				this.db.requestStart();
				WriteResult result = this.dbCollection.insert(dbObjectSubscription);
				result.getLastError().throwOnError();
				this.db.requestDone();
			}
			
			this.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}