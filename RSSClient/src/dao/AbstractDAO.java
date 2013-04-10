package dao;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class AbstractDAO {

	protected Mongo mongo;
	protected DB db;
	protected String collectionName;
	protected DBCollection dbCollection;
	
	protected void connect() {
		try {
			this.mongo = new Mongo("localhost", 27017);
			this.db = this.mongo.getDB("yarssc");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	protected void disconnect() {
		this.dbCollection = null;
		this.db = null;
		if (this.mongo != null) this.mongo.close();
		this.mongo = null;
	}

	public String getCollectionName() {
		return collectionName;
	}
}