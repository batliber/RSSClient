package dao;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.WriteResult;

import entities.Folder;
import entities.Item;
import entities.Subscription;
import global.Constants;

public class ItemDAO extends AbstractDAO {

	private static ItemDAO instance = null;
	
	private ItemDAO() {
		this.collectionName = "Items";
	}
	
	public static ItemDAO getInstance() {
		if (instance == null) {
			instance = new ItemDAO();
		}
		return instance;
	}
	
	public void insert(Item item, Subscription subscription, Folder folder) {
		try {
			this.connect();
			
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			if (item.getHTML() != null) {
				DBObject dbObject = this.bsonFromPOJO(item, folder, subscription, false);
				
				DBObject dbObjectQuery = new BasicDBObject();
				dbObjectQuery.put("GUID", item.getGUID());
				
				this.db.requestStart();
				WriteResult result = this.dbCollection.update(dbObjectQuery, dbObject, true, false);
				result.getLastError().throwOnError();
				this.db.requestDone();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.disconnect();
		}
	}
	
	public Collection<Item> listItemsByFolder(Folder folder, boolean read, Long limit) {
		Collection<Item> result = new LinkedList<Item>();
		
		try {
			this.connect();
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			List<DBRef> listSubscriptions = new ArrayList<DBRef>();
			for (Subscription subscription : folder.getSubscriptions()) {
				listSubscriptions.add(new DBRef(this.db, SubscriptionDAO.getInstance().getCollectionName(), subscription.getId()));
			}
			
			DBObject dbObjectIn = new BasicDBObject();
			dbObjectIn.put("$in", listSubscriptions);
			
			DBObject dbObjectQuery = new BasicDBObject();
			dbObjectQuery.put("subscription", dbObjectIn);
			dbObjectQuery.put("read", read);
			
			DBObject dbObjectSort = new BasicDBObject();
			dbObjectSort.put("PubDate", -1);
			
			DBCursor dbCursor = this.dbCollection.find(dbObjectQuery).sort(dbObjectSort).limit(limit != null ? limit.intValue() : Constants.DEFAULT_ITEMS_LIMIT.intValue());
			if (dbCursor != null) {
				while (dbCursor.hasNext()) {
					DBObject dbObject = dbCursor.next();
					
					Item item = this.pojoFromBSON(dbObject);
					
					result.add(item);
				}
				dbCursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.disconnect();
		}
		
		return result;
	}
	
	public Collection<Item> listItemsBySubscription(Subscription subscription, boolean read, Long limit) {
		Collection<Item> result = new LinkedList<Item>();
		
		try {
			this.connect();
			
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			DBObject dbObjectQuery = new BasicDBObject();
			dbObjectQuery.put("read", read);
			
			DBRef dbRef = new DBRef(this.db, SubscriptionDAO.getInstance().getCollectionName(), subscription.getId());
			dbObjectQuery.put("subscription", dbRef);
			
			DBObject dbObjectSort = new BasicDBObject();
			dbObjectSort.put("PubDate", -1);
			
			DBCursor dbCursor = this.dbCollection.find(dbObjectQuery).sort(dbObjectSort).limit(limit != null ? limit.intValue() : Constants.DEFAULT_ITEMS_LIMIT.intValue());
			
			if (dbCursor != null) {
				while (dbCursor.hasNext()) {
					DBObject dbObject = dbCursor.next();
					
					Item item = this.pojoFromBSON(dbObject);
					
					result.add(item);
				}
				dbCursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.disconnect();
		}
		
		return result;
	}
	
	public Long countUnreadBySubscription(Subscription subscription) {
		Long result = new Long(0);
		
		try {
			this.connect();
			
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			DBObject dbObject = new BasicDBObject();
			dbObject.put("read", false);
			
			DBRef dbRef = new DBRef(this.db, SubscriptionDAO.getInstance().getCollectionName(), subscription.getId());
			
			dbObject.put("subscription", dbRef);
			
			result = this.dbCollection.count(dbObject);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.disconnect();
		}
		
		return result;
	}

	public void markRead(Item item, Subscription subscription, boolean read) {
		try {
			this.connect();
			
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			DBObject dbObject = new BasicDBObject();
			dbObject.put("GUID", item.getGUID());
			
			DBCursor dbCursor = this.dbCollection.find(dbObject);
			
			if (dbCursor != null && dbCursor.hasNext()) {
				dbObject = dbCursor.next();
				
				dbObject.put("read", read);
				
				this.db.requestStart();
				WriteResult result = this.dbCollection.update(new BasicDBObject().append("GUID", dbObject.get("GUID")), dbObject);
				result.getLastError().throwOnError();
				this.db.requestDone();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.disconnect();
		}
	}

	public void purgeItems() {
		try {
			this.connect();
			
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			Map<String, Subscription> subscriptions = new HashMap<String, Subscription>();
			
			DBCursor dbCursorSubscriptions = this.db.getCollection("Subscriptions").find();
			if (dbCursorSubscriptions != null) {
				while (dbCursorSubscriptions.hasNext()) {
					Subscription subscription = SubscriptionDAO.getInstance().pojoFromBSON(dbCursorSubscriptions.next(), true);
					
					String[] pieces = subscription.getSiteURL().split("/");
					String key = pieces[0]+ "//" + pieces[1] + pieces[2] + "/";
					
					System.out.println(key);
					
					subscriptions.put(key, subscription);
				}
			}
			
			DBCursor dbCursor = this.dbCollection.find();
			if (dbCursor != null) {
				while (dbCursor.hasNext()) {
					DBObject dbObject = dbCursor.next();
					
					ObjectId id = (ObjectId) dbObject.get("_id");
					
					String[] linkPieces = dbObject.get("link").toString().split("/");
					
					String key = linkPieces[0] + "//" + linkPieces[1] + linkPieces[2] + "/"; 
					
					Subscription subscription = subscriptions.get(key);
					
					if (subscription != null) {
						DBRef dbRef = new DBRef(this.db, "Subscriptions", subscription.getId());
						
						dbObject.put("subscription", dbRef);
						
						this.db.requestStart();
						WriteResult writeResult = this.dbCollection.update(new BasicDBObject().append("_id", id), dbObject);
						writeResult.getLastError().throwOnError();
						this.db.requestDone();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.disconnect();
		}
	}

	public void consolidateItems(Subscription subscription) {
		try {
			this.connect();
			
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			DBCursor dbCursor = this.db.getCollection(subscription.getTitle()).find();
			
			if (dbCursor != null) {
				while (dbCursor.hasNext()) {
					DBObject dbObject = dbCursor.next();
					dbObject.put("_id", null);
					
					this.db.requestStart();
					WriteResult writeResult = this.dbCollection.insert(dbObject);
					writeResult.getLastError().throwOnError();
					this.db.requestDone();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.disconnect();
		}
	}

	public Item pojoFromBSON(DBObject dbObject) {
		Item result = new Item();
		
		result.setGUID(dbObject.get("GUID").toString());
		if (dbObject.get("HTML") != null) {
			result.setHTML(dbObject.get("HTML").toString());
		}
		if (dbObject.get("title") != null) {
			result.setTitle(dbObject.get("title").toString());
		}
		if (dbObject.get("text") != null) {
			result.setText(dbObject.get("text").toString());
		}
		if (dbObject.get("link") != null) {
			try {
				result.setLink(new URL(dbObject.get("link").toString()));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if (dbObject.get("PubDate") != null) {
			result.setPubDate((Date)dbObject.get("PubDate"));
		}
		result.setFetchDate((Date)dbObject.get("FetchDate"));
		
		DBRef dbRef = (DBRef) dbObject.get("subscription");
		DBObject dbObjectSubscription = dbRef.fetch();
		
		result.setSubscription(SubscriptionDAO.getInstance().pojoFromBSON(dbObjectSubscription, true));
		
		return result;
	}
	
	public DBObject bsonFromPOJO(Item item, Folder folder, Subscription subscription, boolean read) {
		DBObject dbObject = new BasicDBObject();
		
		dbObject.put("GUID", item.getGUID());
		dbObject.put("HTML", item.getHTML());
		dbObject.put("text", item.getText());
		dbObject.put("title", item.getTitle());
		dbObject.put("FetchDate", item.getFetchDate());
//		dbObject.put("ModDate", item.getModDate());
		dbObject.put("PubDate", item.getPubDate());
		dbObject.put("link", item.getLink().toString());
		dbObject.put("read", read);
		
		DBRef dbRef = new DBRef(this.db, SubscriptionDAO.getInstance().getCollectionName(), subscription.getId());
		
		dbObject.put("subscription", dbRef);
		
		if (folder != null) {
			dbRef = new DBRef(this.db, FolderDAO.getInstance().getCollectionName(), folder.getId());
			
			dbObject.put("folder", dbRef);
		}
		
		return dbObject;
	}
}