package dao;


public class MetaDataDAO {

	private static MetaDataDAO instance = null;
	
	private MetaDataDAO() {
		
	}
	
	public static MetaDataDAO getInstance() {
		if (instance == null) {
			instance = new MetaDataDAO();
		}
		return instance;
	}
}