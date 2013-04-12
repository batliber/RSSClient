package dao;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.WriteResult;

import entities.Folder;
import entities.Subscription;

public class FolderDAO extends AbstractDAO {

	private static FolderDAO instance = null;
	
	private FolderDAO() {
		this.collectionName = "Folders";
	}
	
	public static FolderDAO getInstance() {
		if (instance == null) {
			instance = new FolderDAO();
		}
		return instance;
	}
	
	public Map<String, Folder> listFolders() {
		Map<String, Folder> result = new HashMap<String, Folder>();
		Map<ObjectId, Subscription> mapUnread = new HashMap<ObjectId, Subscription>();
		
		try {
			this.connect();
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			DBCursor dbCursor = this.dbCollection.find();
			
			if (dbCursor != null) {
				while (dbCursor.hasNext()) {
					DBObject dbObject = dbCursor.next();
					
					Folder folder = this.pojoFromBSON(dbObject, false);
					
					result.put(folder.getName(), folder);
					
					for (Subscription subscription : folder.getSubscriptions()) {
						mapUnread.put(subscription.getId(), subscription);
					}
				}
				dbCursor.close();
			}
			
			DBCollection itemCollection = this.db.getCollection(ItemDAO.getInstance().getCollectionName());
			
			DBObject dbObjectKey = new BasicDBObject();
			dbObjectKey.put("subscription", true);
			
			DBObject dbObjectCondition = new BasicDBObject();
			dbObjectCondition.put("read", false);
			
			DBObject dbObjectInitial = new BasicDBObject();
			dbObjectInitial.put("count", 0);
			
			BasicDBList countResult = (BasicDBList) itemCollection.group(dbObjectKey, dbObjectCondition, dbObjectInitial, "function(curr, result){ result.count++; }");
			for (Iterator<Object> iterator = countResult.iterator(); iterator.hasNext();) {
				DBObject dbObjectCountResult = (DBObject) iterator.next();
				
				DBRef dbRef = (DBRef) dbObjectCountResult.get("subscription");
				
				if (dbRef != null) {
					Long count = ((Double) dbObjectCountResult.get("count")).longValue();
					
					mapUnread.get(dbRef.getId()).setUnread(count);
				}
			}
			
			for (Folder folder : result.values()) {
				for (Subscription subscription : folder.getSubscriptions()) {
					folder.setUnread(folder.getUnread() + subscription.getUnread());
				}
			}
			
			this.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public void createFolders(Collection<Folder> folders) {
		try {
			this.connect();
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			for (Folder folder : folders) {
				BasicDBObject basicDBObject = new BasicDBObject();
				basicDBObject.put("name", folder.getName());
				
				this.db.requestStart();
				WriteResult result = this.dbCollection.insert(basicDBObject);
				result.getLastError().throwOnError();
				this.db.requestDone();
				
				folder.setId((ObjectId) basicDBObject.get("_id"));
				
				for (Subscription subscription : folder.getSubscriptions()) {
					if (subscription.getId() == null) {
						DBObject dbObjectSubscription = SubscriptionDAO.getInstance().bsonFromPOJO(subscription);
						
						this.db.requestStart();
						result = this.db.getCollection(SubscriptionDAO.getInstance().getCollectionName()).insert(dbObjectSubscription);
						result.getLastError().throwOnError();
						this.db.requestDone();
						
						System.out.println(dbObjectSubscription.get("_id"));
						
						subscription.setId((ObjectId) dbObjectSubscription.get("_id"));
					}
				}
				
				List<DBRef> folderSubscriptionsRefs = new LinkedList<DBRef>();				
				for (Subscription subscription : folder.getSubscriptions()) {
					folderSubscriptionsRefs.add(new DBRef(this.db, SubscriptionDAO.getInstance().getCollectionName(), subscription.getId()));
				}
				
				basicDBObject.put("subscriptions", folderSubscriptionsRefs);
				
				this.db.requestStart();
				result = this.dbCollection.update(new BasicDBObject().append("_id", folder.getId()), basicDBObject);
				result.getLastError().throwOnError();
				this.db.requestDone();
			}
			
			this.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Folder getById(ObjectId id) {
		Folder result = null;
		
		try {
			this.connect();
			
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			DBObject dbObject = new BasicDBObject();
			dbObject.put("_id", id);
			
			DBCursor dbCursor = this.dbCollection.find(dbObject);
			
			if (dbCursor != null && dbCursor.hasNext()) {
				dbObject = dbCursor.next();
				
				result = this.pojoFromBSON(dbObject, false);
			}
			
			Map<ObjectId, Subscription> mapUnread = new HashMap<ObjectId, Subscription>();
			for (Subscription subscription : result.getSubscriptions()) {
				mapUnread.put(subscription.getId(), subscription);
			}
			
			DBCollection itemCollection = this.db.getCollection(ItemDAO.getInstance().getCollectionName());
			
			DBObject dbObjectKey = new BasicDBObject();
			dbObjectKey.put("subscription", true);
			
			DBObject dbObjectCondition = new BasicDBObject();
			dbObjectCondition.put("read", false);
			
			DBObject dbObjectInitial = new BasicDBObject();
			dbObjectInitial.put("count", 0);
			
			BasicDBList countResult = (BasicDBList) itemCollection.group(dbObjectKey, dbObjectCondition, dbObjectInitial, "function(curr, result){ result.count++; }");
			for (Iterator<Object> iterator = countResult.iterator(); iterator.hasNext();) {
				DBObject dbObjectCountResult = (DBObject) iterator.next();
				
				DBRef dbRef = (DBRef) dbObjectCountResult.get("subscription");
				
				Long count = ((Double) dbObjectCountResult.get("count")).longValue();
				
				if (mapUnread.containsKey(dbRef.getId())) {
					Subscription subscription = mapUnread.get(dbRef.getId());
					subscription.setUnread(count);
					
					result.setUnread(result.getUnread() + count);
				}
			}
			
			this.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public Folder pojoFromBSON(DBObject dbObject, boolean shallow) {
		Folder result = new Folder();
		
		result.setId(new ObjectId(dbObject.get("_id").toString()));
		result.setName(dbObject.get("name").toString());
		
		if (!shallow) {
			List<Subscription> subscriptions = new LinkedList<Subscription>();
			Long unread = new Long(0);
			
			BasicDBList basicDBList = (BasicDBList) dbObject.get("subscriptions");
			if (basicDBList != null) {
				for (Iterator<Object> iterator = basicDBList.iterator(); iterator.hasNext();) {
					DBRef dbRef = (DBRef) iterator.next();
					DBObject dbObjectSubscription = dbRef.fetch();
					
					Subscription subscription = SubscriptionDAO.getInstance().pojoFromBSON(dbObjectSubscription, true);
					
					subscriptions.add(subscription);
				}
			}
			
			Collections.sort(subscriptions, new Comparator<Subscription>(){
				public int compare(Subscription o1, Subscription o2) {
					return o1.getTitle().compareTo(o2.getTitle());
				}
			});
			
			result.setSubscriptions(subscriptions);
			result.setUnread(unread);
		}
		
		return result;
	}

	public void updateFolderSubscriptions() {
		try {
			this.connect();
			
			this.dbCollection = this.db.getCollection(this.collectionName);
			
			DBCollection subscriptionCollection = this.db.getCollection(SubscriptionDAO.getInstance().getCollectionName());
			
			DBCursor dbCursor = subscriptionCollection.find();
			
			Map<ObjectId, Collection<Subscription>> folderSubscriptions = new HashMap<ObjectId, Collection<Subscription>>();
			
			if (dbCursor != null) {
				while (dbCursor.hasNext()) {
					DBObject dbObject = dbCursor.next();
					
					Subscription subscription = new Subscription();
					subscription.setId(new ObjectId(dbObject.get("_id").toString()));
					
					BasicDBList basicDBList = (BasicDBList) dbObject.get("folders");
					if (basicDBList != null) {
						for (Iterator<Object> iterator = basicDBList.iterator(); iterator.hasNext();) {
							DBRef dbRef = (DBRef) iterator.next();
							
							if (folderSubscriptions.containsKey((ObjectId)dbRef.getId())) {
								Collection<Subscription> subscriptions = folderSubscriptions.get((ObjectId)dbRef.getId());
								
								if (!subscriptions.contains(subscription)) {
									subscriptions.add(subscription);
								}
							} else {
								Collection<Subscription> subscriptions = new LinkedList<Subscription>();
								
								subscriptions.add(subscription);
								
								folderSubscriptions.put((ObjectId)dbRef.getId(), subscriptions);
							}
						}
					}
				}
				dbCursor.close();
			}
			
			for (ObjectId objectId : folderSubscriptions.keySet()) {
				dbCursor = this.dbCollection.find(new BasicDBObject().append("_id", objectId));
				if (dbCursor != null) {
					DBObject dbObject = dbCursor.next();
					
					Collection<DBRef> subscriptions = new LinkedList<DBRef>();					
					for (Subscription subscription : folderSubscriptions.get(objectId)) {
						subscriptions.add(new DBRef(this.db, SubscriptionDAO.getInstance().getCollectionName(), subscription.getId()));
					}
					
					dbObject.put("subscriptions", subscriptions);
					
					this.db.requestStart();
					WriteResult result = this.dbCollection.update(new BasicDBObject().append("_id", objectId), dbObject);
					result.getLastError().throwOnError();
					this.db.requestDone();
				}
			}
			
			this.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}