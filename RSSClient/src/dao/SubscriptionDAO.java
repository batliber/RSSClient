package dao;

import java.util.Collection;
import java.util.LinkedList;

import org.bson.types.ObjectId;

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
			DBRef dbRef = (DBRef) dbObject.get("folder");
			DBObject dbObjectFolder = dbRef.fetch();
			
			result.setFolder(FolderDAO.getInstance().pojoFromBSON(dbObjectFolder, true));
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
		
		DBRef dbRef = new DBRef(this.db, FolderDAO.getInstance().getCollectionName(), subscription.getFolder().getId());
		
		result.put("folder", dbRef);
		
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
}